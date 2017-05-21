Feature: DB Ryvr
    In order create projections from the events that have happened in a DB
    As a user
    I want to get a paginated list of events from the DB

Background:
    Given a database "TEST_DB" 
    And it has a table "TRANSACTIONS" with the following events
      | ID  | ACCOUNT | DESCRIPTION    | AMOUNT   |
      | 0   | 7786543 | ATM Withdrawal | -10.00   | 
      | 1   | 7786543 | ATM Withdrawal | -20.00   | 
      | 2   | 7786543 | ATM Withdrawal | -30.00   | 
      | 3   | 7786543 | ATM Withdrawal | -40.00   | 
      | 4   | 7786543 | ATM Withdrawal | -50.00   | 
      | 5   | 7786543 | ATM Withdrawal | -60.00   | 
      | 6   | 7786543 | ATM Withdrawal | -70.00   | 
      | 7   | 7786543 | ATM Withdrawal | -80.00   | 
      | 8   | 7786543 | ATM Withdrawal | -90.00   | 
      | 9   | 7786543 | ATM Withdrawal | -100.00  | 
      | 10  | 7786543 | ATM Withdrawal | -110.00  | 
      | 11  | 7786543 | ATM Withdrawal | -120.00  | 
      | 12  | 7786543 | ATM Withdrawal | -130.00  | 
    And a database ryvr with the following configuration
      | name        | transactions  |
      | database    | TEST_DB       |
      | table       | TRANSACTIONS  |
      | ordered by  | ID            |
      | page size   | 10            |
  

Scenario: Get Ryvr - Multiple Pages - Current Page
    When the "transactions" ryvr is retrieved
    Then it will contain
      | ID  | ACCOUNT | DESCRIPTION    | AMOUNT   |
      | 10  | 7786543 | ATM Withdrawal | -110.00  | 
      | 11  | 7786543 | ATM Withdrawal | -120.00  | 
      | 12  | 7786543 | ATM Withdrawal | -130.00  | 
    And it will have the following links
      | self    |
      | first   |
      | last    |
      | current |
      | prev    |
    And it will *not* have the following links
      | next    |

Scenario: Get Ryvr - Multiple Pages - Follow Prev Link
    When the "transactions" ryvr is retrieved
    And the previous page is requested
    Then it will contain
      | ID  | ACCOUNT | DESCRIPTION    | AMOUNT   |
      | 0   | 7786543 | ATM Withdrawal | -10.00   | 
      | 1   | 7786543 | ATM Withdrawal | -20.00   | 
      | 2   | 7786543 | ATM Withdrawal | -30.00   | 
      | 3   | 7786543 | ATM Withdrawal | -40.00   | 
      | 4   | 7786543 | ATM Withdrawal | -50.00   | 
      | 5   | 7786543 | ATM Withdrawal | -60.00   | 
      | 6   | 7786543 | ATM Withdrawal | -70.00   | 
      | 7   | 7786543 | ATM Withdrawal | -80.00   | 
      | 8   | 7786543 | ATM Withdrawal | -90.00   | 
      | 9   | 7786543 | ATM Withdrawal | -100.00  | 
    And it will have the following links
      | self    |
      | first   |
      | current |
      | next    |
    And it will *not* have the following links
      | prev    |
      | last    |
      
Scenario: Get Ryvr - Multiple Pages - Follow First Link
    When the "transactions" ryvr is retrieved
    And the first page is requested
    Then it will contain
      | ID  | ACCOUNT | DESCRIPTION    | AMOUNT   |
      | 0   | 7786543 | ATM Withdrawal | -10.00   | 
      | 1   | 7786543 | ATM Withdrawal | -20.00   | 
      | 2   | 7786543 | ATM Withdrawal | -30.00   | 
      | 3   | 7786543 | ATM Withdrawal | -40.00   | 
      | 4   | 7786543 | ATM Withdrawal | -50.00   | 
      | 5   | 7786543 | ATM Withdrawal | -60.00   | 
      | 6   | 7786543 | ATM Withdrawal | -70.00   | 
      | 7   | 7786543 | ATM Withdrawal | -80.00   | 
      | 8   | 7786543 | ATM Withdrawal | -90.00   | 
      | 9   | 7786543 | ATM Withdrawal | -100.00  | 
    And it will have the following links
      | self    |
      | first   |
      | current |
      | next    |
    And it will *not* have the following links
      | prev    |
      | last    |

Scenario: Get Ryvr - Multiple Pages - Current Page
    When the "transactions" ryvr is retrieved
    And the current page is requested
    Then it will contain
      | ID  | ACCOUNT | DESCRIPTION    | AMOUNT   |
      | 10  | 7786543 | ATM Withdrawal | -110.00  | 
      | 11  | 7786543 | ATM Withdrawal | -120.00  | 
      | 12  | 7786543 | ATM Withdrawal | -130.00  | 
    And it will have the following links
      | self    |
      | first   |
      | last    |
      | current |
      | prev    |
    And it will *not* have the following links
      | next    |

Scenario: Get Ryvr - Multiple Pages - Previous Then Current Page
    When the "transactions" ryvr is retrieved
    And the previous page is requested
    And the current page is requested
    Then it will contain
      | ID  | ACCOUNT | DESCRIPTION    | AMOUNT   |
      | 10  | 7786543 | ATM Withdrawal | -110.00  | 
      | 11  | 7786543 | ATM Withdrawal | -120.00  | 
      | 12  | 7786543 | ATM Withdrawal | -130.00  | 
    And it will have the following links
      | self    |
      | first   |
      | last    |
      | current |
      | prev    |
    And it will *not* have the following links
      | next    |   
      
Scenario: Get Ryvr - Multiple Pages - Previous Then Next Page
    When the "transactions" ryvr is retrieved
    And the previous page is requested
    And the next page is requested
    Then it will contain
      | ID  | ACCOUNT | DESCRIPTION    | AMOUNT   |
      | 10  | 7786543 | ATM Withdrawal | -110.00  | 
      | 11  | 7786543 | ATM Withdrawal | -120.00  | 
      | 12  | 7786543 | ATM Withdrawal | -130.00  | 
    And it will have the following links
      | self    |
      | first   |
      | last    |
      | current |
      | prev    |
    And it will *not* have the following links
      | next    |  


Scenario: Get Ryvr - Multiple Pages - Previous Then Self Link
    When the "transactions" ryvr is retrieved
    And the first page is requested
    And the self link is requested
    Then it will contain
      | ID  | ACCOUNT | DESCRIPTION    | AMOUNT   |
      | 0   | 7786543 | ATM Withdrawal | -10.00   | 
      | 1   | 7786543 | ATM Withdrawal | -20.00   | 
      | 2   | 7786543 | ATM Withdrawal | -30.00   | 
      | 3   | 7786543 | ATM Withdrawal | -40.00   | 
      | 4   | 7786543 | ATM Withdrawal | -50.00   | 
      | 5   | 7786543 | ATM Withdrawal | -60.00   | 
      | 6   | 7786543 | ATM Withdrawal | -70.00   | 
      | 7   | 7786543 | ATM Withdrawal | -80.00   | 
      | 8   | 7786543 | ATM Withdrawal | -90.00   | 
      | 9   | 7786543 | ATM Withdrawal | -100.00  | 
    And it will have the following links
      | self    |
      | first   |
      | current |
      | next    |
    And it will *not* have the following links
      | prev    |  
      | last    |
      