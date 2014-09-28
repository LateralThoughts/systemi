TESTS
-----

##AngularJS tests
###Testing tools installation
The angular tests suite is composed of 2 parts:
- The unit tests
- The end to end (e2e) tests

####Unit tests
The unit tests are build using jasmine and run by karma
To install karma, you need to have nodejs installed with npm and ans exec called "node" (not only nodejs).
Then run:

    npm install karma-jasmine karma-chrome-launcher karma-phantomjs-launcher
    npm install karma karma-cli karma-junit-reporter

To launch the unit tests, cd into tests/angular-tests and launch the server:

    karma start

####E2E
Systemi uses new official e2e testing framework for angularJS: Protractor
First, you need to install it:

    npm install protractor
    webdriver-manager update (install selenium standalone server used by protractor)

Protractor uses jasmine to build the tests too
To start the potractor server:

    webdriver-manager start

Then you have to run your local instance of fac-stemi on port 9999:

    play "run 9999"

To run the e2e tests suite:

    protractor (path/to/test/angular-test/protractor.conf.js)

To learn more about protractor, visit https://github.com/angular/protractor/blob/master/docs/getting-started.md