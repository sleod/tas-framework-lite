Selenide supports running tests in Safari browser.

Setup is pretty easy.

## Way 1: one-liner

```java
Configuration.browser = “org.openqa.selenium.safari.SafariDriver";
```

Use it if you need just a Safari browser without any custom settings.


## Way 2: WebDriverProvider

```
@BeforeEach
  void setUp() {
    Configuration.browser = SafariProvider.class.getName();
  }

  private static class SafariProvider implements WebDriverProvider {
    @Override
    public WebDriver createDriver(DesiredCapabilities desiredCapabilities) {
      SafariOptions options = new SafariOptions(desiredCapabilities);
      return new SafariDriver(options);
    }
  }
```

Use it if you need to set some specific options for the Safari browser.
