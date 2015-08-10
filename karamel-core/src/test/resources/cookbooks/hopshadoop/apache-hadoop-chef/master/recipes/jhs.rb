include_recipe "hadoop::yarn"
libpath = File.expand_path '../../../kagent/libraries', __FILE__
require File.join(libpath, 'inifile')

yarn_service="jhs"
yarn_command="historyserver"

for script in node[:hadoop][:yarn][:scripts]
  template "#{node[:hadoop][:home]}/sbin/#{script}-#{yarn_service}.sh" do
    source "#{script}-#{yarn_service}.sh.erb"
    owner node[:hadoop][:yarn][:user]
    group node[:hadoop][:group]
    mode 0775
  end
end 

service yarn_command do
  supports :restart => true, :stop => true, :start => true, :status => true
  action :nothing
end


tmp_dirs   = ["/mr-history", node[:hadoop][:jhs][:inter_dir], node[:hadoop][:jhs][:done_dir], "/tmp", "/user"]

 for d in tmp_dirs
   Chef::Log.info "Creating hdfs directory: #{d}"
   hadoop_hdfs_directory d do
    action :create
    owner node[:hdfs][:user]
    group node[:hadoop][:group]
    mode "1777"
    not_if ". #{node[:hadoop][:home]}/sbin/set-env.sh && #{node[:hadoop][:home]}/bin/hdfs dfs -test -d #{d}"
   end
 end

node.normal[:mr][:dirs] = [node[:hadoop][:mr][:staging_dir], node[:hadoop][:mr][:tmp_dir], "/user/#{node[:hadoop][:mr][:user]}"]
 for d in node[:mr][:dirs]
   Chef::Log.info "Creating hdfs directory: #{d}"
   hadoop_hdfs_directory d do
    action :create
    owner node[:hadoop][:mr][:user]
    group node[:hadoop][:group]
    mode "0775"
    not_if ". #{node[:hadoop][:home]}/sbin/set-env.sh && #{node[:hadoop][:home]}/bin/hdfs dfs -test -d #{d}"
   end
 end

template "/etc/init.d/#{yarn_command}" do
  source "#{yarn_command}.erb"
  owner node[:hdfs][:user]
  group node[:hadoop][:group]
  mode 0754
  notifies :enable, resources(:service => yarn_command)
  notifies :restart, resources(:service => yarn_command)
end

if node[:kagent][:enabled] == "true" 
  kagent_config yarn_command do
    service "MAP_REDUCE"
    start_script "#{node[:hadoop][:home]}/sbin/root-start-#{yarn_service}.sh"
    stop_script "#{node[:hadoop][:home]}/sbin/stop-#{yarn_service}.sh"
    log_file "#{node[:hadoop][:logs_dir]}/yarn-#{node[:hdfs][:user]}-#{yarn_command}-#{node['hostname']}.log"
    pid_file "/tmp/mapred-#{node[:hdfs][:user]}-#{yarn_command}.pid"
    config_file "#{node[:hadoop][:conf_dir]}/mapred-site.xml"
    web_port node[:hadoop]["#{yarn_service}"][:http_port]
  end
end

hadoop_start "#{yarn_command}" do
end
