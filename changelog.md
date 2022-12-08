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

> :rocket: **Version 3.9.01-SNAPSHOT**
>
> [OPTIMIZATION]
> * Create ApollonErrorKeysEnum for ApollonBaseException
    > [DEPENDENCY UPDATES]
> * added dependency exclusions
    > [BUG FIX]
> * Failure Retest fix according to TFS Run Status Text
>
&nbsp;

> :rocket: **Version 3.7.05-SNAPSHOT**
>
> [DEPENDENCY UPDATES]
> * Selenide updated to 6.5.1
> * Selenium updated to 4.2.0
> * Appium updated to 8.1.0
> * added dependency exclusions to Selenide and Appium
>
> [BUG FIX]
> * bug fix: resolve dependency mismatch with different selenium driver versions
> * bug fix: added bytebuddy class to resolve the "demo.mode=true" issue
>
&nbsp;

> :rocket: **Version 3.7.04-SNAPSHOT**
>
> [FEATURE]
> * Added new Plugin Version for the newest IntelliJ Version 2022.***
>
> [OPTIMIZATION]
> * password for DB Connection: encoded or not (warning message for not)
>
> [BUG FIX]
> * bug fix: db connection request with base64 encoding/decoding
> * bug fix: selenium-remote-driver vulnarability issue with version 4.1.2, upgrade to 4.1.4
>
&nbsp;

> :rocket: **Version 3.7.03-SNAPSHOT**
>
> [FEATURE]
> * Added new tab "Apollon" to Allure report with Framework parameters
>
&nbsp;

> :rocket: **Version 3.7.02-SNAPSHOT**
>
> [OPTIMIZATION]
>* Create a WaitUtils Class, to have a tool to wait for Selenide Element
>* Deprecated all wait methods in WebOperationUtils Class
>* Add a method to parse strings with a single quote for xpath
>
> [BUG FIX]
> * bug fix: load db test data to repeat test cases
>
&nbsp;

> :rocket: **Version 3.7.01-SNAPSHOT**
>
> [BUG FIX]
> * 840845 Fixing an issue where it was not possible to pass an int and a string parameter
> * Hotfix an issue for driver look up service: search first in default project as than current
>
> [OPTIMIZATION]
> * enable more parameter type in TestCaseStep for double, long and boolean
> * not matched driver will be removed runtime
> * optimize property key for driver resource location for look up service:
    >

* Deprecated: remote.web.driver.folder

> * new key: resource.driver.location=Git - RCH Framework Solution Items/Java/DriverVersions/
    >

* new key: resource.project=ap.testtools

> * resolve headless download with new annotation "@NonHeadless"
> * restart driver after single test case execution as default
    >

* new key: default.execution.driver.restart=true

>
&nbsp;

> :rocket: **Version 3.6.04-SNAPSHOT**
>
> [FEATURE]
> * Enable Look up driver in Azure DevOps VC item folder
>
> [OPTIMIZATION]
> * TestRunManager Comments
> * download web driver single file
>
> [DEPENDENCY UPDATES]
> * clear unused Dependencies
>
&nbsp;

> :rocket: **Version 3.6.03-SNAPSHOT**
>
> [FEATURE]
> * Microsoft Edge Driver
> * Jira Rest Client
>
> [OPTIMIZATION]
> * elimination deprecated reference
> * refactoring web driver event handling
> * propertyKey for enabling jira connect and sync
> * add propertyKey enum for override runtime parameter or external job
> * clean up and simplify keys in DefaultTestRunProperties.properties
> * removing path folder for config or driver files in DefaultTestRunProperties.properties
> * rename property keys for jira settings
> * PropertyResolver for getting config or driver files
> * clean up and optimize imports, empty line comment and name inspection
> * clean up driver files
> * remove junit allure report generation in ReportBuilder
> * add comments
> * elimination deprecated reference
>
> [DEPENDENCY UPDATES]
> * Selenide updated to 6.3.4
> * Selenium updated to 4.1.2
> * Allure updated to 2.17.3
> * Appium updated to 8.0.0
> * Framework Support Plugin updated to 1.2.8
> * Jackson updated to 2.13.2
> * Spring updated to 5.3.16
>
> [BUG FIX]
> * 809576 check for duplicate testcase name or id
> * 813702 It is now possible to use int parameters in testdata
> * 823992 using of slash in the tes object name get a path error
>
&nbsp;

