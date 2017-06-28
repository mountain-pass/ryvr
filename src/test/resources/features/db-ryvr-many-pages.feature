Feature: DB Ryvr Caching
    In order create projections from the events that have happened in a DB
    As a user
    I want to get a paginated list of events from the DB

Background:
    Given a database "test_db" 
    And it has a table "transactions" with the following structure
      | id  | account | description    | amount   |
    And it has 46 events
    And a database ryvr with the following configuration
      | name        | transactions  |
      | database    | test_db       |
      | table       | transactions  |
      | ordered by  | id            |
      | page size   | 10            |

Scenario: Get Ryvr - Multiple Pages - Current Page
    When the "transactions" ryvr is retrieved
    Then it will have the following structure
      | id  | account | description    | amount   | 
    And it will have 46 events
