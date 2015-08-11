
service "webclient" do
  supports :restart => true, :stop => true, :start => true, :status => true
  action :nothing
end

template "/etc/init.d/webclient" do
  source "webclient.erb"
  owner node[:flink][:user]
  group node[:hadoop][:group]
  mode 0754
  notifies :restart, resources(:service => "webclient")
end
