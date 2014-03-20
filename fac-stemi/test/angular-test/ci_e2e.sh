# start backend
cd $WORKSPACE/fac-stemi

./target/universal/stage/bin/fac-stemi &

cd $WORKSPACE/fac-stemi/test/angular-test

# wait until backend is up
while ! curl -s -L http://localhost:9000 > /dev/null 2>&1; do :; done

# start selenium
$WORKSPACE/../../tools/node_modules/protractor/bin/webdriver-manager start > /dev/null 2>&1 &

# wait until selenium is up
while ! curl http://localhost:4444/wd/hub/status &>/dev/null; do :; done

# run the build
$WORKSPACE/../../tools/node_modules/protractor/bin/protractor protractor.conf

# stop selenium
curl -s -L http://localhost:4444/selenium-server/driver?cmd=shutDownSeleniumServer > /dev/null 2>&1

kill -HUP `cat $WORKSPACE/fac-stemi/target/universal/stage/RUNNING_PID`