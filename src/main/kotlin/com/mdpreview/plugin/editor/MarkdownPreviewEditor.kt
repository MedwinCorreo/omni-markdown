package com.mdpreview.plugin.editor

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
import java.beans.PropertyChangeListener
import javax.swing.JComponent

class MarkdownPreviewEditor(
    private val project: Project,
    private val file: VirtualFile,
    private val textEditor: TextEditor
) : UserDataHolderBase(), FileEditor {

    private val previewPanel = MarkdownPreviewPanel()
    private val splitter: JBSplitter

    init {
        splitter = JBSplitter(false, 0.5f).apply {
            firstComponent = textEditor.component
            secondComponent = previewPanel.component
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

    override fun getFile(): VirtualFile = file

    override fun getComponent(): JComponent = splitter

    override fun getPreferredFocusedComponent(): JComponent? = textEditor.preferredFocusedComponent

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
