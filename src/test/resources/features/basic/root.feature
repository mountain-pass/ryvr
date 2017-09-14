@basic
Feature: Root
    In order understand what services are available to me when interacting with the server
    As a user
    I want to get a list of services when accessing the server

Scenario: Get Root
    Given the client is authenticated
    When a request is made to the server's base URL
    Then the root entity will contain a link to the api-docs
    And the root entity will contain a link to the ryvrs
    And the root entity will have an application name of "ryvr"
    

