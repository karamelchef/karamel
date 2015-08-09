
if node[:hadoop][:os_defaults] == "true" then
  node.default['sysctl']['allow_sysctl_conf'] = true
  node.default['sysctl']['params']['vm']['swappiness'] = 0
  node.default['sysctl']['params']['vm']['overcommit_memory'] = 1
  node.default['sysctl']['params']['vm']['overcommit_ratio'] = 100
  node.default['sysctl']['params']['net']['core']['somaxconn']= 1024
  include_recipe 'sysctl::apply'

    #
    # http://www.slideshare.net/vgogate/hadoop-configuration-performance-tuning
    #
    case node[:platform_family]
    when "debian"
      bash "configure_os" do
        user "root"
        code <<-EOF
   EOF
      end
    when "redhat"
      bash "configure_os" do
        user "root"
        code <<-EOF
      echo "never" > /sys/kernel/mm/redhat_transparent_hugepages/defrag
     EOF
      end
      
    end

    # limits.d settings
    %w(hdfs mapred yarn).each do |u|
      ulimit_domain u do
        node['hadoop']['limits'].each do |k, v|
          rule do
            item k
            type '-'
            value v
          end
        end
        only_if { node['hadoop'].key?('limits') && !node['hadoop']['limits'].empty? }
      end
    end # End limits.d

    # Remove extra mapreduce file, if it exists
    file '/etc/security/limits.d/mapreduce.conf' do
      action :delete
    end

  end

node.default['java']['jdk_version'] = 7
include_recipe "java"

kagent_bouncycastle "jar" do
end
 
group node[:hadoop][:group] do
  action :create
end

user node[:hdfs][:user] do
  supports :manage_home => true
  action :create
  home "/home/#{node[:hdfs][:user]}"
  system true
  shell "/bin/bash"
  not_if "getent passwd #{node[:hdfs]['user']}"
end

user node[:hadoop][:yarn][:user] do
  supports :manage_home => true
  home "/home/#{node[:hadoop][:yarn][:user]}"
  action :create
  system true
  shell "/bin/bash"
  not_if "getent passwd #{node[:hadoop][:yarn]['user']}"
end

user node[:hadoop][:mr][:user] do
  supports :manage_home => true
  home "/home/#{node[:hadoop][:mr][:user]}"
  action :create
  system true
  shell "/bin/bash"
  not_if "getent passwd #{node[:hadoop][:mr]['user']}"
end

group node[:hadoop][:group] do
  action :modify
  members ["#{node[:hdfs][:user]}", "#{node[:hadoop][:yarn][:user]}", "#{node[:hadoop][:mr][:user]}"]
  append true
end

case node[:platform_family]
when "debian"
  package "openssh-server" do
    action :install
    options "--force-yes"
  end

  package "openssh-client" do
    action :install
    options "--force-yes"
  end
when "rhel"

end

if node[:hadoop][:native_libraries].eql? "true" 

  # build hadoop native libraries: http://www.drweiwang.com/build-hadoop-native-libraries/
  # g++ autoconf automake libtool zlib1g-dev pkg-config libssl-dev cmake

  include_recipe 'build-essential::default'
  include_recipe 'cmake::default'

    protobuf_url = node[:hadoop][:protobuf_url]
    base_protobuf_filename = File.basename(protobuf_url)
    cached_protobuf_filename = "#{Chef::Config[:file_cache_path]}/#{base_protobuf_filename}"

    remote_file cached_protobuf_filename do
      source protobuf_url
      owner node[:hdfs][:user]
      group node[:hadoop][:group]
      mode "0775"
      action :create_if_missing
    end

  protobuf_lib_prefix = "/usr"
  case node[:platform_family]
  when "debian"
    package "g++" do
      options "--force-yes"
    end
    package "autoconf" do
      options "--force-yes"
    end
    package "automake" do
      options "--force-yes"
    end
    package "libtool" do
      options "--force-yes"
    end
    package "zlib1g-dev" do
      options "--force-yes"
    end
    package "libssl-dev" do
      options "--force-yes"
    end
    package "pkg-config" do
      options "--force-yes"
    end
    package "maven" do
      options "--force-yes"
    end

  when "rhel"
  protobuf_lib_prefix = "/" 

# https://github.com/burtlo/ark
    ark "maven" do
      url "http://apache.mirrors.spacedump.net/maven/maven-3/#{node[:maven][:version]}/binaries/apache-maven-#{node[:maven][:version]}-bin.tar.gz"
      version "#{node[:maven][:version]}"
      path "/usr/local/maven/"
      home_dir "/usr/local/maven"
 #     checksum  "#{node[:maven][:checksum]}"
      append_env_path true
      owner "#{node[:hdfs][:user]}"
    end
#    bash 'install-maven' do
#       user "root"
#       code <<-EOH
#         set -e
#        sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
#        sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
#         sudo yum install -y apache-maven
# 	EOH
#      not_if { ::File.exist?("/etc/yum.repos.d/epel-apache-maven.repo") }
#     end
       
  
  end
   protobuf_name_no_extension = File.basename(base_protobuf_filename, ".tar.gz")
   protobuf_name = "#{protobuf_lib_prefix}/.#{protobuf_name_no_extension}_downloaded"
   bash 'extract-protobuf' do
      user "root"
      code <<-EOH
        set -e
        cd #{Chef::Config[:file_cache_path]}
	tar -zxf #{cached_protobuf_filename} 
        cd #{protobuf_name_no_extension}
        ./configure --prefix=#{protobuf_lib_prefix}
        make
        make check
        make install
        touch #{protobuf_name}
	EOH
     not_if { ::File.exist?("#{protobuf_name}") }
    end

