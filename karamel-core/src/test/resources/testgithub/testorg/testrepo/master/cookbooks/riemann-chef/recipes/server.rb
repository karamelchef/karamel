#
# Cookbook Name:: riemann
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
include_recipe 'runit'

runit_service "riemann-server" do
  default_logger true
end