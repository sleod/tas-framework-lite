This page brings an overview of how Selenide API is simpler and more powerful than <a href="http://seleniumhq.org/projects/webdriver/">Selenium WebDriver</a> API.

# Keynote
Remember, this doesn't mean that Selenium WebDriver is "bad":
* Selenium WebDriver just has lower-level API which is suitable for wider range of tasks. 
* Selenide API is higher-level, but it was designed specifically for UI- and Acceptance testing. 

<img src="https://tfs-prod.service.raiffeisen.ch:8081/tfs/RCH/33029407-7843-42d8-9cc1-caec23331f35/_apis/tfvc/Items?path=%24%2FAP.Testtools%2FGit+-+RCH+Framework+Solution+Items%2FJava%2F_backup%2Fselenide-logo-big.png&versionDescriptor%5BversionType%5D=1&versionDescriptor%5Bversion%5D=761987&download=false&%24format=octetStream&api-version=4.1-preview.1" align="left"/>
Though <a href="http://seleniumhq.org/projects/webdriver/">Selenium WebDriver</a> is a great library for operating web browser, it's API is too low-level. Developer needs to write some boilerplate code to create/shutdown webdriver, to search radio buttons, to wait for javascript interactions etc. With Selenide You don't need to operate with Selenium WebDriver directly, don't need to wait for ajax requests etc. 

So, let's compare how to do primitive actions with both libraries. Probably the most tasty feature is the  <a href="#ajax-support">Ajax support</a>.

# 1. Create a browser
### Selenium WebDriver:
```java
DesiredCapabilities desiredCapabilities = DesiredCapabilities.htmlUnit();
desiredCapabilities.setCapability(HtmlUnitDriver.INVALIDSELECTIONERROR, true);
desiredCapabilities.setCapability(HtmlUnitDriver.INVALIDXPATHERROR, false);
desiredCapabilities.setJavascriptEnabled(true);
WebDriver driver = new HtmlUnitDriver(desiredCapabilities);
```

### Selenide:
```java
open("/my-application/login");
// And run tests with option -Dbrowser=htmlunit (or "chrome" or "ie", default value is "firefox")
```

# 2. Shutdown a browser
### Selenium WebDriver:
```java
if (driver != null) {
    driver.close();
}
```

### Selenide:
    // Do not care! Selenide closes the browser automatically.
With Selenide You don't need to operate with Selenium WebDriver directly. WebDriver will be automatically created/deleted during test start/finished.

# 3. Find element by id
### Selenium WebDriver:
```java
WebElement customer = driver.findElement(By.id("customerContainer"));
```

### Selenide:
```java
WebElement customer = $("#customerContainer"); 
```
or a longer conservative option:
```java
WebElement customer = $(By.id("customerContainer"));
```

# 4. Assert that element has a correct text
### Selenium WebDriver:
```java
assertEquals("Customer profile", driver.findElement(By.id("customerContainer")).getText());
```

### Selenide:
```java
$("#customerContainer").shouldHave(text("Customer profile"));
```

<a name="ajax-support"></a>
# 5. Ajax support (waiting for some event to happen)
### Selenium WebDriver (OMG!):
```
FluentWait<By> fluentWait = new FluentWait<By>(By.tagName("TEXTAREA"));
fluentWait.pollingEvery(100, TimeUnit.MILLISECONDS);
fluentWait.withTimeout(1000, TimeUnit.MILLISECONDS);
fluentWait.until(new Predicate<By>() {
    public boolean apply(By by) {
        try {
            return browser.findElement(by).isDisplayed();
        } catch (NoSuchElementException ex) {
            return false;
        }
    }
});
assertEquals("John", browser.findElement(By.tagName("TEXTAREA")).getAttribute("value"));
```

### Selenide:
```java
$("TEXTAREA").shouldHave(value("John"));
```
This command automatically waits until element gets visible AND gets expected value.<br/>
Default timeout is 4 seconds and it's configurable.

# 6. Assert that element has a correct CSS class
### Selenium WebDriver:
```java
assertTrue(driver.findElement(By.id("customerContainer")).getAttribute("class").indexOf("errorField") > -1);
```

