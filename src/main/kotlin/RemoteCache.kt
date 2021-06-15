package io.starlight.inspector

import java.net.URL

/**
 * Caches remote file requests.
 *
 * This should add a significant performance improvement as it caches web requests to the same
 * remote file.
 */
object RemoteCache {
    private val map: MutableMap<String, String> = mutableMapOf()

    operator fun get(url: String): String = map.getOrPut(url) { URL(url).readText() }
    operator fun get(url: String, lines: LineRange): String =
        this[url]
            .lineSequence()
            .filterIndexed { i, _ -> i + 1 in lines }
            .joinToString(separator = "\n")
}
