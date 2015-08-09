node.default['java']['jdk_version'] = 7
node.default['java']['install_flavor'] = "openjdk"


# Pre-Experiment Code

include_recipe 'java'

bash 'fix_java_path_for_glassfish_cookbook' do
user "root"
    code <<-EOF
# upstart job in glassfish expects java to be installed in /bin/java
test -f /usr/bin/java && ln -sf /usr/bin/java /bin/java 
EOF
end

group node[:glassfish][:group] do
end

user node['glassfish']['user'] do
  supports :manage_home => true
  home "/home/#{node['glassfish']['user']}"
  shell '/bin/bash'
  action :create
  system true
  not_if "getent passwd #{node['glassfish']['user']}"
end

group node[:glassfish][:group] do
  action :modify
  members node[:glassfish][:user] 
  append true
end


package_url = node['glassfish']['package_url']
base_package_filename = File.basename(package_url)
cached_package_filename = "#{Chef::Config[:file_cache_path]}/#{base_package_filename}"

remote_file cached_package_filename do
  source package_url
  mode '0600'
  action :create
end

package 'unzip'

majorVersion = node['glassfish']['version'].each_char.first

bash 'unpack_glassfish' do
    code <<-EOF
rm -rf /tmp/glassfish
mkdir /tmp/glassfish
cd /tmp/glassfish
unzip -qq #{cached_package_filename}
mkdir -p #{File.dirname(node['glassfish']['base_dir'])}
mkdir -p #{File.dirname(node['glassfish']['install_dir'])}
mv glassfish#{majorVersion} #{node['glassfish']['base_dir']}
chown -R #{node['glassfish']['user']} #{node['glassfish']['base_dir']}
chgrp -R #{node['glassfish']['group']} #{node['glassfish']['base_dir']}
chmod -R 0770 #{node['glassfish']['base_dir']}/bin/
chmod -R 0770 #{node['glassfish']['base_dir']}/glassfish/bin/
rm -rf #{node['glassfish']['base_dir']}/glassfish/domains/domain1
#Bugfix http://stackoverflow.com/questions/26723273/timeoutexception-on-remote-glassfish-v4-1-deployment
rm #{node['glassfish']['base_dir']}/glassfish/modules/nucleus-grizzly-all.jar
cd #{node['glassfish']['base_dir']}/glassfish/modules
wget #{node[:grizzly][:jar_url]}
test -d #{node['glassfish']['base_dir']}
EOF
  not_if { ::File.exists?( node['glassfish']['base_dir'] ) }
end

directory "#{node['glassfish']['base_dir']}/glassfish/lib/templates" do
  owner node['glassfish']['user']
  group node['glassfish']['group']
  mode 0644
  action :create
  recursive true
end

cookbook_file "#{node['glassfish']['base_dir']}/glassfish/lib/templates/domain.xml" do
  source 'domain.xml'
  owner node['glassfish']['user']
  group node['glassfish']['group']
  mode 0644
end

cookbook_file "#{node['glassfish']['base_dir']}/glassfish/lib/templates/login.conf" do
  source 'login.conf'
  owner node['glassfish']['user']
  group node['glassfish']['group']
  mode 0644
end


template "#{node['glassfish']['base_dir']}/glassfish/lib/templates/server.policy" do
  source 'server.policy.erb'
  owner node['glassfish']['user']
  group node['glassfish']['group']
  mode 0644
end

bash 'generate_public_private_key' do
  user node['glassfish']['user']
  group node['glassfish']['group']
    code <<-EOF
    mkdir -p #{node['glassfish']['base_dir']}/.ssh
    chmod 700 #{node['glassfish']['base_dir']}/.ssh
    ssh-keygen -b 2048 -t rsa -q -N "" -f #{node['glassfish']['base_dir']}/.ssh/id_rsa
EOF
  not_if { ::File.exists?( "#{node['glassfish']['base_dir']}/.ssh/id_rsa" ) }
end

hopsworks_grants "set_public_key_attribute" do
  action :sshkeys
end

Chef::Log.info("Public key: #{node[:hopsworks][:public_key]}")


if platform_family?("debian")
  # Add the following lines to /etc/security/limits.conf to increase the maximum number of open files for the user that Glassfish will run as:
  bash 'increase_limits_glassfish' do
    user "root"
    code <<-EOF
  glassfish soft nofile 32768
  glassfish hard nofile 65536
  echo "session required pam_limits.so" >> /etc/pam.d/su
  touch #{node['glassfish']['base_dir']}/.limits_increased
  EOF
    not_if { ::File.exists?( "#{node['glassfish']['base_dir']}/.limits_increased" ) }
  end
end

# Configuration Files
