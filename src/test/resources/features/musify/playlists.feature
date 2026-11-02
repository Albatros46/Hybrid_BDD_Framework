@musify
Feature: Musify playlists, checked in the browser and through the API
  The Musify UI is a React app on top of the Spotify Clone API. Data is prepared through the API
  (fast, exact), the behaviour is driven in the browser (what the user does), and the result is
  checked where it matters: on the page or in the API.

  Background:
    Given a Musify listener "Anna"

  @hybrid @smoke
  Scenario: A playlist prepared through the API is shown correctly in the browser
    Given "Anna" has a private playlist "Road Trip" with the songs:
      | Morning Rain |
      | Night Drive  |
    When "Anna" opens her library in the browser
    Then the library shows "Road Trip" as "Private · Anna"
    When "Anna" opens the playlist "Road Trip" in the browser
    Then the playlist page shows "Private playlist", "Road Trip" and "Anna · 2 songs"
    And the songs are listed in this order:
      | Morning Rain |
      | Night Drive  |

  @hybrid
  Scenario: A public playlist created in the browser is listed by the API
    When "Anna" creates the public playlist "Summer Hits" in her library
    Then the API lists "Summer Hits" among the public playlists, owned by "Anna"

  @hybrid
  Scenario: Removing a song in the browser renumbers the songs in the API
    Given "Anna" has a private playlist "Focus" with the songs:
      | Blue Hour    |
      | Deep Work    |
      | Last Light   |
    When "Anna" opens the playlist "Focus" in the browser
    And she removes "Deep Work" from the playlist
    Then the API returns the playlist "Focus" to "Anna" with the songs:
      | position | title      |
      | 1        | Blue Hour  |
      | 2        | Last Light |

  @hybrid
  Scenario: A playlist made public in the browser can be read by another user
    Given a Musify listener "Bob"
    And "Anna" has a private playlist "Jazz Nights" with the songs:
      | Slow Smoke |
    And the API answers 404 to "Bob" for the playlist "Jazz Nights"
    When "Anna" opens the playlist "Jazz Nights" in the browser
    And she makes the playlist public
    Then "Bob" can read the playlist "Jazz Nights" through the API

  @hybrid
  Scenario: A playlist deleted in the browser is gone in the API
    Given "Anna" has a private playlist "Old Stuff" with the songs:
      | Dusty Tape |
    When "Anna" opens the playlist "Old Stuff" in the browser
    And she deletes the playlist and confirms
    Then the API answers 404 to "Anna" for the playlist "Old Stuff"

  @hybrid @security
  Scenario: Another user cannot open a private playlist by its address
    Given a Musify listener "Bob"
    And "Anna" has a private playlist "Diary" with the songs:
      | Secret Song |
    When "Bob" opens the playlist "Diary" in the browser
    Then the browser shows the error that the playlist "Diary" was not found

  @ui
  Scenario: A wrong password is rejected on the login page
    When "Anna" logs in on the Musify login page with a wrong password
    Then the Musify login page shows "Invalid email or password"
