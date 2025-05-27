package com.github.ranmewo.scriptslauncher.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import com.github.ranmewo.scriptslauncher.services.ScriptLauncherService

class RunStartupScriptsAction : AnAction("Run Startup Scripts", "Execute all enabled startup scripts", null) {

    override fun actionPerformed(e: AnActionEvent) {
        val service = ApplicationManager.getApplication().service<ScriptLauncherService>()
        
        val startupScripts = service.getStartupScripts()
        if (startupScripts.isEmpty()) {
            Messages.showInfoMessage(
                "No enabled startup scripts found. Add startup scripts in the Scripts Launcher tool window or settings.",
                "No Startup Scripts"
            )
            return
        }
        
        try {
            service.executeStartupScripts()
            Messages.showInfoMessage(
                "Manually executed ${startupScripts.size} startup script(s).",
                "Startup Scripts Executed"
            )
        } catch (e: Exception) {
            Messages.showErrorDialog(
                "Error executing startup scripts:\n\n${e.message}",
                "Startup Scripts Execution Error"
            )
        }
    }
}
