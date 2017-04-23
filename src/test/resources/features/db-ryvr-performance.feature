@performance
Feature: DB Ryvr
    In order create projections from the events that have happened in a DB
    As a user
    I want to get a paginated list of events from the DB

Background:
    Given a database "TEST_DB" 
    And it has a table "TRANSACTIONS" with the following structure
      | ID  | ACCOUNT | DESCRIPTION    | AMOUNT   |
    And it has 10000 events
    And a "transactions" ryvr for "TEST_DB" for table "TRANSACTIONS" ordered by "ID"
  

Scenario: Get Ryvr - Multiple Pages - Current Page
    When the "transactions" ryvr is retrieved
    And all the events are retrieved
    Then 95% of the pages should be loaded within 30ms
    And 100% of the pages should be loaded within 500ms