> :rocket: **Version 3.6.01-SNAPSHOT**
>
> [FEATURE]
> * implement edge driver provider
>
> [OPTIMIZATION]
>* change WebDriverEventCapture to implement WebDriverListener
>* resolve deprecated class in WebPageObject
>* optimized WebDriverProvider
>* correct DBConnector
>* clean up PropertyKey and fix PropertyResolver
>* clean up properties
>* change property driver.chrome.headless to driver.browser.headless
>* optimized web driver download function in ExternalAppController
>* optimized trace info of error handling
>
&nbsp;

> :rocket: **Version: 3.5.01-SNAPSHOT**
>
> [FEATURE]
>* consideration of JSON compilation errors
>
> [OPTIMIZATION]
>* correction of remote driver and mobile driver because of appium upgrade
>* eliminated / excluded most conflicts between Maven dependencies
>* Framework specific exception and message handler added
>
> [DEPENDENCY UPDATES]
>* Microsoft SQL Server upgrade to 9.4.1.jre11
>* Log4J updated to 2.17.1
>* Selenide upgrade to 6.1.2
>* Selenium upgrade to 4.1.1
>* Allure updated to 2.17.2
>* Appium upgrade to 8.0.0-beta2
>* Framework Support Plugin updated to 1.2.5
>* Added com.atlassian.jira Maven dependency (jira rest java client)
>* Deleted net.rcarz Maven dependency
>
> [BUG FIX]
>* 815693 It is not possible to use a value in the testData File. It must be a key
>
&nbsp;

> :rocket: **Version: 3.0.02-SNAPSHOT**
>
> [FEATURE]
>* 756955 Remove all unused JSON libraries. Now the Jackson library is in use
>* 783468 Create Test Structure with Junit Test and Scenario Test
>
> [BUG FIX]
>* Different Bug Fixes which were discovered when the JSON libraries was changed

Version: 2.0.02a-SNAPSHOT

* Feature: new execution and report process with sync to TFS and Allure report server

Version: 2.0.02-SNAPSHOT

* Selenide up to 5.23.0
* Bug Fix: Chrome Option Merge fixed
* Feature: 775832 Java Framework: Extra Attachment for Test Case
* Feature: 775834 Java Framework: Single Test Data Filter für Test Case with CSV Data
* Optimization: show sequence Number in Runner
* Feature: 765287 Java Framework - Allure JUnit 5 Engine

Version: 2.0.01-SNAPSHOT

* Feature: API for reset Webdriver before test start
* Optimization: Split tfs feedback setting to connection and syn test case
* Optimization: warning message and error handling
* Bug Fix: Check existence before cleanResult in AllureRestClient
* Excluding: ch.qos.logback from net.sourceforge.tess4j
* Add language level to maven setting
* Optimization Selenide Conditions
* Upgrade dependencies to latest: jackson, jersey
* Remove jsonSimple lib

Version: 1.0.7.03-Snapshot

* Dependency Upgrade:
    1. java up to openJDK 14.0.1
    2. Junit up to 5.7.2 with dependencyManagement
    3. Appium up to 7.5.1
    4. Allure up to 2.14.0
    5. Selenide up to 5.21.0
* Optimization: TestDataContainer

Version: 1.0.7.02-Snapshot

* Optimization: If an error occurs, a screenshot is always taken
* Feature: new local allure report structure to fix history problem
* Optimization: upload only result files of current run
* Bug Fix: executor.json and Trends with wrong run number
* Bug Fix: Fixing an error in which the number of executed test were not displayed correctly in Allure Report Service
* Optimization: Since ChromeDriver V90 it is not possible to display the screen in full screen mode now the resolution
  is determined and set

Version: 1.0.7.01-Snapshot

* Feature: Add environment.properties to Allure Report Web Service
* Improvement: Redirect allure upload content to sub folder and generate report locally
* Optimization: Remove unused allure.properties file
* Optimization: All path properties end with slash
* Plugin: Update und compatible mit neuster IntelliJ Version

Version: 1.0.7.00-Snapshot

* Feature: Add Allure Report Web Service

Version: 1.0.6.06-Snapshot

* Feature: Selenide Timeout setting 'selenide.configuration.timeout' into 'TestRunProperties.properties'

Version: 1.0.6.05-Snapshot

