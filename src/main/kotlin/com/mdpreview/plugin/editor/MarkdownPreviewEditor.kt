package com.mdpreview.plugin.editor

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBSplitter
import com.mdpreview.plugin.listener.MarkdownDocumentListener
import com.mdpreview.plugin.panel.MarkdownPreviewPanel
import java.awt.BorderLayout
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import javax.swing.JPanel

class MarkdownPreviewEditor(
    private val project: Project,
    private val file: VirtualFile,
    private val textEditor: TextEditor
) : UserDataHolderBase(), FileEditor {

    private val previewPanel = MarkdownPreviewPanel()
    private val splitter: JBSplitter
    private val mainPanel: JPanel
    private var viewMode: ViewMode = ViewMode.SPLIT

    init {
        splitter = JBSplitter(false, 0.5f).apply {
            firstComponent = textEditor.component
            secondComponent = previewPanel.component
        }

        val toolbar = createToolbar()

        mainPanel = JPanel(BorderLayout()).apply {
            add(toolbar.component, BorderLayout.NORTH)
            add(splitter, BorderLayout.CENTER)
        }

        // Register disposer for the preview panel
        Disposer.register(this, previewPanel)

        // Attach document listener for live refresh
        val document = textEditor.editor.document
        val listener = MarkdownDocumentListener(previewPanel, this)
        document.addDocumentListener(listener, this)

        // Initial render
        previewPanel.updateContent(document.text)
    }

    private fun createToolbar(): ActionToolbar {
        val actionGroup = DefaultActionGroup().apply {
            add(ViewModeToggleAction("Code", AllIcons.Actions.EditSource, ViewMode.CODE_ONLY))
            add(ViewModeToggleAction("Split", AllIcons.Actions.SplitVertically, ViewMode.SPLIT))
            add(ViewModeToggleAction("Preview", AllIcons.Actions.Preview, ViewMode.RENDER_ONLY))
        }

        return ActionManager.getInstance()
            .createActionToolbar("MarkdownPreviewToolbar", actionGroup, true)
            .apply {
                targetComponent = mainPanel
            }
    }

    private fun setViewMode(mode: ViewMode) {
        if (viewMode == mode) return
        viewMode = mode

        when (mode) {
            ViewMode.CODE_ONLY -> {
                splitter.firstComponent = textEditor.component
                splitter.secondComponent = null
            }
            ViewMode.SPLIT -> {
                splitter.firstComponent = textEditor.component
                splitter.secondComponent = previewPanel.component
            }
            ViewMode.RENDER_ONLY -> {
                splitter.firstComponent = null
                splitter.secondComponent = previewPanel.component
            }
        }

        // Refresh preview when it becomes visible again
        if (mode != ViewMode.CODE_ONLY) {
            val document = textEditor.editor.document
            previewPanel.updateContent(document.text)
        }

        mainPanel.revalidate()
        mainPanel.repaint()
    }

    private inner class ViewModeToggleAction(
        text: String,
        icon: javax.swing.Icon,
        private val mode: ViewMode
    ) : ToggleAction(text, "Switch to $text view", icon) {

        override fun isSelected(e: AnActionEvent): Boolean = viewMode == mode

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            if (state) {
                setViewMode(mode)
            }
        }
    }

    override fun getFile(): VirtualFile = file

    override fun getComponent(): JComponent = mainPanel

    override fun getPreferredFocusedComponent(): JComponent? {
        return when (viewMode) {
            ViewMode.RENDER_ONLY -> previewPanel.component
            else -> textEditor.preferredFocusedComponent
        }
    }

    override fun getName(): String = "Markdown Preview"

    override fun setState(state: FileEditorState) {
        textEditor.setState(state)
    }

    override fun isModified(): Boolean = textEditor.isModified

    override fun isValid(): Boolean = textEditor.isValid

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        textEditor.addPropertyChangeListener(listener)
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
        textEditor.removePropertyChangeListener(listener)
    }

    override fun getCurrentLocation(): FileEditorLocation? = textEditor.currentLocation

    override fun dispose() {
        Disposer.dispose(textEditor)
    }
}
