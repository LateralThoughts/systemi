#!/bin/bash
# start backend
cd $WORKSPACE/fac-stemi

export NODE_PATH=$WORKSPACE/../../tools/node_modules/jasmine-reporters/
pkill play
rm ./target/universal/stage/RUNNING_PID
./target/universal/stage/bin/fac-stemi -Dhttp.port=9999 &

cd $WORKSPACE/fac-stemi/test/angular-test

# wait until backend is up
n=0
   until [ $n -ge 10 ]
   do
       curl -s -L http://localhost:9999 > /dev/null 2>&1
       [ $? -eq 0 ] && break
       n=$[$n+1]
       sleep 1
   done

# start selenium
$WORKSPACE/../../tools/node_modules/protractor/bin/webdriver-manager start > /dev/null 2>&1 &

# wait until selenium is up
n=0
   until [ $n -ge 10 ]
   do
       curl http://localhost:4444/wd/hub/status &>/dev/null
       [ $? -eq 0 ] && break
       n=$[$n+1]
       sleep 1
   done

# run the build
$WORKSPACE/../../tools/node_modules/protractor/bin/protractor protractor.conf

# stop selenium
curl -s -L http://localhost:4444/selenium-server/driver?cmd=shutDownSeleniumServer > /dev/null 2>&1

kill -9 `cat $WORKSPACE/fac-stemi/target/universal/stage/RUNNING_PID`
