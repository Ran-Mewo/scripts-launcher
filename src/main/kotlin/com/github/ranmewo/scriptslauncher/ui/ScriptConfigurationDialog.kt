package com.github.ranmewo.scriptslauncher.ui

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.github.ranmewo.scriptslauncher.model.ScriptConfiguration
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane

class ScriptConfigurationDialog(
    private val initialScript: ScriptConfiguration = ScriptConfiguration.createEmpty()
) : DialogWrapper(true) {

    private val nameField = JBTextField(initialScript.name)
    private val commandField = JBTextField(initialScript.command)
    private val workingDirectoryField = TextFieldWithBrowseButton()
    private val descriptionArea = JBTextArea(initialScript.description, 3, 30)
    private val enabledCheckBox = JBCheckBox("Enabled", initialScript.isEnabled)
    private val startupCheckBox = JBCheckBox("Run on startup", initialScript.isStartupScript)
    private val ignoreErrorsCheckBox = JBCheckBox("Ignore errors", initialScript.ignoreErrors)

    init {
        title = if (initialScript.name.isBlank()) "Add Script" else "Edit Script"
        workingDirectoryField.text = initialScript.workingDirectory
        workingDirectoryField.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
            )
        )
        descriptionArea.lineWrap = true
        descriptionArea.wrapStyleWord = true
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Name:"), nameField, 1, false)
            .addLabeledComponent(JBLabel("Command:"), commandField, 1, false)
            .addLabeledComponent(JBLabel("Working Directory:"), workingDirectoryField, 1, false)
            .addLabeledComponent(JBLabel("Description:"), JScrollPane(descriptionArea), 1, true)
            .addComponent(enabledCheckBox)
            .addComponent(startupCheckBox)
            .addComponent(ignoreErrorsCheckBox)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        panel.preferredSize = Dimension(500, 300)
        return panel
    }

    override fun doValidate(): ValidationInfo? {
        if (nameField.text.isBlank()) {
            return ValidationInfo("Name cannot be empty", nameField)
        }
        if (commandField.text.isBlank()) {
            return ValidationInfo("Command cannot be empty", commandField)
        }
        return null
    }

    fun getScriptConfiguration(): ScriptConfiguration {
        return ScriptConfiguration(
            id = initialScript.id,
            name = nameField.text.trim(),
            command = commandField.text.trim(),
            workingDirectory = workingDirectoryField.text.trim(),
            description = descriptionArea.text.trim(),
            isEnabled = enabledCheckBox.isSelected,
            isStartupScript = startupCheckBox.isSelected,
            ignoreErrors = ignoreErrorsCheckBox.isSelected
        )
    }
}
