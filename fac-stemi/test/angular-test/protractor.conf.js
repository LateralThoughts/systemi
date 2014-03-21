// An example configuration file.
exports.config = {
  // The address of a running selenium server.
  seleniumAddress: 'http://localhost:4444/wd/hub',

  // Capabilities to be passed to the webdriver instance.
  capabilities: {
    'browserName': 'firefox'
    //'browserName': 'phantomjs'
  },

  // Spec patterns are relative to the location of the spec file. They may
  // include glob patterns.
  specs: ['e2e/invoice.js'],

  onPrepare: function() {
    // The require statement must be down here, since jasmine-reporters
    // needs jasmine to be in the global and protractor does not guarantee
    // this until inside the onPrepare function.
    require('jasmine-reporters');
    jasmine.getEnv().addReporter(
       new jasmine.JUnitXmlReporter('./', true, true));
  },
    
  // Options to be passed to Jasmine-node.
  jasmineNodeOpts: {
    //isVerbose: true,
    showColors: true,
    //includeStackTrace: true,
    defaultTimeoutInterval: 30000
  }
};
