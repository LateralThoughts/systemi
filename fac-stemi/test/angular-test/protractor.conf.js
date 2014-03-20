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

  // Options to be passed to Jasmine-node.
  jasmineNodeOpts: {
    //isVerbose: true,
    showColors: true,
    //includeStackTrace: true,
    defaultTimeoutInterval: 30000
  }
};