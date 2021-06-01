### Wait for an event
```java
    $(".username").shouldHave(text("John"));
```
All "should"- methods automatically wait until web element appears and gets given property (text in this example). Timeout is 4 seconds by default.

### Wait for an event with a timeout (longer than default):
```java
    $("#username").waitUntil(hasText("Hello, Johny!"), 8000);
```

> Note that you need "waitUntil" methods in rare specific cases - when you definitely know that this is a long-lasting query, and event is designed to happen after a pause (longer than default 4 seconds).

### Get list of matched elements
```java
    $$("#paymentScheduleTable tr")
```

### Get element by index
```java
    $("#paymentScheduleTable tr", 5)
```

### Clear browser cache
```java
    clearBrowserCache();
```

### Hamcrest-style checks
```java
    assertThat($("#insuranceDetailsHeader").getText(), equalTo("Страховые полисы"));
    assertThat($$("#paymentScheduleTable tr").size(), equalTo(7));
```

# Concise API
Here we will provide some examples how Selenide can be used to write short and expressive UI tests.

### Example 1: Checking the page title
LONG way:
```java
    assertEquals("EPP", getElement(By.tagName("title")).getText());
```

SHORTER way:
```java
    assertElement(By.tagName("title"), hasText("EPP"));
```

CONCISE way:
```java
    assertEquals("EPP", title());
```
### Example 2: Checking the number of elements
LONG way:
```java
    assertEquals(2, getElements(By.className("item")).size());
```

BETTER way:
```java
    assertEquals(2, $$(".item").size());
```

BEST way:
```java
    $$(".item").shouldHave(size(2));
```


### Example 3: Finding elements inside parent
LONG way:
```java
    assertThat( getElement(By.id("documentsTable")).findElement( By.tagName("tbody")).findElements( By.tagName("tr")).size(), equalTo(4));
```

CONCISE way:
```java
    $("#documentsTable", 2).findAll("tbody tr").shouldHave(size(4));
```