@mobile @Regression
Feature: Catalog

  Scenario: Cart is available after login
    Given I am on the login screen
    When I log in with the configured credentials
    Then I see the products catalog
    And the cart control is visible
