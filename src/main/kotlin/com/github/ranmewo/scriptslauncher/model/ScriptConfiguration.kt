package com.github.ranmewo.scriptslauncher.model

import com.intellij.util.xmlb.annotations.Tag
import java.io.Serializable

@Tag("script")
data class ScriptConfiguration(
    var id: String = "",
    var name: String = "",
    var command: String = "",
    var workingDirectory: String = "",
    var description: String = "",
    var isEnabled: Boolean = true,
    var isStartupScript: Boolean = false,
    var ignoreErrors: Boolean = false
) : Serializable {
    
    fun isValid(): Boolean {
        return name.isNotBlank() && command.isNotBlank()
    }
    
    companion object {
        fun createEmpty(): ScriptConfiguration {
            return ScriptConfiguration(
                id = java.util.UUID.randomUUID().toString(),
                name = "",
                command = "",
                workingDirectory = "",
                description = "",
                isEnabled = true,
                isStartupScript = false,
                ignoreErrors = false
            )
        }
    }
}
