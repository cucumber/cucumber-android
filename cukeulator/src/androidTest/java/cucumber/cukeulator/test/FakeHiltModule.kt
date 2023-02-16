package cucumber.cukeulator.test

import cucumber.cukeulator.GreetingService
import cucumber.cukeulator.HiltModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

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