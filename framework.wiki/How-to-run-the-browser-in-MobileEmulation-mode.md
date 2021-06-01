Implement a WebDriverProvider:

```java
package example;

public class MobileEmulationWebDriver implements WebDriverProvider {
  @Override
  public WebDriver createDriver(DesiredCapabilities capabilities) {
    Map<String, Object> mobileEmulation = new HashMap<>();
    mobileEmulation.put("deviceName", "iPhone X");
    ChromeOptions chromeOptions = new ChromeOptions();
    chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
    WebDriverManager.chromedriver().setup();
    ChromeDriver chromeDriver = new ChromeDriver(chromeOptions);
    return chromeDriver;
  }
}
```

Use it in your test

```java
Configuration.browser="example.MobileEmulationWebDriver";
```

or pass in a system property `-Dselenide.browser=example.MobileEmulationWebDriver`

Please note, that not every phone is supported as "deviceName", consult Chrome documentation site for supported devices.