package io.starlight.inspector

import com.github.starlight.actions.Output
import java.util.*

object Report {
    private val upToDateFiles = LinkedList<RliFile>()
    private val upToDate = LinkedList<Triple<String, LineRange, Location>>()
    private val outdated = LinkedList<LocatedRli>()
    private val invalid = LinkedList<Pair<Location, String>>()

    private inline fun <T> locationComparator(crossinline mapper: (T) -> Location): Comparator<T> =
            Comparator { first, second ->
        val loc1 = mapper(first)
        val loc2 = mapper(second)
        loc1.compareTo(loc2)
    }

    /** Report a whole file as up-to-date */
    fun upToDateFile(file: RliFile) {
        upToDateFiles.push(file)
        upToDate.removeAll { (_, _, loc) -> loc.file == file }
    }

    /** Report an RLI as up-to-date */
    fun upToDate(url: String, lines: LineRange, loc: Location) =
        if (loc.file in upToDateFiles) Unit
        else {
            val triple = Triple(url, lines, loc)
            if (triple !in upToDate) upToDate.push(triple) else Unit
        }

    /** Report an RLI as outdated and automatically fixed */
    fun outdated(rli: Rli, location: Location): Unit = outdated.push(location to rli)

    /** Report an RLI as invalid and requires manual attention */
    fun invalid(obj: RliStatus.Invalid) =
        with(obj) {
            invalid.push(
                location to
                    """
                |> [${Constants.latestVersion} |](${diff.old.withLatest.fullUrl}) [${diff.old.version}/${diff.old.url}#${diff.old.lines}](${diff.old.fullUrl}) @ <${location.file}:${location.line}>
                |```diff
                |${buildDiffBlock(diff)}
                |```
                |
            """.trimMargin()
            )
        }

    override fun toString(): String =
        """
    |# Inspector Report
    |
    |***
    |
    |### Up To Date
    |
    |<details>
    |
    |```
    |${upToDateFiles.sorted().joinToString("\n") { "ALL @ <${it}>" }}
    |${upToDate.sortedWith(locationComparator{it.third}).joinToString("\n") { (url, lines, loc) -> "${url}#${lines} @ <${loc.file}:${loc.line}>" }}
    |```
    |
    |</details>
    |
    |### Outdated - Automatically Fixed
    |${outdated.run { if (isNotEmpty()) sortedWith(locationComparator { it.first }).joinToString(prefix = "\n<details>\n\n```\n", separator = "\n", postfix = "\n```\n\n</details>\n") { (location, rli) -> "${rli.url}#${rli.lines} @ <${location.file}:${location.line}>"} else "None"}}
    |
    |### Invalid - Manual Intervention Needed
    |${invalid.run { if (isNotEmpty()) sortedWith(locationComparator { it.first }).joinToString(prefix = "\n<details>\n\n", separator = "\n", postfix = "\n\n</details>\n"){ it.second } else "None"}}
    |
    |${Constants.ignoredFiles.run { if (isNotEmpty()) joinToString(prefix = "##### Ignored Files\n```\n", separator = "\n", postfix = "\n```\n") else ""}}
    |
  """.trimMargin()

    private var needsManual by Output<Boolean>("needs-manual")
    private var isUpToDate by Output<Boolean>("up-to-date")
    private var reportFilePath by Output<String>("report-file-path")
    private var report by Output
    operator fun invoke() {
        val _needsManual = invalid.isNotEmpty()
        needsManual = _needsManual
        isUpToDate = !_needsManual && outdated.isEmpty()
        reportFilePath = Constants.reportFile.canonicalPath
        report = toString()
        Constants.reportFile.writeText(report)
        println(report)
    }
}
