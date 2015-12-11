Developer Guide
=================

We have organized our code into two main projects, *karamel-core* and *karamel-ui*. The core is our engine for launching, installing and monitoring clusters. The UI is a standalone web application containing several designers and visualizers. There is a REST-API in between the UI and the core.

The core and REST-API are programmed in Java 7, and the UI is programmed in `Angular JS <https://angularjs.org/>`_.  

Code quality 
~~~~~~~~~~~~

1. Testability and mockability: Write your code in a way that you test each unit separately. Split concerns into different modules that you can mock one when testing the other. We use JUnit-4 for unit testing and `mockito <http://mockito.org/>`_ for mocking. 
2. Code styles: Write a DRY (Don't repeat yourself) code, use spaces instead of tab and line width limit is 120. 
3. We use `Google Guava <https://code.google.com/p/guava-libraries/wiki/GuavaExplained>`_ and its best practices, specially the basic ones such as nullity checks and preconditions. 

Build and run from Source
~~~~~~~~~~~~~~~~~~~~~~~~~

Ubuntu Requirements:

.. code-block:: bash

  apt-get install lib32z1 lib32ncurses5 lib32bz2-1.0

Centos 7 Requirements:

.. code-block:: bash

  Install zlib.i686, ncurses-libs.i686, and bzip2-libs.i686 on CentOS 7

Building from root directory:

.. code-block:: bash

  mvn install 

Running:

.. code-block:: bash

  cd karamel-ui/target/appassembler
  ./bin/karamel


Building Window Executables
~~~~~~~~~~~~~~~~~~~~~~~~~~~

You need to have 32-bit libraries to build the windows exe from Linux, as the launch4j plugin requires them.

.. code-block:: bash

  sudo apt-get install gcc binutils-mingw-w64-x86-64 -y
  # Then replace 32-bit libraries with their 64-bit equivalents
  cd /home/ubuntu/.m2/repository/net/sf/launch4j/launch4j/3.8.0/launch4j-3.8.0-workdir-linux/bin
  rm ld windres
  ln -s /usr/bin/x86_64-w64-mingw32-ld ./ld
  ln -s /usr/bin/x86_64-w64-mingw32-windres ./windres

Then run maven with the -Pwin to run the plugin:
.. code-block:: bash

  mvn -Dwin package
