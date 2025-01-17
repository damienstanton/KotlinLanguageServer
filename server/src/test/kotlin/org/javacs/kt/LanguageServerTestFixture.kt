package org.javacs.kt

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.LanguageClient
import org.junit.Before
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture

abstract class LanguageServerTestFixture(relativeWorkspaceRoot: String) : LanguageClient {
    val workspaceRoot = absoluteWorkspaceRoot(relativeWorkspaceRoot)
    val languageServer = createLanguageServer()
    val diagnostics = mutableListOf<Diagnostic>()

    fun absoluteWorkspaceRoot(relativeWorkspaceRoot: String): Path {
        val testResources = testResourcesRoot()
        return testResources.resolve(relativeWorkspaceRoot)
    }

    private fun createLanguageServer(): KotlinLanguageServer {
        val languageServer = KotlinLanguageServer()
        val init = InitializeParams().apply {
            capabilities = ClientCapabilities().apply {
                textDocument = TextDocumentClientCapabilities().apply {
                    completion = CompletionCapabilities().apply {
                        completionItem = CompletionItemCapabilities().apply {
                            snippetSupport = true
                        }
                    }
                }
            }
        }

        init.rootUri = workspaceRoot.toUri().toString()
        languageServer.initialize(init)
        languageServer.connect(this)

        return languageServer
    }

    fun completionParams(relativePath: String, line: Int, column: Int): CompletionParams {
        val file = workspaceRoot.resolve(relativePath)
        val fileId = TextDocumentIdentifier(file.toUri().toString())
        val position = position(line, column)

        return CompletionParams(fileId, position)
    }

    fun textDocumentPosition(relativePath: String, line: Int, column: Int): TextDocumentPositionParams {
        return textDocumentPosition(relativePath, position(line, column))
    }

    fun textDocumentPosition(relativePath: String, position: Position): TextDocumentPositionParams {
        val file = workspaceRoot.resolve(relativePath)
        val fileId = TextDocumentIdentifier(file.toUri().toString())
        return TextDocumentPositionParams(fileId, position)
    }

    fun position(line: Int, column: Int) = Position(line - 1, column - 1)

    fun range(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int) =
        Range(position(startLine, startColumn), position(endLine, endColumn))

    fun uri(relativePath: String) =
            workspaceRoot.resolve(relativePath).toUri()

    fun referenceParams(relativePath: String, line: Int, column: Int): ReferenceParams {
        val request = ReferenceParams(ReferenceContext(true))
        request.textDocument = TextDocumentIdentifier(uri(relativePath).toString())
        request.position = position(line, column)
        return request
    }

    fun open(relativePath: String) {
        val file =  workspaceRoot.resolve(relativePath)
        val content = file.toFile().readText()
        val document = TextDocumentItem(file.toUri().toString(), "Kotlin", 0, content)

        languageServer.textDocumentService.didOpen(DidOpenTextDocumentParams(document))
    }

    private var version = 1

    fun replace(relativePath: String, line: Int, char: Int, oldText: String, newText: String) {
        val range = Range(position(line, char), Position(line - 1, char - 1 + oldText.length))
        val edit = TextDocumentContentChangeEvent(range, oldText.length, newText)
        val doc = VersionedTextDocumentIdentifier(uri(relativePath).toString(), version++)

        languageServer.textDocumentService.didChange(DidChangeTextDocumentParams(doc, listOf(edit)))
    }

    // LanguageClient functions

    override fun publishDiagnostics(diagnostics: PublishDiagnosticsParams) {
        this.diagnostics.addAll(diagnostics.diagnostics)
    }

    override fun showMessageRequest(request: ShowMessageRequestParams?): CompletableFuture<MessageActionItem>? {
        println(request.toString())
        return null
    }

    override fun telemetryEvent(`object`: Any?) {
        println(`object`.toString())
    }

    override fun logMessage(message: MessageParams?) {
        println(message.toString())
    }

    override fun showMessage(message: MessageParams?) {
        println(message.toString())
    }
}

fun testResourcesRoot(): Path {
    val anchorTxt = LanguageServerTestFixture::class.java.getResource("/Anchor.txt").toURI()
    return Paths.get(anchorTxt).parent!!
}

open class SingleFileTestFixture(relativeWorkspaceRoot: String, val file: String) : LanguageServerTestFixture(relativeWorkspaceRoot) {
    @Before fun openFile() {
        open(file)

        // Wait for lint, so subsequent replace(...) operations cause recovery
        languageServer.textDocumentService.debounceLint.waitForPendingTask()
    }
}
