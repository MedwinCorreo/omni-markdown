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
    private val wrapperPanel = JPanel(BorderLayout())

    var currentViewMode: ViewMode = ViewMode.SPLIT
        private set

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

        // Default: split mode
        wrapperPanel.add(splitter, BorderLayout.CENTER)
    }

    fun setViewMode(mode: ViewMode) {
        if (mode == currentViewMode) return
        currentViewMode = mode

        wrapperPanel.removeAll()
        when (mode) {
            ViewMode.CODE_ONLY -> {
                splitter.firstComponent = null
                splitter.secondComponent = null
                wrapperPanel.add(textEditor.component, BorderLayout.CENTER)
            }
            ViewMode.SPLIT -> {
                splitter.firstComponent = textEditor.component
                splitter.secondComponent = previewPanel.component
                wrapperPanel.add(splitter, BorderLayout.CENTER)
            }
            ViewMode.PREVIEW_ONLY -> {
                splitter.firstComponent = null
                splitter.secondComponent = null
                wrapperPanel.add(previewPanel.component, BorderLayout.CENTER)
                // Ensure preview is up-to-date
                previewPanel.updateContent(textEditor.editor.document.text)
            }
        }
        wrapperPanel.revalidate()
        wrapperPanel.repaint()
    }

    override fun getFile(): VirtualFile = file

    override fun getComponent(): JComponent = wrapperPanel

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
