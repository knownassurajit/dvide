package com.knownassurajit.dvide_finance.app.domain.model

enum class AppFont(val key: String, val label: String) {
    GOOGLE_SANS("google_sans", "Google Sans"),
    DM_SANS("dm_sans", "DM Sans"),
    GEIST("geist", "Geist"),
    INTER("inter", "Inter"),
    MANROPE("manrope", "Manrope"),
    PLUS_JAKARTA_SANS("plus_jakarta_sans", "Plus Jakarta Sans");

    companion object {
        fun fromKey(key: String?): AppFont =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: GOOGLE_SANS
    }
}
