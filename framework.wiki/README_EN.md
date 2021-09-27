#### Version Stack
==================

* Master:  `2.0.01-RELEASE`
* Develop: `2.0.02-SNAPSHOT`

#### New Features and Changes
========================
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
* Optimization: Since ChromeDriver V90 it is not possible to display the screen in full screen mode now the resolution is determined and set

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
* Feature: Selenide Integration - view the [Selenide How To](https://tfs-prod.service.raiffeisen.ch:8081/tfs/RCH/AP.Testtools/_git/RCH_Framework_Java?path=%2Frch.framework.wiki%2FHome.md&version=GBmaster ) and the [Selenide vs Selenium](https://tfs-prod.service.raiffeisen.ch:8081/tfs/RCH/AP.Testtools/_git/RCH_Framework_Java?path=%2Frch.framework.wiki%2FSelenide-vs-Selenium.md&version=GBmaster ) Documentation. 
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
* Optimization: tfsRunnerConfig – failureRetest property run all other AzureDevOps TestCases that have not the state passed

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
-   start power shell as admin
-   execute command: taskkill /f /fi "pid gt 0" /im chromedriver_810.exe

FAQ
===

1.  What about the compile problem with maven?

-   Check settings in pom.xml file. Note that maven plug-in need the java
    executable path to run the test. (In case, system environment does not have
    the parameter for JDK bin)

>   \<plugin\>  
>   \<groupId\>org.apache.maven.plugins\</groupId\>  
>   \<artifactId\>maven-compiler-plugin\</artifactId\>  
>   \<version\>\${maven.compiler.version}\</version\>  
>   \<configuration\>  
>   \<fork\>true\</fork\>  
>   \<executable\>**D:/Java/x64/jdk1.8.0_111/bin/javac.exe**\</executable\>  
>   \</configuration\>  
>   \</plugin\>

