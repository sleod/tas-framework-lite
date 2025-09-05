# TAS Framework Lite

**TAS Framework Lite** is a lightweight, extensible **Java-based Test Automation Framework**.  
It unifies **Web and Rest API testing** under a consistent JSON-based test case and test data format, supports database and external integrations (JIRA/Xray, HP QC), and generates detailed execution reports via **Allure**. It use the junit 5 as runner engine.

---

## ✨ Key Features

### Unified Test Case Format (`.tas`)
- JSON-based files with `.tas` extension (schema supported for IDE auto-completion).
- Attributes include `meta`, `name`, `description`, `type`, `testCaseId`, `requirement`, `reference`, `screenshotLevel`, `testDataRef`, `conditions`, `story`, `epic`, `feature`, `startURL`, `steps`, etc.
- Supported test types: `web_app`, `rest`, `app`
- Steps can define `stopOnError`, `retry`, `comment`, and `takeScreenshot`.

### Driver Resolution & Execution
- Automatically selects and initializes driver based on test type.
- Supports **Selenium (Selenide)** and **Playwright** for web.
- Remote driver configs via JSON in `driverConfig/`.
- Automatic driver download and version check with strategies: `AUTO`, `SharedFile`, `Online`.

### REST Testing
- **SimpleRestDriver**: Direct Jakarta client with simple GET/POST/PUT/DELETE/PATCH methods.
- **RestDriver**: Extended functionality with JSON config and proxy auto-detection.
- Authentication supported (Basic, Bearer, PAT).
- Extensible for specific clients (Jira, Allure Cloud).

### Integrations
- **JIRA/Xray Connector**: Sync issues, executions, results, and evidence. Requires `jiraConfig.json` and `jiraExecutionConfig.json` plus Atlassian client credentials.
- **HP QC Connector**: Create/manage test sets, runs, attach results (requires `qcConfig.json`).

### Reporting
- Integrated with **Allure** report framework.
- Generates `allure-results` during test run, produces HTML reports.
- Reports include overview, suite structure, test case and step parameters, and links to external test management tools.
- Test statuses: `PASSED`, `FAILED`, `SKIPPED`, `BROKEN`, `NO_RUN`, `COMPLETE`, `NOT_COMPLETE`.

### Logging
- Based on **Log4j2**.
- Central `SystemLogger` API for step logging, warnings, debug, errors.
- Step-specific logging with `logStepInfo()`.
- Custom assertions via extended `Assertion` class, including `assessTrue` for known issues (→ `BROKEN`).

### Screenshots
- Configurable levels: `ERROR`, `SUCCESS`.
- Can override per-step.
- Full-page screenshots supported via CDP (`setCDPEnabled(true)`).

### Enumerations
- Provides enums for `TestType`, `TestStatus`, `BrowserName`, `WebDriverName`, `FileFormat`, `ImageFormat`, `ConfigType`, `DownloadStrategy`, `Direction`, `TestCaseSource`, `ScreenshotLevel`.
- Simplifies usage and avoids string literals.

---

## 📂 Project Structure

```
src/
  main/
    java/
      ch/qa/testautomation/tas/
        common/         # Common utilities, enums, logging, etc.
        configuration/  # Configuration classes
        core/           # Core framework logic (annotations, controller, service, etc.)
        exception/      # Custom exceptions and error handling
        intefaces/      # Driver and REST interfaces
        rest/           # REST driver providers and integrations
        web/            # Web driver providers and page objects
    resources/
      allure.properties         # Allure reporting config
      log4j2.xml                # Logging configuration
      messages_en.properties    # Message resources
      driverConfig/             # Driver and integration configs (JSON)
      schema/                   # Test case and surefire schemas
changelog.md                   # Project changelog
pom.xml                        # Maven build file
README.md                      # Project documentation
```

---

## 🚀 Quickstart

1. **Including**  
```xml
<Dependency>
    <GroupId>io.github.sleod</GroupId>
    <ArtifactId>tas-framework-lite</ArtifactId>
    <Version>6.1.00-RELEASE</Version>
</Dependency>

```

