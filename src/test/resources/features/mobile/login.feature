@mobile @Smoke
Feature: Login

  The Sauce Labs Mobile Sample App opens on the login screen. Valid demo credentials are
  standard_user / secret_sauce. locked_out_user is locked out (swipe down on the login
  screen in the app to see the full list).

  Scenario: Log in with valid credentials
    Given I am on the login screen
    When I log in with the configured credentials
    Then I see the products catalog

  @Regression
  Scenario: Locked-out user sees an error
    Given I am on the login screen
    When I log in with username "locked_out_user" and password "secret_sauce"
    Then I see a login error containing "locked out"
