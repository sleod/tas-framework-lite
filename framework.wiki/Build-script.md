You've developed some tests with IDE and want to run them from command line / CI server.

In this case you need a build script. 
De-facto standards in Java world are [Ant](http://ant.apache.org/), [Maven](http://maven.apache.org/) and [Gradle](http://www.gradle.org/). Just in case, Selenide itself uses Gradle as the youngest one. :) 

### ANT

Here is an example for running UNIT-tests and UI-tests:
```xml
  <target name="test" depends="compile-tests">
    <mkdir dir="test-results"/>
    <junit maxmemory="512m" haltonfailure="false" failureproperty="tests-failed" fork="true">
      <batchtest todir="test-results">
        <fileset dir="classes-test" includes="**/*Test.class" excludes="**/Abstract*"/>
        <formatter type="xml"/>
        <formatter type="plain" usefile="false"/>
      </batchtest>
      <classpath>
        <path path="classes"/>
        <path path="classes-test"/>
        <fileset dir="lib" includes="**/*.jar"/>
      </classpath>
    </junit>
    <fail if="tests-failed"/>
  </target>

  <target name="ui-test" depends="compile-ui-tests">
    <mkdir dir="ui-test-results"/>
    <junit maxmemory="512m" haltonfailure="false" failureproperty="tests-failed" fork="true">
      <batchtest todir="ui-test-results">
        <fileset dir="classes-ui-test" includes="**/*Test.class" excludes="**/Abstract*"/>
        <formatter type="xml"/>
        <formatter type="plain" usefile="false"/>
      </batchtest>
      <classpath>
        <path path="classes"/>
        <path path="classes-ui-test"/>
        <fileset dir="lib" includes="**/*.jar"/>
      </classpath>
    </junit>
    <fail if="tests-failed"/>
  </target>
```

### Gradle

A build script example from [Hangman game](https://github.com/asolntsev/hangman):

```groovy
dependencies {
  testCompile 'junit:junit:4.12'
  testCompile 'com.codeborne:selenide:2.24'
  testCompile group: 'com.codeborne', name: 'phantomjsdriver', version: '1.2.1', transitive: false
}

test { // for running UNIT-tests
  include 'ee/era/hangman/**'
}

task uitest(type: Test) {
  systemProperties['browser'] = 'firefox'
}

task ie(type: Test) {
  systemProperties['browser'] = 'ie'
  systemProperties['timeout'] = '12000'
}

task htmlunit(type: Test) {
  systemProperties['browser'] = 'htmlunit'
}

task chrome(type: Test) {
  systemProperties['browser'] = 'chrome'
  systemProperties['webdriver.chrome.driver'] = '/usr/bin/chromedriver'
}

task phantomjs(type: Test) {
  systemProperties['browser'] = 'phantomjs'
}

tasks.withType(Test).all { testTask ->
  testTask.systemProperties['file.encoding'] = 'UTF-8'
  testTask.testLogging.showStandardStreams = true
  testTask.outputs.upToDateWhen { false }
}
```