Feature: DB Ryvr
    In order create projections from the events that have happened in a DB
    As a user
    I want to get a paginated list of events from the DB

Scenario: Find Ryvr in Collection
    Given a database "TEST_DB" 
    And it has a table "TRANSACTIONS" with the following events
      | ID | ACCOUNT | DESCRIPTION    | AMOUNT  |
      | 0  | 7786543 | ATM Withdrawal | -200.00 | 
    And a "transactions" ryvr for "TEST_DB" for table "TEST_EVENTS"
    When the ryvrs list is retrieved
    Then the count of ryvrs will be 1
    Then the ryvrs list will contain the following entries
      | transactions |
    

@current
Scenario: Get Ryvr
    Given a database "TEST_DB" 
    And it has a table "TRANSACTIONS" with the following events
      | ID | ACCOUNT | DESCRIPTION    | AMOUNT  |
      | 0  | 7786543 | ATM Withdrawal | -200.00 | 
    And a "transactions" ryvr for "TEST_DB" for table "TRANSACTIONS"
    When the "transactions" ryvr is retrieved
    Then it will contain
      | ID | ACCOUNT | DESCRIPTION    | AMOUNT  |
      | 0  | 7786543 | ATM Withdrawal | -200.00 | 
    And it will have the following links
      | self    |
      | first   |
      | last    |
      | current |
    And it will *not* have the following links
      | prev    |
      | next    |
      