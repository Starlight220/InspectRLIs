package io.starlight.inspector

import io.starlight.inspector.agents.Agent
import java.io.File
import java.util.HashSet
import java.util.stream.Stream

fun File.normalized(root: File) = this.toRelativeString(root).replace('\\', '/')

/** Represents a file that contains RLIs. */
class RliFile(private val file: File) : Comparable<RliFile> {
    var content: String = file.readText()
    private var offset: Int = 0

    /**
     * Replace the given index [range] with [replacement].
     *
     * Accounts for multiple shifts of the same file with replacements of differing lengths.
     */
    fun replaceRange(range: IntRange, replacement: String) {
        val offsetRange = IntRange(range.first + offset, range.last + offset)
        val newContent = content.replaceRange(offsetRange, replacement)

        offset += newContent.length - content.length
        content = newContent
        file.writeText(newContent)
    }

    /**
     * Allows for immutable manipulation of the file's content.
     *
     * Since [String]s are immutable, any changes through this function will **not** be reflected in
     * the file.
     */
    inline fun <reified R> use(const: (String) -> R) = const(content)

    override fun toString(): String = name
    override operator fun compareTo(other: RliFile): Int = this.file.compareTo(other.file)
    override fun equals(other: Any?): Boolean =
        if (other is RliFile) {
            this.file == other.file
        } else false

    val name: String
        get() = file.normalized(Agent.getCurrent().root)
}

/**
 * Return a [Sequence] of all files that match the given [predicate].
 *
 * Searches recursively for files.
 * @see walk
 */
fun File.walkDir(predicate: File.() -> Boolean): Stream<RliFile> =
    walk().filterTo(HashSet(), predicate).parallelStream().map(::RliFile)

fun File.isFileIgnored(): Boolean =
    Agent.getCurrent().ignoredFiles.firstOrNull { isSubpath(this, it) }?.let { true } ?: false

fun isSubpath(file: File, str: String) = file.normalized(Agent.getCurrent().root).startsWith(str)
