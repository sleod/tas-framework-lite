# What about IE?
Our experience says that Selenium WebDriver doesn't always correctly work with IE browser. But we typically
want to test our code with IE because many users still use it.

* Body tag doesn't appear immediately - we need to wait for it.
* IE caches pages - we need to clear cache after every test and generate unique URLs.
* IE crashes when running too many tests at a time. Making pauses between tests helps.

Selenide contains some workarounds for IE. It was sufficient to make IE working in our projects, but probably you will encounter more problems in your projects. Feel free to report them and suggest your workarounds!

### To close browser after every test class:
    @AfterClass
    public static void ieRelax() {
      if (isIE()) {
        closeWebDriver();
        sleep(500);
      }
    }

### To clear browser cache between tests:
    @Before
    public void clearIeCache() {
      if (isIE()) {
        clearBrowserCache();
      }
    }