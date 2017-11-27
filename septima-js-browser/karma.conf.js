module.exports = function (config) {
    config.set({
        basePath: '.',
        // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
        frameworks: ['browserify', 'jasmine'],
        /*
        client: {
            jasmine: {
                random: true
            }
        },
        */
        // list of files / patterns to load in the browser
        files: [
            'src/**/*.js',
            'test/**/*.js',
            { pattern: 'assets/**/*.*', included: false }
        ],
        // list of files to exclude
        exclude: [
        ],
        // preprocess matching files before serving them to the browser
        // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
        preprocessors: {
            'src/**/*.js': ['browserify'],
            'test/**/*.js': ['browserify']
        },
        browserify: {
            debug: true,
            bundleDelay: 1000,
            transform: [
                [
                    'babelify', {
                        ignore: /node_modules/
                    }
                ]
                ,['browserify-babel-istanbul']
            ],
            extensions: ['.js']
        },
        // test results reporter to use
        // possible values: 'dots', 'progress'
        // available reporters: https://npmjs.org/browse/keyword/karma-reporter
        reporters: ['progress', 'kjhtml', 'coverage'],
        coverageReporter: {
            reporters: [
                {
                    type: 'text'
                },
                {
                    type: 'html',
                    dir: 'build',
                    subdir: 'coverage'
                }
            ]
        }, // web server port
        port: 9876,
        // enable / disable colors in the output (reporters and logs)
        colors: true,
        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,
        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,
        // start these browsers
        // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
        browsers: ['Chrome'],
        // Concurrency level
        // how many browser should be started simultaneous
        concurrency: Infinity,
        // Continuous Integration mode
        // if true, Karma captures browsers, runs the tests and exits
        singleRun: true
    });
};