* Feature: Add a property to show only environment properties from File DefaultTestRunProperties.properties
* Bug Fix: Fixing an error where Fullscreen Property not working
* Bug Fix: Using multi-parameter without [] not working
* Feature: enable to set default download folder to Chrome
* Optimization: waiting function in WebOperationUtils
* Optimization: Chrome Headless mode options
* Optimization: imports optimization
* Clean up: deprecated chrome option "useAutomationExtension"
* Bug Fix: fix restore session after chrome driver version update

Version: 1.0.6.04-Snapshot

* Bug Fix: Detect wrong number of values in CSV
* Bug Fix: Fixing an error when use keyword "using" in test in which occurs JSON and CSV
* Bug Fix: Parameter Array with casting exception (use list instead of array as parameter of method)
* Optimization: Package name displayed better in Allure report
* Optimization: Chrome Driver - Enable Screen Initial Size Setting
* Optimization: Write Report of every test case immediately after its single run
* Refactor: WebPageObject.checkFields() marked as deprecated, and it is no longer an abstract method

Version: 1.0.6.03-Snapshot

* Clean up: Examples removed
* Feature: Add Comment for teststep
* Bug Fix: Metafilter now ignore "@" sign
* Bug Fix: by get test case id from TFS with wrong test plan id or suite id
* Optimization: Add error handling for such error

Version: 1.0.6.02-Snapshot

* Clean up: unused codes and duplicate log infos
* Feature: Enable setting for screenshot and stopOnError in json step
* Feature: Step log is attached in allure report
* Delete Feature: Add Page HTML File to Screenshots
* Feature: enable additional test data file in json format
* Optimization: add appName field and its getter setter to mobile page object

Version: 1.0.6.01-Snapshot

