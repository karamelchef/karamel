node.default['java']['jdk_version'] = 7
node.default['java']['install_flavor'] = "openjdk"

# install Java 1.7
include_recipe "java"
