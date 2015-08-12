##########################################################################
# Create databases to be used by web application and hop and populate them.
###############################################################################

# Links for reading:
# http://computingat40s.wordpress.com/how-to-create-a-custom-realm-in-glassfish-3-1-2-2/


private_ip=my_private_ip()
kthfs_db = "kthfs"
hops_db = "hops"
mysql_user=node[:mysql][:user]
mysql_pwd=node[:mysql][:password]


hopsworks_grants "create_kthfs_db"  do
  action :create_db
end 

tables_path = "#{Chef::Config[:file_cache_path]}/tables.sql"
rows_path = "#{Chef::Config[:file_cache_path]}/rows.sql"

hopsworks_grants "kthfs"  do
  tables_path  "#{tables_path}"
  rows_path  "#{rows_path}"
  action :nothing
end 

 template "#{rows_path}" do
   source File.basename("#{rows_path}") + ".erb"
   owner node[:glassfish][:user]
   group node[:glassfish][:group]
   mode 0755
   action :create
 end

begin
  t = resources("template[#{tables_path}]")
rescue
  Chef::Log.info("Could not find previously defined #{tables_path} resource")
  t = template tables_path do
    source File.basename("#{tables_path}") + ".erb"
    owner "root" 
    mode "0600"
    action :create
    variables({
                :private_ip => private_ip
              })
    notifies :populate_db, "hopsworks_grants[kthfs]", :immediately
  end 
end


###############################################################################
# config glassfish
###############################################################################

Chef::Log.info "Admin user: #{node.default[:hopsworks][:admin][:user]}"

domain_name="domain1"
admin_port=node[:glassfish][:admin][:port] 
port=node[:glassfish][:port]
secure=false
username="#{node[:hopsworks][:admin][:user]}"
password="#{node[:hopsworks][:admin][:password]}"
master_password="#{node[:glassfish][:cert][:password]}"
password_file="#{node[:glassfish]['domains_dir']}/#{domain_name}_admin_passwd"
config_dir="#{node[:glassfish][:domains_dir]}/#{domain_name}/config"
asadmin="#{node[:glassfish][:base_dir]}/glassfish/bin/asadmin"

glassfish_domain "#{domain_name}" do
  username username
  password password
  password_file username ? "#{node[:glassfish]['domains_dir']}/#{domain_name}_admin_passwd" : nil
  master_password master_password
  port port 
  admin_port admin_port
  secure secure 
  echo true
  terse false
  min_memory node[:glassfish][:min_mem]
  max_memory node[:glassfish][:max_mem]
  max_stack_size node[:glassfish][:max_stack_size]
  max_perm_size node[:glassfish][:max_perm_size]
  action :create
end


keytool_path="#{node[:java][:java_home]}/bin"

if node[:java][:java_home].to_s == ''
 if ENV['JAVA_HOME'].to_s != ''
   keytool_path="#{ENV['JAVA_HOME']}/bin"
 else
   keytool_path="/usr/bin"
 end