* Feature: Selenide Integration - view
  the [Selenide How To](https://tfs-prod.service.raiffeisen.ch:8081/tfs/RCH/AP.Testtools/_git/RCH_Framework_Java?path=%2Frch.framework.wiki%2FHome.md&version=GBmaster )
  and
  the [Selenide vs Selenium](https://tfs-prod.service.raiffeisen.ch:8081/tfs/RCH/AP.Testtools/_git/RCH_Framework_Java?path=%2Frch.framework.wiki%2FSelenide-vs-Selenium.md&version=GBmaster )
  Documentation.
* Feature: enable test data Location setting for diff test environment
* Feature: Add Page HTML File to Screenshots

Version: 1.0.5.11-Snapshot

* Feature: enable overriding tfsRunnerConfig with TFS test plan configuration
* Feature: enable test data Location setting for diff test environment

Version: 1.0.5.10-Snapshot

* Bug Fix: tfsRunnerConfig.json not initialized
* Bug Fix: single test case sync into TFS

Version: 1.0.5.09-Snapshot

* Bug Fix: JSON Driver Container with remote device config
* Feature: OCR Text Line Check and enable settings
* Feature: Enable finding image in bigger image
* Feature: Step Info added into Allure Report
* Feature: Enable TFS Runner Config to set Feedback single Test Case result after its run

Version: 1.0.5.08-Snapshot

* Feature: Auto detection and selection of web driver matched to current browser version
* Feature: Auto download from TFS Source if web driver is missing
* Optimization: delete chromeDriverConfig.json, ieDriverConfig.json
* Optimization: add new property driver.name, remote.web.driver.folder
* Feature: External App Controller with OCR and windows robot
* Feature: Enable global test data in JSON with name: *-testdata-global.json
* Error Handling: Resolve Empty Report with Null Point Exception
* Bug Fix: Trim all property value to void match issue
* Bug Fix: Attach Screenshot when exists

Version: 1.0.5.07a.-Snapshot

* Dependency upgrade to last version:
* Appium 7.3.0
* JUnit 4.13
* Selenium 3.141.59

Version: 1.0.5.07-Snapshot

* Feature: new State of Test Step for KnownIssueException
* Feature: new annotation for setting per step for the stop on error option
* Optimization: Test Case Name Format

Version: 1.0.5.06-SNAPSHOT

* Feature: Test Plan Configuration
* Feature: DB Data retrieve interface 'DBDataCollector'
* Feature: Test Data input with key: CustomizedDataMap for map access
* Bug Fix: Sync Test Case Variant Name with Series number
* Bug fix: Android Page Object initialize without Activity
* Change: enable object id visible setting
* Change: enable Parameter with JSONArray values as List
* Change: enable temp data storage in TestDataContainer in object map

Version: 1.0.5.05-SNAPSHOT

* Feature: Fail info direct under Step Name
* Feature: enable customized step info log
* Error Handling: to avoid class inherit with annotation but not specified in own class
* Change: add explicit wait for an element disappear
* Bug fix: get json data in object with right order
* Bug fix: edited property feature tfsRunnerConfig – fullRun to avoid LinkedList errors
* Optimization: tfsRunnerConfig – failureRetest property run all other AzureDevOps TestCases that have not the state
  passed

Version: 1.0.5.04-SNAPSHOT

* Feature: advanced parallel execution for mixed cases without retry mode
* Update: multi-threading control
* Bug fix: trim properties key to avoid String match issue
* Optimization: provide extra json test data global and wait function for loading element with selector
* Change: Failed Only retest change to Success Except retest

Version: 1.0.5.03-SNAPSHOT

* Feature: first parallel execution for single cases only without retry mode
* Update: Smart clean up remain driver service
* Update: better junit report runner, fixed several bugs within

Version: 1.0.5.02-SNAPSHOT

* Code Optimization
* More better error handling and better logs

Version: 1.0.5.01-SNAPSHOT (update base on 1.0.4.25-SNAPSHOT)

* Feature: Video Recorder of test run also in headless mode (Web Application only)
* Feature: Test Run Video integration in Allure Report
* Bug Fix: TFS Runner Run Option Mode
* Code Optimization

Version: 1.0.4.24-SNAPSHOT

* Bug Fix: CSV Test Data without test case id will stop test run
* Error Handling: Lacy Existence check of config files until it will be used

Version: 1.0.4.23-SNAPSHOT

* Feature: Allure Report - Executors Information and Better Properties List
  (executor.build.url, executor.name, executor.build.name)
* Error Handling: Test Case has no Test Data Ref cause Null Point
* Clean Up: no plaint text with password in properties
* Clean up: unnecessary URISyntaxException

Version: 1.0.4.22-SNAPSHOT

* Update: Selenium Version update to 3.141.59
* Bug Fix: function "isDisplayed()" deprecated
* Bug Fix: Capability Problem of Remote Web Driver and Mobile Driver after update
* Clean Up: Eliminate Dependency Duplicate and conflict

Version: 1.0.4.21-SNAPSHOT

* Update: Allure Version 2.13.1
* Bug Fix: StringIndexOutOfBoundsException of Parameter
* Feature: Retry Option with Session Restore for Web Application with Chrome

Version: 1.0.4.20-SNAPSHOT

* Update: Web Drivers version

Version: 1.0.4.19-SNAPSHOT

* Feature: Single Test Runner with config file in JSON
* Feature: Maven XML Report for TFS Run with "mvn test"
* Change: CSV Test Data extended with test case id for TFS Feedback Mapping
* Change: Rebase Allure Report

Version: 1.0.4.18-SNAPSHOT

* Feature: API for Test Run using Commandline with Release within TFS
* Feature: TFS Feedback with Test Run Result for automated Test Run
* Feature: Resource retrieve with jar file
* Feature: Non-Driver Provider for test case without any driver

Version: 1.0.4.17-SNAPSHOT

* Change: Optimization and further implementation of Rest Driver
* Change: Documentation of SSL Certificate Import
* Feature: TFS Rest Connector and Rest Client
* Feature: Enable CSV Test Data and dynamic rename of test run while executing same test cases
* Feature: Enable Setting to stop test run on error

Error Handling
===
Kill remain driver process: (please change the version number properly)

- start powerShell as admin
- execute command: taskkill /f /fi "pid gt 0" /im chromedriver_810.exe

FAQ
===

1. What about the compile problem with maven?

- Check settings in pom.xml file. Note that maven plug-in need the java executable path to run the test. (In case,
  system environment does not have the parameter for JDK bin)

> \<plugin\>  
> \<groupId\>org.apache.maven.plugins\</groupId\>  
> \<artifactId\>maven-compiler-plugin\</artifactId\>  
> \<version\>\${maven.compiler.version}\</version\>  
> \<configuration\>  
> \<fork\>true\</fork\>  
> \<executable\>**D:/Java/x64/jdk1.8.0_111/bin/javac.exe**\</executable\>  
> \</configuration\>  
> \</plugin\>

