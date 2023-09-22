Feature: Compose Hello world

  Scenario: Show hello world
    When I open compose activity
    Then "test hello world" text is presented

  Scenario Outline: Show hello world with different text
    When I open compose activity with "<text>"
    Then "<text>" text is presented

    Examples:
      | text        |
      | some text 1 |
      | some text 2 |


  @CustomComposable
  Scenario: Custom composable
    When I show custom composable
    Then custom "Custom composable" text is presented