2. **Create a Test Case (`.tas`)**  
```json
{
  "meta": ["@all", "@smoke"],
  "name": "My Homepage",
  "description": "UI Smoke Test",
  "type": "web_app",
  "testCaseId": "TEST-123",
  "screenshotLevel": "ERROR",
  "testDataRef": "File:my-testData.json",
  "startURL": "https://www.my.ch/de.html",
  "steps": [
    { "name": "Cookie Handling", "testObject": "My Homepage Page"},
    { "name": "Click Menu", "testObject": "My Homepage Page" }
  ]
}
```
2.1 **Test Data (`my-testData.json`)**  
```json
{
  "zip": "2342",
  "linkText": "my link",
  "menu": {
    "card": "card",
    "payment": "billing"
  },
  "testArray": [
    123,
    "rar",
    22.1,
    true
  ],
  "testValues": {
    "a": 123,
    "b": "name",
    "c": 12.33,
    "d": true
  },
  "nestedObject": {
    "nameObject1": {
      "nameObject1_1": "text1_1",
      "nameObject1_2": "text1_2"
    }
  }
}
```
2.2 **Test Objects (`MyPage.java`)**  
```java
@TestObject(name = "My Homepage Page")
public class MyHomePage extends WebPageObject{
    @FindBy(css = "list-item")
    SelenideElement firstValueFromList;
    @FindBy(id = "hamburger-icon")
    private SelenideElement hamburgerIcon;
    @FindBy(xpath = "//button[contains(text(),'Decline all cookies')]")
    private SelenideElement declineAllCookies;
    
    @TestStep(name = "Cookie Handling")
    public void handleRaiffeisenCookie() {
        if(declineAllCookies.exists() && declineAllCookies.isDisplayed()) {
            declineAllCookies.click();
        }
    }

    @TestStep(name = "Click Menu")
    public void clickHamburgerIcon() {
        raiffeisenHamburgerIcon.click();
        //just for demo
        WaitUtils.waitStep(3);
    }
}
```
```java
@TestObject(name = "Test using Parameters")
public class TestUsingParameters extends SingleTestObject {

    @TestStep(name = "Change Language")
    public void assertUsingParameter(String code, String language) {
        logStepInfo("Change Language: " + code + " - " + language);
    }

    @TestStep(name = "Assert using parameter")
    public void assertUsingParameter(int param1) {
        Assertions.assertEquals(param1, 5);
    }

    @TestStep(name = "Test complex parameter - map and list")
    public void testParamMapAndList(Map<String, String> param1, List<String> param2) {
        logStepInfo("Test complex parameter - map");
    }

    @TestStep(name = "Test complex parameter - json array")
    public void testParamJsonArray(ArrayNode arrayNode) {
        logStepInfo("Test complex parameter - jsonArray");
    }

    @TestStep(name = "Test complex parameter - json Object")
    public void testParamJsonObject(ObjectNode objectNode) {
        logStepInfo("Test complex parameter - jsonObject");
    }
}

```

3. **Configure Drivers**  
Example `driverDownloadConfig.json`:
```json
{
  "platform": "linux64",
  "installBrowser": false,
  "browser": "chrome",
  "downloadLink": "https://storage.googleapis.com/chrome-for-testing-public/",
  "downloadStrategy": "AUTO",
  "proxyHost": "",
  "proxyPort": ""
}
```
3.1 **Selenium Web Driver**
Make sure that the driver is compatible to browser. In case the driver bin file is in local folder:
```
webdriver.chrome.driver=/path/to/chromedriver
```
***Additional settings:***
```
webdriver.name=chrome
driver.browser.headless=true
```

4. **Run Tests**  
```bash
mvn -Drun.start.url=”http://xxxx.xxx” -Dtest=TestCasesRunner test
```

5. **Report**  
Allure report will be generated automatically in /target folder als default


---

## 🔧 Configuration Files

- **Drivers:** `driverDownloadConfig.json`
- **Proxy:** `proxyConfig.json`
- **Azure DevOps:** `azureDevOpsConfig.json`, `azureDevOpsExecution.json`
- **JIRA/Xray:** `jiraConfig.json`, `jiraExecutionConfig.json`
- **Microfocus QC:** `qcConfig.json`
- **Drivers:** `driverConfig/*.json`

---

## 🧪 Example REST Test

```java
@TestStep(name = "Perform GET")
public void performGet(String url) {
    SimpleRestDriver driver = new SimpleRestDriver();
    driver.setBearerToken("token");
    Response response = driver.get(url);
    Assertion.assertEquals(200, driver.getStatus(response));
}
```

---

## 📊 Test Status Mapping

- **PASS**: successful execution  
- **FAIL**: step/test failure  
- **SKIPPED**: skipped (e.g. missing data)  
- **BROKEN**: known issue (Allure highlights in yellow)  
- **NO_RUN**: not executed  
- **COMPLETE / NOT_COMPLETE**: reserved for HP QC

---

## 📄 License

Apache License 2.0 — see [LICENSE](./LICENSE).

---

## 🗺️ Roadmap

- Extend integrations (TestRail, Zephyr, others)  
- Enhanced dashboards and reports  
- More prebuilt `.tas` templates
- Advanced mobile/device farm support
- Improved error handling and debugging tools
- OCR support for test case
