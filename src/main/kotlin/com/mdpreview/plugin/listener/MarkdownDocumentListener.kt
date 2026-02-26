package com.mdpreview.plugin.listener

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.util.Alarm
import com.mdpreview.plugin.panel.MarkdownPreviewPanel

class MarkdownDocumentListener(
    private val panel: MarkdownPreviewPanel,
    disposable: Disposable
) : DocumentListener {

    private val alarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, disposable)

    companion object {
        private const val DEBOUNCE_DELAY_MS = 300
    }

    override fun documentChanged(event: DocumentEvent) {
        alarm.cancelAllRequests()
        alarm.addRequest({
            panel.updateContent(event.document.text)
        }, DEBOUNCE_DELAY_MS)
    }
}
