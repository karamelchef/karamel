require File.expand_path(File.dirname(__FILE__) + '/get_ndbapi_addrs')
require File.expand_path(File.dirname(__FILE__) + '/find_mysqld')

ndb_connectstring()

case node[:platform_family]
when "debian"

  libaio1="libaio1_0.3.109-2ubuntu1_amd64.deb"
  cached_libaio1 = "#{Chef::Config[:file_cache_path]}/#{libaio1}"
  Chef::Log.info "Installing libaio1 to #{cached_libaio1}"

  cookbook_file cached_libaio1 do
    source libaio1
    owner node[:ndb][:user]
    group node[:ndb][:group]
    mode "0755"
    action :create_if_missing
  end

  package libaio1 do
    provider Chef::Provider::Package::Dpkg
    source cached_libaio1
    action :install
  end

when "rhel"
  package "libaio" do
    action :install
  end
end

#scripts/mysql_install_db requires perl to be installed
  package "perl" do
    action :install
  end

directory node[:ndb][:mysql_server_dir] do
  owner node[:mysql][:run_as_user]
  group node[:ndb][:group]
  mode "0755"
  action :create
  recursive true  
end

my_ip = my_private_ip()
#hostId=""
found_id=-1

found_id=find_mysql_id(my_ip)
#hostId="ndbapi#{found_id}" 
#generate_hosts(hostId, my_ip)

for script in node[:mysql][:scripts]
  template "#{node[:ndb][:scripts_dir]}/#{script}" do
    source "#{script}.erb"
    owner node[:ndb][:user]
    group node[:ndb][:group]
    mode 0775
    variables({
                :node_id => found_id
              })
  end
end 

pid_file="#{node[:ndb][:log_dir]}/mysql_#{found_id}.pid"
template "/etc/init.d/mysqld" do
  source "mysqld.erb"
  owner node[:mysql][:run_as_user]
  group node[:ndb][:user]
  mode 0755
  variables({
              :pid_file => pid_file,
              :node_id => found_id
            })
end

service "mysqld" do
  supports :restart => true, :stop => true, :start => true, :status => true
  action :nothing
end

template "mysql.cnf" do
  owner node[:mysql][:run_as_user]
  group node[:ndb][:group]
  path "#{node[:ndb][:root_dir]}/my.cnf"
  source "my-ndb.cnf.erb"
  mode "0644"
  variables({
              :mysql_id => found_id,
              :my_ip => my_ip
            })
  notifies :enable, "service[mysqld]"
end

bash 'mysql_install_db' do
#  user node[:mysql][:run_as_user]
  user "root"
  code <<-EOF
    export MYSQL_HOME=#{node[:ndb][:root_dir]}
    # --force causes mysql_install_db to run even if DNS does not work. In that case, grant table entries that normally use host names will use IP addresses.
    cd #{node[:mysql][:base_dir]}
    ./scripts/mysql_install_db --user=#{node[:mysql][:run_as_user]} --basedir=#{node[:mysql][:base_dir]} --defaults-file=#{node[:ndb][:root_dir]}/my.cnf --force 
    touch #{node[:ndb][:mysql_server_dir]}/.installed
    # sanity check to set ownership of files to 'mysql' user
    chown -R #{node[:mysql][:run_as_user]} #{node[:ndb][:mysql_server_dir]}
    EOF
  not_if { ::File.exists?( "#{node[:ndb][:mysql_server_dir]}/.installed" ) }
end

ndb_mysql_basic "install" do
   action :nothing
end

grants_path = "#{Chef::Config[:file_cache_path]}/grants.sql"
  template grants_path do
    source File.basename(grants_path) + ".erb"
    owner "root" 
    mode "0600"
    action :create
    variables({
                :my_ip => my_ip
              })
    notifies :install_grants, "ndb_mysql_basic[install]", :immediately
  end


if node[:ndb][:enabled] == "true"

  ndb_mysql_ndb "install" do
    action [:install_distributed_privileges, :install_memcached]
  end

  if node[:kagent][:enabled] == "true"
    kagent_config "mysqld" do
      service "NDB"
      start_script "#{node[:ndb][:scripts_dir]}/mysql-server-start.sh"
      stop_script  "#{node[:ndb][:scripts_dir]}/mysql-server-stop.sh"
      log_file "#{node[:ndb][:log_dir]}/mysql_#{found_id}_out.log"
      pid_file "#{node[:ndb][:log_dir]}/mysql_#{found_id}.pid"
      command "mysql"
      command_user node[:ndb][:user] 
      command_script "#{node[:ndb][:scripts_dir]}/mysql-client.sh"
    end
  end
end

ndb_start "mysqld" do
end
