package com.github.ranmewo.scriptslauncher.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.github.ranmewo.scriptslauncher.services.ScriptLauncherService
import com.github.ranmewo.scriptslauncher.ui.ScriptConfigurationDialog

class AddScriptAction : AnAction("Add Script", "Add a new script configuration", null) {

    override fun actionPerformed(e: AnActionEvent) {
        val service = ApplicationManager.getApplication().service<ScriptLauncherService>()
        
        val dialog = ScriptConfigurationDialog()
        if (dialog.showAndGet()) {
            val script = dialog.getScriptConfiguration()
            service.addScript(script)
        }
    }
}
