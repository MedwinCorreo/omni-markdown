package com.mdpreview.plugin.panel

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.logger
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import javax.swing.JComponent
import javax.swing.JLabel

private val LOG = logger<MarkdownPreviewPanel>()

class MarkdownPreviewPanel : Disposable {

    private val browser: JBCefBrowser?
    val component: JComponent

    // Holds markdown to render once page finishes loading
    @Volatile private var pendingContent: String? = null
    @Volatile private var pageLoaded = false

    init {
        if (JBCefApp.isSupported()) {
            browser = JBCefBrowser()

            // Wait for page to finish loading before pushing content
            browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
                override fun onLoadEnd(cefBrowser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
                    if (frame.isMain) {
                        pageLoaded = true
                        pendingContent?.let {
                            executeUpdate(it)
                            pendingContent = null
                        }
                    }
                }
            }, browser.cefBrowser)

            browser.loadHTML(buildInitialHtml(), "https://mdpreview/")
            component = browser.component
        } else {
            LOG.warn("JCEF is not supported in this environment. Markdown preview is unavailable.")
            browser = null
            component = JLabel("JCEF not supported — Markdown preview unavailable").apply {
                horizontalAlignment = JLabel.CENTER
            }
        }
    }

    fun updateContent(markdown: String) {
        if (pageLoaded) {
            executeUpdate(markdown)
        } else {
            // Page still loading — store and send once onLoadEnd fires
            pendingContent = markdown
        }
    }

    private fun executeUpdate(markdown: String) {
        val cefBrowser = browser?.cefBrowser ?: return
        val escaped = markdown
            .replace("\\", "\\\\")
            .replace("`", "\\`")
            .replace("$", "\\$")
        cefBrowser.executeJavaScript("updateMarkdown(`$escaped`);", "", 0)
    }

    private fun buildInitialHtml(): String {
        val template = readResource("/html/preview-template.html")
        val markedJs = readResource("/html/marked.min.js")
        val mermaidJs = readResource("/html/mermaid.min.js")

        return template
            .replace("<script src=\"marked.min.js\"></script>", "<script>\n$markedJs\n</script>")
            .replace("<script src=\"mermaid.min.js\"></script>", "<script>\n$mermaidJs\n</script>")
    }

    private fun readResource(path: String): String {
        return MarkdownPreviewPanel::class.java.getResourceAsStream(path)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
            ?: error("Bundled resource not found: $path")
    }

    override fun dispose() {
        browser?.dispose()
    }
}
