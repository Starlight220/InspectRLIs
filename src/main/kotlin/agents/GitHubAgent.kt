package io.starlight.inspector.agents

import io.starlight.inspector.InspectorEnv

class GitHubAgent(env: InspectorEnv) : Agent(env) {
    override val baseUrl: String
    override val baseUrlRegex: Regex

    init {
        val match = GITHUB_REGEX.toRegex().matchEntire(env.baseUrl)!!
        val (user, repo) = match.destructured

        baseUrl = """https://github.com/$user/$repo/raw"""
        baseUrlRegex =
            """https://(?:github.com/$user/$repo/raw)|(?:raw.githubusercontent.com/$user/$repo)"""
                .toRegex()
    }
}

const val GITHUB_REGEX = """^@github: ([a-z-A-Z0-9]+)/([a-z-A-Z0-9]+)/?$"""
