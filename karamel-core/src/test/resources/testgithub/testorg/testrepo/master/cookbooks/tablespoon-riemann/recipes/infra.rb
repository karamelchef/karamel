#
# Cookbook Name:: tablespoon-riemann
# Recipe:: infra
#
# Copyright (c) 2015 The Authors, All Rights Reserved.
=begin
#<
Configure user and group for riemann-servie and riemann-dash
#>
=end
group node.tablespoon-riemann.group do
  action :create
end

user node.tablespoon-riemann.user do
  action :create
  home node.tablespoon-riemann.home_dir
  comment 'Riemann User'
  system true
  gid node.tablespoon-riemann.group
  shell '/bin/false'
end