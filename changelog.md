#### Version Stack
========================

* Release: `4.3.05-RELEASE`
* Development: `4.3.12-SNAPSHOT`

#### New Features and Changes
========================
> :rocket: **Version 4.3.12-SNAPSHOT**
>
> [OPTIMIZATION]
> * optimize writeToString with better error handling

> :rocket: **Version 4.3.11-SNAPSHOT**
>
> [OPTIMIZATION]
> * add try catch to afterTest() to catch unexpected exception and change to warning

> :rocket: **Version 4.3.10-SNAPSHOT**
>
> [OPTIMIZATION]
> * add try catch to afterTest() to catch unexpected exception

> :rocket: **Version 4.3.09-SNAPSHOT**
>
> [OPTIMIZATION]
> * trim name of Test Case to avoid log file path problem with white space

> :rocket: **Version 4.3.08-SNAPSHOT**
>
> [OPTIMIZATION]
> * allow rest dirver base without host
> * update dependencies version

> :rocket: **Version 4.3.07-SNAPSHOT**
>
> [OPTIMIZATION]
> * add resetMediaType() in RestDriverBase

> :rocket: **Version 4.3.06-SNAPSHOT**
>
> [BUG FIX]
> * Fix Links Feature with TestCaseIdMap

> :rocket: **Version 4.3.05-SNAPSHOT**
>
> [BUG FIX]
> * Fix JiraRestClient Authorization with pat

> :rocket: **Version 4.3.04-SNAPSHOT**
>
> [BUG FIX]
> * Fix Enum Check in ConfigService for load driver config with ConfigType
> * Fix Exception Message
> * Fix Mobile Driver Config and Provider with emulator

> :rocket: **Version 4.3.03-SNAPSHOT**
>
> [FEATURE]
> * Add scrollIntoView method in MobilePageObject
> * Add Links as Label to Allure Report in Test Case Detail via testCaseId and source
> * Use Source with value "AzureDevOps" or "TFS" or "JIRA" for Links
> * Add Thread Name Label for to Allure Report in Test Case Detail
>
> [OPTIMIZATION]
> * Optimize scroll and swipe methods in MobilePageObject
> * Optimize find element methods in MobilePageObject with selenide style
> * Optimize conflict check in PropertyResolver
> * Optimize WebUtils with Selenide Style
> * Optimize TestCase Init
>
> [BUG FIX]
> * Fix ConfigType check by loading DriverConfigs
> * Fix Exception overload

> :rocket: **Version 4.3.02-SNAPSHOT**
>
> [FEATURE]
> * Add SimpleHttpClient with java.net.HttpClient
> * Add Info output for Parameters of Method
> * Enable multiple complex parameter types
>
> [OPTIMIZATION]
> * Optimization: Simplify RestDriverBase initialization
> * Optimization: imports
> * Add More Rest Connection Tests
> * Update several Dependencies
>
> > [BUG FIX]
> * Fix Parameter Validation by TestDataContainer
> * Fix Download Driver from TFS

> :rocket: **Version 4.3.01-SNAPSHOT**
>
> [OPTIMIZATION]
> * Remove AllureServiceConnector and using RestDriverBase instead
> * Remove unused method in ChromeDriverProvider
> * Add to test: TestRestDriverBase
> * Optimize RestDriverBase and consolidate with AllureServiceConnector
> * Remove AndroidPageObject
> * Remove IOSPageObject
> * Optimize MobilePageObject with selenide feature
> * Add activeApp and startActivity Methods into MobilePageObject
>
> [BUG FIX]
> * Add condition for download driver from TFS

> :rocket: **Version 4.2.09-SNAPSHOT**
>
> [OPTIMIZATION]
> * Rename ApollonErrorKeysEnum to ApollonErrorKeys
> * Merge messages_en and ApollonErrorKeys from feature branch
> * Optimize messages_en and ApollonErrorKeys