end

  bash "delete_invalid_certs" do
    user node[:glassfish][:user]
   group node[:glassfish][:group]
    code <<-EOF
 # This cert has expired, blocks startup of glassfish
    #{keytool_path}/keytool -delete -alias gtecybertrust5ca -keystore #{config_dir}/cacerts.jks -storepass #{master_password}
    #{keytool_path}/keytool -delete -alias gtecybertrustglobalca -keystore #{config_dir}/cacerts.jks -storepass #{master_password}
    EOF
    only_if "#{keytool_path}/bin/keytool -keystore #{config_dir}/cacerts.jks -storepass #{master_password} -list | grep -i gtecybertrustglobalca"
 end

 bash "create_glassfish_certs" do
   user node[:glassfish][:user]
   group node[:glassfish][:group]
    code <<-EOF
    #{keytool_path}/keytool -delete -alias s1as -keystore #{config_dir}/keystore.jks -storepass #{master_password}
    #{keytool_path}/keytool -delete -alias glassfish-instance -keystore #{config_dir}/keystore.jks -storepass #{master_password}

  # Generate two new certs with same alias as original certs
    #{keytool_path}/keytool -keysize 2048 -genkey -alias s1as -keyalg RSA -dname "CN=#{node[:karamel][:cert][:cn]},O=#{node[:karamel][:cert][:o]},OU=#{node[:karamel][:cert][:ou]},L=#{node[:karamel][:cert][:l]},S=#{node[:karamel][:cert][:s]},C=#{node[:karamel][:cert][:c]}" -validity 3650 -keypass #{node[:hopsworks][:cert][:password]} -storepass #{master_password} -keystore #{config_dir}/keystore.jks
    #{keytool_path}/keytool -keysize 2048 -genkey -alias glassfish-instance -keyalg RSA -dname "CN=#{node[:karamel][:cert][:cn]},O=#{node[:karamel][:cert][:o]},OU=#{node[:karamel][:cert][:ou]},L=#{node[:karamel][:cert][:l]},S=#{node[:karamel][:cert][:s]},C=#{node[:karamel][:cert][:c]}" -validity 3650 -keypass #{node[:hopsworks][:cert][:password]} -storepass #{master_password} -keystore #{config_dir}/keystore.jks


    #Add two new certs to cacerts.jks
    #{keytool_path}/keytool -export -alias glassfish-instance -file glassfish-instance.cert -keystore #{config_dir}/keystore.jks -storepass #{master_password}
    #{keytool_path}/keytool -export -alias s1as -file #{config_dir}/s1as.cert -keystore #{config_dir}/keystore.jks -storepass #{master_password}
  
    #{keytool_path}/keytool -import -noprompt -alias s1as -file #{config_dir}/s1as.cert -keystore #{config_dir}/cacerts.jks -storepass #{master_password}
    #{keytool_path}/keytool -import -noprompt -alias glassfish-instance -file #{config_dir}/glassfish-instance.cert -keystore #{config_dir}/cacerts.jks -storepass #{master_password}
  
    touch #{node[:glassfish][:base_dir]}/.certs_generated
    EOF
    not_if "test -f #{node[:glassfish][:base_dir]}/.certs_generated"
 end

admin_pwd="#{node[:glassfish][:domains_dir]}/#{domain_name}_admin_passwd"

file "#{admin_pwd}" do
   action :delete
end

template "#{admin_pwd}" do
  cookbook 'hopsworks'
  source "password.erb"
  owner node[:glassfish][:user]
  group node[:glassfish][:group]
  mode "0600"
  variables(:password => password, :master_password => master_password)
end

login_cnf="#{node[:glassfish][:domains_dir]}/#{domain_name}/config/login.conf"
file "#{login_cnf}" do
   action :delete
end
template "#{login_cnf}" do
  cookbook 'hopsworks'
  source "login.conf.erb"
  owner node[:glassfish][:user]
  group node[:glassfish][:group]
  mode "0600"
end

cauth = File.basename(node[:glassfish][:cauth_url])

remote_file "#{node[:glassfish][:domains_dir]}/#{domain_name}/lib/#{cauth}"  do
  user node[:glassfish][:user]
  group node[:glassfish][:group]
  source node[:glassfish][:cauth_url]
  mode 0755
  action :create_if_missing
end


glassfish_secure_admin domain_name do
  domain_name domain_name
  password_file password_file 
  username username
  admin_port admin_port
  secure false
  action :enable
end


template "/etc/init.d/glassfish" do
  source "glassfish.erb"
  mode "0751"
  variables(:domain_name => "#{domain_name}", :username => "#{username}", :password_file => "#{admin_pwd}", :listen_ports => [node[:glassfish][:port], node[:glassfish][:admin][:port]],
  :command => "#{node[:glassfish][:base_dir]}/glassfish/bin/start-domain.java_command")
end



 bash "restart_#{domain_name}_after_enable_security" do
  user "root"
  code <<-EOF
    service glassfish restart || true
   EOF
 end

glassfish_auth_realm "cauthRealm" do
 domain_name domain_name
 realm_name "cauthRealm"
 username username
 password_file password_file
 secure false
 classname "se.kth.bbc.crealm.CustomAuthRealm"
 jaas_context "cauthRealm"
  properties('encoding' => "hex", 'password-column' => "password", 'datasource-jndi' => "jdbc/#{kthfs_db}", 'group-table' => "users_groups", 'user-table' => "users", 'charset' => "UTF-8", 'group-name-column' => "group_name", 'user-name-column' => "email", 'otp-secret-column' => "secret", 'user-status-column' => "status", 'group-table-user-name-column' => "email", 'yubikey-table' => "yubikey") # 
 action :create
