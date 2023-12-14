package cucumber.cukeulator.test

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import io.cucumber.junit.WithJunitRule
import org.junit.Rule
import javax.inject.Inject
import javax.inject.Singleton

@WithJunitRule("not @CustomComposable")
@Singleton
class ComposeRuleHolder @Inject constructor() {

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()
}

@WithJunitRule("@CustomComposable")
@Singleton
class CustomComposableRuleHolder @Inject constructor(){

    @get:Rule(order = 1)
    val composeRule = createComposeRule()
}