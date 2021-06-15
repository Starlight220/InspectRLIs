package io.starlight.inspector

import kotlin.streams.asSequence

fun main() {
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
    Constants.root
        .walkDir { extension == "rst" && !isFileIgnored(this)}
        .map { it.findRlis() }
        .asSequence()
        .flatMap { seq -> seq.map { it.status } }
        .forEach { it() }

    Report()
}
