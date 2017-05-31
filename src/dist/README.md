## Ryvr 
_**- Let Your Data Flow**_

### Getting Started

1. Edit `etc/application.yml` and configure your data sources. You'll find an example data source configuration commented out.

2. On Linux, OS X and Unix based systems, run `bin/ryvr`. On Windows, run `bin/ryvr.bat`

3. Direct you REST client or browser to `https://localhost:8443`

4. Application logs and Tomcat access logs can be found in `var/logs`

### Configuration

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
| page-size | Specifies how many records to include in each page. You will need to tune this. Try 128 to start with. |
| catalog | The name of the schema/database your table/view is in |
| table | The name of the table containing the records |
| ordered-by | The name of the column in the table, which can be used for sorting the records into order. |

These properties can be set using the `etc/application.yml` file.

##### Example

    au.com.mountainpass.ryvr:
      data-sources:
        - url: jdbc:mysql://localhost
          username: dbuser
          password: dbpass
          ryvrs:
            transactions:
              page-size: 128
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

By default, if Ryvr is unable to find the certificate specified by `server.ssl.key-alias`, it will generate one for you. The type of certificate generated is controlled via the `au.com.mountainpass.ryvr.ssl.genCert` application property, which defaults to `selfSigned`.

Currently, `selfSigned` is the only type of certificate generation implemented. `au.com.mountainpass.ryvr.ssl.genCert` can be set to false, which will disable certificate generation. 

Ryvr will shutdown on startup if `au.com.mountainpass.ryvr.ssl.genCert` is false and it is unable to find the certificate specified by `server.ssl.key-alias`.

When generating a certificate, Ryvr will set the hostname to the value of the `au.com.mountainpass.ryvr.ssl.hostname` application property. `au.com.mountainpass.ryvr.ssl.hostname` defaults to `localhost`.
