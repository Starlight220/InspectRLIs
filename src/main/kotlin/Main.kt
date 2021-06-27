package io.starlight.inspector

import com.github.starlight.actions.Environment
import io.starlight.inspector.agents.Agent
import java.io.File
import kotlin.streams.asSequence

private val env: InspectorEnv =
    Environment(
        File(
                System.getenv("GITHUB_WORKSPACE").takeUnless { it.isNullOrEmpty() },
                System.getenv("INSPECTOR_CONFIG")
            )
            .readText()
    )

fun main() =
    Agent.load(env).run {
        /*
        Process:
        1. Recursively walk files; for each file:
            1. Find all RLIs; for each RLI:
                1. Destructure into source, line range, version, and file location
                2. Check if version is up-to-date
                   - if yes, fold (**up-to-date**)
                3. Compare responses for line ranges of both current and latest versions
                    - if identical, autofix (**outdated**)
                    - else, require manual attention (**invalid**)
         */
        root
            .walkDir { extension == "rst" && !isFileIgnored() }
            .map { it.findRlis() }
            .asSequence()
            .flatMap { seq -> seq.map { it.status } }
            .forEach { it() }

        Report()
    }
