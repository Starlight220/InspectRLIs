package io.starlight.inspector

import com.github.starlight.actions.Environment
import kotlinx.serialization.Serializable

const val REPORT_FILE = "report.md"
const val OLD_TMP_FILE = "old.tmp"
const val NEW_TMP_FILE = "new.tmp"

const val RLI_HEADER_REGEX = """\.\. (?:rli)|(?:remoteliteralinclude)::"""
const val RLI_PATH_REGEX = """([/\w.]+)\r?\n.*"""
const val RLI_LINES_REGEX = """\r?\n[ ]*:lines: (\d*-\d*)"""
const val DIFF_COMMAND = "git diff --no-index --no-prefix -U200 -- "
const val DIFF_SPLIT_REGEX = """@@ [-]?\d+,?\d* [+]?\d+,?\d* @@"""

@Serializable
data class InspectorEnv(
    @JvmField val root: String,
    @JvmField val versionScheme: String,
    @JvmField val baseUrl: String,
    @JvmField val latestVersion: String,
    @JvmField val ignoredFiles: Set<String> = emptySet(),
) : Environment