> :rocket: **Version 4.2.09-SNAPSHOT**
>
> [OPTIMIZATION]
> * Optimize RestfulDriver and rename to RestDriverBase
> * Remove JIRAConnector, using RestDriverBase instead
> * Optimize RestClientBase
> * Optimize TFSConnector via removing duplicate code
> * Remove all Deprecated Methods in WebOperationUtils and rename to WebUtils
> * Rename TimeUtils to DateTimeUtils
> * Remove useless Interface DBDataCollector
> * Remove unused Method in TestRunManger for Store Result to DB
> * Remove MobileTestObject, using MobilePageObject instead
> * Optimize Interface JSONContainer
> * Optimize TestDataContainer and remove Method for load XML Content incl. removing FileFormat Enum XML
> * Add more Methods into DBConnector for operating DB Result set
> * Optimize Imports
> * Remove Commented Codes
> * Change screenshot OutputType to .BASE64
> * Optimize Screenshot and ScreenCapture
>
> [BUG FIX]
> * Fix @UndoOnError on Error Case
> * Fix Video Recording for Mobile App Driver
> * Fix IllegalArgumentException by taking screenshot

> :rocket: **Version 4.2.08-SNAPSHOT**
>
> [FEATURE]
> * Add selenide-appium as dependency instead of appium and selenide single
> * Enable feature for selenide style coding for mobile
>
> [OPTIMIZATION]
> * Optimize MobilePageObject
> * Optimize Mobile Driver Video Recording with options, but still not recommend to use (Error: moov atom not found)
>
> [BUG FIX]
> * Fix NullPointException by not initialized TestStatus

> :rocket: **Version 4.2.07-SNAPSHOT**
>
> [FEATURE]
> * Enable Appium Screen Recording
>
> [OPTIMIZATION]
> * Dependencies update to latest
> * Enable more file extensions
> * Optimize attachment adding with detecting file extension and using proper MIME type
> * Remove unused dependencies
>
> [BUG FIX]
> * Fix upload extra file for test case problem
> * Change trace info color to green to fix exception

> :rocket: **Version 4.2.04-SNAPSHOT**
>
> [OPTIMIZATION]
> * Remove global cache of Page Object
> * Remove global cache of ReportBuilder
> * Change ReportBuilder to non-static
> * Resolve warnings in TestCaseStep
> * Remove optional properties for demo mode and video settings
> * Remove HightLightElement.java
> * Improve simple video generation
> * Improve demo mode

> [BUG FIX]
> * Fix demo mode failure by styled element
> * Fix download path problem in different OS

> :rocket: **Version 4.2.02-SNAPSHOT**
>
> [FEATURE]
> * Enable headless download with chrome driver
> * Add method for find mobile element with accessibilityId
> * Enable killall web driver process on non Windows OS
> * Add new Property to enable open pdf in default system reader
>
> [OPTIMIZATION]
> * Health check after each setProperty
> * Change testEnvironment() to setTestEnvironment()
> * Add comment for ApollonConfiguration
> * Optimize download folder setting
> * Replace field call with methode call getApollonConfiguration()
> * Password for REST connection will be decoded with Base64
> * Failure in Check version of Driver will stop the run
> * Failure in HealCheck of Properties will stop the run
> * Change ReportBuilderAllureService to non-static
> * Re-factor chrome driver options and prefs
>
> [BUG FIX]
> * Fix exception for no global test data
> * Revive feature 'keep browser on Error'
> * Fix suite name issue by parallel execution with driverConfig service
> * Fix exception by response close in RestfulDriver
> * Fix download setting

> :rocket: **Version 4.0.10-SNAPSHOT**
>
> [FEATURE]
> * Enable Allure Report for Remote Parallel Execution
> * Add DriverConfigService for remote web driver and mobile app driver
> * Add ConfigService
> * Add ConfigType
>
> [OPTIMIZATION]
> * More trace info for initialize mobile app driver
> * Mobile App Driver initialization with only one config instead a list
> * Remove all local fields in MobileAppDriverProvider to keep threads safety
>
> [BUG FIX]
> * Fix driver close issue on Windows with single web driver
> * Fix report environment properties problem
>

> :rocket: **Version 4.0.03-SNAPSHOT**
>
> [FEATURE]
> * add new property allure.report.cleanup (default = true) to set the allure service clean up API functionality
> * Enable Driver bin file input with absolute path outside of resource folder
> * Enable Remote Parallel Execution: Thread safe for driver and properties
>
> [OPTIMIZATION]
> * Clean up driver using "taskkill" only on Windows
> * Optimize Load test run properties
>
> [BUG FIX]
> * Fix command execution on Linux
> * Fix problem with load global test data
>

