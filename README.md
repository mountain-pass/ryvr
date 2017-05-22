## Ryvr 
_**- Let Your Data Flow**_

[![Run Status](https://api.shippable.com/projects/58eb44005a50220700d2d0c1/badge?branch=master)](https://app.shippable.com/github/mountain-pass/ryvr)
[![Coverage Badge](https://api.shippable.com/projects/58eb44005a50220700d2d0c1/coverageBadge?branch=master)](https://app.shippable.com/github/mountain-pass/ryvr)
[![Dependency Status](https://www.versioneye.com/user/projects/58ee953c0f9f35004e5c4bf2/badge.svg?style=flat-round)](https://www.versioneye.com/user/projects/58ee953c0f9f35004e5c4bf2)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7785f1049bd045dda89fcfff65bff3da)](https://www.codacy.com/app/mountain-pass/ryvr?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mountain-pass/ryvr&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/7785f1049bd045dda89fcfff65bff3da)](https://www.codacy.com/app/mountain-pass/ryvr?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mountain-pass/ryvr&amp;utm_campaign=Badge_Coverage)

[![Sauce Test Status](https://saucelabs.com/browser-matrix/tompahoward.svg)](https://saucelabs.com/u/tompahoward)

Provides extremely fast access to your data, in fine precise slices

## Running

    java -jar ryvr-<VERSION>.jar --spring.config.location=application.yml

### Configuration

Ryvrs are configured using application properties.

#### Database Ryvrs

Database Ryvrs are configured as [spring data sources|https://docs.spring.io/spring-boot/docs/current/reference/html/howto-data-access.html] under the property prefix `au.com.mountainpass.ryvr.data-sources`. This property expects an
array of data sources. For example

    au.com.mountainpass.ryvr:
      data-sources:
        - url: jdbc:mysql://db_host_1
          username: dbuser1
          password: dbpass1
        - url: jdbc:postgresql://localhost/postgres
          username: dbuser2
          password: dbpass2

For each data source, you can configure one or more ryvrs as a map under the
`au.com.mountainpass.ryvr.data-sources[*].ryvrs` property prefix.

The key of the map specifies the name of the ryvr, which must be unique.

| Property | Description |
| -------- | ----------- |
| page-size | Specifies how many records to include in each page. You will need to tune this. Try 1024 to start with. |
| catalog | The name of the schema/database your table/view is in |
| table | The name of the table containing the records |
| ordered-by | The name of the column in the table, which can be used for sorting the records into order. |

These properties can be set using an `application.yml` file within the same directory as Ryvr.

##### Example

    au.com.mountainpass.ryvr:
      data-sources:
        - url: jdbc:mysql://localhost
          username: dbuser
          password: dbpass
          ryvrs:
            transactions:
              page-size: 10
              catalog: test_db
              table: transactions
              ordered-by: id


**NOTE:** At this time, only MySQL, PostGresSQL and H2 Ryvrs are supported.

See the [Externalized Configuration section of the Spring Boot documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) for other ways to set application properties.

#### HTTPS

Ryvr is configured to enable HTTPS and disable HTTP, in order to ensure that all traffic is encrypted.

HTTPS is configured using the following standard Spring Boot application properties:

    server.ssl.key-store
    server.ssl.key-alias
    server.ssl.key-password
    server.ssl.key-store
    server.ssl.key-store-password

See https://docs.spring.io/spring-boot/docs/current/reference/html/howto-embedded-servlet-containers.html#howto-configure-ssl for more details regarding these properties

##### Automatic Certificate Generation

By default, if Ryvr is unable to find the certificate specified by `server.ssl.key-alias`, it will generate one for you. The type of certificate generated is controlled via the `au.com.mountainpass.ryvr.ssl.genCert` application property, which defaults to `selfSigned`. Currently, `selfSigned` is the only type of certificate generation implemented. `au.com.mountainpass.ryvr.ssl.genCert` can be set to false, which will disable certificate generation. Ryvr will shutdown on startup if `au.com.mountainpass.ryvr.ssl.genCert` is false and it is unable to find the certificate specified by `server.ssl.key-alias`.

When generating a certificate, Ryvr will set the hostname to the value of the `au.com.mountainpass.ryvr.ssl.hostname` application property. `au.com.mountainpass.ryvr.ssl.hostname` defaults to `localhost`.

## Building

The project uses [Gradle](https://gradle.org/) for its build system and you can build the project by running:

    ./gradlew build

You can also run the app using the [Spring Boot Gradle Plugin](http://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-gradle-plugin.html) like so: 

    ./gradlew bootRun

The swagger definition will be available at the following URI:

 - [https://localhost:8443/api-docs/](http://localhost:8443/api-docs/)

The Spring Boot Actuator end-points are available here:

- [https://localhost:8443/info](http://localhost:8443/info)


## Testing

Ryvr uses JUnit with layered Cucumber testing in order to execute the same tests against the Java, REST and UI layers, which
are execute as separate test runs. Each run shares the same Cucumber scenarios and test steps, which calls a test 
client to execute the step against the appropriate layer. Spring profiles are used to enable the appropriate client
for each layer.

Tests for the three layers can be executed by running `./gradlew test`

### Unit Tests

Ryvr does not have unit test in the traditional sense. Unit test will verify if the unit behaves as we
expected, but it does not verify if the unit behaves as we need in the context of the full application.

### Integration Tests

The integration tests verify that Ryvr behaves as we expect as an integrated application.

The integration tests are run with the `integrationTest` Spring profile, which uses @SpringBootTest to run launch
Ryvr in the same JVM as our tests.

### System Tests

The system tests verify that Ryvr behaves as we expect when it's run as a seperate application.

The System tests are run with the `systemTest` Spring profile, which launches
Ryvr in a separate.

At this time, Ryvr is launched using the `bootRun` gradle task. Other profiles will be 

### Profiles

The `javaApi` spring profile is used to verify the behaviour of Ryvr's internal Java API. This profile is only used (and only makes sense) when running integration tests. It cannot be used during system tests as Ryvr is 
running in a separate JVM.

The `restApi` spring profile is used to verify the behaviour of Ryvr's REST API.

The `ui` spring profile is used to verify the behaviour of Ryvr's User Interface.

The `h2` spring profile is used to verify the behaviour of Ryvr's using a H2 embedded database. This profile is only used (and only makes sense) when running integration tests. It cannot be used during system tests as Ryvr is 
running in a separate JVM and therefore the database is running in a separate JVM, preventing us from setting up the
test data. We could configure H2 to accept connections from seperate processes, but we see no point.

The `mysql` spring profile is used to verify the behaviour of Ryvr's using a MySQL database

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
