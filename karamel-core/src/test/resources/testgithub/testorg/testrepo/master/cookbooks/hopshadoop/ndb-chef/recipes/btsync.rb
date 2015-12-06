#
# JClouds leechers have to be passed the leecher_secret parameter when calling btsync recipes.
# The dashboard (bootstrap) reads the leecher_secret from /tmp/vagrant-chef-1/.leecher_secret before
# launching all leechers.
#
tgt="ndb"
node.override[:btsync][:active_recipe] = tgt

include_recipe "btsync::install"

master_secret=""
if node[:btsync][:bootstrap] 
  bash "generate_secrets" do
    user "root"
    code <<-EOF
   btsync --generate-secret > #{Chef::Config[:file_cache_path]}/.master_secret_#{tgt}
   cat #{Chef::Config[:file_cache_path]}/.master_secret_#{tgt} | xargs btsync --get-ro-secret > #{Chef::Config[:file_cache_path]}/.leecher_secret_#{tgt}
   chmod 755 #{Chef::Config[:file_cache_path]}/.leecher_secret_#{tgt}
   EOF
  not_if {  ::File.exists?( "#{Chef::Config[:file_cache_path]}/.leecher_secret_#{tgt}" ) }
  end
else
  bt_secret = node[:ndb][:leecher_secret]
end 

directory node[:ndb][:shared_folder] do
  owner node[:ndb][:user]
  mode 0775
  action :create
  recursive true
end

service "btsync-#{tgt}" do
  supports :restart => true, :stop => true, :start => true
  action :restart
end

if node[:btsync][:bootstrap]
  Chef::Log.info "neighbours : #{node[:btsync][:ndb][:leechers].join("\", \"")}"
  package_url = node[tgt][:package_url]

  Chef::Log.info "Downloading mysql cluster binaries from #{package_url}"
  base_package_filename =  File.basename(node[tgt][:package_url])
  Chef::Log.info "Into file #{base_package_filename}"
  base_package_dirname =  File.basename(base_package_filename, ".tar.gz")
  cached_package_filename = "#{node[:ndb][:shared_folder]}/#{base_package_filename}"
  # TODO - HTTP Proxy settings, checksum
  # checksum node[:program][:checksum] 
  remote_file cached_package_filename do
    owner node[:ndb][:user]
    group node[:ndb][:group]
    source package_url
    mode 0755
    action :create_if_missing
  end
else 
  btsync_wait_download "ndb" do
  end
end

