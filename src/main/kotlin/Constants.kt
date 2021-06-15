package io.starlight.inspector

import com.github.starlight.actions.Environment
import com.github.starlight.actions.Input
import kotlinx.serialization.Serializable
import java.io.File

private const val reportFilePath = "report.md"
private const val oldTmpFilePath = "old.tmp"
private const val newTmpFilePath = "new.tmp"

private const val rliHeaderRegex = """\.\. (?:rli)|(?:remoteliteralinclude)::"""
private const val rliLinesRegex = """\r?\n[ ]*:lines: (\d*-\d*)"""

// for local testing ONLY
private const val envFilePath = "inputs.inspect_rli.json"

/** Constants Namespace */
object Constants {
    private val env: InspectorEnv = Environment(File(envFilePath).readText())

    // files
    val reportFile = File(reportFilePath)
    val oldTmpFile = File(oldTmpFilePath)
    val newTmpFile = File(newTmpFilePath)

    /** Search root for RLI files */
    val root by Input(mapper = ::File)

    const val diffCommand: String = "git diff --no-index --no-prefix -U200 -- "
    val diffSplitRegex = """@@ [-]?\d+,?\d* [+]?\d+,?\d* @@""".toRegex()

    /** Base URL for all RLIs. Contains terminating `/`. */
    val baseUrl by Input { str -> if (str.endsWith('/')) str else "$str/" }

    /** Version Regex */
    val versionScheme by Input

    /** Latest Version */
    val latestVersion by Input

    val rliRegex by lazy {
        val r =
            """$rliHeaderRegex ${Regex.fromLiteral(baseUrl).pattern}($versionScheme)/([/\w.]+)\r?\n.*$rliLinesRegex""".toRegex()
        println(r)
        r
    }

    val ignoredFiles: Set<String> by env::ignoredFiles

    override fun toString(): String {
        return """
      $root
      $reportFilePath
      $rliHeaderRegex
      $baseUrl
      $versionScheme
      $rliLinesRegex
      ${rliRegex.pattern}
        """.trimIndent()
    }
}

@Serializable
internal data class InspectorEnv(
    @JvmField val root: String,
    @JvmField val versionScheme: String,
    @JvmField val baseUrl: String,
    @JvmField val latestVersion: String,
    @JvmField val ignoredFiles: Set<String> = emptySet(),
) : Environment