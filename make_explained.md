Apache Shindig version for Graasp
=================================
Use Makefile to build shindig, makefile does the following steps.

Changes if run on production, devtest, geneva or fribourg
=========================================================
Change these files before compile:

socialjpa.properties
--------------------
Uncomment these lines for production and comment them for development
    
    db.url=jdbc:mysql://localhost:3306/graaasp_development
    db.user=graaasp
    db.password=graaasp

shindig.properties
----------------------
Uncomment these lines for production and comment them for development

    shindig.signing.global-callback-url=http://localhost:8080/gadgets/oauthcallback

web.xml: shindig.host
---------------------
Change the port and host for your shindig installation

     <context-param>
    	<param-name>system.properties</param-name>
       <param-value>
         shindig.host=localhost
      	 shindig.port=8080
         shindig.container_url=http://localhost:3000

      	 aKey=/shindig/gadgets/proxy?container=default&amp;url=

       </param-value>
    </context-param>


Production: compiled .war file should be renamed into ROOT.war on the Tomcat server.
    
    java/server/target/shindig-server-2.0.0.war


Changes if run on production, devtest, geneva or fribourg
-----------------------------------------
Change these files before compile:

socialjpa.properties
--------------------
Uncomment these lines for production and comment them for development
    
    db.url=jdbc:mysql://localhost:3306/graaasp_development
    db.user=graaasp
    db.password=graaasp

shindig.properties
----------------------
Uncomment these lines for production and comment them for development

    shindig.signing.global-callback-url=http://localhost:8080/gadgets/oauthcallback

web.xml: shindig.host
---------------------
Change the port and host for your shindig installation

     <context-param>
    	<param-name>system.properties</param-name>
       <param-value>
         shindig.host=localhost
      	 shindig.port=8080
         shindig.container_url=http://localhost:3000

      	 aKey=/shindig/gadgets/proxy?container=default&amp;url=

       </param-value>
    </context-param>

Move compiled .war file to current dir
======================================

    from: java/server/target/shindig-server-2.0.0.war

		to:   ./production.war or ./devtest.war

