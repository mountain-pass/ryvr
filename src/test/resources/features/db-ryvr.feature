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

  Scenario: Find Ryvr in Collection
    Given the "transactions" table has the following events
      | id | account | description    | amount  |
      |  0 | 7786543 | ATM Withdrawal | -200.00 |
    And a database ryvr with the following configuration
      | name      | transactions                                                                          |
      | query     | select `id`, `account`, `description`, `amount` from `transactions` ORDER BY `id` ASC |
      | page size |                                                                                    10 |
    When the ryvrs list is retrieved
    Then the count of ryvrs will be 1
    Then the ryvrs list will contain the following entries
      | transactions |

  Scenario: Find Ryvr in Collection - Direct
    Given the "transactions" table has the following events
      | id | account | description    | amount  |
      |  0 | 7786543 | ATM Withdrawal | -200.00 |
    And a database ryvr with the following configuration
      | name      | transactions                                                                          |
      | query     | select `id`, `account`, `description`, `amount` from `transactions` ORDER BY `id` ASC |
      | page size |                                                                                    10 |
    When the ryvrs list is retrieved directly
    Then the count of ryvrs will be 1
    Then the ryvrs list will contain the following entries
      | transactions |

  Scenario: Get Ryvr That Doesnt Exist - Links
    Given the "transactions" table has the following events
      | id | account | description    | amount  |
      |  0 | 7786543 | ATM Withdrawal | -200.00 |
    And a database ryvr with the following configuration
      | name      | transactions                                                                          |
      | query     | select `id`, `account`, `description`, `amount` from `transactions` ORDER BY `id` ASC |
      | page size |                                                                                    10 |
    When the "doesNotExist" ryvr is retrieved
    Then the ryvr will not be found

  Scenario: Get Ryvr That Doesnt Exist - Direct
    Given the "transactions" table has the following events
      | id | account | description    | amount  |
      |  0 | 7786543 | ATM Withdrawal | -200.00 |
    And a database ryvr with the following configuration
      | name      | transactions                                                                          |
      | query     | select `id`, `account`, `description`, `amount` from `transactions` ORDER BY `id` ASC |
      | page size |                                                                                    10 |
    When the "doesNotExist" ryvr is retrieved directly
    Then the ryvr will not be found

  Scenario: Get Ryvr That Has Been Deleted
    Given the "transactions" table has the following events
      | id | account | description    | amount  |
      |  0 | 7786543 | ATM Withdrawal | -200.00 |
    And a database ryvr with the following configuration
      | name      | transactions                                                                          |
      | query     | select `id`, `account`, `description`, `amount` from `transactions` ORDER BY `id` ASC |
      | page size |                                                                                    10 |
    When the ryvrs list is retrieved
    And the "transactions" rvyr is deleted
    And the "transactions" ryvr is retrieved
    Then the ryvr will not be found

  Scenario: Get Ryvr Record That Doesnt Exist - minus 1
    Given the "transactions" table has the following events
      | id | account | description    | amount  |
      |  0 | 7786543 | ATM Withdrawal | -200.00 |
    And a database ryvr with the following configuration
      | name      | transactions                                                                          |
      | query     | select `id`, `account`, `description`, `amount` from `transactions` ORDER BY `id` ASC |
      | page size |                                                                                    10 |
    When -1th record of the "transactions" ryvr is retrieved
    Then the record will not be found

  Scenario: Get Ryvr Page That Doesnt Exist - 0th page
    Given the "transactions" table has the following events
      | id | account | description    | amount  |
      |  0 | 7786543 | ATM Withdrawal | -200.00 |
    And a database ryvr with the following configuration
      | name      | transactions                                                                          |
      | query     | select `id`, `account`, `description`, `amount` from `transactions` ORDER BY `id` ASC |
      | page size |                                                                                    10 |
    When 0th page of the "transactions" ryvr is retrieved
    Then the page will not be found

  Scenario: Get Ryvr Page That Doesnt Exist - minus 1th page
    Given the "transactions" table has the following events
      | id | account | description    | amount  |
      |  0 | 7786543 | ATM Withdrawal | -200.00 |
    And a database ryvr with the following configuration
      | name      | transactions                                                                          |
      | query     | select `id`, `account`, `description`, `amount` from `transactions` ORDER BY `id` ASC |
      | page size |                                                                                    10 |
    When -1th page of the "transactions" ryvr is retrieved
    Then the page will not be found

  Scenario: Get Ryvr - Single Record
    Given the "transactions" table has the following events
      | id | account | description    | amount  |
      |  0 | 7786543 | ATM Withdrawal | -200.00 |
    And a database ryvr with the following configuration
      | name      | transactions                                                                          |
      | query     | select `id`, `account`, `description`, `amount` from `transactions` ORDER BY `id` ASC |
      | page size |                                                                                    10 |
    When the "transactions" ryvr is retrieved
    Then it will contain
      | id | account | description    | amount  |
      |  0 | 7786543 | ATM Withdrawal | -200.00 |

  Scenario: Get Ryvr - Empty
    Given the "transactions" table has the following events
      | id | account | description | amount |
    And a database ryvr with the following configuration
      | name      | transactions                                                                          |
      | query     | select `id`, `account`, `description`, `amount` from `transactions` ORDER BY `id` ASC |
      | page size |                                                                                    10 |
    When the "transactions" ryvr is retrieved
    Then it will contain
      | id | account | description | amount |

  Scenario: Get Ryvr - Multiple Transactions
    Given the "transactions" table has the following events
      | id | account | description    | amount |
      |  0 | 7786543 | ATM Withdrawal | -10.00 |
      |  1 | 7786543 | ATM Withdrawal | -20.00 |
      |  2 | 7786543 | ATM Withdrawal | -30.00 |
      |  3 | 7786543 | ATM Withdrawal | -40.00 |
      |  4 | 7786543 | ATM Withdrawal | -50.00 |
      |  5 | 7786543 | ATM Withdrawal | -60.00 |
      |  6 | 7786543 | ATM Withdrawal | -70.00 |
    And a database ryvr with the following configuration
      | name      | transactions                                                                          |
      | query     | select `id`, `account`, `description`, `amount` from `transactions` ORDER BY `id` ASC |
      | page size |                                                                                    10 |
    When the "transactions" ryvr is retrieved
    Then it will contain
      | id | account | description    | amount |
      |  0 | 7786543 | ATM Withdrawal | -10.00 |
      |  1 | 7786543 | ATM Withdrawal | -20.00 |
      |  2 | 7786543 | ATM Withdrawal | -30.00 |
      |  3 | 7786543 | ATM Withdrawal | -40.00 |
      |  4 | 7786543 | ATM Withdrawal | -50.00 |
      |  5 | 7786543 | ATM Withdrawal | -60.00 |
      |  6 | 7786543 | ATM Withdrawal | -70.00 |
