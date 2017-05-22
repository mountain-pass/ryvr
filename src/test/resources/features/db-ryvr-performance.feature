@performance
Feature: DB Ryvr
    In order create projections from the events that have happened in a DB
    As a user
    I want to get a paginated list of events from the DB

Background:
    Given a database "test_db" 
    And it has a table "transactions" with the following structure
      | id  | account | description    | amount   |
    And it has 100000 events
    And a database ryvr with the following configuration
      | name        | transactions  |
      | database    | test_db       |
      | table       | transactions  |
      | ordered by  | id            |
      | page size   | 2048          |
  

Scenario: Get Ryvr - Multiple Pages - Current Page
    When the "transactions" ryvr is retrieved
    And all the events are retrieved
    Then 95% of the pages should be loaded within 50ms
    And 100% of the pages should be loaded within 700ms
