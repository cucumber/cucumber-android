package cucumber.cukeulator.test

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import cucumber.cukeulator.GreetingService
import javax.inject.Inject

abstract class BaseKotlinSteps: SemanticsNodeInteractionsProvider {

    @Inject
    lateinit var composeRuleHolder: ComposeRuleHolder

    @Inject
    lateinit var greetingService: GreetingService

    override fun onAllNodes(matcher: SemanticsMatcher, useUnmergedTree: Boolean): SemanticsNodeInteractionCollection {
        return composeRuleHolder.composeRule.onAllNodes(matcher, useUnmergedTree)
    }

    override fun onNode(matcher: SemanticsMatcher, useUnmergedTree: Boolean): SemanticsNodeInteraction {
        return composeRuleHolder.composeRule.onNode(matcher, useUnmergedTree)
    }
}