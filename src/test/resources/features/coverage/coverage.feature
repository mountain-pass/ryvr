@coverage
Feature: Coverage
    In order make sure I'm alerted when the code coverage falls
    As developer
    I want the build to be marked as unstable if the coverage is too low

Scenario: Coverage
    Given the "RyvrTests_Integration_Java_H2Local" test has been run
    And the "RyvrTests_Integration_Rest_H2Local" test has been run
    And the "RyvrTests_Integration_Rest_MySqlLocal" test has been run
    And the "RyvrTests_Integration_Rest_PostgresLocal" test has been run
    #And the "RyvrTests_Integration_Ui_MySqlLocal_ChromeSauceLabs" test has been run
    And the "RyvrTests_System_DistZipRun_Rest_MySqlLocal" system test has been run
    #And the "RyvrTests_System_DistZipRun_Ui_MySqlLocal_ChromeSauceLabs" system test has been run
    And the test coverage report has been generated
    Then the "INSTRUCTION" coverage should be at least 90%
    And the "BRANCH" coverage should be at least 75%
    And the "LINE" coverage should be at least 90%
    And the "COMPLEXITY" coverage should be at least 84%
    And the "METHOD" coverage should be at least 95%
    And the "CLASS" coverage should be at least 96%
    

