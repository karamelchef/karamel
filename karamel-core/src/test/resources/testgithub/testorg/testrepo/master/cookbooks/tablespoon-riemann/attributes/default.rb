# 'user' and 'group' define the unix user and group, respectively, 
# that the experiment will be excecuted as.
default.tablespoon-riemann.group = "riemann"
default.tablespoon-riemann.user = "riemann"
default.tablespoon-riemann.home_dir = "/usr/local/riemann"

default.tablespoon-riemann.download.url       = 'http://aphyr.com/riemann/'
default.tablespoon-riemann.download.checksum  = 'ec697519e80781dc478983de04204760f2790da1715acfd233b35da071be5455'
default.tablespoon-riemann.download.version   = '0.2.10'

default.tablespoon-riemann.server.public_ips        =   ['']
default.tablespoon-riemann.server.private_ips		 =   ['']
default.tablespoon-riemann.server.bind        =   '0.0.0.0'
default.tablespoon-riemann.server.port        =   '5555'
default.tablespoon-riemann.dash.port          =   '5556'     
default.tablespoon-riemann.config.userfile    =   '/usr/local/riemann/etc/user.config'

default.java.jdk_version = '7'
default.java.install_flavor = 'oracle'
default.java.oracle.accept_oracle_download_terms = true

