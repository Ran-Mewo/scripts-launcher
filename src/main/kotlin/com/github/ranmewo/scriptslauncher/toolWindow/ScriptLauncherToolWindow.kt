package com.github.ranmewo.scriptslauncher.toolWindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.AnActionButton
import com.intellij.ui.AnActionButtonRunnable
import com.intellij.util.ui.JBUI
import com.github.ranmewo.scriptslauncher.model.ScriptConfiguration
import com.github.ranmewo.scriptslauncher.services.ScriptLauncherService
import com.github.ranmewo.scriptslauncher.ui.ScriptConfigurationDialog
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class ScriptLauncherToolWindow(private val project: Project) {

    private val service = ApplicationManager.getApplication().service<ScriptLauncherService>()
    private val listModel = DefaultListModel<ScriptConfiguration>()
    private val scriptList = JBList(listModel)
    private val startupListModel = DefaultListModel<ScriptConfiguration>()
    private val startupScriptList = JBList(startupListModel)

    init {
        setupList()
        setupStartupList()
        refreshLists()
    }

    private fun setupList() {
        scriptList.cellRenderer = ScriptListCellRenderer()
        scriptList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        
        // Double-click to execute script
        scriptList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val selectedScript = scriptList.selectedValue
                    if (selectedScript != null && selectedScript.isEnabled) {
                        executeScriptWithErrorHandling(selectedScript)
                    }
                }
            }
        })
    }

    private fun setupStartupList() {
        startupScriptList.cellRenderer = ScriptListCellRenderer()
        startupScriptList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        
        // Double-click to execute startup script
        startupScriptList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val selectedScript = startupScriptList.selectedValue
                    if (selectedScript != null && selectedScript.isEnabled) {
                        executeScriptWithErrorHandling(selectedScript)
                    }
                }
            }
        })
    }

    fun getContent(): JComponent {
        val tabbedPane = JBTabbedPane()
        
        // Regular Scripts Tab
        val regularScriptsPanel = createScriptsPanel(scriptList, false)
        tabbedPane.addTab("Scripts", regularScriptsPanel)
        
        // Startup Scripts Tab
        val startupScriptsPanel = createScriptsPanel(startupScriptList, true)
        tabbedPane.addTab("Startup Scripts", startupScriptsPanel)
        
        return tabbedPane
    }

    private fun createScriptsPanel(list: JBList<ScriptConfiguration>, isStartup: Boolean): JComponent {
        val panel = JPanel(BorderLayout())
        
        val toolbar = ToolbarDecorator.createDecorator(list)
            .setAddAction { if (isStartup) addStartupScript() else addScript() }
            .setRemoveAction { if (isStartup) removeStartupScript() else removeScript() }
            .setEditAction { if (isStartup) editStartupScript() else editScript() }
            .addExtraAction(object : AnActionButton("Run Script", "Execute the selected script", AllIcons.Actions.Execute) {
                override fun actionPerformed(e: AnActionEvent) {
                    if (isStartup) executeSelectedStartupScript() else executeSelectedScript()
                }
            })
            .createPanel()

        panel.add(toolbar, BorderLayout.CENTER)
        panel.border = JBUI.Borders.empty(5)
        
        return panel
    }

    private fun addScript() {
        val dialog = ScriptConfigurationDialog()
        if (dialog.showAndGet()) {
            val script = dialog.getScriptConfiguration()
            service.addScript(script)
            refreshLists()
        }
    }

    private fun addStartupScript() {
        val emptyScript = ScriptConfiguration.createEmpty().copy(isStartupScript = true)
        val dialog = ScriptConfigurationDialog(emptyScript)
        if (dialog.showAndGet()) {
            val script = dialog.getScriptConfiguration().copy(isStartupScript = true)
            service.addScript(script)
            refreshLists()
        }
    }

    private fun removeScript() {
        val selectedScript = scriptList.selectedValue ?: return
        val result = JOptionPane.showConfirmDialog(
            scriptList,
            "Are you sure you want to delete script '${selectedScript.name}'?",
            "Delete Script",
            JOptionPane.YES_NO_OPTION
        )
        if (result == JOptionPane.YES_OPTION) {
            service.removeScript(selectedScript.id)
            refreshLists()
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
            refreshLists()
        }
    }

    private fun editScript() {
        val selectedScript = scriptList.selectedValue ?: return
        val dialog = ScriptConfigurationDialog(selectedScript)
        if (dialog.showAndGet()) {
            val updatedScript = dialog.getScriptConfiguration()
            service.updateScript(updatedScript)
            refreshLists()
        }
    }

    private fun editStartupScript() {
        val selectedScript = startupScriptList.selectedValue ?: return
        val dialog = ScriptConfigurationDialog(selectedScript)
        if (dialog.showAndGet()) {
            val updatedScript = dialog.getScriptConfiguration().copy(isStartupScript = true)
            service.updateScript(updatedScript)
            refreshLists()
        }
    }

    private fun executeSelectedScript() {
        val selectedScript = scriptList.selectedValue
        if (selectedScript != null && selectedScript.isEnabled) {
            executeScriptWithErrorHandling(selectedScript)
        }
    }

    private fun executeSelectedStartupScript() {
        val selectedScript = startupScriptList.selectedValue
        if (selectedScript != null && selectedScript.isEnabled) {
            executeScriptWithErrorHandling(selectedScript)
        }
    }
    
    private fun executeScriptWithErrorHandling(script: ScriptConfiguration) {
        try {
            val result = service.executeScript(script)
            Messages.showInfoMessage(
                project,
                result,
                "Script Executed"
            )
        } catch (e: Exception) {
            // Only show error dialog if the script doesn't ignore errors
            if (!script.ignoreErrors) {
                Messages.showErrorDialog(
                    project,
                    "Error executing script '${script.name}':\n\n${e.message}",
                    "Script Execution Error"
                )
            }
        }
    }

    private fun refreshLists() {
        listModel.clear()
        service.getNonStartupScripts().forEach { listModel.addElement(it) }
        
        startupListModel.clear()
        service.getAllScripts().filter { it.isStartupScript }.forEach { startupListModel.addElement(it) }
    }

    private class ScriptListCellRenderer : DefaultListCellRenderer() {
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
                    if (value.isStartupScript) {
                        append(" [STARTUP]")
                    }
                    if (value.ignoreErrors) {
                        append(" [IGNORE ERRORS]")
                    }
                }
                icon = if (value.isEnabled) {
                    if (value.isStartupScript) AllIcons.Actions.Restart else AllIcons.Actions.Execute
                } else {
                    AllIcons.Actions.Suspend
                }
                
                if (!value.isEnabled) {
                    foreground = foreground.brighter()
                } else if (value.isStartupScript) {
                    foreground = java.awt.Color(0, 120, 0) // Dark green for startup scripts
                }
            }
            
            return this
        }
    }
}
