package com.mdpreview.plugin.renderer

/**
 * Utility for generating HTML fragments. The actual rendering is done
 * client-side by marked.js + mermaid.js in the JCEF browser panel.
 * This class is reserved for server-side pre-processing if needed in future.
 */
object MermaidHtmlGenerator {

    /**
     * Wraps raw markdown text for safe delivery to the browser.
     * Returns the markdown unchanged — transformation happens in JS.
     */
    fun prepareMarkdown(raw: String): String = raw
}
