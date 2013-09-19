SOCIALCONF = java/graaasp/src/main/resources/
SOCIALJPA = java/graaasp/src/main/resources/socialjpa.properties
SHINDIGCONF = java/common/conf/
SHINDIG = java/common/conf/shindig.properties
WEBCONF = java/server-resources/src/main/webapp/WEB-INF/
WEBXML = java/server-resources/src/main/webapp/WEB-INF/web.xml
WAR = java/server/target/shindig-server-2.5.0.war
TOMCAT = /Library/Tomcat

default:
	@echo "Compiling shindig"
	@mvn -Dmaven.test.skip

compile:
	@echo "Cleaning and compiling shindig"
	@mvn clean && mvn -Dmaven.test.skip

start:
	@echo "Starting the server"
	@cd java/server && mvn jetty:run

clean:
	@echo "Cleaning temporal changes"
	@echo "socialjpa.properties"
	@cp $(SOCIALJPA)_development $(SOCIALJPA)
	@echo "shindig.properties"
	@cp $(SHINDIG)_development $(SHINDIG)
	@echo "web.xml"
	@cp $(WEBXML)_development $(WEBXML)
	@if [[ -a reacttest.war ]]; then rm reacttest.war; fi
	@if [[ -a production.war ]]; then rm production.war; fi
	@mvn clean

prepare:
	@echo "Getting config files and putting them into correct locations"
	@rm -rf prod_config
	@git clone ssh://admin@reacttest.epfl.ch/opt/git/config.git prod_config
	@cp prod_config/shindig/web.xml_* $(WEBCONF)
	@cp prod_config/shindig/shindig.properties_* $(SHINDIGCONF)
	@cp prod_config/shindig/socialjpa.properties_* $(SOCIALCONF)
	@cp $(SOCIALJPA)_development $(SOCIALJPA)
	@cp $(SHINDIG)_development $(SHINDIG)
	@cp $(WEBXML)_development $(WEBXML)
	@rm -rf prod_config

all: production reacttest

production:
	@echo "Creating production.war file"
	@echo "socialjpa.properties"
	@cp $(SOCIALJPA)_production $(SOCIALJPA)
	@echo "shindig.properties"
	@cp $(SHINDIG)_production $(SHINDIG)
	@echo "web.xml"
	@cp $(WEBXML)_production $(WEBXML)
	@mvn clean
	@mvn -Dmaven.test.skip && cp $(WAR) production.war \
		&& echo "Move production.war to ROOT.war for Tomcat"

reacttest:
	@echo "Creating reacttest.war file"
	@echo "socialjpa.properties"
	@cp $(SOCIALJPA)_reacttest $(SOCIALJPA)
	@echo "shindig.properties"
	@cp $(SHINDIG)_reacttest $(SHINDIG)
	@echo "web.xml"
	@cp $(WEBXML)_reacttest $(WEBXML)
	@mvn clean
	@mvn -Dmaven.test.skip && cp $(WAR) reacttest.war \
		&& echo "Move reacttest.war to ROOT.war for Tomcat"

deploy_test:
	echo "Starting deployment of reacttest.war to Test server (reacttest.epfl.ch)"
	echo "Copying file to the remote server"
	scp -C reacttest.war admin@reacttest.epfl.ch:/Library/Tomcat/webapps/ROOT.war.new
	echo "Backing up the previous version and setting the new one"
	ssh admin@reacttest.epfl.ch 'cd $(TOMCAT)/webapps; if [[ -a ROOT.war ]]; then mv ROOT.war ROOT.war.bak; fi; mv ROOT.war.new ROOT.war'
	echo "Restarting the test server"
	ssh admin@reacttest.epfl.ch '$(TOMCAT)/bin/shutdown.sh > /dev/null 2>&1 || true; rm -rf $(TOMCAT)/webapps/ROOT; nohup $(TOMCAT)/bin/startup.sh;'
#	ssh admin@reacttest.epfl.ch 'sudo nohup $(TOMCAT)/bin/restart.sh'

deploy_prod:
	echo "Starting deployment of production.war to main server (graasp.epfl.ch)"
	echo "Copying file to the remote server"
	scp -C production.war admin@graasp.epfl.ch:/Library/Tomcat/webapps/ROOT.war.new
	echo "Backing up the previous version and setting the new one"
	ssh admin@graasp.epfl.ch 'cd $(TOMCAT)/webapps; if [[ -a ROOT.war ]]; then mv ROOT.war ROOT.war.bak; fi; mv ROOT.war.new ROOT.war'
	echo "Restarting the test server"
	ssh admin@graasp.epfl.ch '$(TOMCAT)/bin/shutdown.sh > /dev/null 2>&1 || true; rm -rf $(TOMCAT)/webapps/ROOT; nohup $(TOMCAT)/bin/startup.sh;'

release:
	# merge changes from master to release
	@git checkout master
	@git pull origin master
	@git checkout release
	@git pull origin release
	@git merge master
	@git push origin release
	# create a new tag
	@git tag -a `date "+%Y-%m-%d,%Hh%M"` -m "tag created on `date`"
	# push tag to the repo
	@git push origin `date "+%Y-%m-%d,%Hh%M"`
	# go back to master
	@git checkout master

