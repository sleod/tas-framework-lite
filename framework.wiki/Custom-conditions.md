# List of custom conditions, created by Selenide community

* [Moving](#moving)
* [Child](#child)
* [ValueInAttribute](#valueInAttribute)

## Moving

**Author:** Andrejs Kalnačs [@andrejska](https://github.com/andrejska) 

**Description:** Checks that element is moving

**Note:** Element should implement `Locatable`

**Sample:** `$("#loginLink").should(moveAround(300))`

**Code:**

```java
public static Condition moveAround(int movePeriod) {
  return new Condition("moveAround") {
    @Override
    public boolean apply(WebElement element) {
      if (!(element instanceof Locatable)) {
        throw new RuntimeException("Provided WebElement is not Locatable, cannot understand if it moving or not");
      }
      Point initialLocation = ((Locatable) element).getCoordinates().inViewPort();
      Selenide.sleep(movePeriod);
      Point finalLocation = ((Locatable) element).getCoordinates().inViewPort();
      return !initialLocation.equals(finalLocation);
    }
  };
}
```

## Child

**Author:** Olivier Grech [@olivier-grech](https://github.com/olivier-grech) 

**Description:** Check condition on child 

**Sample:** `$$("#multirowTable tr").filterBy(child("td:nth-child(2)", text("Norris")));`

**Code:**

```java
public static Condition child(final String childCssSelector, final Condition condition) {
  return new Condition("child " + childCssSelector + " has " + condition.name) {
    @Override
    public boolean apply(WebElement element) {
    SelenideElement child = $(element.findElement(By.cssSelector(childCssSelector)));
    return child.has(condition);
    }
  };
}
```

## ValueInAttribute

**Author:** Ostap Oleksyn [@ostap-oleksyn](https://github.com/ostap-oleksyn) 

**Description:** Check if element attribute contains given value. **Case sensitive**

**Note:** You can also give a name such as `attributeContains` for better readability.

**Sample:** `$("#task").shouldHave(valueInAttribute("class", "done"));`

**Code:**

```java
public static Condition valueInAttribute(String attributeName, String value) {
  return new Condition(String.format("value '%s' in attribute '%s'", value, attributeName)) {
    @Override
    public boolean apply(Driver driver, WebElement element) {
    return getAttributeValue(element, attributeName).contains(value);
    }
  };
}
```