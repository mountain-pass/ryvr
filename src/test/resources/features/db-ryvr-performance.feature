@performance
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
    And it has 100000 events
    And a database ryvr with the following configuration
      | name      | transactions                                                                          |
      | query     | select `id`, `account`, `description`, `amount` from `transactions` ORDER BY `id` ASC |
      | page size |                                                                                  8192 |

  Scenario: Get Ryvr First Hit
    When the "transactions" ryvr is retrieved
    And all the events are retrieved
    Then the average page should be loaded within 57ms
    And 95% of the pages should be loaded within 75ms
    And 100% of the pages should be loaded within 235ms
    And the event retrieval throughput should be at least 4.7MB/s
    And the event retrieval rate should be at least 116000TPS

  Scenario: Get Ryvr Multiple Hits
    When the "transactions" ryvr is retrieved
    And all the events are retrieved
    And all the events are retrieved again
    Then on the second retrieve, the average page should be loaded within 0.4ms
    And 95% of the pages should be loaded within 0.65ms
    And 100% of the pages should be loaded within 0.7ms
    And the event retrieval throughput should be at least 900MB/s
    And the event retrieval rate should be at least 20MTPS

  @current
  Scenario: Get Ryvr Multiple Consumers
    When the "transactions" ryvr is retrieved
    And all the events are retrieved
    And all the events are retrieved again
    And all the events are retrieved by 1000 consumers
    Then the average page should be loaded within 0.1ms
    And 95% of the pages should be loaded within 0.2ms
    And 100% of the pages should be loaded within 1050ms
    And the minmium event retrieval throughput should be at least 3.5MB/s
    And the average event retrieval throughput should be at least 160MB/s
    And the peak event retrieval throughput should be at least 2.1GB/s
    And the minimum event retrieval rate should be at least 90000TPS
    And the average event retrieval rate should be at least 4MTPS
    And the peak event retrieval rate should be at least 55MTPS
    