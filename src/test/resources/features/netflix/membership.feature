@netflix
Feature: Netflix membership: sign-up, e-mail verification and access
  A new member must confirm her e-mail address before the first login. A deactivated member
  must lose access. The e-mails are read from Mailpit, the SMTP catcher the app sends to.

  @hybrid @mail @smoke
  Scenario: Sign-up in the browser, verification link from the e-mail, first login
    When "Nina" signs up on the Netflix sign-up page
    Then "Nina" receives a verification e-mail with a link to the Netflix app
    When she opens the verification link in the browser
    Then the page confirms "E-Mail-Adresse bestätigt! Sie können sich jetzt anmelden."
    When "Nina" logs in on the Netflix login page
    Then the browser shows the film overview

  @ui @mail
  Scenario: Login before the e-mail is verified is refused, with an offer to resend the e-mail
    Given "Nina" has signed up for Netflix but not verified her e-mail
    When "Nina" logs in on the Netflix login page
    Then the page shows the error "Bitte bestätigen Sie zuerst Ihre E-Mail-Adresse (Link in der Bestätigungs-E-Mail)."
    And the login page offers to send the verification e-mail again

  @hybrid @mail
  Scenario: A verification link works only once
    When "Nina" signs up on the Netflix sign-up page
    And "Nina" receives a verification e-mail with a link to the Netflix app
    And she opens the verification link in the browser
    And she opens the verification link in the browser
    Then the page shows the error "Der Link ist ungültig oder abgelaufen."

  @api @mail @security
  Scenario: A deactivated member cannot log in any more
    Given a verified Netflix member "Nina"
    When the admin deactivates "Nina"
    And "Nina" tries to log in to Netflix through the API
    Then the response status is 403
    And the error message is "Your account has been deactivated. Please contact support for assistance."

  @api @mail @security @known-bug
  Scenario: NF-01 — a deactivated member's existing token must stop working
    Given a verified Netflix member "Nina"
    When the admin deactivates "Nina"
    Then the old token of "Nina" is rejected with status 401

  @api @known-bug
  Scenario: NF-02 — an invalid user id is a client error, not a server error
    When the admin changes the status of the user with the id "abc"
    Then the response status is 400
