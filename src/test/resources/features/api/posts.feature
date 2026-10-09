@api @Smoke
Feature: API smoke

  Demonstrates the shared ApiClient against a public JSON API. Point API_BASE_URL at your
  own service when you adopt the framework.

  Scenario: Fetch a post
    When I GET "/posts/1"
    Then the API response status is 200
    And the API response JSON field "id" equals 1
