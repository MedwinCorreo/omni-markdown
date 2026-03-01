package com.mdpreview.plugin.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.mdpreview.plugin.editor.MarkdownPreviewEditor
import com.mdpreview.plugin.editor.ViewMode

class ShowPreviewOnlyAction : AnAction(
    "Preview Only",
    "Show only the rendered preview",
    AllIcons.Actions.Preview
) {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = getMarkdownEditor(e) ?: return
        editor.setViewMode(ViewMode.PREVIEW_ONLY)
    }

    override fun update(e: AnActionEvent) {
        val editor = getMarkdownEditor(e)
        e.presentation.isEnabled = editor != null
    }

    private fun getMarkdownEditor(e: AnActionEvent): MarkdownPreviewEditor? {
        val project = e.project ?: return null
        return FileEditorManager.getInstance(project).selectedEditor as? MarkdownPreviewEditor
    }
}