end

mysql_tgz = File.basename(node[:glassfish]['mysql_connector'])
mysql_base = File.basename(node[:glassfish]['mysql_connector'], ".tar.gz") 

path_mysql_tgz = "#{Chef::Config[:file_cache_path]}/#{mysql_tgz}"

remote_file path_mysql_tgz do
  user "root"
  source node[:glassfish]['mysql_connector']
  mode 0755
  action :create_if_missing
end


bash "unpack_mysql_connector" do
    user "root"
    code <<-EOF
   tar -xzf #{path_mysql_tgz} -C #{Chef::Config[:file_cache_path]}
   # copy mysql-connector jar file to lib/ dir of domain1.
   chown #{node[:glassfish][:user]}:#{node[:glassfish][:group]} #{Chef::Config[:file_cache_path]}/#{mysql_base}/#{mysql_base}-bin.jar
   chmod 755 #{Chef::Config[:file_cache_path]}/#{mysql_base}/#{mysql_base}-bin.jar
   cp #{Chef::Config[:file_cache_path]}/#{mysql_base}/#{mysql_base}-bin.jar #{node[:glassfish]['domains_dir']}/#{domain_name}/lib/
   chown #{node[:glassfish][:user]}:#{node[:glassfish][:group]} #{node[:glassfish]['domains_dir']}/#{domain_name}/lib/#{mysql_base}-bin.jar
   touch #{Chef::Config[:file_cache_path]}/.#{mysql_base}_downloaded
EOF
  not_if { ::File.exists?( "#{node[:glassfish][:base_dir]}/.#{mysql_base}_downloaded")}
end


if node[:ndb][:mysqld][:private_ips].length == 1
  mysql_ips = private_recipe_ip("ndb","mysqld")
else
  mysql_ips = node[:ndb][:mysqld][:private_ips].join("\\:" + node[:ndb][:mysql_port].to_s + ",")
  mysql_ips.chop
end

Chef::Log.info "JDBC Connection: #{mysql_ips}"

# package "expect" do
# end                                                                                                                                                                            
# bash "change_master_passwd" do
#    user node[:glassfish][:user]
#    group node[:glassfish][:group]
#    code <<-EOF
#     service glassfish stop
#     expect -c 'spawn #{asadmin} change-master-password --savemasterpassword=true
#     expect "Please enter the new master password> "
#     send "#{node[:karamel][:master][:password]}\r"
#     expect "Please enter the new master password again> "
#     send "#{node[:karamel][:master][:password]}\r"
#     expect eof'
#     service glassfish start
#   EOF
#   not_if { ::File.exists?( "#{password_file}")}
# end

bash "install_jdbc" do
   user node[:glassfish][:user]
   group node[:glassfish][:group]
 code <<-EOF
#{asadmin} --user #{username} --passwordfile #{admin_pwd} create-jdbc-connection-pool  --datasourceclassname com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource --restype javax.sql.DataSource --creationretryattempts=1 --creationretryinterval=2 --validationmethod=auto-commit --isconnectvalidatereq=true --property "ServerName=#{mysql_ips}:Port=#{node[:ndb][:mysql_port]}:User=#{mysql_user}:Password=#{mysql_pwd}:DatabaseName=#{kthfs_db}" #{kthfs_db}
#{asadmin} --user #{username} --passwordfile #{admin_pwd} create-jdbc-resource --connectionpoolid #{kthfs_db} --enabled=true jdbc/#{kthfs_db}
#{asadmin} --user #{username} --passwordfile #{admin_pwd} create-jdbc-connection-pool  --datasourceclassname com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource --restype javax.sql.DataSource --creationretryattempts=1 --creationretryinterval=2 --validationmethod=auto-commit --isconnectvalidatereq=true --property "ServerName=#{mysql_ips}:Port=#{node[:ndb][:mysql_port]}:User=#{mysql_user}:Password=#{mysql_pwd}:DatabaseName=#{hops_db}" #{hops_db}
#{asadmin} --user #{username} --passwordfile #{admin_pwd} create-jdbc-resource --connectionpoolid #{hops_db} --enabled=true jdbc/#{hops_db}
touch #{node[:glassfish][:domains_dir]}/#{domain_name}/.#{kthfs_db}_jdbc_installed
EOF
  not_if { ::File.exists?( "#{node[:glassfish][:domains_dir]}/#{domain_name}/.#{kthfs_db}_jdbc_installed") }
 end

