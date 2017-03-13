Feature: Root
    In order understand what services are available to me when interacting with the server
    As a user
    I want to get a list of services when accessing the server

Scenario: Get Root
    When a request is made to the server's base URL
    Then the root entity will contain a link to the api-docs
    Then the root entity will contain a link to the ryvrs

