## How to soft assert using Selenide

Selenide provide point to perform built in verification softly - this mean that Selenide will skip failed verification and collect all of it and throw error only at the end of test.

### Mechanisms:
1. TestNG listener - `com.codeborne.selenide.testng.SoftAsserts`
2. JUnit4 rule - `com.codeborne.selenide.junit.SoftAsserts`
3. JUnit5 extension - `com.codeborne.selenide.junit5.SoftAssertsExtension`

### Example: 
Page with 2 elements, where 2 at invisible
```html
...
  <div id='first' style='display:none;'>First</div>
  <div id='second' style='display:none;'>Second</div>
...
```

And use code like this:
```java
public class Tests {
  @Test
  public void test() {
    $("#first").should(visible).click();
    $("#second").should(visible).click();
  }
}
```

In normal way test fail on a first row because first item is not visible and second item will never be touched and verified.

But if you want to verify both items you can use Selenide soft assets.
In this case Selenide will perform condition verification and then click on both of the items and it some item unavailable for some reason it will generate errors for each fail after test end.

1. Using TestNG just register listener bypass annotation for test class:
```java
@Listeners({ SoftAsserts.class})
public class Tests {
  @Test
  public void test() {
    Configuration.assertionMode = SOFT;
    open("page.html");

    $("#first").should(visible).click();
    $("#second").should(visible).click();
  }
}
```
2. Using JUnit4 just describe rule inside test class:
```java
public class Tests {
  @Rule 
  public SoftAsserts softAsserts = new SoftAsserts();

  @Test
  public void test() {
    Configuration.assertionMode = SOFT;
    open("page.html");

    $("#first").should(visible).click();
    $("#second").should(visible).click();
  }
}
```
3. Using JUnit5 extend test class:
```java
@ExtendWith({SoftAssertsExtension.class})
class Tests {
  @Test
  void test() {
    Configuration.assertionMode = SOFT;
    open("page.html");

    $("#first").should(visible).click();
    $("#second").should(visible).click();
  }
}
```
Or register extension inside test class:
```java
class Tests {
  @RegisterExtension 
  static SoftAssertsExtension softAsserts = new SoftAssertsExtension();

  @Test
  void test() {
    Configuration.assertionMode = SOFT;
    open("page.html");

    $("#first").should(visible).click();
    $("#second").should(visible).click();
  }
}
```

### _Without_ soft assertions you will get report:
```java
Element not found {#first}
Expected: visible

Screenshot: file://build/reports/tests/1536060081565.0.png
Page source: file://build/reports/tests/1536060081565.0.html
Timeout: 4 s.
Caused by: NoSuchElementException: no such element: Unable to locate element: {"method":"css selector","selector":"#first"}
```
### _With_ soft assertions you will get report:
```java

FAIL #1: Element not found {#first}
Expected: visible
Screenshot: file://build/reports/tests/1536060329615.0.png
Page source: file://build/reports/tests/1536060329615.0.html
Timeout: 4 s.
Caused by: NoSuchElementException: no such element: Unable to locate element: {"method":"css selector","selector":"#first"}

FAIL #2: Element not found {#second}
Expected: visible
Screenshot: file://build/reports/tests/1536060334390.1.png
Page source: file://build/reports/tests/1536060334390.1.html
Timeout: 4 s.
Caused by: NoSuchElementException: no such element: Unable to locate element: {"method":"css selector","selector":"#second"}
```