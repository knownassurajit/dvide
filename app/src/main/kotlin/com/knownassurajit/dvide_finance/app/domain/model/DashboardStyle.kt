package com.knownassurajit.dvide_finance.app.domain.model

enum class DashboardStyle(val key: String, val label: String, val description: String) {
    CONTAINER("container", "Modern", "Tonal surface containers with clean contrast"),
    TINTED("tinted", "Vibrant", "Accent-tinted card backgrounds and glowing borders"),
    MINIMAL("minimal", "Minimal", "Crisp outlines with neutral dark surfaces");

    companion object {
        val DEFAULT = CONTAINER

        fun fromKey(key: String?): DashboardStyle =
            entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}
