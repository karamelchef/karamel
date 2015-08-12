bash 'kill_running_services' do
user "root"
ignore_failure :true
code <<-EOF
 killall -9 memcached
EOF
end

file "/etc/init.d/memcached" do
  action :delete
end

# TODO - is there no memcached directory to delete?

for script in node[:memcached][:scripts] do
  file "#{node[:ndb][:scripts_dir]}/#{script}" do
    action :delete
  end
end 

