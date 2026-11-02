@netflix @mail
Feature: Films and "Meine Liste"
  Films are prepared by the admin through the API; the member works in the browser;
  the result is checked in the API and on the page.

  Background:
    Given a verified Netflix member "Nina"
    And the admin has published the film "Silent Harbor"

  @hybrid @smoke
  Scenario: A film added to the list in the browser is on the list in the API
    When "Nina" searches for the film "Silent Harbor" in the browser
    Then the search result shows the film "Silent Harbor"
    When she adds "Silent Harbor" to her list in the browser
    Then the API has "Silent Harbor" on the list of "Nina"
    When "Nina" opens her list in the browser
    Then her list in the browser shows "Silent Harbor"

  @hybrid
  Scenario: An unpublished film disappears for members, in the API and in the browser
    When "Nina" searches for the film "Silent Harbor" in the browser
    Then the search result shows the film "Silent Harbor"
    When the admin unpublishes the film "Silent Harbor"
    Then the API answers 404 to "Nina" for the film "Silent Harbor"
    And the search in the browser finds nothing for "Silent Harbor"
