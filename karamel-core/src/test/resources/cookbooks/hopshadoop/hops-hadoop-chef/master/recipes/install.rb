node.default[:hadoop][:download_url] = node[:hops][:download_url]
node.default[:hadoop][:hadoop_src_url] = node[:hops][:hadoop_src_url]
node.normal[:hadoop][:home]            = "#{node[:hadoop][:dir]}/hadoop-#{node[:hadoop][:version]}"
node.normal[:hadoop][:logs_dir]        = "#{node[:hadoop][:home]}/logs"
node.normal[:hadoop][:tmp_dir]         = "#{node[:hadoop][:home]}/tmp"
node.normal[:hadoop][:conf_dir]        = "#{node[:hadoop][:home]}/etc/hadoop"
node.normal[:hadoop][:sbin_dir]        = "#{node[:hadoop][:home]}/sbin"

include_recipe "hops::wrap"
include_recipe "hadoop::install"
include_recipe "hops"


