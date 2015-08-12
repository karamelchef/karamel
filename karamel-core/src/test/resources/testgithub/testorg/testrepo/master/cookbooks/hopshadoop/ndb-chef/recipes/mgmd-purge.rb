bash 'kill_running_services' do
user "root"
ignore_failure :true
code <<-EOF
 pkill -9 ndb_mgmd
EOF
end

file "/etc/init.d/ndb_mgmd" do
  action :delete
end

directory node[:ndb][:mgm_dir] do
  recursive true
  action :delete
end

for script in node[:mgm][:scripts] do
  file "#{node[:ndb][:scripts_dir]}/#{script}" do
    action :delete
  end
end 
