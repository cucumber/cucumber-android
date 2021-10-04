package cucumber.cukeulator.test

import androidx.test.platform.app.InstrumentationRegistry
import cucumber.cukeulator.GreetingService
import cucumber.cukeulator.HiltModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.cucumber.java.Before
import io.cucumber.junit.WithJunitRule
import org.junit.Assert
import org.junit.Rule
import javax.inject.Inject
import javax.inject.Singleton

@WithJunitRule(useAsTestClassInDescription = true)
@HiltAndroidTest
class HiltRuleHolder {

    @Rule(order = 0)
    @JvmField
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var greetingService: GreetingService

    @Before
    fun init() {
        hiltRule.inject()
        Assert.assertEquals("test hello world",greetingService.greeting(InstrumentationRegistry.getInstrumentation().targetContext))
    }

}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [HiltModule::class]
)
class FakeHiltModule {

    @Singleton
    @Provides
    fun service(): GreetingService = GreetingService { "test hello world" }
}
