@mobile @Smoke
Feature: Login

  The Sauce Labs Mobile Sample App opens on the login screen. Valid demo credentials are
  bob@example.com / 10203040. alice@example.com is locked out.

  Scenario: Log in with valid credentials
    Given I am on the login screen
    When I log in with the configured credentials
    Then I see the products catalog

  @Regression
  Scenario: Locked-out user sees an error
    Given I am on the login screen
    When I log in with username "alice@example.com" and password "10203040"
    Then I see a login error containing "locked out"
