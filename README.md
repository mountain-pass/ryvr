## Ryvr 
_**- Let Your Data Flow**_

[![Run Status](https://api.shippable.com/projects/58eb44005a50220700d2d0c1/badge?branch=master)](https://app.shippable.com/github/mountain-pass/ryvr)
[![Coverage Badge](https://api.shippable.com/projects/58eb44005a50220700d2d0c1/coverageBadge?branch=master)](https://app.shippable.com/github/mountain-pass/ryvr)
[![Dependency Status](https://www.versioneye.com/user/projects/58ee953c0f9f35004e5c4bf2/badge.svg?style=flat-round)](https://www.versioneye.com/user/projects/58ee953c0f9f35004e5c4bf2)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7785f1049bd045dda89fcfff65bff3da)](https://www.codacy.com/app/mountain-pass/ryvr?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mountain-pass/ryvr&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/7785f1049bd045dda89fcfff65bff3da)](https://www.codacy.com/app/mountain-pass/ryvr?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mountain-pass/ryvr&amp;utm_campaign=Badge_Coverage)

[![Sauce Test Status](https://saucelabs.com/browser-matrix/tompahoward.svg)](https://saucelabs.com/u/tompahoward)

Provides extremely fast access to your data, in fine precise slices

The project uses [Gradle](https://gradle.org/) for its build system and you can build the project by running:

    ./gradlew build

You can also run the app using the [Spring Boot Gradle Plugin](http://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-gradle-plugin.html) like so: 

    ./gradlew bootRun

The swagger definition will be available at the following URI:

 - [https://localhost:8443/api-docs/](http://localhost:8443/api-docs/)

The Spring Boot Actuator end-points are available here:

- [https://localhost:8443/info](http://localhost:8443/info)

## HTTPS

Ryvr is configured to enable HTTPS and disable HTTP, in order to ensure that all traffic is encrypted.

HTTPS is configured using the following standard Spring Boot application properties:

    server.ssl.key-store
    server.ssl.key-alias
    server.ssl.key-password
    server.ssl.key-store
    server.ssl.key-store-password

See https://docs.spring.io/spring-boot/docs/current/reference/html/howto-embedded-servlet-containers.html#howto-configure-ssl for more details regarding these properties

### Automatic Certificate Generation

By default, if Ryvr is unable to find the certificate specified by `server.ssl.key-alias`, it will generate one for you. The type of certificate generated is controlled via the `au.com.mountainpass.ryvr.ssl.genCert` application property, which defaults to `selfSigned`. Currently, `selfSigned` is the only type of certificate generation implemented. `au.com.mountainpass.ryvr.ssl.genCert` can be set to false, which will disable certificate generation. Ryvr will shutdown on startup if `au.com.mountainpass.ryvr.ssl.genCert` is false and it is unable to find the certificate specified by `server.ssl.key-alias`.

When generating a certificate, Ryvr will set the hostname to the value of the `au.com.mountainpass.ryvr.ssl.hostname` application property. `au.com.mountainpass.ryvr.ssl.hostname` defaults to `localhost`.


## Testing

Ryvr uses JUnit with layered Cucumber testing in order to execute the same tests against the Java, REST and UI layers, which
are execute as separate test runs. Each run shares the same Cucumber scenarios and test steps, which calls a test 
client to execute the step against the appropriate layer. Spring profiles are used to enable the appropriate client
for each layer.

Tests for the three layers can be executed by running `./gradlew test`

#### Unit Tests

The Unit tests are run with the `integrationTest` Spring profile, which enables the
`au.com.mountainpass.ryvr.testclient.JavaRyvrClient` client.

The `RyvrintegrationTests` is used to run the tests against the Java layer and automatically activates the `integrationTest` Spring
profile. It can either by run as a JUnit test from within your IDE or run using `./gradlew integrationTest`.

`./gradlew integrationTest` will record test results in `build/test-results-ut`

#### System Tests

The System tests are run with the `systemTest` Spring profile, which enables the
`au.com.mountainpass.ryvr.testclient.RestRyvrClient` client.

The `RyvrSystemTests` is used to run the tests against the Java layer and automatically activates the `systemTest` Spring
profile. It can either by run as a JUnit test from within your IDE or run using `./gradlew systemTest`

`./gradlew systemTest` will record test results in `build/test-results-st`

#### UI Tests

The UI tests are run with the `uiTest` Spring profile, which enables the
`au.com.mountainpass.ryvr.testclient.HtmlRyvrClient` client.

The `RyvrUiTests` is used to run the tests against the Java layer and automatically activates the `uiTest` Spring
profile. It can either by run as a JUnit test from within your IDE or run using `./gradlew uiTest`

`./gradlew uiTest` will record test results in `build/test-results-ui`


## Road Map

 - [X] Add tests using MySQL instead of H2
 - [X] SSL Config
 - [X] Add support for configuring ryvrs from config file
 - [X] Rename test phases to correctly specify their nature
 - [ ] Add tests for various ways of starting Ryvr
 - [ ] release
 - [ ] Add Circuit Breaker
 - [ ] Modify Perf test to use latency per event rather than latency per page
 - [ ] Add logic to calculate optimal page size
 - [ ] Switch to async io
 - [ ] Change rest of ryvr response to use StringBuilder
 - [ ] Add support for more databases
 - [ ] Add client library
 - [ ] Add caching headers
 - [ ] Add rest ryvr
 - [ ] Add logic to create triggers to allow ryvrs from non-event based tables
