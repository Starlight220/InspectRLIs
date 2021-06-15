package io.starlight.inspector

/**
 * Represents the line range of an RLI, fetched from the remote.
 */
sealed class LineRange {
    abstract operator fun contains(e: Int): Boolean
    abstract override fun toString(): String
    abstract val start: Int

    /**
     * Both edges are specified, and are different
     */
    protected class RangedLineRange(private val range: IntRange) : LineRange() {
        override fun contains(e: Int): Boolean = e in range
        override fun toString(): String = "L${range.first}-L${range.last}"
        override fun equals(other: Any?): Boolean = other is RangedLineRange && this.range == other.range
        override fun hashCode(): Int = range.hashCode()
        override val start: Int by range::first
    }

    /**
     * The RLI only includes one line, indicated by
     * ```rst
     *      :lines: 10-10
     * ```
     */
    protected class SingletonLineRange(private val element: Int) : LineRange() {
        override fun contains(e: Int): Boolean = element == e
        override fun toString(): String = "L$element"
        override fun equals(other: Any?): Boolean = other is SingletonLineRange && this.element == other.element
        override fun hashCode(): Int = Integer.hashCode(element)
        override val start: Int by ::element
    }

    /**
     * Only the lower bound is specified, continuing until the end of the remote file.
     *
     * Indicated by:
     * ```rst
     *      :lines: 20-
     * ```
     */
    protected class LowerBoundedLineRange(override val start: Int) : LineRange() {
        override fun contains(e: Int): Boolean = start <= e
        override fun toString(): String = "L$start-EOF"
        override fun equals(other: Any?): Boolean = other is LowerBoundedLineRange && this.start == other.start
        override fun hashCode(): Int = Integer.hashCode(start)

    }

    companion object {
        operator fun invoke(s: String): LineRange =
            with(s) {
                val parts = this.split('-')
                check(parts.size == 2) { "error in parsing numbers: `$this`" }
                if (parts[1].isBlank()) {
                    return@with LowerBoundedLineRange(parts[0].toInt())
                }
                val start = parts[0].toInt()
                val end = parts[1].toInt()
                return if (start == end) SingletonLineRange(start) else RangedLineRange(start..end)
            }
    }
}
