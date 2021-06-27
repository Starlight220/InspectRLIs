package io.starlight.inspector.agents

import io.starlight.inspector.*
import java.io.File

/**
 * Agents allow for altering how Inspector behaves under certain circumstances.
 * @see GitHubAgent
 */
open class Agent(env: InspectorEnv) {
    open val baseUrl: String = env.baseUrl.trimEnd('/')
    open val baseUrlRegex: Regex by lazy { Regex.fromLiteral(baseUrl) }
    open val versionRegex: Regex = Regex(env.versionScheme)
    open val latestVersion: String by env::latestVersion
    open val rliRegex: Regex by lazy {
        val r =
            """$RLI_HEADER_REGEX ${baseUrlRegex.pattern}/(${versionRegex.pattern})/$RLI_PATH_REGEX$RLI_LINES_REGEX""".toRegex()
        println(r)
        r
    }
    open val root: File = File(env.root)
    open val ignoredFiles: Set<String> = env.ignoredFiles
    open val reportFile = File(REPORT_FILE)
    open val oldTmpFile = File(OLD_TMP_FILE)
    open val newTmpFile = File(NEW_TMP_FILE)
    open val diffCommand = Regex(DIFF_COMMAND)
    open val diffSplitRegex = Regex(DIFF_SPLIT_REGEX)

    /** Returns a [Sequence] of RLIs in this file */
    open fun RliFile.findRlis(): Sequence<LocatedRli> {
        val matches = use { rliRegex.findAll(it) }
        if (matches.count() < 1) return emptySequence()

        val rlis = matches.map { this.buildRli(it) }.filterNotNull()
        if (rlis.count() < 1) Report.upToDateFile(this)
        return rlis
    }

    /** Dissects the [result] struct from the Regex match */
    open fun RliFile.buildRli(result: MatchResult): LocatedRli? {
        // sanity check to make sure that there isn't any funny business going on
        check(result.value matches rliRegex)
        // index of RLI URL in file
        val idxRange = result.groups[1]?.range ?: IntRange.EMPTY
        // (RLI version, RLI URL, RLI line range)
        val (version, url, _lines) = result.destructured
        val lines = LineRange(_lines)
        val loc = Location(this, idxRange)
        if (version == latestVersion) {
            // if the RLI is up-to-date, no need to waste time on it
            Report.upToDate(url, lines, loc)
            return null
        }
        return loc to Rli(version, url, lines)
    }

    override fun toString(): String =
        """
        agent: ${this.javaClass.simpleName}
        baseUrl: $baseUrl
        urlRegex: $baseUrlRegex
        versionRegex: $versionRegex
        latestVersion: $latestVersion
        rliRegex: $rliRegex
        root: $root
        ignoredFiles: $ignoredFiles
        reportFile: $reportFile
        oldTmpFile: $oldTmpFile
        diffCommand: $diffCommand
        diffSplitRegex: $diffSplitRegex
        
    """.trimIndent()

    companion object AgentRegistry {
        private data class AgentIdentity(
            val ctor: (InspectorEnv) -> Agent,
            val predicate: (InspectorEnv) -> Boolean
        )

        private val list =
            mutableListOf(
                AgentIdentity(::Agent) { true },
                AgentIdentity(::GitHubAgent) { it.baseUrl.startsWith("@github") }
            )

        fun load(env: InspectorEnv): Agent {
            currentAgent = list.last { it.predicate(env) }.ctor(env)
            return currentAgent
        }

        private lateinit var currentAgent: Agent

        fun getCurrent(): Agent = currentAgent
    }
}
