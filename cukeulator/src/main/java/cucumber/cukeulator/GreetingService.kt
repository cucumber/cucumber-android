package cucumber.cukeulator

import android.content.Context

fun interface GreetingService {

    fun greeting(context: Context):String
}