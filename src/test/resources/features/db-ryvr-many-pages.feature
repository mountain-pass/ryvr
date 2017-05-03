Feature: DB Ryvr
    In order create projections from the events that have happened in a DB
    As a user
    I want to get a paginated list of events from the DB

Background:
    Given a database "TEST_DB" 
    And it has a table "TRANSACTIONS" with the following structure
      | ID  | ACCOUNT | DESCRIPTION    | AMOUNT   |
    And it has 46 events
    And a database ryvr with the following configuration
      | name        | transactions  |
      | database    | TEST_DB       |
      | table       | TRANSACTIONS  |
      | ordered by  | ID            |
      | page size   | 10            |
  
Scenario: Get Ryvr - Multiple Pages - Current Page
    When the "transactions" ryvr is retrieved
    Then it will have the following structure
      | ID  | ACCOUNT | DESCRIPTION    | AMOUNT   | 
    And it will have the last 6 events
    And it will have the following links
      | self    |
      | first   |
      | last    |
      | current |
      | prev    |
    And it will *not* have the following links
      | next    |

      