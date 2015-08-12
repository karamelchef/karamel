service "kagent" do
  supports :restart => true, :start => true, :stop => true, :enable => true
end

template "/etc/init.d/kagent" do
  source "kagent.erb"
  owner node[:kagent][:run_as_user]
  group node[:kagent][:run_as_user]
  mode 0655
  notifies :enable, "service[kagent]"
end

template"#{node[:kagent][:base_dir]}/agent.py" do
  source "agent.py.erb"
  owner node[:kagent][:run_as_user]
  group node[:kagent][:run_as_user]
  mode 0655
  notifies :enable, "service[kagent]"
end

['start-agent.sh', 'stop-agent.sh', 'restart-agent.sh', 'get-pid.sh'].each do |script|
  Chef::Log.info "Installing #{script}"
  template "#{node[:kagent][:base_dir]}/#{script}" do
    source "#{script}.erb"
    owner node[:kagent][:run_as_user]
    group node[:kagent][:run_as_user]
    mode 0655
  end
end 

['services'].each do |conf|
  Chef::Log.info "Installing #{conf}"
  template "#{node[:kagent][:base_dir]}/#{conf}" do
    source "#{conf}.erb"
    owner node[:kagent][:run_as_user]
    group node[:kagent][:run_as_user]
    mode 0644
  end
end

private_ip = my_private_ip()
public_ip = my_public_ip()

dashboard_endpoint = node[:kagent][:dashboard][:ip_port]
if dashboard_endpoint.eql? ""
  if node.attribute? "kmon"
    dashboard_endpoint = private_cookbook_ip("kmon")  + ":8080"
  end
end

template "#{node[:kagent][:base_dir]}/config.ini" do
  source "config.ini.erb"
  owner node[:kagent][:run_as_user]
  group node[:kagent][:run_as_user]
  mode 0600
  variables({
              :rest_url => "http://#{dashboard_endpoint}/#{node[:kagent][:dashboard_app]}",
              :rack => '/default',
              :public_ip => public_ip,
              :private_ip => private_ip
            })
  notifies :restart, "service[kagent]"
end

# TODO install MONIT to restart the agent if it crashes

bash "start_kagent" do
  user "root"
  code <<-EOF
   service kagent restart
 EOF
end

case node[:platform_family]
when "rhel"

  bash "disable-iptables" do
    code <<-EOH
    service iptables stop
  EOH
    only_if "test -f /etc/init.d/iptables && service iptables status"
  end

if node[:instance_role] == 'vagrant'
  bash "fix-sudoers-for-vagrant" do
    code <<-EOH
    echo "" >> /etc/sudoers
    echo "#includedir /etc/sudoers.d" >> /etc/sudoers
    echo "" >> /etc/sudoers
    touch /etc/sudoers.d/.vagrant_fix
  EOH
    only_if "test -f /etc/sudoers.d/.vagrant_fix"
  end
end

# Fix sudoers to allow root exec shell commands for Centos
node.default['authorization']['sudo']['include_sudoers_d']=true
# default 'commands' attribute for this LWRP is 'ALL'
sudo 'root' do
  user      "root"
  runas     'ALL:ALL'
end

end

if node[:kagent][:allow_kmon_ssh_access] == 'true'

  if node.attribute? "kmon"
    if node[:kmon].attribute? "public_key"
      bash "add_dashboards_public_key" do
        user "root"
        code <<-EOF
         mkdir -p /root/.ssh
         chmod 700 /root/.ssh
         cat #{node[:kmon][:public_key]} >> /root/.ssh/authorized_keys
        EOF
        not_if "test -f /root/.ssh/authorized_keys"
      end
    end
  end
end
jdowling@jim-dell:~/NetBeansProjects/kagent-chef/recipes$ 
jdowling@jim-dell:~/NetBeansProjects/kagent-chef/recipes$ cat install.rb 
# ubuntu python-mysqldb package install only works if we first run "apt-get update; apt-get upgrade"

case node[:platform_family]
when "debian"
  bash "apt_update_install_build_tools" do
    user "root"
    code <<-EOF
   apt-get update -y
#   DEBIAN_FRONTEND=noninteractive apt-get upgrade -y
   apt-get install build-essential -y
   apt-get install libssl-dev -y
 EOF
  end
when "rhel"
# gcc, gcc-c++, kernel-devel are the equivalent of "build-essential" from apt.
  package "gcc" do
    action :install
  end
  package "gcc-c++" do
    action :install
  end
  package "kernel-devel" do
    action :install
  end
  package "openssl-devel" do
    action :install
  end
end

include_recipe "openssh"
include_recipe "python"
# The openssl::upgrade recipe doesn't install openssl-dev/libssl-dev, needed by python-ssl
# Now using packages in ubuntu/centos.
#include_recipe "openssl::upgrade"

