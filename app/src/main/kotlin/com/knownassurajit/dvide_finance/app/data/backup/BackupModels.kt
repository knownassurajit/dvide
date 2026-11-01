package com.knownassurajit.dvide_finance.app.data.backup

import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.domain.model.AppFont
import com.knownassurajit.dvide_finance.app.domain.model.DashboardVariant
import com.knownassurajit.dvide_finance.app.domain.model.SwipeAction

/**
 * The preferences that travel with a backup. Everything is nullable on purpose: a field that is missing or
 * invalid in a file is left untouched on the device instead of being reset. Authentication tokens, the
 * profile photo reference and the one-time `seeded` flag are deliberately not part of a backup.
 */
data class BackupSettings(
    val seedHue: Int? = null,
    val darkTheme: Boolean? = null,
    val dynamicColor: Boolean? = null,
    val dashboardVariant: DashboardVariant? = null,
    val fontOption: AppFont? = null,
    val currencyCode: String? = null,
    val regionCode: String? = null,
    val weekStartDay: Int? = null,
    val numberFormat: String? = null,
    val showDecimals: Boolean? = null,
    val payday: Int? = null,
    val swipeLeft: SwipeAction? = null,
    val swipeRight: SwipeAction? = null,
    val userName: String? = null,
    val userEmail: String? = null,
)

data class BackupSnapshot(
    val cycles: List<ManualCycle>,
    val transactions: List<Transaction>,
    val settings: BackupSettings,
)

/** Header information shown to the user before they confirm a restore. */
data class BackupMeta(
    val version: Int,
    val exportedAt: String?,
    val appVersion: String?,
)

sealed interface BackupParse {
    data class Success(
        val snapshot: BackupSnapshot,
        val meta: BackupMeta,
        /** Non-fatal problems (e.g. an unknown preference value that was skipped). */
        val warnings: List<String>,
    ) : BackupParse

    /** [reason] is written for end users. */
    data class Failure(val reason: String) : BackupParse
}
