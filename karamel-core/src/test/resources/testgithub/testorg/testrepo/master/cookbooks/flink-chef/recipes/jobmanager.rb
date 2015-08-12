include_recipe "flink::default"

file "#{node[:flink][:conf_dir]}/slaves" do
   owner node[:flink][:user]
   action :delete
end

slaves = node[:flink][:taskmanager][:private_ips].join("\n")
slaves += "\n"

# Default behaviour of attribute "content" is to replace the contents of
# the existing file as long as the new contents have a non-default value.
file "#{node[:flink][:conf_dir]}/slaves" do
  owner node[:flink][:user]
  group node[:flink][:group]
  mode '644'
  content slaves.to_s
  action :create
end

service "jobmanager" do
  supports :restart => true, :stop => true, :start => true, :status => true
  action :nothing
end

template "/etc/init.d/jobmanager" do
  source "jobmanager.erb"
  owner node[:flink][:user]
  group node[:flink][:group]
  mode 0754
  variables({
              :mode => "#{node[:flink][:jobmanager][:mode]}"
            })
  notifies :enable, resources(:service => "jobmanager")
  notifies :restart, resources(:service => "jobmanager"), :immediately
end

hadoop_hdfs_directory "/User/#{node[:flink][:user]}" do
  action :create_as_superuser
  owner node[:flink][:user]
  group node[:flink][:group]
  mode "1775"
end

hadoop_hdfs_directory "/User/#{node[:flink][:user]}/checkpoints" do
  action :create_as_superuser
  owner node[:flink][:user]
  group node[:flink][:group]
  mode "1775"
end


homedir = node[:flink][:user].eql?("root") ? "/root" : node[:flink][:home]

bash "generate-ssh-keypair-for-jobmgr" do
 user node[:flink][:user]
  code <<-EOF
     ssh-keygen -b 2048 -f #{homedir}/.ssh/id_rsa -t rsa -q -N ''
  EOF
 not_if { ::File.exists?( "#{homedir}/.ssh/id_rsa" ) }
end

template "#{homedir}/.ssh/config" do
  source "ssh_config.erb"
  owner node[:flink][:user]
  group node[:flink][:user]
  mode 0664
end

flink_jobmanager "#{homedir}" do
  action :return_publickey
end