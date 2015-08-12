libpath = File.expand_path '../../../kagent/libraries', __FILE__

master_ip = private_recipe_ip("hadoop","nn")


remote_file "#{Chef::Config[:file_cache_path]}/apache.txt" do
  source "https://www.apache.org/licenses/LICENSE-2.0.txt"
end

hadoop_hdfs_directory "#{Chef::Config[:file_cache_path]}/apache.txt" do
  action :put
  dest "/User/#{node[:flink][:user]}"
  owner node[:flink][:user]
  mode "775"
end

nn="#{master_ip}:#{node[:hadoop][:nn][:port]}"

 bash 'wordcount-example' do
  user node[:flink][:user]
  code <<-EOH
     set -e && set -o pipefail
     cd #{node[:flink][:home]}
     # This generates about 100 MB of random data, takes about 30 seconds on my laptop
     # head -c 100000000 /dev/urandom >> examples/dummy.txt
     echo "./bin/flink run -p 1 -j ./examples/flink-java-examples-#{node[:flink][:version]}-WordCount.jar hdfs://#{nn}/User/#{node[:flink][:user]}/apache.txt hdfs://#{nn}/User/#{node[:flink][:user]}/wordcount-result.txt\n" > README.wordcount
  EOH
 end