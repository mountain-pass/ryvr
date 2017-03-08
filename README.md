## Ryvr

Provides extremely fast access to your data, in fine precise slices


The project uses [Gradle](https://gradle.org/) for its build system and you can build the project by running:

	./gradlew build

You can also run the app using the [Spring Boot Gradle Plugin](http://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-gradle-plugin.html) like so: 

	./gradlew bootRun

The swagger definition will be available at the following URI:

 - [https://localhost:8443/api-docs/](http://localhost:8443/api-docs/)

The Spring Boot Actuator end-points are available here:

- [https://localhost:8443/system/info](http://localhost:8443/system/info)
- [https://localhost:8080/system/env](http://localhost:8443/system/env)

Unimplemented controllers and methods are available here:

 - [https://localhost:8443/debug.json](http://localhost:8080/debug.json)

Have fun!

