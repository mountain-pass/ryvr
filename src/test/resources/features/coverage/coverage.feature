@coverage
Feature: Coverage
    In order make sure I'm alerted when the code coverage falls
    As developer
    I want the build to be marked as unstable if the coverage is too low

Scenario: Coverage
    Given the "RyvrTests_Integration_Java_H2Local" test has been run
    And the "RyvrTests_Integration_Rest_H2Local" test has been run
#    And the "RyvrTests_Integration_Rest_MySqlLocal" test has been run
    And the test coverage report has been generated
    Then the "INSTRUCTION" coverage should be at least 46%
    And the "BRANCH" coverage should be at least 35%
    And the "LINE" coverage should be at least 50%
    And the "COMPLEXITY" coverage should be at least 45%
    And the "METHOD" coverage should be at least 55%
    And the "CLASS" coverage should be at least 48%
    

