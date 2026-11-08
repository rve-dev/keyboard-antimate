package dev.rve.keyboardantimate

/**
 * Extension point for an optional AI-backed soft-word suggestion service.
 *
 * The provider must only be called after the user explicitly enables AI fallback
 * and accepts that the current word may be sent to the configured service.
 * Do not put API keys in the Android app; use a protected backend.
 */
interface SoftWordProvider {
    fun suggestReplacement(
        word: String,
        languageTag: String,
        callback: (String?) -> Unit
    )
}

/** Used until the project owner supplies and reviews an API contract. */
class NoOpSoftWordProvider : SoftWordProvider {
    override fun suggestReplacement(
        word: String,
        languageTag: String,
        callback: (String?) -> Unit
    ) {
        callback(null)
    }
}