### Selenide:
```java
$("#customerContainer").shouldHave(cssClass("errorField"));
```

# 7. Find element by text
### Selenium WebDriver:
   No way (except XPath)

### Selenide:
```java
WebElement customer = $(byText("Customer profile")); 
```

# 8. Assert that element text matches a regular expression
### Selenium WebDriver:
```java
WebElement element = driver.findElement(By.id("customerContainer"));
assertTrue(Pattern.compile(".*profile.*", DOTALL).matcher(element.getText()).matches());
```

### Selenide:
```java
$("#customerContainer").should(matchText("profile"));
```

# 9. Assert that element does not exist
### Selenium WebDriver:
```java
try {
    WebElement element = driver.findElement(By.id("customerContainer"));
    fail("Element should not exist: " + element);
}
catch (WebDriverException itsOk) {}
```

### Selenide:
```java
$("#customerContainer").shouldNot(exist);
```

# 10. Looking for element inside parent element
### Selenium WebDriver:
```java
WebElement parent = driver.findElement(By.id("customerContainer"));
WebElement element = parent.findElement(By.className("user_name"));
```

### Selenide:
```java
$("#customerContainer").find(".user_name");
```

# 11. Looking for Nth element
### Selenium WebDriver:
```java
WebElement element = driver.findElements(By.tagName("li")).get(5);
```
### Selenide:
```java
$("li", 5);
```

# 12. Click "Ok" in alert dialog
### Selenium WebDriver:
```java
    try {
      Alert alert = checkAlertMessage(expectedConfirmationText);
      alert.accept();
    } catch (UnsupportedOperationException alertIsNotSupportedInHtmlUnit) {
      return;
    }
    Thread.sleep(200); // sometimes it will fail
```
### Selenide:
```java
    confirm("Are you sure to delete your profile?");
```
or
```java
    dismiss("Are you sure to delete your profile?");
```

# 13. Debugging info for elements
### Selenium WebDriver:
```java
    WebElement element = driver.findElement(By.id("customerContainer"));
    System.out.println("tag: " + element.getTag());
    System.out.println("text: " + element.getText());
    System.out.println("id: " + element.getAttribute("id"));
    System.out.println("name: " + element.getAttribute("name"));
    System.out.println("class: " + element.getAttribute("class"));
    System.out.println("value: " + element.getAttribute("value"));
    System.out.println("visible: " + element.isDisplayed());
    // etc. 
```
### Selenide:
```java
    System.out.println($("#customerContainer"));
    // output looks like this: "<option value=livemail.ru checked=true selected:true>@livemail.ru</option>"
```

# 14. Take a screenshot
### Selenium WebDriver:
```java
    if (driver instanceof TakesScreenshot) {
      File scrFile = ((TakesScreenshot) webdriver).getScreenshotAs(OutputType.FILE);
      File targetFile = new File("c:\temp\" + fileName + ".png");
      FileUtils.copyFile(scrFile, targetFile);
    }
```
### Selenide:
```java
    takeScreenShot("my-test-case");
```
For JUnit users it's even more simpler:
```java
    public class MyTest {
      @Rule // automatically takes screenshot of every failed test
      public ScreenShooter makeScreenshotOnFailure = ScreenShooter.failedTests();
    }
```
# 15. Select a radio button
### Selenium WebDriver:
```java
    for (WebElement radio : driver.findElements(By.name("sex"))) {
      if ("woman".equals(radio.getAttribute("value"))) {
        radio.click();
      }
    }
    throw new NoSuchElementException("'sex' radio field has no value 'woman'");
```
### Selenide:
```java
    selectRadio(By.name("sex"), "woman");
```
# 16. Reload current page
### Selenium WebDriver:
```java
    webdriver.navigate().to(webdriver.getCurrentUrl());
```
### Selenide:
```java
    refresh();
```
# 17. Get the current page URL, title or source
### Selenium WebDriver:
```java
    webdriver.getCurrentUrl();
    webdriver.getTitle();
    webdriver.getPageSource();
```
### Selenide:
```java
    url();
    title();
    source();
```

# Don't believe?
Then try it yourself!
<a href="https://github.com/codeborne/selenide/wiki/Quick-Start">Let's start writing concise UI Tests</a>!