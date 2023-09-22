package cucumber.cukeulator.test

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import io.cucumber.junit.WithJunitRule
import org.junit.Rule

@WithJunitRule("not @CustomComposable")
class ComposeRuleHolder {

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()
}

@WithJunitRule("@CustomComposable")
class CustomComposableRuleHolder {

    @get:Rule(order = 1)
    val composeRule = createComposeRule()
}