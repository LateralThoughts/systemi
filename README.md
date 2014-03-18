systemi - LT's invoice & cra webapp
===================================

TESTS
-----

##AngularJS tests
###Testing tools installation
The angular tests suite is composed of 2 parts:
- The unit tests
- The end to end (e2e) tests

####Unit tests
The unit tests are build using jasmine and run by karma
To install karma, you need to have nodejs installed with npm.
Run:
  npm install karma-jasmine karma-chrome-launcher karma-phantomjs-launcher
  npm install karma karma-cli
  
To launch the unit tests, cd into tests/angular-tests and launch the server:
  karma start
  
Then run the tests in antoher shell:
  karma run
