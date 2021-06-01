How to start?

Just put actual [selenide.jar](http://search.maven.org/remotecontent?filepath=com/codeborne/selenide/4.9.1/selenide-4.9.1.jar) to your project.

For Maven users:

Add these lines to file pom.xml:

```
<dependency>
    <groupId>com.codeborne</groupId>
    <artifactId>selenide</artifactId>
    <version>4.9.1</version>
    <scope>test</scope>
</dependency>
```
For Ivy users:

Add these lines to file ivy.xml:

```
<ivy-module>
  <dependencies>
    <dependency org="com.codeborne" name="selenide" rev="4.9.1"/>
  </dependencies>
</ivy-module>
```

For Gradle users:

Add these lines to file build.gradle:

```
dependencies {
  testCompile 'com.codeborne:selenide:4.9.1'
}
```

Import the following methods:
```java
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;
```
And start writing UI Tests!
```java
@Test
public void userCanLoginByUsername() {
   open("/login");
   $(By.name("username")).setValue("johny");
   $("#submit").click();
   $(".success-message").shouldHave(text("Hello, Johny!"));
}
```

You can find some Selenide usages in the example project [Hangman](https://github.com/selenide-examples/hangman/blob/master/test/uitest/selenide/HangmanSpec.java).

There is also a separate project with examples of Selenide usages:
[selenide-examples] (https://github.com/selenide-examples)