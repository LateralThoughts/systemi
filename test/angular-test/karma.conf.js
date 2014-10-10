// Karma configuration
// http://karma-runner.github.io/0.10/config/configuration-file.html
module.exports = function (config) {
    config.set({
        // base path, that will be used to resolve files and exclude
        basePath: '',

        // testing framework to use (jasmine/mocha/qunit/...)
        frameworks: ['jasmine'],

        // list of files / patterns to load in the browser
        files: [
            '../../public/javascripts/jquery-1.9.0.min.js',
            '../../public/javascripts/angular.min.js',
            '../../public/javascripts/angular-mocks.js',
            '../../public/javascripts/moment-with-langs.min.js',
            '../../public/javascripts/moment-range.js',
            '../../public/javascripts/*.js',
            '../../public/javascripts/angular/*.js',
            '../../public/javascripts/angular/**/*.js',
            '../../public/javascripts/angular/templates/*.html',
            'unit-test/**/*.js'
        ],

        preprocessors: {
            'app/views/**/*.html': 'ng-html2js',
            'test/test_views/*.html': 'ng-html2js'
        },

        ngHtml2JsPreprocessor: {
            // strip this from the file path
            stripPrefix: 'app/',
        },

        // web server port
        port: 8082,

        // level of logging
        // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
        logLevel: config.LOG_INFO,


        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: false,


        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera
        // - Safari (only Mac)
        // - PhantomJS
        // - IE (only Windows)
        browsers: ['PhantomJS'],


        // Continuous Integration mode
        // if true, it capture browsers, run tests and exit
        singleRun : true,

        reporters : ['dots', 'junit'],

        junitReporter : {
          outputFile: 'test-results.xml'
        }
    });
};

