package com.github.ranmewo.scriptslauncher.startup

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.application.ApplicationManager
import com.github.ranmewo.scriptslauncher.services.ScriptLauncherService

class MyProjectActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        // Initialize the script launcher service
        val service = ApplicationManager.getApplication().service<ScriptLauncherService>()
        thisLogger().info("Scripts Launcher initialized globally for project: ${project.name}")
        thisLogger().info("Loaded ${service.getAllScripts().size} global scripts")
        
        // Execute startup scripts
        service.executeStartupScripts()
    }
}