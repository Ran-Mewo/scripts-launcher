package com.github.ranmewo.scriptslauncher.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.ui.components.JBList
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.AnActionButton
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.util.ui.JBUI
import com.intellij.icons.AllIcons
import com.github.ranmewo.scriptslauncher.model.ScriptConfiguration
import com.github.ranmewo.scriptslauncher.services.ScriptLauncherService
import com.github.ranmewo.scriptslauncher.ui.ScriptConfigurationDialog
import java.awt.BorderLayout
import javax.swing.*

class ScriptLauncherConfigurable : Configurable {
    
    private val service = ApplicationManager.getApplication().service<ScriptLauncherService>()
    private val startupListModel = DefaultListModel<ScriptConfiguration>()
    private val startupScriptList = JBList(startupListModel)
    private var panel: JPanel? = null

    override fun getDisplayName(): String = "Scripts Launcher"

    override fun createComponent(): JComponent {
        if (panel == null) {
            panel = createMainPanel()
        }
        return panel!!
    }

    private fun createMainPanel(): JPanel {
        val mainPanel = JPanel(BorderLayout())
        
        // Startup Scripts Section
        val startupPanel = JPanel(BorderLayout())
        startupPanel.border = JBUI.Borders.empty(10)
        
        // Header panel with title and description
        val headerPanel = JPanel()
        headerPanel.layout = BoxLayout(headerPanel, BoxLayout.Y_AXIS)
        
        val titleLabel = JLabel("Startup Scripts")
        titleLabel.font = titleLabel.font.deriveFont(titleLabel.font.size + 2f)
        titleLabel.border = JBUI.Borders.empty(0, 0, 5, 0)
        headerPanel.add(titleLabel)
        
        val descLabel = JLabel("<html><body style='width: 400px'>Global scripts that will be executed automatically when the plugin starts or when any project opens. These are useful for setting up development environments, starting services, or performing initialization tasks across all projects.</body></html>")
        descLabel.border = JBUI.Borders.empty(0, 0, 10, 0)
        headerPanel.add(descLabel)
        
        startupPanel.add(headerPanel, BorderLayout.NORTH)
        
        setupStartupScriptsList()
        
        val toolbar = ToolbarDecorator.createDecorator(startupScriptList)
            .setAddAction { addStartupScript() }
            .setRemoveAction { removeStartupScript() }
            .setEditAction { editStartupScript() }
            .addExtraAction(object : AnActionButton("Run Script", "Execute the selected startup script", AllIcons.Actions.Execute) {
                override fun actionPerformed(e: AnActionEvent) {
                    runStartupScript()
                }
            })
            .createPanel()
        
        startupPanel.add(toolbar, BorderLayout.CENTER)
        mainPanel.add(startupPanel, BorderLayout.CENTER)
        
        return mainPanel
    }

    private fun setupStartupScriptsList() {
        startupScriptList.cellRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): java.awt.Component {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                
                if (value is ScriptConfiguration) {
                    text = buildString {
                        append(value.name)
                        if (value.description.isNotBlank()) {
                            append(" - ${value.description}")
                        }
                        if (!value.isEnabled) {
                            append(" (DISABLED)")
                        }
                    }
                    
                    if (!value.isEnabled) {
                        foreground = foreground.brighter()
                    }
                }
                
                return this
            }
        }
        
        refreshStartupScriptsList()
    }

    private fun addStartupScript() {
        val emptyScript = ScriptConfiguration.createEmpty().copy(isStartupScript = true)
        val dialog = ScriptConfigurationDialog(emptyScript)
        if (dialog.showAndGet()) {
            val script = dialog.getScriptConfiguration().copy(isStartupScript = true)
            service.addScript(script)
            refreshStartupScriptsList()
        }
    }

    private fun removeStartupScript() {
        val selectedScript = startupScriptList.selectedValue ?: return
        val result = JOptionPane.showConfirmDialog(
            startupScriptList,
            "Are you sure you want to delete startup script '${selectedScript.name}'?",
            "Delete Startup Script",
            JOptionPane.YES_NO_OPTION
        )
        if (result == JOptionPane.YES_OPTION) {
            service.removeScript(selectedScript.id)
            refreshStartupScriptsList()
        }
    }

    private fun editStartupScript() {
        val selectedScript = startupScriptList.selectedValue ?: return
        val dialog = ScriptConfigurationDialog(selectedScript)
        if (dialog.showAndGet()) {
            val updatedScript = dialog.getScriptConfiguration().copy(isStartupScript = true)
            service.updateScript(updatedScript)
            refreshStartupScriptsList()
        }
    }

    private fun runStartupScript() {
        val selectedScript = startupScriptList.selectedValue
        if (selectedScript != null && selectedScript.isEnabled) {
            try {
                val result = service.executeScript(selectedScript)
                JOptionPane.showMessageDialog(
                    startupScriptList,
                    result,
                    "Script Executed",
                    JOptionPane.INFORMATION_MESSAGE
                )
            } catch (e: Exception) {
                // Only show error dialog if the script doesn't ignore errors
                if (!selectedScript.ignoreErrors) {
                    JOptionPane.showMessageDialog(
                        startupScriptList,
                        "Error executing script: ${e.message}",
                        "Script Execution Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            }
        } else {
            JOptionPane.showMessageDialog(
                startupScriptList,
                "Please select an enabled startup script to run.",
                "No Script Selected",
                JOptionPane.INFORMATION_MESSAGE
            )
        }
    }

    private fun refreshStartupScriptsList() {
        startupListModel.clear()
        service.getStartupScripts().forEach { startupListModel.addElement(it) }
        // Also add disabled startup scripts
        service.getAllScripts().filter { it.isStartupScript && !it.isEnabled }.forEach { 
            startupListModel.addElement(it) 
        }
    }

    override fun isModified(): Boolean {
        // Changes are applied immediately to the service
        return false
    }

    override fun apply() {
        // Changes are applied immediately to the service
    }

    override fun reset() {
        refreshStartupScriptsList()
    }
}
