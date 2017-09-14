@basic
Feature: Ryvrs
    In order understand what ryvrs are available
    As a user
    I want to get a list of ryvrs provided by the server

Scenario: Get Ryvrs
    Given there are no ryvrs configured
    And the client is authenticated
    When the ryvrs list is retrieved
    Then the ryvrs list will be empty
    And the count of ryvrs will be 0
    

