package io.starlight.inspector

import io.starlight.inspector.agents.Agent

data class Location(val file: RliFile, val indexRange: IntRange) {
    fun compareTo(other: Location): Int {
        return this.file.compareTo(other.file).takeUnless { it == 0 }
            ?: this.indexRange.first.compareTo(other.indexRange.first)
    }

    override fun equals(other: Any?): Boolean = other is Location && this.compareTo(other) == 0
    override fun hashCode(): Int = 31 * file.hashCode() + indexRange.hashCode()

    val line by lazy {
        file.use {
            var counter = 1
            repeat(indexRange.first) { i -> if (it[i] == '\n') counter++ }
            return@use counter
        }
    }
}

data class Rli(val version: String, val url: String, val lines: LineRange) {
    val fullUrl: String = """${Agent.getCurrent().baseUrl}/$version/$url"""
    val response: String by lazy { RemoteCache[fullUrl, lines] }
    val withLatest by lazy { copy(version = Agent.getCurrent().latestVersion) }
}

typealias LocatedRli = Pair<Location, Rli>
