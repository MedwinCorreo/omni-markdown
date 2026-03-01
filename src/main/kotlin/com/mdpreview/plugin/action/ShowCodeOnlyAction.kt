package com.mdpreview.plugin.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.mdpreview.plugin.editor.MarkdownPreviewEditor
import com.mdpreview.plugin.editor.ViewMode

class ShowCodeOnlyAction : AnAction(
    "Code Only",
    "Show only the code editor",
    AllIcons.Actions.Edit
) {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = getMarkdownEditor(e) ?: return
        editor.setViewMode(ViewMode.CODE_ONLY)
    }

    override fun update(e: AnActionEvent) {
        val editor = getMarkdownEditor(e)
        e.presentation.isEnabled = editor != null
        e.presentation.icon = if (editor?.currentViewMode == ViewMode.CODE_ONLY)
            AllIcons.Actions.Edit else AllIcons.Actions.Edit
    }

    private fun getMarkdownEditor(e: AnActionEvent): MarkdownPreviewEditor? {
        val project = e.project ?: return null
        return FileEditorManager.getInstance(project).selectedEditor as? MarkdownPreviewEditor
    }
}
