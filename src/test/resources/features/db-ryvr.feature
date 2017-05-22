Feature: DB Ryvr
    In order create projections from the events that have happened in a DB
    As a user
    I want to get a paginated list of events from the DB

Scenario: Find Ryvr in Collection
    Given a database "test_db" 
    And it has a table "transactions" with the following events
      | id | account | description    | amount  |
      | 0  | 7786543 | ATM Withdrawal | -200.00 | 
    And a database ryvr with the following configuration
      | name        | transactions  |
      | database    | test_db       |
      | table       | transactions  |
      | ordered by  | id            |
      | page size   | 10            |
    When the ryvrs list is retrieved
    Then the count of ryvrs will be 1
    Then the ryvrs list will contain the following entries
      | transactions |
    

Scenario: Get Ryvr
    Given a database "test_db" 
    And it has a table "transactions" with the following events
      | id | account | description    | amount  |
      | 0  | 7786543 | ATM Withdrawal | -200.00 | 
    And a database ryvr with the following configuration
      | name        | transactions  |
      | database    | test_db       |
      | table       | transactions  |
      | ordered by  | id            |
      | page size   | 10            |
    When the "transactions" ryvr is retrieved
    Then it will contain
      | id | account | description    | amount  |
      | 0  | 7786543 | ATM Withdrawal | -200.00 | 
    And it will have the following links
      | self    |
      | first   |
      | last    |
      | current |
    And it will *not* have the following links
      | prev    |
      | next    |

Scenario: Get Ryvr - Multiple Transactions
    Given a database "test_db" 
    And it has a table "transactions" with the following events
      | id  | account | description    | amount  |
      | 0   | 7786543 | ATM Withdrawal | -10.00  | 
      | 1   | 7786543 | ATM Withdrawal | -20.00  | 
      | 2   | 7786543 | ATM Withdrawal | -30.00  | 
      | 3   | 7786543 | ATM Withdrawal | -40.00  | 
      | 4   | 7786543 | ATM Withdrawal | -50.00  | 
      | 5   | 7786543 | ATM Withdrawal | -60.00  | 
      | 6   | 7786543 | ATM Withdrawal | -70.00  | 
    And a database ryvr with the following configuration
      | name        | transactions  |
      | database    | test_db       |
      | table       | transactions  |
      | ordered by  | id            |
      | page size   | 10            |
    When the "transactions" ryvr is retrieved
    Then it will contain
      | id  | account | description    | amount  |
      | 0   | 7786543 | ATM Withdrawal | -10.00  | 
      | 1   | 7786543 | ATM Withdrawal | -20.00  | 
      | 2   | 7786543 | ATM Withdrawal | -30.00  | 
      | 3   | 7786543 | ATM Withdrawal | -40.00  | 
      | 4   | 7786543 | ATM Withdrawal | -50.00  | 
      | 5   | 7786543 | ATM Withdrawal | -60.00  | 
      | 6   | 7786543 | ATM Withdrawal | -70.00  | 
    And it will have the following links
      | self    |
      | first   |
      | last    |
      | current |
    And it will *not* have the following links
      | prev    |
      | next    |

            