
## Preface

Selenide primary goals are:
* Stable tests
* Readable tests
* Fast tests
* Effectiveness of test writers

To achieve these goals, we have the following roadmap. These are the major areas that we will probably work in the near future. 

#### Selenide API improvements
* Create a plugin system for Selenide
* Add parameterized methods like `$.click(ClickOptions)`, `$.download(DownloadOptions)`.

#### Integrations with 3rd party libraries
  ...to suggest new features (not available in WebDriver):
* [CDP](https://github.com/selenide/selenide/issues/1157) (downloading files, intercepting responses, emulating geolocation etc.)
* JSoup (analysis of HTML, fast search inside HTML)
* Selenoid (downloading of files from container)

#### Intelligence
* IDEA checks for Selenide code
* [Analyze DOM after test failure](https://github.com/selenide/selenide/issues/129) (find changed locator / similar elements)

#### Test performance
* Optimize performance of collections, e.g. [access by index](https://github.com/selenide/selenide/issues/1266). 
* [Snapshot](https://github.com/selenide/selenide/issues/1132)
* Profiler (further development of TextReport for detecting slow WD methods, selectors etc)

#### New areas to think about
* Selenide + Playwright browsers (ability to use 3 major browser engines via CDP)
* Selenide+Espresso (is it really needed? What about Kaspresso etc?)
* Test Rest API (like built-in RestAssured, but executing rest requests "like from browser". Selenide-like syntax for verifying JSON/XML)
* Test databases (Selenide-like syntax for DB queries and checks)

#### Technical hygiene
* Create an infrastructure for user to provide their own plugins
* [Upgrade to Selenium 4](https://github.com/selenide/selenide/issues/1162)
* Support for Java 9 modules (?)
* Better integration with Appium
* improve usability of proxy

#### Organisational
* [Redesign of selenide.org](https://github.com/selenide/selenide/issues/1161)
* Selenide ambassadors program (?)
* Donations (?)
* Migrate to Github Actions (?)
* CI with Windows agents (for testing Selenide in IE and Edge)

# Off road
Currently we are not planning to work on the following areas (because they don't help to achieve primary Selenide goals):

#### "Beautiful" reports
There are other reporting solutions.  
For example, [Allure](https://docs.qameta.io/allure/#_selenide) provides seamless integration with Selenide.

#### "Machine learning"
I am rather skeptical about artificial intelligence.  
I think people just write a few IFs and name it "machine learning" because it helps to sell.  
We probably don't want to be part of this affair. :)
I rather believe in human brain power. We focus on designing Selenide to free up test writers' brains from boring technical details. 

#### Selenide on Python, JavaScript, Ruby, .NET
There are several ports of Selenide created by our friends:
* Python: [Selene](https://github.com/yashaka/selene)
* JavaScript: [SelenideJS](https://github.com/KnowledgeExpert/selenidejs)
* PHP: [phpSelenide](https://github.com/razielsd/phpSelenide)
* .NET: [NSelene](https://github.com/yashaka/NSelene)
* .NET: [selenide-for-c-sharp](https://github.com/neooleg/selenide-for-c-sharp)
* .NET: [Selenious](https://vitalyzinevich.visualstudio.com/_git/Selenious)

Currently we don't have plans to create another one.  
Probably in a form of paid enterprise support if you really wish. 

P.S. We all are humans. This roadmap is just a current vision. It can change in time.   
NB! You can influence it. Any kinds of ideas, questions, suggestions, discussions are welcome. This is open source, you know.  

Andrei Solntsev & Co