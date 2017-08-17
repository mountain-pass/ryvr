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
    And the "transactions" table has the following events
      | id | account | description    | amount  |
      |  0 | 7786543 | ATM Withdrawal |  -10.00 |
      |  1 | 7786543 | ATM Withdrawal |  -20.00 |
      |  2 | 7786543 | ATM Withdrawal |  -30.00 |
      |  3 | 7786543 | ATM Withdrawal |  -40.00 |
      |  4 | 7786543 | ATM Withdrawal |  -50.00 |
      |  5 | 7786543 | ATM Withdrawal |  -60.00 |
      |  6 | 7786543 | ATM Withdrawal |  -70.00 |
      |  7 | 7786543 | ATM Withdrawal |  -80.00 |
      |  8 | 7786543 | ATM Withdrawal |  -90.00 |
      |  9 | 7786543 | ATM Withdrawal | -100.00 |
      | 10 | 7786543 | ATM Withdrawal | -110.00 |
      | 11 | 7786543 | ATM Withdrawal | -120.00 |
      | 12 | 7786543 | ATM Withdrawal | -130.00 |
    And a database ryvr with the following configuration
      | name      | transactions                                                                          |
      | query     | select `id`, `account`, `description`, `amount` from `transactions` ORDER BY `id` ASC |
      | page size |                                                                                    10 |

  Scenario: Get Ryvr - Multiple Pages
    When the "transactions" ryvr is retrieved
    Then it will contain
      | id | account | description    | amount  |
      |  0 | 7786543 | ATM Withdrawal |  -10.00 |
      |  1 | 7786543 | ATM Withdrawal |  -20.00 |
      |  2 | 7786543 | ATM Withdrawal |  -30.00 |
      |  3 | 7786543 | ATM Withdrawal |  -40.00 |
      |  4 | 7786543 | ATM Withdrawal |  -50.00 |
      |  5 | 7786543 | ATM Withdrawal |  -60.00 |
      |  6 | 7786543 | ATM Withdrawal |  -70.00 |
      |  7 | 7786543 | ATM Withdrawal |  -80.00 |
      |  8 | 7786543 | ATM Withdrawal |  -90.00 |
      |  9 | 7786543 | ATM Withdrawal | -100.00 |
      | 10 | 7786543 | ATM Withdrawal | -110.00 |
      | 11 | 7786543 | ATM Withdrawal | -120.00 |
      | 12 | 7786543 | ATM Withdrawal | -130.00 |
