package com.dvide.app.domain.model

enum class DashboardVariant(val key: String, val label: String) {
    EDITORIAL("editorial", "Editorial"),
    GAUGE    ("gauge",     "Gauge"),
    CARDS    ("cards",     "Cards");

    companion object {
        fun fromKey(key: String): DashboardVariant =
            entries.firstOrNull { it.key == key } ?: EDITORIAL
    }
}