end

directory node[:hadoop][:dir] do
  owner node[:hdfs][:user]
  group node[:hadoop][:group]
  mode "0774"
  recursive true
  action :create
end

directory node[:hadoop][:data_dir] do
  owner node[:hdfs][:user]
  group node[:hadoop][:group]
  mode "0774"
  recursive true
  action :create
end


directory node[:hadoop][:dn][:data_dir] do
  owner node[:hdfs][:user]
  group node[:hadoop][:group]
  mode "0774"
  recursive true
  action :create
end

directory node[:hadoop][:nn][:name_dir] do
  owner node[:hdfs][:user]
  group node[:hadoop][:group]
  mode "0774"
  recursive true
  action :create
end


package_url = node[:hadoop][:download_url]
  #"#{node[:download_url]}/hadoop-#{node[:hadoop][:version]}.tar.gz"

Chef::Log.info "Downloading hadoop binaries from #{package_url}"
base_package_filename = File.basename(package_url)
cached_package_filename = "#{Chef::Config[:file_cache_path]}/#{base_package_filename}"

remote_file cached_package_filename do
  source package_url
  owner node[:hdfs][:user]
  group node[:hadoop][:group]
  mode "0755"
  # TODO - checksum
  action :create_if_missing
end

hin = "#{node[:hadoop][:home]}/.#{base_package_filename}_downloaded"
base_name = File.basename(base_package_filename, ".tar.gz")
# Extract and install hadoop
bash 'extract-hadoop' do
  user "root"
  code <<-EOH
	tar -zxf #{cached_package_filename} -C #{node[:hadoop][:dir]}
        # chown -L : traverse symbolic links
        chown -RL #{node[:hdfs][:user]}:#{node[:hadoop][:group]} #{node[:hadoop][:home]}
        # remove the config files that we would otherwise overwrite
        rm #{node[:hadoop][:home]}/etc/hadoop/yarn-site.xml
        rm #{node[:hadoop][:home]}/etc/hadoop/core-site.xml
        rm #{node[:hadoop][:home]}/etc/hadoop/hdfs-site.xml
        rm #{node[:hadoop][:home]}/etc/hadoop/mapred-site.xml
        touch #{hin}
	EOH
  not_if { ::File.exist?("#{hin}") }
end


if node[:hadoop][:native_libraries] == "true" 

  hadoop_src_url = node[:hadoop][:hadoop_src_url]
  base_hadoop_src_filename = File.basename(hadoop_src_url)
  cached_hadoop_src_filename = "/tmp/#{base_hadoop_src_filename}"

  remote_file cached_hadoop_src_filename do
    source hadoop_src_url
    owner node[:hdfs][:user]
    group node[:hadoop][:group]
    mode "0755"
    action :create_if_missing
  end

  hadoop_src_name = File.basename(base_hadoop_src_filename, ".tar.gz")
  natives="#{node[:hadoop][:dir]}/.downloaded_#{hadoop_src_name}"

  bash 'build-hadoop-from-src-with-native-libraries' do
    user node[:hdfs][:user]
    code <<-EOH
        set -e
        cd /tmp
	tar -xf #{cached_hadoop_src_filename} 
        cd #{hadoop_src_name}
        mvn package -Pdist,native -DskipTests -Dtar
        cp -r hadoop-dist/target/hadoop-#{node[:hadoop][:version]}/lib/native/* #{node[:hadoop][:home]}/lib/native/
        chown -R #{node[:hdfs][:user]} #{node[:hadoop][:home]}/lib/native/
        touch #{natives}
	EOH
    not_if { ::File.exist?("#{natives}") }
  end

end

 directory node[:hadoop][:logs_dir] do
   owner node[:hdfs][:user]
   group node[:hadoop][:group]
   mode "0775"
   action :create
 end

 directory node[:hadoop][:tmp_dir] do
   owner node[:hdfs][:user]
   group node[:hadoop][:group]
   mode "1777"
   action :create
 end

link "#{node[:hadoop][:dir]}/hadoop" do
  to node[:hadoop][:home]
end
include_recipe "hadoop"


bash 'update_permissions_etc_dir' do
  user "root"
  code <<-EOH
    set -e
    chmod 775 #{node[:hadoop][:conf_dir]}
  EOH
end

if node[:hadoop][:cgroups].eql? "true" 

  case node[:platform_family]
  when "debian"
    package "libcgroup-dev" do
    end

  when "redhat"

    # This doesnt work for rhel-7
    package "libcgroup" do
    end
  end
  cgroups_mounted= "#{Chef::Config[:file_cache_path]}/.cgroups_mounted"
  bash 'setup_mount_cgroups' do
    user "root"
    code <<-EOH
    set -e
    if [ ! -d "/cgroup" ] ; then
       mkdir /cgroup
    fi
    mount -t cgroup -o cpu cpu /cgroup
    touch #{cgroups_mounted}
  EOH
     not_if { ::File.exist?("#{cgroups_mounted}") }
  end

end
