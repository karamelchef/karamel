bash 'kill_running_services' do
user "root"
ignore_failure :true
code <<-EOF
 killall -9 ndbmtd
EOF
end

file "/etc/init.d/ndbd" do
  action :delete
end

directory node[:ndb][:data_dir] do
  recursive true
  action :delete
end

for script in node[:ndbd][:scripts] do
  file "#{node[:ndb][:scripts_dir]}/#{script}" do
    action :delete
  end
end 
