@performance
@basic
Feature: DB Ryvr
    In order create projections from the events that have happened in a DB
    As a user
    I want to get a paginated list of events from the DB

  Background: 
    Given a database "test_db"
    And it has a table "transactions" with the following structure
      | id          | INT           |
      | account     | VARCHAR(255)  |
      | description | VARCHAR(255)  |
      | amount      | DECIMAL(19,4) |
    And a database ryvr with the following configuration
      | name      | transactions                                                                          |
      | query     | select `id`, `account`, `description`, `amount` from `transactions` ORDER BY `id` ASC |
      | page size |                                                                                   512 |
    And the client is authenticated

  # the write-read latency isn't that good at the moment.
  # This is because we set the TTL on the current/last page to 1 second, so that when there is a large
  # number of Ryvr clients, they don't smash the data source when polling for new records.
  # i.e. even if you had 10,000 clients for a Ryvr, polling every 100ms, the database will
  # still only see 1 query per second, rather than 1,000,000 queries per second.
  # Ideally in the short term, we would reduce the TTL to 100ms, but the HTTP spec (RFC2616) doesn't
  # allow subsecond TTLs.
  #
  # If you know that you will only have a small number of clients for a Ryvr, you can
  # greatly improve the write-read latency by setting `au.com.mountainpass.ryvr.cache.current-page-max-age` to `0`
  #
  # Longer term, there are a number of approaches we intend to use to improve this
  # - [Hystrix Request Collapsing](https://github.com/Netflix/Hystrix/wiki/How-it-Works#RequestCollapsing)
  #   This would allow us combine requests received within a certain time period. For instance if we
  #   collapsed all the requests within 100ms, then in the above example of 10,000 clients polling
  #   every 100ms, with a HTTP TTL of 0, the database would still only see 10 queries per second.
  #   However, we expect this will result in a mean latency of just over 50ms and a max latency
  #   of just over 100ms
  # - Notifications
  #   Some event sources provide a mechanism for alerting a registered subsriber when there are new events
  #   For instance, for MySQL event sources, the
  #   (mysql-binlog-connector-java)[https://github.com/shyiko/mysql-binlog-connector-java] library allows a
  #   client to subsribe to and receive committed change events, such as inserts, on the database
  #   A similar capability is provided by
  #   [CouchDB's Continuous Changes](http://guide.couchdb.org/draft/notifications.html#continuous) if we
  #   had a CouchDB event source, and the
  #   [WatchService API](http://docs.oracle.com/javase/tutorial/essential/io/notification.html) for file system
  #   changes if we had File bases event sources.
  #   For these sorts of event sources, we can trigger a refresh only when there is an actual change. This
  #   would allow us to remove the TTL on the current/last page without increasing the query load on the
  #   data source.
  #
  #   At the same time we would look to implement a change notification service to advise ryvr clients when there
  #   are new events.
  #
  #   With both of these in place, ryvr clients can subscribe to changes, when there is a new event in the
  #   data source they ryvr will perform and refresh and then clients will get notified that they can refresh
  #   the current/last page, allowing to receive new events with a very small latency.
  #
  #   With a large number of ryvr clients, this can result in a large deluge of requests for the current/last page
  #   each time there are new events, however since the ryvr has already been refreshed, no additional load
  #   would be applied to the data source. At the same time, we would need to make sure that Ryvr is capable of
  #   handling the large deluge of requests and if there is an issue, we'll be investigating it at that time, however
  #   load balancing across multiple ryvr instances would be the most likely solution.
  #
  #   For data sources that don't have a change notifaction mechansim, the polling can be moved to Ryvr, which would
  #   still allow us to provide a change notifications to ryvr clients (albeit less efficently).
  @current
  Scenario: Get Ryvr New Records
    When the "transactions" ryvr is retrieved
    And 4000 records are added at a rate of 200 records/s
    And all the records are retrieved while the records are added
    Then the average write-read latency should be less that 735ms
    And write-read latency for 95% of the records should be less that 1250ms
    And the maximium write-read latency should be less that 1600ms
