#
# Cookbook Name:: tablespoon-riemann
# Recipe:: server
#
# Copyright (c) 2015 The Authors, All Rights Reserved.
=begin
#<
Installs `riemann-server` with `/usr/local/riemann/etc/user.config` for manual changes to rieman-server.
TODO: 
  - add temlate `riemann-chef.config` to include configuration from the cookbook.
  
#>
=end

case node.platform_family
when 'debian'
  include_recipe 'apt'
when 'rhel'
  include_recipe 'yum'
else
  log 'Platform Not Supported'
  exit 1
end

conf_dir = ::File.join(
  node.tablespoon-riemann.home_dir,
  'etc'
)

include_recipe 'runit'
include_recipe 'java'
include_recipe 'ark'
include_recipe 'tablespoon-riemann::infra'

ark 'riemann' do
  url "#{node.tablespoon-riemann.download.url}riemann-#{node.tablespoon-riemann.download.version}.tar.bz2"
  version node.tablespoon-riemann.download.version
  checksum node.tablespoon-riemann.download.checksum
  owner node.tablespoon-riemann.user
  home_dir node.tablespoon-riemann.home_dir
  action :install
end

link '/etc/riemann' do
  to conf_dir
end

template ::File.join(conf_dir, 'riemann.config') do
  owner node.tablespoon-riemann.user
  group node.tablespoon-riemann.group
  source 'riemann.config.erb'
  mode '0644'
  notifies :hup, 'runit_service[riemann-server]'
end

runit_service 'riemann-server'

file node.tablespoon-riemann.config.userfile do
  owner node.tablespoon-riemann.user
  group node.tablespoon-riemann.group
  action :create_if_missing
  mode '0644'
end
