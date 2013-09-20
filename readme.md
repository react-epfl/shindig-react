Apache Shindig version for Graasp
=================================
An Apache Shindig extended with Spaces and other adaptations
for Graasp (based on apache shindig 2.5.0)

Memo
====

    $ make info        # shows useful info and all the commands


Get the code
================

Checkout the main code
--------
Get the shindig source code

    $ git clone git@github.com:react-epfl/shindig.git

Checkout the configuration files
----------
The Graasp-specific configuration files are extracted from the public repo for security reasons. They are hosted on the internal React server as a git repo: ssh://admin@reacttest.epfl.ch/opt/git/config.git. The following command gets all config files and put the into correct places in the code.

    $ make prepare

Install OAuth keys
================

Create folder
-------

    /Graaasp/current/lib/java_shindig
    
Copy there the folder from the trunk
-------

    ssl_keys
    
If you have your ssl_keys in another location, specify this location in the following line (shindig.properties file)

    shindig.signing.key-file=/Graaasp/current/lib/java_shindig/ssl_keys/oauthkey.pem

Compile
===================

    $ make

Run server at localhost
===================

    $ make start

Shindig should be accessible at [localhost:8080](http://localhost:8080)

Clear all production and reacttest temporal changes
=================================================
  
    $ make clean

Prepare .war files for Production and Reacttest
=======================

    make react  -> build reacttest.war in the current directory
    make prod  -> build production.war in the current directory
    make all   -> build both reacttest.war and production
    
!!! Compiled .war file should be renamed into ROOT.war on the Tomcat server.

Deployment
=======================
The master branch is automatically deployed to [REACT test server](http://reacttest.epfl.ch) when new changes
are pushed. To delploy reacttest.war to REACT test server manually, use:

    $ make deploy_react
    $ make deploy_prod

Restart server
=======================
from local machine

    $ make restart_react
    $ make restart_prod

from production machine

    $ ssh admin@graasp.epfl.ch
    $ /Library/Tomcat/bin/shutdown.sh
    $ /Library/Tomcat/bin/startup.sh

License - ASF
=============
```
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
```
    
