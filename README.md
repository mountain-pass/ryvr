## Ryvr 
_**- Let Your Data Flow**_

[![CircleCI](https://img.shields.io/circleci/project/github/mountain-pass/ryvr.svg)](https://circleci.com/gh/mountain-pass/workflows/ryvr)
[![Dependency Status](https://www.versioneye.com/user/projects/58ee953c0f9f35004e5c4bf2/badge.svg?style=flat-round)](https://www.versioneye.com/user/projects/58ee953c0f9f35004e5c4bf2)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7785f1049bd045dda89fcfff65bff3da)](https://www.codacy.com/app/mountain-pass/ryvr?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mountain-pass/ryvr&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/7785f1049bd045dda89fcfff65bff3da)](https://www.codacy.com/app/mountain-pass/ryvr?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mountain-pass/ryvr&amp;utm_campaign=Badge_Coverage)

[![GitHub release](https://img.shields.io/github/release/mountain-pass/ryvr.svg)](https://github.com/mountain-pass/ryvr/releases/latest)
[![license](https://img.shields.io/github/license/mountain-pass/ryvr.svg)](https://github.com/mountain-pass/ryvr/blob/master/LICENSE)
[![Github All Releases](https://img.shields.io/github/downloads/mountain-pass/ryvr/total.svg)](https://github.com/mountain-pass/ryvr/releases)

[![Sauce Test Status](https://saucelabs.com/browser-matrix/tompahoward.svg)](https://saucelabs.com/u/tompahoward)

Ryvr provides highly optimised access to your data, when it's stored as a series of immutable events.
Think bank transaction lists, trade histories, log records, etc. 

## Performance<sup id="myfootnotelink-*">[*](#myfootnote-*)</sup>

### First Read

When reading a ryvr for the first time (i.e. none of the data is in cache), clients can pull down over **100,000 records per second** at a throughput of **4.7MB/s**. That's an average of **less than 7µs per record**.

### Subsequent Reads

On subsequent reads (i.e. previously read data is in cache), clients can pull down over **20 million records per second** at a throughput of **900MB/s**. That's an average of **less than 0.049µs per record**.

### Multiple Consumers

Ryvrs have very good economies-of-scale when there are multiple consumers. With 1000 clients all consuming the same ryvr, the average client can pull down over 4 million records per second, with the fastest client pulling down **55 million records per second**. This provides a throughput of over **160MB/s** for the average client and over **2.1GB/s** for the fastest client.

<a id="myfootnote-*" href="#myfootnotelink-*">*</a> All performance results measured on:
 - [Shippable's Default Dedicated Dynamic Nodes](http://docs.shippable.com/platform/tutorial/runtime/dynamic-nodes/)'s, which are running Ubuntu 14.04.5 LTS on 2 Cores and 3.675 GB of Memory (AWS's C4.Large)
 -  MacBook Pro 2.8 GHz Intel Core i7, with 16 GB 1600 MHz DDR3 and 1TB SSD, running OS X 10.11.6, using the REST API and a local running MySQL v5.7.18 data source. 

We often see performance test runs with significantly better results than the above, however the results
above represent the performance results that are consistently achieved.

In the future, these performance metrics will be updated with AWS based results.

You can reproduce the performance tests results yourself, by running  `RyvrTests_Integration_Performance_Rest_MySqlLocal.java` as a JUnit test, or by running the `testRyvrTests_Integration_Performance_Rest_MySqlLocal` gradle task. This will require a local MySql database called `test_db` with a `dbuser` user with the password `dbpass`.

### Latency

The write-read latency isn't that good at the moment. The average latency between adding a record to a data source and reading it from a ryvr is less than 0.72s. The 95th percentile is less than 1.25s and the maximum is 1.5s.

This is because we set the TTL on the current/last page to 1 second, so that when there is a large
number of Ryvr clients, they don't smash the data source when polling for new records.
i.e. even if you had 10,000 clients for a Ryvr, polling every 100ms, the database will
still only see 1 query per second, rather than 1,000,000 queries per second.
Ideally in the short term, we would reduce the time to live (TTL) to 100ms, but the HTTP spec (RFC2616) doesn't
allow sub-second TTLs.
  
If you know that you will only have a small number of clients for a Ryvr, you can
greatly improve the write-read latency by setting `au.com.mountainpass.ryvr.cache.current-page-max-age` to `0`
  
Longer term, there are a number of approaches we intend to use to improve this
#### [Hystrix Request Collapsing](https://github.com/Netflix/Hystrix/wiki/How-it-Works#RequestCollapsing)

  This would allow us combine requests received within a certain time period. For instance if we
  collapsed all the requests within 100ms, then in the above example of 10,000 clients polling
  every 100ms, with a HTTP TTL of 0, the database would still only see 10 queries per second.
  However, we expect this will result in a mean latency of just over 50ms and a max latency
  of just over 100ms

#### Notifications

  Some event sources provide a mechanism for alerting a registered subscriber when there are new events
  For instance, for MySQL event sources, the [mysql-binlog-connector-java](https://github.com/shyiko/mysql-binlog-connector-java) library allows a
  client to subsribe to and receive committed change events, such as inserts, on the database
  A similar capability is provided by [CouchDB's Continuous Changes](http://guide.couchdb.org/draft/notifications.html#continuous) if we
  had a CouchDB event source, and the [WatchService API](http://docs.oracle.com/javase/tutorial/essential/io/notification.html) for file system
  changes if we had File bases event sources.
  For these sorts of event sources, we can trigger a refresh only when there is an actual change. This
  would allow us to remove the TTL on the current/last page without increasing the query load on the
  data source.
  
  At the same time we would look to implement a change notification service to advise ryvr clients when there
  are new events.
  
  With both of these in place, ryvr clients can subscribe to changes, when there is a new event in the
  data source they ryvr will perform and refresh and then clients will get notified that they can refresh
  the current/last page, allowing to receive new events with a very small latency.
  
  With a large number of ryvr clients, this can result in a large deluge of requests for the current/last page
  each time there are new events, however since the ryvr has already been refreshed, no additional load
  would be applied to the data source. At the same time, we would need to make sure that Ryvr is capable of
  handling the large deluge of requests and if there is an issue, we'll be investigating it at that time, however
  load balancing across multiple ryvr instances would be the most likely solution.
  
  For data sources that don't have a change notifaction mechansim, the polling can be moved to Ryvr, which would
  still allow us to provide a change notifications to ryvr clients (albeit less efficently).


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
| query | Specifies the SQL for querying the database. The records *MUST* be order from oldest to newest |

These properties can be set using an `application.yml` file within the same directory as Ryvr.

*NOTE:* Only string, numeric and boolean datatypes are supported at this time.

##### Example

    au.com.mountainpass.ryvr:
      data-sources:
        - url: jdbc:mysql://localhost
          username: dbuser
          password: dbpass
          ryvrs:
            transactions:
              page-size: 10
              query: select `id`, `account`, `description`, `amount` from `transactions` ORDER BY `id` ASC


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

At this time, Ryvr can be launched using the `bootRun` and `distZipRun` gradle tasks. Other profiles will be added
in the future

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
 - [X] Add tests for various ways of starting Ryvr
 - [X] release
 - [X] Switch to [Link headers](https://tools.ietf.org/html/rfc5988#page-6) rather than HAL links, which would allow
    navigation without having to parse the body and URL rewriting without having to parse the body
 - [X] Examine faster options than deserialisation with Jackson
 - [X] Change rest of ryvr response to use StringBuilder or OutputStreams
 - [X] Add caching headers
 - [X] Fix test clients and UI after performance tuning improvements
 - [X] move serialiser out of datesource ryvr
 - [X] Performance test with multiple clients
 - [X] Add performance test on write -> read latency
 - [X] Add rest ryvr
 - [X] Add vary response header
 - [X] Add integration with SourceClear for security scans
 - [X] Use a different ryvr name for each test, so as to avoid the need to clear cache between test scenarios
 - [X] Add test for incorrect Ryvr name (404)
 - [X] Add test for deleted Ryvr (404)
 - [X] Add test for getting a RyvrsCollection as HTML
 - [X] Add test with negative page number (404)
 - [ ] Swtich to using a proper load generation framework for perf testing
 - [ ] Add test with different SQL types
 - [ ] Add test with different characters that require JSON escaping
 - [ ] Test with non-self-signed certificates, because [Chrome doesn't cache when using self-signed certs](https://www.sitepoint.com/solve-caching-conundrums/)
 - [ ] refactor remaining endpoints to use link headers and remove hal
 - [ ] remove dead code
 - [ ] Stablise API
 - [ ] Fix API Docs
 - [ ] Performance test with real world dataset 
 - [ ] release as zip/tarball with example properties
 - [ ] Add authentication
 - [ ] Modify Perf test to use latency per event rather than latency per page
 - [ ] Compare performance to [Kafka](https://engineering.linkedin.com/kafka/benchmarking-apache-kafka-2-million-writes-second-three-cheap-machines)
 - [ ] Add Circuit Breaker
 - [ ] Add logic to calculate optimal page size
 - [ ] Switch to async io
 - [ ] Add support for more databases
 - [ ] Add logic to create triggers or use [change data capture|https://github.com/shyiko/mysql-binlog-connector-java] to allow ryvrs from non-event based tables
 - [ ] Add support for more non-DB datasources
 - [ ] Add client library
 - [ ] decouple rest ryvr page size and UI page size
 - [ ] Add change notification service
