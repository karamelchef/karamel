require File.expand_path(File.dirname(__FILE__) + '/get_ndbapi_addrs')

ndb_connectstring()
#generate_etc_hosts()


Chef::Log.info "Hostname is: #{node[:hostname]}"
Chef::Log.info "IP address is: #{node[:ipaddress]}"

directory node[:ndb][:data_dir] do
  owner node[:ndb][:user]
  group node[:ndb][:group]
  mode "755"
  action :create
  recursive true
end

my_ip = my_private_ip()

found_id = -1
#hostId=""
id = 1
for ndbd in node[:ndb][:ndbd][:private_ips]
  if my_ip.eql? ndbd 
    Chef::Log.info "Found matching IP address in the list of data nodes: #{ndbd}. ID= #{id}"
    found_id = id
  end
  id += 1
end 
Chef::Log.info "ID IS: #{id}"

if found_id == -1
  raise "Ndbd: Could not find matching IP address in list of data nodes."
end

#hostId="ndbd#{found_id}" 
#generate_hosts(hostId, my_ip)

directory "#{node[:ndb][:data_dir]}/#{found_id}" do
  owner node[:ndb][:user]
  group node[:ndb][:group]
  mode "755"
  action :create
  recursive true
end


for script in node[:ndb][:scripts]
  template "#{node[:ndb][:scripts_dir]}/#{script}" do
    source "#{script}.erb"
    owner node[:ndb][:user]
    group node[:ndb][:group]
    mode 0655
    variables({ :node_id => found_id })
  end
end 

service "ndbd" do
  supports :restart => true, :stop => true, :start => true, :status => true
  action :nothing
end

template "/etc/init.d/ndbd" do
  source "ndbd.erb"
  owner node[:ndb][:user]
  group node[:ndb][:group]
  mode 0754
  variables({ :node_id => found_id })
  notifies :enable, "service[ndbd]"
  notifies :restart,"service[ndbd]", :immediately
end

if node[:kagent][:enabled] == "true"
  Chef::Log.info "Trying to infer the ndbd ID by examining the local IP. If it matches the config.ini file, then we have our node."

  found_id = -1
  id = 1
  my_ip = my_private_ip()

  for ndbd in node[:ndb][:ndbd][:private_ips]
    if my_ip.eql? ndbd
      Chef::Log.info "Found matching IP address in the list of data nodes: #{ndbd} . ID= #{id}"
      found_id = id
    end
    id += 1
  end 

  Chef::Log.info "ID IS: #{id}"

  if found_id == -1
    Chef::Log.fatal "Could not find matching IP address is list of data nodes."
  end

  kagent_config "ndb" do
    service "NDB"
    start_script "#{node[:ndb][:scripts_dir]}/ndbd-start.sh"
    stop_script "#{node[:ndb][:scripts_dir]}/ndbd-stop.sh"
    init_script "#{node[:ndb][:scripts_dir]}/ndbd-init.sh"
    log_file "#{node[:ndb][:log_dir]}/ndb_#{found_id}_out.log"
    pid_file "#{node[:ndb][:log_dir]}/ndb_#{found_id}.pid"
  end

end

ndb_start "ndbd" do
end


# Here we set interrupts to be handled by only the first CPU

if (node[:ndb][:interrupts_isolated_to_single_cpu] == "true") && (not ::File.exists?( "#{node[:mysql][:base_dir]}/.balance_irqs"))
  case node["platform_family"]
  when "debian"
    
    file "/etc/default/irqbalance" do 
      owner node[:hdfs][:user]
      action :delete
    end

    template "/etc/default/irqbalance" do
      source "irqbalance.ubuntu.erb"
      owner "root"
      mode 0644
    end

  # Need to isolate CPUs from handling interrupts using grub:
    # http://wiki.linuxcnc.org/cgi-bin/wiki.pl?The_Isolcpus_Boot_Parameter_And_GRUB2
    # Test using strees:
    #  apt-get install stress && stress -c 24
    # Sometimes you may need to disable hyper-threading in bios, restart, then restart and re-enable
    # hyperthreading and it works
      template "/etc/grub.d/07_rtai" do
      source "07_rtai.erb"
      owner "root"
      mode 0644
    end

    execute "set_interrupts_to_first_cpu" do
      user "root"
      code <<-EOF
          service irqbalance stop
          source /etc/default/irqbalance 
          irqbalance
          update-grub
      touch #{node[:mysql][:base_dir]}/.balance_irqs
      EOF
      not_if { ::File.exists?( "#{node[:mysql][:base_dir]}/.balance_irqs" ) }
    end
    
  when "rhel"
    execute "set_interrupts_to_first_cpu" do
      user "root"
      code <<-EOF

      touch #{node[:mysql][:base_dir]}/.balance_irqs
      EOF
      not_if { ::File.exists?( "#{node[:mysql][:base_dir]}/.balance_irqs" ) }
    end

  end

end

homedir = node[:ndb][:user].eql?("root") ? "/root" : "/home/#{node[:ndb][:user]}"

# Add the mgmd hosts' public key, so that they can start/stop the ndbd on this node
# using passwordless ssh.
# Dont append if the public key is already in the authorized_keys or is empty
bash "add_mgmd_public_key" do
 user node[:ndb][:user]
 group node[:ndb][:group]
 code <<-EOF
      mkdir #{homedir}/.ssh
      echo "#{node[:ndb][:mgmd][:public_key]}" >> #{homedir}/.ssh/authorized_keys
      touch #{homedir}/.ssh/.mgmd_key_authorized
  EOF
 not_if { ::File.exists?( "#{homedir}/.ssh/.mgmd_key_authorized" || "#{node[:ndb][:mgmd][:public_key]}".empty? ) }
end


