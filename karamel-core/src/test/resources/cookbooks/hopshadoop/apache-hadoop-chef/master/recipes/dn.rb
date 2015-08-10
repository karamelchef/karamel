libpath = File.expand_path '../../../kagent/libraries', __FILE__
require File.join(libpath, 'inifile')


for script in node[:hadoop][:dn][:scripts]
  template "#{node[:hadoop][:home]}/sbin/#{script}" do
    source "#{script}.erb"
    owner node[:hdfs][:user]
    group node[:hadoop][:group]
    mode 0775
  end
end 


service "datanode" do
  supports :restart => true, :stop => true, :start => true, :status => true
  action :nothing
end

template "/etc/init.d/datanode" do
  source "datanode.erb"
  owner node[:hdfs][:user]
  group node[:hadoop][:group]
  mode 0754
  notifies :enable, resources(:service => "datanode")
  notifies :restart, resources(:service => "datanode"), :immediately
end


if node[:kagent][:enabled] == "true" 
  kagent_config "datanode" do
    service "HDFS"
    start_script "#{node[:hadoop][:home]}/sbin/root-start-dn.sh"
    stop_script "#{node[:hadoop][:home]}/sbin/stop-dn.sh"
    log_file "#{node[:hadoop][:logs_dir]}/hadoop-#{node[:hdfs][:user]}-datanode-#{node['hostname']}.log"
    pid_file "#{node[:hadoop][:logs_dir]}/hadoop-#{node[:hdfs][:user]}-datanode.pid"
    config_file "#{node[:hadoop][:conf_dir]}/hdfs-site.xml"
    web_port node[:hadoop][:dn][:http_port]
    command "hdfs"
    command_user node[:hdfs][:user]
    command_script "#{node[:hadoop][:home]}/bin/hdfs"
  end
end

hadoop_start "datanode" do
end
