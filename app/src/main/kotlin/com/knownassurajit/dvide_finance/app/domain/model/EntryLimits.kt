package com.knownassurajit.dvide_finance.app.domain.model

/**
 * Single source of truth for user-text limits. Room stores these as unbounded TEXT, so the limits are
 * enforced at input, in the ViewModel, and when validating a backup — never by silent truncation on read.
 */
object EntryLimits {
    const val NOTE_MAX = 200
    const val CATEGORY_MAX = 40
    const val PROFILE_NAME_MAX = 60
    const val PROFILE_EMAIL_MAX = 120

    /**
     * Live-typing filter: line breaks become spaces (the fields are logically single-line) and the text is
     * cut to [max] UTF-16 units without splitting a surrogate pair (emoji). Does not trim, so typing stays natural.
     */
    fun clampInput(raw: String, max: Int): String {
        val flat = raw.replace("\r\n", " ").replace('\n', ' ').replace('\r', ' ')
        if (flat.length <= max) return flat
        var end = max
        if (end > 0 && Character.isHighSurrogate(flat[end - 1])) end--
        return flat.substring(0, end)
    }

    /** Final value to persist: clamped and trimmed. */
    fun normalize(raw: String, max: Int): String = clampInput(raw, max).trim()
}
