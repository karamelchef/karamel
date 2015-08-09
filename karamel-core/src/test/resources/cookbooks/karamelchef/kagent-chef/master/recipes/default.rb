service "kagent" do
  supports :restart => true, :start => true, :stop => true, :enable => true
end

template "/etc/init.d/kagent" do
  source "kagent.erb"
  owner node[:kagent][:run_as_user]
  group node[:kagent][:run_as_user]
  mode 0655
  notifies :enable, "service[kagent]"
end

template"#{node[:kagent][:base_dir]}/agent.py" do
  source "agent.py.erb"
  owner node[:kagent][:run_as_user]
  group node[:kagent][:run_as_user]
  mode 0655
  notifies :enable, "service[kagent]"
end

['start-agent.sh', 'stop-agent.sh', 'restart-agent.sh', 'get-pid.sh'].each do |script|
  Chef::Log.info "Installing #{script}"
  template "#{node[:kagent][:base_dir]}/#{script}" do
    source "#{script}.erb"
    owner node[:kagent][:run_as_user]
    group node[:kagent][:run_as_user]
    mode 0655
  end
end 

['services'].each do |conf|
  Chef::Log.info "Installing #{conf}"
  template "#{node[:kagent][:base_dir]}/#{conf}" do
    source "#{conf}.erb"
    owner node[:kagent][:run_as_user]
    group node[:kagent][:run_as_user]
    mode 0644
  end
end

private_ip = my_private_ip()
public_ip = my_public_ip()

dashboard_endpoint = node[:kagent][:dashboard][:ip_port]
if dashboard_endpoint.eql? ""
  if node.attribute? "kmon"
    dashboard_endpoint = private_cookbook_ip("kmon")  + ":8080"
  end
end

template "#{node[:kagent][:base_dir]}/config.ini" do
  source "config.ini.erb"
  owner node[:kagent][:run_as_user]
  group node[:kagent][:run_as_user]
  mode 0600
  variables({
              :rest_url => "http://#{dashboard_endpoint}/#{node[:kagent][:dashboard_app]}",
              :rack => '/default',
              :public_ip => public_ip,
              :private_ip => private_ip
            })
  notifies :restart, "service[kagent]"
end

# TODO install MONIT to restart the agent if it crashes

bash "start_kagent" do
  user "root"
  code <<-EOF
   service kagent restart
 EOF
end

case node[:platform_family]
when "rhel"

  bash "disable-iptables" do
    code <<-EOH
    service iptables stop
  EOH
    only_if "test -f /etc/init.d/iptables && service iptables status"
  end

if node[:instance_role] == 'vagrant'
  bash "fix-sudoers-for-vagrant" do
    code <<-EOH
    echo "" >> /etc/sudoers
    echo "#includedir /etc/sudoers.d" >> /etc/sudoers
    echo "" >> /etc/sudoers
    touch /etc/sudoers.d/.vagrant_fix
  EOH
    only_if "test -f /etc/sudoers.d/.vagrant_fix"
  end
end

# Fix sudoers to allow root exec shell commands for Centos
node.default['authorization']['sudo']['include_sudoers_d']=true
# default 'commands' attribute for this LWRP is 'ALL'
sudo 'root' do
  user      "root"
  runas     'ALL:ALL'
end

end

if node[:kagent][:allow_kmon_ssh_access] == 'true'

  if node.attribute? "kmon"
    if node[:kmon].attribute? "public_key"
      bash "add_dashboards_public_key" do
        user "root"
        code <<-EOF
         mkdir -p /root/.ssh
         chmod 700 /root/.ssh
         cat #{node[:kmon][:public_key]} >> /root/.ssh/authorized_keys
        EOF
        not_if "test -f /root/.ssh/authorized_keys"
      end
    end
  end
end
