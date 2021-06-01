Selenide is basically a wrapper for Selenium WebDriver. This means that you can use all WebDriver methods in case if you feel some functionality missing in Selenide.

## How to get WebDriver
Step 1:
`import static com.codeborne.selenide.WebDriverRunner.getWebDriver`

Step 2:
Use method `getWebDriver` everywhere you need, e.g.:
```java
getWebDriver().findElement(By.id("username"))
```

## Which browser Selenide uses
By default Selenide uses Firefox web browser. Also Selenide takes care about dowloading browser drivers binaries(chrome\geckodriver and etc).

You can easy run Selenide with another browser by passing "browser" parameter when running tests:
```java
java -Dselenide.browser=chrome -classpath junit-rt.jar... org.junit.Runner...
```

The following values are supported:
* firefox (default)
* chrome (the fastest. Recommended.)
* htmlunit (headless browser)
* ie
* opera (slow and unstable, not recommended)
* phantomjs (newborn headless browser)
* legacy_firefox (old Firefox versions lower than 47)

## How to run Selenide with another browser
To run Selenide with any other web browser driver, just pass name of it's class to "browser" parameter:
```
java -Dselenide.browser=org.openqa.selenium.firefox.FirefoxDriver
```

Another option is to directly say Selenide use your desired WebDriver instance:
```
WebDriverRunner.setWebDriver(myWebDriver).
```
In this case Selenide is not responsible for handling webdriver lifecycle and you should close and quit it manually.

## How to run Selenide with custom profile
You can pass Selenide a name of factory class that creates WebDriver instance by your needs:
```
java -Dselenide.browser=com.mycompany.CustomWebDriverProvider
```

This factory should implement interface WebDriverProvider with method createDriver:
```java
  public static class CustomWebDriverProvider implements WebDriverProvider {
    @Override
    public WebDriver createDriver(DesiredCapabilities capabilities) {
       FirefoxProfile profile = new FirefoxProfile(new File("/home/test/MozzillaProf/"));
       FirefoxOptions firefoxOptions = new FirefoxOptions()
         .setProfile(profile)
         .setAcceptInsecureCerts(true)
         .addPreference("general.useragent.override", "some UA string")
         .merge(capabilities);

       return new FirefoxDriver(firefoxOptions);
    }
  }
```
*Note:* Line `.merge(capabilities);` is extremely important. It transfers base capabilities and pass it to custom driver instance.