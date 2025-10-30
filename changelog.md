#### Version Stack
========================

* Latest development version: `6.2.00-RELEASE` (branch `main`)

#### New Features and Changes
========================
> :rocket: **Version 6.2.00-RELEASE**
>
> [FEATURE]
> * Add NodeLocator to identify installation of node.js
>
> [BUG FIX]
> * Fix: Playwright open 2 tabs on start
> * Fix: Change Annotation Value in runtime for @SearchCriteria

> :rocket: **Version 6.1.02-RELEASE**
> 
> [OPTIMIZATION]
> * update selenide to 7.10.0 (resolve security issue)


> :rocket: **Version 6.1.01-RELEASE**
> 
> [BUG FIX]
> * Fix immutable list issue in for loading test data

Error Handling
===
Kill remain driver process: (please change the version number properly)

- start powerShell as admin
- execute command: taskkill /f /fi "pid gt 0" /im chromedriver*.exe

FAQ
===

1. What about the compile problem with maven?

- Check settings in pom.xml file. Note that maven plug-in need the java executable path to run the test. (In case,
  system environment does not have the parameter for JDK bin)
