private_ip = my_private_ip()
public_ip = my_public_ip()

file "/tmp/jd.txt" do
  owner 'root'
  group 'root'
  mode '0755'
  action :create
end

script 'run_experiment' do
  cwd "/usr/local/hadoop"
  user 'yarn'
  group 'hadoop'
  interpreter "bash"
  code <<-EOM
    cat /tmp/jd.txt
  EOM
end