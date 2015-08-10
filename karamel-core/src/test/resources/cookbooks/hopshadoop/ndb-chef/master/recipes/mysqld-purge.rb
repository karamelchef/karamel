bash 'kill_running_services' do
user "root"
ignore_failure :true
code <<-EOF
 killall -9 mysqld
EOF
end

file "/etc/init.d/mysqld" do
  action :delete
end

directory node[:ndb][:mysql_server_dir] do
  recursive true
  action :delete
end

for script in node[:mysql][:scripts] do
  file "#{node[:ndb][:scripts_dir]}/#{script}" do
    action :delete
  end
end 
