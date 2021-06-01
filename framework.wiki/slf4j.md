### Easy solution:

##### For Gradle users:

The easiest solution would be to add one line to your `build.gradle`:

```
testRuntimeOnly 'org.slf4j:slf4j-simple:1.7.30'
```

##### For Maven users:

Add this block to `pom.xml`:

```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>1.7.30</version>
    <scope>test</scope>
</dependency>
```

### More details
#### What is slf4j:

* Selenide and other libraries use slf4j for logging. 
* Slf4J is a “facade”, something like “interface”. 
* Log4j, Logback, JCL etc. are implementations of this "interface". You can choose any of them in your project.


To use one of slf4J implementations, you need to add corresponding dependency to your project:

* `slf4j-log4j12-*.jar`
* `logback-classic-*.jar`
* `slf4j-simple-*.jar`
* `slf4j-jdk14-*.jar`
* `slf4j-jcl-*.jar`

> Most probably you already have one of these dependencies. Then you don’t need to do anything.


Here is background info: [why we decided to migrate from JUL to SLF4J](https://selenide.org/2019/10/31/selenide-5.5.0/)