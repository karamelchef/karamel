libpath = File.expand_path '../../../kagent/libraries', __FILE__
require File.join(libpath, 'inifile')

private_ip = my_private_ip()
public_ip = my_public_ip()

for script in node[:hadoop][:nn][:scripts]
  template "#{node[:hadoop][:home]}/sbin/#{script}" do
    source "#{script}.erb"
    owner node[:hdfs][:user]
    group node[:hadoop][:group]
    mode 0775
  end
end 


# it is ok if all namenodes format the fs. Unless you add a new one later..

if node[:hadoop][:format].eql? "true"

# TODO: test if the NameNode is running
  if ::File.directory?("#{node[:hadoop][:nn][:name_dir]}/current")
    # if the nn has already been formatted, re-formatting it returns error
    Chef::Log.info "Not formatting the NameNode. Remove this directory before formatting: (sudo rm -rf #{node[:hadoop][:tmp_dir]}/dfs/name/current)"
  else 
    hadoop_start "format-nn" do
      action :format_nn
    end
  end
end

service "namenode" do
  supports :restart => true, :stop => true, :start => true, :status => true
  action :nothing
end

template "/etc/init.d/namenode" do
  source "namenode.erb"
  owner node[:hdfs][:user]
  group node[:hadoop][:group]
  mode 0754
  notifies :enable, resources(:service => "namenode")
  notifies :restart, resources(:service => "namenode"), :immediately
end

if node[:kagent][:enabled] == "true" 
  kagent_config "namenode" do
    service "HDFS"
    start_script "#{node[:hadoop][:home]}/sbin/root-start-nn.sh"
    stop_script "#{node[:hadoop][:home]}/sbin/stop-nn.sh"
    init_script "#{node[:hadoop][:home]}/sbin/format-nn.sh"
    config_file "#{node[:hadoop][:conf_dir]}/core-site.xml"
    log_file "#{node[:hadoop][:logs_dir]}/hadoop-#{node[:hdfs][:user]}-namenode-#{node['hostname']}.log"
    pid_file "#{node[:hadoop][:logs_dir]}/hadoop-#{node[:hdfs][:user]}-namenode.pid"
    web_port node[:hadoop][:nn][:http_port]
  end
end

hadoop_start "namenode" do
end

