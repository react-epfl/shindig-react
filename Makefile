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

info:
	# Server urls
	# ===========
	# Reacttest:
	#     reacttest.epfl.ch
	#     shindigdev.epfl.ch
	# Production:
	#     graasp.epfl.ch
	#     shindig.epfl.ch
	#
	# Commands
	# ========
	# $ make prepare         # clean everything and loads local/prod/react settings into project
	# $ make                 # compile locally
	# $ make compile         # call "mvn clean" and compile locally
	# $ make start           # start the local server
	# $ make react            # compile .war for reactest
	# $ make prod            # compile .war for production
	# $ make deploy_react     # deploy .war to reacttest
	# $ make deploy_prod     # deploy .war to production
	# $ make restart_react    # restart shindig at reacttest
	# $ make restart_prod    # restart shindig at production

start:
	@echo "Starting the server"
	@cd java/server && mvn jetty:run

prepare: clean settings

clean:
	@echo "Cleaning temporary changes"
	@echo "socialjpa.properties"
	@if [[ -a $(SOCIALJPA)_development ]]; then cp $(SOCIALJPA)_development $(SOCIALJPA); fi
	@rm -rf $(SOCIALJPA)_* > /dev/null 2>&1
	@echo "shindig.properties"
	@if [[ -a $(SHINDIG)_development ]]; then cp $(SHINDIG)_development $(SHINDIG); fi
	@rm -rf $(SHINDIG)_* > /dev/null 2>&1
	@echo "web.xml"
	@if [[ -a $(WEBXML)_development ]]; then cp $(WEBXML)_development $(WEBXML); fi
	@rm -rf $(WEBXML)_* > /dev/null 2>&1
	@if [[ -a reacttest.war ]]; then rm reacttest.war; fi
	@if [[ -a production.war ]]; then rm production.war; fi
	@mvn clean

settings:
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

all: prod react

prod:
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

react:
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

jenkins_pre_mvn:
	# Forces shindig to use reacttest settings during compilation
	@echo "socialjpa.properties"
	@cp $(SOCIALJPA)_reacttest $(SOCIALJPA)
	@echo "shindig.properties"
	@cp $(SHINDIG)_reacttest $(SHINDIG)
	@echo "web.xml"
	@cp $(WEBXML)_reacttest $(WEBXML)
	@mvn clean

jenkins_post_mvn:
	# Renames build .war file
	@ cp $(WAR) reacttest.war

restart_react:
	echo "Restarting the reacttest server"
	ssh admin@reacttest.epfl.ch '$(TOMCAT)/bin/shutdown.sh > /dev/null 2>&1 || true; nohup $(TOMCAT)/bin/startup.sh;'


deploy_react:
	echo "Starting deployment of reacttest.war to Test server (reacttest.epfl.ch|shindigdev.epfl.ch)"
	echo "Copying file to the remote server"
	scp -C reacttest.war admin@reacttest.epfl.ch:/Library/Tomcat/webapps/ROOT.war.new
	echo "Backing up the previous version and setting the new one"
	ssh admin@reacttest.epfl.ch 'cd $(TOMCAT)/webapps; if [[ -a ROOT.war ]]; then mv ROOT.war ROOT.war.bak; fi; mv ROOT.war.new ROOT.war'
	echo "Restarting the reacttest server"
	ssh admin@reacttest.epfl.ch '$(TOMCAT)/bin/shutdown.sh > /dev/null 2>&1 || true; rm -rf $(TOMCAT)/webapps/ROOT; nohup $(TOMCAT)/bin/startup.sh;'

restart_prod:
	echo "Restarting the production server"
	ssh admin@graasp.epfl.ch '$(TOMCAT)/bin/shutdown.sh > /dev/null 2>&1 || true; nohup $(TOMCAT)/bin/startup.sh;'

deploy_prod:
	echo "Starting deployment of production.war to main server (graasp.epfl.ch|shindig.epfl.ch)"
	echo "Copying file to the remote server"
	scp -C production.war admin@graasp.epfl.ch:/Library/Tomcat/webapps/ROOT.war.new
	echo "Backing up the previous version and setting the new one"
	ssh admin@graasp.epfl.ch 'cd $(TOMCAT)/webapps; if [[ -a ROOT.war ]]; then mv ROOT.war ROOT.war.bak; fi; mv ROOT.war.new ROOT.war'
	echo "Restarting the production server"
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

