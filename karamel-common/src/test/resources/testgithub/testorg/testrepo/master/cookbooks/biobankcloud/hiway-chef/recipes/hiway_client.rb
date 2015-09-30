# create user hiway
user node[:hiway][:user] do
  password "$1$salt$N3yBrMPqdSW4WoCjoBJMm."
  supports :manage_home => true
  home "#{node[:hiway][:home]}"
  action :create
  shell '/bin/bash'
  system true
  not_if "getent passwd #{node[:hiway][:user]}"
end

# create data directory
directory "#{node[:hiway][:data]}" do
  owner node[:hiway][:user]
  group node[:hadoop][:group]
  mode "755"
  action :create
end

# add user hiway to group hadoop
group node[:hadoop][:group] do
  action :modify
  members node[:hiway][:user] 
  append true
end

if node[:hiway][:release] == "true"
  # download Hi-WAY binaries
  remote_file "#{Chef::Config[:file_cache_path]}/#{node[:hiway][:hiway][:release][:targz]}" do
    source node[:hiway][:hiway][:release][:url]
    owner node[:hiway][:user]
    group node[:hadoop][:group]
    mode "775"
    action :create_if_missing
  end
  
  # install Hi-WAY binaries
  bash "install_hiway" do
    user node[:hiway][:user]
    group node[:hadoop][:group]
    code <<-EOH
    set -e && set -o pipefail
      tar xvfz #{Chef::Config[:file_cache_path]}/#{node[:hiway][:hiway][:release][:targz]} -C #{node[:hiway][:software][:dir]}
    EOH
    not_if { ::File.exist?("#{node[:hiway][:hiway][:home]}") }
  end
else
  # install Git
  include_recipe "git"
  
  # install Maven
  package "maven" do
    options "--force-yes"
  end
  
  # git clone Hi-WAY
  git "/tmp/hiway" do
    repository node[:hiway][:hiway][:github_url]
    reference "master"
    user node[:hiway][:user]
    group node[:hadoop][:group]
    action :sync
  end
  
  # maven build Hi-WAY
  bash 'build-hiway' do
    user node[:hiway][:user]
    group node[:hadoop][:group]
    code <<-EOH
    set -e && set -o pipefail
      sed -i 's%<hadoop.version>[^<]*</hadoop.version>%<hadoop.version>#{node[:hadoop][:version]}</hadoop.version>%g' /tmp/hiway/hiway-core/pom.xml
      mvn -f /tmp/hiway/pom.xml package
      version=$(grep -Po '(?<=^\t<version>)[^<]*(?=</version>)' /tmp/hiway/pom.xml)
      cp -r /tmp/hiway/hiway-dist/target/hiway-dist-$version/hiway-$version #{node[:hiway][:hiway][:home]}
      mv #{node[:hiway][:hiway][:home]}/hiway-core-$version.jar #{node[:hiway][:hiway][:home]}/hiway-core.jar
    EOH
    not_if { ::File.exist?("#{node[:hiway][:hiway][:home]}") }
  end
end

# add script for running Hi-WAY
template "#{node[:hiway][:hiway][:home]}/hiway" do 
  source "hiway.erb"
  owner node[:hiway][:user]
  group node[:hadoop][:group]
  mode "755"
  action :create_if_missing
end

# add script for staging in workflow output data
template "#{node[:hiway][:hiway][:home]}/stage" do 
  source "stage.erb"
  owner node[:hiway][:user]
  group node[:hadoop][:group]
  mode "755"
  action :create_if_missing
end

# update Hadoop user environment for user hiway
hadoop_user_envs node[:hiway][:user] do
  action :update
end

# add HIWAY_HOME environment variable
bash 'update_env_variables' do
  user node[:hiway][:user]
  group node[:hadoop][:group]
  code <<-EOH
  set -e && set -o pipefail
    echo "export HIWAY_HOME=#{node[:hiway][:hiway][:home]}" | tee -a #{node[:hiway][:home]}/.bash*
  EOH
  not_if "grep -q HIWAY_HOME #{node[:hiway][:home]}/.bash_profile"
end

# add hiway to /usr/bin
bash 'update_env_variables' do
  user "root"
  code <<-EOH
  set -e && set -o pipefail
    ln -f -s #{node[:hiway][:hiway][:home]}/hiway /usr/bin/
    ln -f -s #{node[:hiway][:hiway][:home]}/stage /usr/bin/
  EOH
  not_if { ::File.exist?("/usr/bin/hiway") }
end

# copy Hi-WAY conf file to Hadoop conf dir
template "#{node[:hadoop][:conf_dir]}/hiway-site.xml" do
  user node[:hiway][:user]
  group node[:hadoop][:group]
  source "hiway-site.xml.erb"
  mode "0755"
end

# add the Hi-WAY jars to Hadoop classpath
bash "configure_hadoop_for_hiway" do
  user node[:hiway][:user]
  group node[:hadoop][:group]
  code <<-EOH
  set -e && set -o pipefail
    if grep -q "yarn.application.classpath" #{node[:hadoop][:home]}/etc/hadoop/yarn-site.xml
    then
      perl -i -0pe 's%<name>yarn.application.classpath</name>\\s*<value>%<name>yarn.application.classpath</name>\\n\\t\\t<value>#{node[:hiway][:hiway][:home]}/\\*, #{node[:hiway][:hiway][:home]}/lib/\\*, %' #{node[:hadoop][:home]}/etc/hadoop/yarn-site.xml
    else
      sed -i 's%</configuration>%\t<property>\n\t\t<name>yarn.application.classpath</name>\\n\\t\\t<value>#{node[:hiway][:hiway][:home]}/\\*, #{node[:hiway][:hiway][:home]}/lib/\\*, #{node[:hadoop][:yarn][:app_classpath]}</value>\\n\\t</property>\\n</configuration>%' #{node[:hadoop][:home]}/etc/hadoop/yarn-site.xml
    fi
  EOH
  not_if "grep -q yarn.application.classpath #{node[:hadoop][:home]}/etc/hadoop/yarn-site.xml && grep -q #{node[:hiway][:hiway][:home]} #{node[:hadoop][:home]}/etc/hadoop/yarn-site.xml"
end

# restart YARN RM for changes to yarn-site to take effect
service "resourcemanager" do
  action :restart
end

# create hiway user directory in HDFS (if necessary) and grant read and write rights to all users in hadoop group such that both the yarn and hiway users can use the directory
hadoop_hdfs_directory "#{node[:hiway][:hiway][:hdfs][:basedir]}" do
  action :create
  owner node[:hiway][:user]
  group node[:hiway][:group]
  mode "0775"
end
