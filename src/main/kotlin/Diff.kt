package io.starlight.inspector

import io.starlight.inspector.agents.Agent

data class Diff(val old: Rli, val new: Rli)

infix fun Rli.diff(other: Rli): Diff? =
    if (this.response != other.response) Diff(this, other) else null

fun buildDiffBlock(diff: Diff): String =
    with(Agent.getCurrent()) {
        oldTmpFile.writeText(diff.old.response + "\n")
        newTmpFile.writeText(diff.new.response + "\n")

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
            .exec("$diffCommand ${oldTmpFile.canonicalPath} ${newTmpFile.canonicalPath}")
            .inputStream
            .bufferedReader()
            .readText()
            .split(diffSplitRegex)
            .asSequence()
            .drop(1) // drop header
            .flatMap { it.lineSequence() }
            .filter { it.isNotEmpty() }
            .map { processLine(it) }
            .filterNot { it.isNullOrEmpty() }
            .joinToString("\n")
    }