user node[:kagent][:run_as_user] do
  action :create
  system true
  shell "/bin/bash"
end
  
# package "rubygems" do
#   action :install
# end

inifile_gem = "inifile-2.0.2.gem"
cookbook_file "#{Chef::Config[:file_cache_path]}/#{inifile_gem}" do
  source "#{inifile_gem}"
  owner node[:kagent][:run_as_user]
  group node[:kagent][:run_as_user]
  mode 0755
  action :create_if_missing
end

requests="requests-1.0.3"
cookbook_file "#{Chef::Config[:file_cache_path]}/#{requests}.tar.gz" do
  source "#{requests}.tar.gz"
  owner node[:kagent][:run_as_user]
  group node[:kagent][:run_as_user]
  mode 0755
  action :create_if_missing
end

bottle="bottle-0.11.4"
cookbook_file "#{Chef::Config[:file_cache_path]}/#{bottle}.tar.gz" do
  source "#{bottle}.tar.gz"
  owner node[:kagent][:run_as_user]
  group node[:kagent][:run_as_user]
  mode 0755
  action :create_if_missing
end

cherry="CherryPy-3.2.2"
cookbook_file "#{Chef::Config[:file_cache_path]}/#{cherry}.tar.gz" do
  source "#{cherry}.tar.gz"
  owner node[:kagent][:run_as_user]
  group node[:kagent][:run_as_user]
  mode 0755
end

openSsl="pyOpenSSL-0.13"
cookbook_file "#{Chef::Config[:file_cache_path]}/#{openSsl}.tar.gz" do
  source "#{openSsl}.tar.gz"
  owner node[:kagent][:run_as_user]
  group node[:kagent][:run_as_user]
  mode 0755
  action :create_if_missing
end

# ubuntu python-mysqldb package install only works if we first run "apt-get update; apt-get upgrade"
if platform?("ubuntu", "debian") 
  package "python-mysqldb" do
   options "--force-yes"
   action :install
  end
elsif platform?("centos","redhat","fedora")
  package "MySQL-python" do
    action :install
  end
else
  python_pip "MySQL-python" do
    action :install
  end
end

netifaces="netifaces-0.8"
cookbook_file "#{Chef::Config[:file_cache_path]}/#{netifaces}.tar.gz" do
  source "#{netifaces}.tar.gz"
  owner node[:kagent][:run_as_user]
  group node[:kagent][:run_as_user]
  mode 0755
  action :create_if_missing
end

ipy="IPy-0.81"
cookbook_file "#{Chef::Config[:file_cache_path]}/#{ipy}.tar.gz" do
  source "#{ipy}.tar.gz"
  owner node[:kagent][:run_as_user]
  group node[:kagent][:run_as_user]
  mode 0755
  action :create_if_missing
end

pexpect="pexpect-2.3"
cookbook_file "#{Chef::Config[:file_cache_path]}/#{pexpect}.tar.gz" do
  source "#{pexpect}.tar.gz"
  owner node[:kagent][:run_as_user]
  group node[:kagent][:run_as_user]
  mode 0755
  action :create_if_missing
end


bash "install_python" do
  user "root"
  code <<-EOF
  cd #{Chef::Config[:file_cache_path]}
  tar zxf "#{bottle}.tar.gz"
  cd #{bottle}
  python setup.py install
  cd ..
  tar zxf "#{requests}.tar.gz"
  cd #{requests}
  python setup.py install
  cd ..
  tar zxf "#{cherry}.tar.gz"
  cd #{cherry}
  python setup.py install
  cd ..
  tar zxf "#{openSsl}.tar.gz"
  cd #{openSsl}
  python setup.py install
  cd ..
  tar zxf "#{netifaces}.tar.gz"
  cd #{netifaces}
  python setup.py install
  cd ..
  tar zxf "#{ipy}.tar.gz"
  cd #{ipy}
  python setup.py install
  cd ..
  tar zxf "#{pexpect}.tar.gz"
  cd #{pexpect}
  python setup.py install
  touch #{Chef::Config[:file_cache_path]}/.python_libs_installed
 EOF
  not_if "test -f #{Chef::Config[:file_cache_path]}/.python_libs_installed"
end


bash "make_gemrc_file" do
  user "root"
  code <<-EOF
   echo "gem: --no-ri --no-rdoc" > ~/.gemrc
 EOF
  not_if "test -f ~/.python_libs_installed"
end

gem_package "inifile" do
  source "#{Chef::Config[:file_cache_path]}/#{inifile_gem}"
  action :install
end

directory node[:kagent][:base_dir] do
  owner node[:kagent][:run_as_user]
  group node[:kagent][:run_as_user]
  mode "755"
  action :create
  recursive true
end

file node.default[:kagent][:services] do
  owner "root"
  group "root"
  mode 00755
  action :create_if_missing
end

set_my_hostname
