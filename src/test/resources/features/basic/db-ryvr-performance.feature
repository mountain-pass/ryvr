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
    And it has 1000000 events
    And a database ryvr with the following configuration
      | name                 | transactions                                                                          |
      | query                | select `id`, `account`, `description`, `amount` from `transactions` ORDER BY `id` ASC |
      | exposed page size    | 4096                                                                                  |
      | underlying page size | 262144                                                                                |
    And the client is authenticated

  @wip
  Scenario: Get Ryvr First Hit
    When the "transactions" ryvr is retrieved
    And all the events are retrieved
    And the event retrieval throughput should be at least 9.5MB per s
    Then the event retrieval rate should be at least 130000TPS
    And the median record should load within 3.0µs
    And 95% of the records should each load within 6.0µs
    And 99% of the records should each load within 15.0µs
    And 100% of the records should each load within 550ms

  Scenario: Get Ryvr Multiple Hits
    When the "transactions" ryvr is retrieved
    And all the events are retrieved
    And all the events are retrieved again
    Then on the second retrieve, the average page should be loaded within 0.4ms
    And 95% of the pages should be loaded within 0.65ms
    And 100% of the pages should be loaded within 1.6ms
    And the event retrieval throughput should be at least 900MB/s
    And the event retrieval rate should be at least 20MTPS

  Scenario: Get Ryvr Multiple Consumers
    When the "transactions" ryvr is retrieved
    And all the events are retrieved
    And all the events are retrieved again
    And all the events are retrieved by 500 consumers
    Then the average page should be loaded within 0.2ms
    And 95% of the pages should be loaded within 0.3ms
    And 100% of the pages should be loaded within 5000ms
    And the minmium event retrieval throughput should be at least 2.7MB/s
    And the average event retrieval throughput should be at least 80MB/s
    And the peak event retrieval throughput should be at least 1.8GB/s
    And the minimum event retrieval rate should be at least 60000TPS
    And the average event retrieval rate should be at least 2MTPS
    And the peak event retrieval rate should be at least 40MTPS