> :rocket: **Version 4.0.02-SNAPSHOT**
>
> [FEATURE]
> * Update to OpenJDK 17
> * Add Compile Args in pom to resolve warnings and open encapsulated modules
> * Add ApollonConfiguration Class for Setup Framework programmatically
> * Add Settings for Remote Parallel Execution
> * Add Health Check in PropertyResolver to check conflict settings
> * Add Health Check in MobileDriverProvider to check needed caps
>
> [OPTIMIZATION]
> * mobile driver initialization with correct bundleId or appPackage and activity
> * Remove duplicate methods in PropertyResolver
> * Remove TestType "mobile_web_app" and "web_app_remote"
> * Rename Methods for better Understanding
> * System Property overlay local Property to prioritize Property Setting from JVM
> * Remove unused Property Keys
> * Rename property keys for generify and regroup
> * Password and Pat will be not displayed in Console
> * Environment Display for password and pat
>
> [IMPORTANT]
> * Add / Modify Compile Args into Run Configuration in each Project:
    > ```--add-opens java.base/sun.reflect.annotation=ALL-UNNAMED```
> * reimport all certification to truststore in java 17
>

> :rocket: **Version 4.0.01-SNAPSHOT**
>
> [FEATURE]
> * Add "debug.keep.browser=true" for keeping browser by run stopped on error
> * Add "report.suite_rename.by_parallel_execution=false" for parallel execution on server to avoid reporting overriding
    with suite name
> * Add "testcase.file.extension=.tas" for extension setting of test case file
> * Add "run.meta.filter=+CI, +Demo" for meta setting of test run
> * Change package name to ch.qa.testautomation.framework
> * Add useful functions in MobilePageObject more
> * Add DynamicMobileAppDriverProvider Class and TestTyp "mobile_app" to enable Hyper Test Automation for iOS/Android
    App together
> * Add allure.properties file to redirect junit 5 auto. generated results
> * Add @UndoOnError("MethodName") as option for after step on error case to reset preview actions
> * Add DBConnector.execute(sql) to simplify SQL execution with settings in dbConfig.json
> * Add useful methods in SingleTestObject
> * add attribute testCaseIdMap for mobile_app test case, which contains platformName as key and testCaseId as value
> * Change Suite Name for Allure report with device name and platform
>
> [OPTIMIZATION]
> * Upgrade from JUnit 4 to Junit 5 - using @TestFactory
> * Generify in PerformableTestCases Class to simplify extension for testRunner
> * Expose all Properties for runtime overriding
> * Simplify Property file for test run settings
> * Optimize Property Keys
> * Change all runtime exception to ApollonException with better message
> * Catch most IOException and throw ApollonException with more info
> * Optimize JSONContainer and ContainerFactory
> * Optimize FileLocator functions
> * Add more trace info for process
> * Remove unused and deprecated methods and implementations
> * Rename method for better sight
> * Preset webDriver for selenide call
> * Change logStepInfo to static
> * Refactoring in TestTyp for 'web_app_remote' and 'mobile_web_app'
> * Change Methods in ReportBuilderAllureService to static
> * Optimierung NonHeadless and resolve side effect with setting on reset driver after run
> * Update Capabilities for Mobile App Test according to W3C Standard
> * Remove Multi-Threading for local Run (pipeline execution instead)
> * Change alle password ins Base64 encoded
> * Property setting in maven for encoding
> * better test case id handling
> * optimize JSONRunnerConfig fit for jiraExecutionConfig.json
> * change typ from String to TestTyp Enum in TestCaseObject
> * add attribute projectKey in jiraExecutionConfig.json for better usage
> * wait for sec after kill driver to avoid fail restart of driver in stream flow
> * add health check for Property settings
>
> [BUG FIX]
> * Test Case with CSV file initialization with Sequence
> * Fix TFS outcome text change from success to passed
> * Fix Parameter with primitive data typ for methods
> * System termination on step not found
> * Return original string by base64.decode for not decoded string
> * Fix Retry Feature caused by file path problem
> * Fix error while create test execution in JIRA
> * Fix NullPoint by TFSTestPlanConfig not exists


Error Handling
===
Kill remain driver process: (please change the version number properly)

- start powerShell as admin
- execute command: taskkill /f /fi "pid gt 0" /im chromedriver_810.exe
