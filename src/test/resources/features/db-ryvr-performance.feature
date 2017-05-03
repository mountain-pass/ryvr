@performance
Feature: DB Ryvr
    In order create projections from the events that have happened in a DB
    As a user
    I want to get a paginated list of events from the DB

Background:
    Given a database "TEST_DB" 
    And it has a table "TRANSACTIONS" with the following structure
      | ID  | ACCOUNT | DESCRIPTION    | AMOUNT   |
    And it has 100000 events
    And a database ryvr with the following configuration
      | name        | transactions  |
      | database    | TEST_DB       |
      | table       | TRANSACTIONS  |
      | ordered by  | ID            |
      | page size   | 8192          |
  

Scenario: Get Ryvr - Multiple Pages - Current Page
    When the "transactions" ryvr is retrieved
    And all the events are retrieved
    Then 95% of the pages should be loaded within 50ms
    And 100% of the pages should be loaded within 700ms
