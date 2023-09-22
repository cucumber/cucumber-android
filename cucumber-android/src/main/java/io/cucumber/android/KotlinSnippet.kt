package io.cucumber.android

import io.cucumber.core.backend.Snippet
import java.lang.reflect.Type
import java.text.MessageFormat

internal class KotlinSnippet : Snippet {
    override fun tableHint(): String = ""
    override fun arguments(arguments: MutableMap<String, Type>): String {
        return arguments.entries.joinToString { "${(it.value as? Class<*>)?.simpleName ?: it.value.toString()} ${it.key}" }
    }

    override fun escapePattern(pattern: String): String {
        return pattern.replace("\\", "\\\\").replace("\"", "\\\"")
    }

    override fun template(): MessageFormat {
        return MessageFormat("@{0}(\"{1}\")\n fun {2}({3} {5}) \'{\'\n    // {4}\n throw PendingException()\n\'}\'\n")
    }

}