package io.starlight.inspector

sealed class RliStatus(val rli: Rli, val location: Location) {
    constructor(base: LocatedRli) : this(base.second, base.first)

    class Valid(base: LocatedRli) : RliStatus(base) {
        override fun invoke() {
            Report.outdated(rli, location)
            location.file.replaceRange(location.indexRange, Constants.latestVersion)
        }
    }

    class Invalid(base: LocatedRli, val diff: Diff) : RliStatus(base) {
        override fun invoke() {
            Report.invalid(this)
        }
    }

    abstract operator fun invoke()
}

val LocatedRli.status: RliStatus
    get() =
        with(second) {
            val new =
                Rli(url.replace(Regex(Constants.versionScheme), Constants.latestVersion), lines)
            val diff = this diff new
            if (diff == null) {
                RliStatus.Valid(this@status)
            } else {
                RliStatus.Invalid(this@status, diff)
            }
        }
