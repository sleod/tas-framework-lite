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
- **Appium** for mobile (iOS and Android).
- Remote driver configs via JSON in `driverConfig/`.
- Automatic driver download and version check with strategies: `AUTO`, `AzureDevOps`, `SharedFile`, `Online`.

### REST Testing
- **SimpleRestDriver**: Direct Jakarta client with simple GET/POST/PUT/DELETE/PATCH methods.
- **RestDriver**: Extended functionality with JSON config and proxy auto-detection.
- Authentication supported (Basic, Bearer, PAT).
- Extensible for specific clients (Jira, TFS, Allure Cloud).

### Integrations
- **Azure DevOps Connector**: Sync results to Test Plans, full runs, failure retests, or selected cases via config files (`azureDevOpsConfig.json`, `azureDevOpsExecution.json`).
- **JIRA/Xray Connector**: Sync issues, executions, results, and evidence. Requires `jiraConfig.json` and `jiraExecutionConfig.json` plus Atlassian client credentials.
- **Microfocus QC Connector**: Create/manage test sets, runs, attach results (requires `qcConfig.json`).

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
  main/java/           # Framework source
  main/resources/      # Config (drivers, integrations etc.)
```

---

## 🚀 Quickstart

1. **Including**  
```xml
<Dependency>
  comming soon...
</Dependency>

```

2. **Create a Test Case (`.tas`)**  
```json
{
  "meta": ["@all", "@smoke"],
  "name": "My Homepage",
  "description": "UI Smoke Test",
  "type": "web_app",
  "testCaseId": "BANKING-3131",
  "screenshotLevel": "ERROR",
  "testDataRef": "File:my-testData.json",
  "startURL": "https://www.my.ch/de.html",
  "steps": [
    { "name": "Click Menu", "testObject": "My Homepage Page" },
    { "name": "Insert Bank Name", "testObject": "My Homepage Page", "using": "zip", "takeScreenshot": true }
  ]
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
