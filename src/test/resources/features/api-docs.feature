Feature: Api Docs
    In order understand how to interact with Ryvr
    As a user
    I want to be able to get documentation for the services that Ryvr provides

Scenario: Get Api Docs
    When a request is made for the API Docs
    Then the API Docs will contain an operation for getting the API Docs
