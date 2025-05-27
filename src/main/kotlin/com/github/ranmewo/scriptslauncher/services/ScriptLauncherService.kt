package com.github.ranmewo.scriptslauncher.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.xmlb.annotations.XCollection
import com.github.ranmewo.scriptslauncher.model.ScriptConfiguration

@Service(Service.Level.APP)
@State(
    name = "ScriptLauncherSettings",
    storages = [Storage("scriptLauncherSettings.xml")]
)
class ScriptLauncherService : PersistentStateComponent<ScriptLauncherService.State> {

    private val logger = thisLogger()
    
    private var state = State()
    
    class State {
        @get:XCollection(elementName = "script")
        var scripts: MutableList<ScriptConfiguration> = mutableListOf()
    }

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    fun addScript(script: ScriptConfiguration) {
        state.scripts.add(script)
        logger.info("Added script: ${script.name}")
        // Trigger state save
        ApplicationManager.getApplication().saveSettings()
    }

    fun removeScript(scriptId: String) {
        state.scripts.removeIf { it.id == scriptId }
        logger.info("Removed script with id: $scriptId")
        // Trigger state save
        ApplicationManager.getApplication().saveSettings()
    }

    fun updateScript(script: ScriptConfiguration) {
        val index = state.scripts.indexOfFirst { it.id == script.id }
        if (index != -1) {
            state.scripts[index] = script
            logger.info("Updated script: ${script.name}")
            // Trigger state save
            ApplicationManager.getApplication().saveSettings()
        }
    }

    fun getScript(scriptId: String): ScriptConfiguration? {
        return state.scripts.find { it.id == scriptId }
    }

    fun getAllScripts(): List<ScriptConfiguration> {
        return state.scripts.toList()
    }

    fun getStartupScripts(): List<ScriptConfiguration> {
        return state.scripts.filter { it.isStartupScript && it.isEnabled }
    }

    fun getNonStartupScripts(): List<ScriptConfiguration> {
        return state.scripts.filter { !it.isStartupScript }
    }

    fun executeStartupScripts() {
        val startupScripts = getStartupScripts()
        logger.info("Executing ${startupScripts.size} startup scripts")
        
        startupScripts.forEach { script ->
            logger.info("Executing startup script: ${script.name}")
            try {
                executeScript(script)
            } catch (e: Exception) {
                logger.error("Startup script failed: ${script.name}", e)
            }
        }
    }

    fun executeScript(script: ScriptConfiguration): String {
        if (!script.isValid()) {
            val errorMessage = "Cannot execute invalid script: ${script.name}"
            logger.warn(errorMessage)
            if (!script.ignoreErrors) {
                throw IllegalArgumentException(errorMessage)
            }
            return "Script validation failed (ignored): ${script.name}"
        }

        try {
            logger.info("Executing script: ${script.name}")
            val processBuilder = ProcessBuilder()
            
            // Set working directory first if specified
            var workDir: java.io.File? = null
            if (script.workingDirectory.isNotBlank()) {
                workDir = java.io.File(script.workingDirectory)
                if (workDir.exists() && workDir.isDirectory) {
                    processBuilder.directory(workDir)
                    logger.info("Set working directory to: ${workDir.absolutePath}")
                } else {
                    logger.warn("Working directory does not exist: ${script.workingDirectory}")
                }
            }
            
            // Parse command - handle both simple commands and complex ones
            val commands = if (script.command.contains(" ")) {
                // For complex commands, use shell
                when {
                    System.getProperty("os.name").lowercase().contains("windows") -> {
                        listOf("cmd", "/c", script.command)
                    }
                    else -> {
                        listOf("sh", "-c", script.command)
                    }
                }
            } else {
                // For simple executable paths, check if it's relative and working directory is set
                if (workDir != null && !java.io.File(script.command).isAbsolute) {
                    val executablePath = java.io.File(workDir, script.command)
                    if (executablePath.exists()) {
                        listOf(executablePath.absolutePath)
                    } else {
                        logger.warn("Executable not found at: ${executablePath.absolutePath}")
                        listOf(script.command)
                    }
                } else {
                    listOf(script.command)
                }
            }
            
            processBuilder.command(commands)
            logger.info("Executing command: ${commands.joinToString(" ")}")
            
            // Start the process
            val process = processBuilder.start()
            logger.info("Successfully started script: ${script.name}")
            
            // Return success message
            return "Script '${script.name}' executed successfully"
            
        } catch (e: Exception) {
            val errorMessage = "Failed to execute script '${script.name}': ${e.message}"
            logger.error(errorMessage, e)
            if (!script.ignoreErrors) {
                // Re-throw with user-friendly message
                throw RuntimeException(errorMessage, e)
            }
            return "Script execution failed (ignored): ${script.name} - ${e.message}"
        }
    }
}