command_string = []
command_string << "#{asadmin} --user #{username} --passwordfile #{admin_pwd}  create-auth-realm --classname com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm --property \"jaas-context=jdbcRealm:datasource-jndi=jdbc/#{kthfs_db}:group-table=users_groups:user-table=users:group-name-column=GROUPNAME:digest-algorithm=none:user-name-column=EMAIL:encoding=Hex:password-column=PASSWORD:assign-groups=ADMIN,USER,AGENT:group-table-user-name-column=EMAIL:digestrealm-password-enc-algorithm= :db-user=#{mysql_user}:db-password=#{mysql_pwd}\" DBRealm"
command_string << "#{asadmin} --user #{username} --passwordfile #{admin_pwd}  set server-config.security-service.default-realm=cauthRealm"
command_string << "#{asadmin} --user #{username} --passwordfile #{admin_pwd}  set domain.resources.jdbc-connection-pool.#{kthfs_db}.is-connection-validation-required=true"
command_string << "#{asadmin} --user #{username} --passwordfile #{admin_pwd}  set domain.resources.jdbc-connection-pool.#{hops_db}.is-connection-validation-required=true"
command_string << "#{asadmin} --user #{username} --passwordfile #{admin_pwd} set server-config.network-config.protocols.protocol.admin-listener.security-enabled=true"
command_string << "#{asadmin} --user #{username} --passwordfile #{admin_pwd} enable-secure-admin"
command_string << "# #{asadmin} --user #{username} --passwordfile #{admin_pwd}  set-log-level javax.enterprise.system.core.security=FINEST"




Chef::Log.info(command_string.join("\t"))
# See http://docs.oracle.com/cd/E26576_01/doc.312/e24938/create-auth-realm.htm
 bash "jdbc_auth_realm" do
   user node[:glassfish][:user]
   group node[:glassfish][:group]
   code command_string.join("\n")
   not_if "#{asadmin} --user #{username} --passwordfile #{admin_pwd} list-auth-realms | grep -i DBRealm"
 end


bash "restart_#{domain_name}_after_jdbc_installed" do
 user "root"
 code <<-EOF
   sleep 5
   service glassfish restart || true
  EOF
end



kthfsmgr_url = node['kthfs']['mgr']
kthfsmgr_filename = File.basename(kthfsmgr_url)
cached_kthfsmgr_filename = "#{Chef::Config[:file_cache_path]}/#{kthfsmgr_filename}"

Chef::Log.info "Downloading #{cached_kthfsmgr_filename} from #{kthfsmgr_url} "

remote_file cached_kthfsmgr_filename do
    source kthfsmgr_url
    mode 0755
    owner node[:glassfish][:user]
    group node[:glassfish][:group]
    action :create_if_missing
end


bash "set_long_timeouts_for_ssh_ops_fix_cdi_bug" do
  user node[:glassfish][:user]
  group node[:glassfish][:group]
 code <<-EOF
   cd #{node[:glassfish][:base_dir]}/glassfish/bin   
   #{asadmin} --user #{username} --passwordfile #{admin_pwd}  set server-config.network-config.protocols.protocol.http-listener-1.http.request-timeout-seconds=7200
   #{asadmin} --user #{username} --passwordfile #{admin_pwd}  set server-config.network-config.protocols.protocol.http-listener-2.http.request-timeout-seconds=7200
  
   #{asadmin} --user #{username} --passwordfile #{admin_pwd}  set config1.availability-service.web-container-availability.sso-failover-enabled="true"
 
   # http://www.eclipse.org/forums/index.php/t/490794/
   #{asadmin} --user #{username} --passwordfile #{admin_pwd}  set configs.config.server-config.cdi-service.enable-implicit-cdi=false
 EOF
end

