package cucumber.cukeulator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.google.accompanist.appcompattheme.AppCompatTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ComposeTestActivity:AppCompatActivity() {

    companion object {

        private val key = "text"

        fun create(context: Context, text:String? = null):Intent = Intent(context,ComposeTestActivity::class.java).putExtra(
            key,text)
    }

    @Inject
    lateinit var greetingService: GreetingService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppCompatTheme {
                Greeting()
            }

        }
    }

    @Composable
    private fun Greeting() {
        Text(
            text = intent.getStringExtra(key)?:greetingService.greeting(this),
            modifier = Modifier
                .padding(horizontal = dimensionResource(R.dimen.activity_horizontal_margin))
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
    }

}