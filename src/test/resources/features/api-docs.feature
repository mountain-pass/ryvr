Feature: Api Docs
    In order understand how to interact with Ryvr
    As a user
    I want to be able to get documentation for the services that Ryvr provides
    
Scenario: Get Api Docs - JSON
    When a request is made for the API Docs as "application/json"
    Then the API Docs for Ryvr will be returned
    And the API Docs will contain an operation for getting the API Docs
    
Scenario: Get Api Docs - YAML
    When a request is made for the API Docs as "application/yaml"
    Then the API Docs for Ryvr will be returned
    And the API Docs will contain an operation for getting the API Docs