# email resources https://docs.oracle.com/cd/E18930_01/html/821-2416/giowr.html
bash "enable_mail_connector" do
  user node[:glassfish][:user]
  group node[:glassfish][:group]
 code <<-EOF
   cd #{node[:glassfish][:base_dir]}/glassfish/bin   
   #{asadmin} --user #{username} --passwordfile #{admin_pwd} create-javamail-resource --mailhost #{node[:hopsworks][:smtp]}  --mailuser #{node[:hopsworks][:email_address]} --fromaddress #{node[:hopsworks][:email_address]} --storeprotocol imap --storeprotocolclass com.sun.mail.imapIMAPStore --transprotocol=smtp --transprotocolclass=com.sun.mail.smtp.SMTPTransport --debug=false --enabled=true --property "smtp.starttls.enable=true:smtp.socketFactory.fallback=false:smtp.socketFactory.port=465:smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory: smtp.password=#{node[:hopsworks][:smtp_password]}#:smtp.port=465:smtp.user=#{node[:hopsworks][:email_address]}:mail-smtp.auth=true" mail/BBCMail
 EOF
   not_if "#{asadmin} --user #{username} --passwordfile #{admin_pwd} list-javamail-resources | grep -i BBCMail"
end

Chef::Log.info "Installing HopsHub "
command_string = []
# Some dependencies, such as guice and jclouds use JSR 330 annotation, which means we have to switch off CDI.
# We need to specify the beans explicitly
#  --verify=true --createtables=true 
# https://blogs.oracle.com/theaquarium/entry/default_cdi_enablement_in_java
# asadmin set configs.config.server-config.cdi-service.enable-implicit-cdi=false  
# http://www.eclipse.org/forums/index.php/t/490794/
command_string << "#{asadmin} --user #{username} --passwordfile #{admin_pwd} deploy --enabled=true --upload=true --availabilityenabled=true --force=true --name HopsHub #{cached_kthfsmgr_filename}"
Chef::Log.info(command_string.join("\t"))
bash "installing_dashboard" do
  user node[:glassfish][:user]
  group node[:glassfish][:group]
   code command_string.join("\n")
  not_if "#{asadmin} --user #{username} --passwordfile #{admin_pwd}  list-applications --type ejb | grep -w 'HopsHub'"
end


# http://www.nabisoft.com/tutorials/glassfish/installing-glassfish-311-on-ubuntu
bash "disable_poweredby" do
   user "root"
   code <<-EOF
   cd #{node[:glassfish][:base_dir]}/glassfish/bin   
   #disable sending x-powered-by in http header (Glassfish obfuscation)
   #{asadmin} --user #{username} --passwordfile #{admin_pwd}  set server.network-config.protocols.protocol.http-listener-1.http.xpowered-by=false 
   #{asadmin} --user #{username} --passwordfile #{admin_pwd}  set server.network-config.protocols.protocol.http-listener-2.http.xpowered-by=false
   #{asadmin} --user #{username} --passwordfile #{admin_pwd}  set server.network-config.protocols.protocol.admin-listener.http.xpowered-by=false
  
   #get rid of http header field value "server" (Glassfish obfuscation)
   # skip if failed: || true
   #{asadmin} --user #{username} --passwordfile #{admin_pwd}  create-jvm-options -Dproduct.name="" || true
   chown #{node[:glassfish][:user]} /usr/local
   touch #{node[:glassfish][:base_dir]}/.poweredby_disabled
   EOF
  not_if { ::File.exist?("#{node[:glassfish][:base_dir]}/.poweredby_disabled") }
end


if "#{node[:ndb][:enabled]}" == "true"
   alter_table_path = "#{Chef::Config[:file_cache_path]}/alter-table-ndb.sql"

  hopsworks_restart "switchToNdb" do
    alter_path alter_table_path
  end

   begin
     t = resources("template[#{alter_table_path}]")
   rescue
     Chef::Log.info("Could not find previously defined #{alter_table_path} resource")
     t = template alter_table_path do
       source File.basename("#{alter_table_path}") + ".erb"
       owner "root"
       group node['mysql']['root_group']
       mode "0600"
       action :create
       notifies :alter_tables, "hopsworks_restart[switchToNdb]", :immediately
     end 
   end
end


if node[:kagent][:enabled] == "true"
 kagent_kagent "restart_agent_to_register" do
   action :restart
 end
end
