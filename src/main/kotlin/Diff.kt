package io.starlight.inspector

@JvmInline
value class Diff(private val diff: Pair<Rli, Rli>) {
    val old
        get() = diff.first
    val new
        get() = diff.second

    operator fun component1() = old
    operator fun component2() = new
}

infix fun Rli.diff(other: Rli): Diff? =
    if (this.response != other.response) Diff(this to other) else null

fun buildDiffBlock(diff: Diff): String {
    Constants.oldTmpFile.writeText(diff.old.response + "\n")
    Constants.newTmpFile.writeText(diff.new.response + "\n")

    var oldLine = diff.old.lines.start
    var newLine = diff.new.lines.start

    fun processLine(line: String): String? =
        when (line[0]) {
            '+' -> line.replaceFirst("+", "+\t \t${newLine++}\t")
            '-' -> line.replaceFirst("-", "-\t${oldLine++}\t \t")
            ' ' -> line.replaceFirst(" ", " \t${oldLine++}\t${newLine++}\t")
            '\\' -> null
            else ->
                error(
                    "Git Diff output line should not start with something other than `+`, `-`, ` `, or `\\`.\nGot:$line"
                )
        }

    return Runtime.getRuntime()
        .exec(
            Constants.run { "$diffCommand ${oldTmpFile.canonicalPath} ${newTmpFile.canonicalPath}" }
        )
        .inputStream
        .bufferedReader()
        .readText()
        .split(Constants.diffSplitRegex)
        .asSequence()
        .drop(1) // drop header
        .flatMap { it.lineSequence() }
        .filter { it.isNotEmpty() }
        .map { processLine(it) }
        .filterNot { it.isNullOrEmpty() }
        .joinToString("\n")
}
