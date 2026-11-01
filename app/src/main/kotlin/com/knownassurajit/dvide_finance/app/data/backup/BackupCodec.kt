package com.knownassurajit.dvide_finance.app.data.backup

import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.domain.model.AppFont
import com.knownassurajit.dvide_finance.app.domain.model.DashboardVariant
import com.knownassurajit.dvide_finance.app.domain.model.EntryLimits
import com.knownassurajit.dvide_finance.app.domain.model.SwipeAction
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Versioned JSON backup format ("dvide-backup", version 1).
 *
 * ```
 * { "format": "dvide-backup", "version": 1, "exportedAt": "<ISO-8601 instant>", "appVersion": "…",
 *   "settings":     { seedHue, darkTheme, dynamicColor, dashboardVariant, fontOption, currencyCode, regionCode,
 *                     weekStartDay, numberFormat, showDecimals, payday, swipeLeft, swipeRight, userName, userEmail },
 *   "cycles":       [ { id, month, year, startDate, endDate, income } ],
 *   "transactions": [ { id, date, category, kind, amount, note, color?, cycleId? } ] }
 * ```
 *
 * Rules: user data (cycles, transactions) is validated strictly and a single bad record rejects the whole file —
 * there is no partial restore. Preferences are validated leniently (bad value → skipped + warning). A
 * transaction may reference a cycle that no longer exists: deleting a cycle intentionally keeps its entries.
 * Fields unknown to this version are ignored so older apps can read files from newer minor revisions.
 */
object BackupCodec {
    const val FORMAT = "dvide-backup"
    const val VERSION = 1
    const val MAX_CYCLES = 10_000
    const val MAX_TRANSACTIONS = 500_000

    private const val MAX_MONEY = 1.0e12
    private val KINDS = setOf("expense", "aside", "income")

    // ─────────────────────────── encode ───────────────────────────

    fun encode(snapshot: BackupSnapshot, exportedAt: Instant, appVersion: String): String {
        val root = JSONObject()
        root.put("format", FORMAT)
        root.put("version", VERSION)
        root.put("exportedAt", exportedAt.toString())
        root.put("appVersion", appVersion)
        root.put("settings", encodeSettings(snapshot.settings))

        val cycles = JSONArray()
        snapshot.cycles.sortedBy { it.id }.forEach { c ->
            require(c.income.isFinite()) { "Cycle ${c.id} has a non-finite income" }
            cycles.put(
                JSONObject()
                    .put("id", c.id)
                    .put("month", c.month)
                    .put("year", c.year)
                    .put("startDate", c.startDate.toString())
                    .put("endDate", c.endDate.toString())
                    .put("income", c.income)
            )
        }
        root.put("cycles", cycles)

        val txs = JSONArray()
        snapshot.transactions.sortedBy { it.id }.forEach { t ->
            require(t.amount.isFinite()) { "Entry ${t.id} has a non-finite amount" }
            val o = JSONObject()
                .put("id", t.id)
                .put("date", t.date.toString())
                .put("category", t.category)
                .put("kind", t.kind)
                .put("amount", t.amount)
                .put("note", t.note)
            t.color?.let { o.put("color", it) }
            t.cycleId?.let { o.put("cycleId", it) }
            txs.put(o)
        }
        root.put("transactions", txs)
        return root.toString(2)
    }

    private fun encodeSettings(s: BackupSettings): JSONObject {
        val o = JSONObject()
        s.seedHue?.let { o.put("seedHue", it) }
        s.darkTheme?.let { o.put("darkTheme", it) }
        s.dynamicColor?.let { o.put("dynamicColor", it) }
        s.dashboardVariant?.let { o.put("dashboardVariant", it.key) }
        s.fontOption?.let { o.put("fontOption", it.key) }
        s.currencyCode?.let { o.put("currencyCode", it) }
        s.regionCode?.let { o.put("regionCode", it) }
        s.weekStartDay?.let { o.put("weekStartDay", it) }
        s.numberFormat?.let { o.put("numberFormat", it) }
        s.showDecimals?.let { o.put("showDecimals", it) }
        s.payday?.let { o.put("payday", it) }
        s.swipeLeft?.let { o.put("swipeLeft", it.key) }
        s.swipeRight?.let { o.put("swipeRight", it.key) }
        s.userName?.let { o.put("userName", it) }
        s.userEmail?.let { o.put("userEmail", it) }
        return o
    }

    // ─────────────────────────── decode ───────────────────────────

    fun decode(text: String): BackupParse = try {
        decodeOrThrow(text)
    } catch (e: BackupFormatException) {
        BackupParse.Failure(e.message ?: "The backup file could not be read.")
    }

    private class BackupFormatException(message: String) : Exception(message)

    private fun fail(message: String): Nothing = throw BackupFormatException(message)

    private fun decodeOrThrow(text: String): BackupParse.Success {
        val root = try {
            JSONObject(text)
        } catch (e: JSONException) {
            fail("This file isn't a DVIDE backup (it is not valid JSON).")
        }
        if (root.opt("format") != FORMAT) fail("This file isn't a DVIDE backup.")

        val version = (root.opt("version") as? Number)?.toInt()
            ?: fail("The backup has no format version.")
        if (version < 1) fail("The backup has an invalid format version ($version).")
        if (version > VERSION) {
            fail("This backup was made by a newer version of DVIDE (format $version). Update the app to restore it.")
        }

        val cyclesJson = root.opt("cycles") as? JSONArray ?: fail("The backup is missing its cycles list.")
        val txJson = root.opt("transactions") as? JSONArray ?: fail("The backup is missing its entries list.")
        if (cyclesJson.length() > MAX_CYCLES) fail("The backup has too many cycles (limit $MAX_CYCLES).")
        if (txJson.length() > MAX_TRANSACTIONS) fail("The backup has too many entries (limit $MAX_TRANSACTIONS).")

        val cycles = ArrayList<ManualCycle>(cyclesJson.length())
        val seenCycleIds = HashSet<Long>()
        for (i in 0 until cyclesJson.length()) {
            val where = "Cycle ${i + 1}"
            val o = cyclesJson.opt(i) as? JSONObject ?: fail("$where is not an object.")
            val id = o.requireLong("id", where)
            if (id < 1) fail("$where has an invalid id.")
            if (!seenCycleIds.add(id)) fail("$where repeats id $id.")
            val month = o.requireLong("month", where)
            if (month !in 1..12) fail("$where has an invalid month.")
            val year = o.requireLong("year", where)
            if (year !in 1900..2200) fail("$where has an invalid year.")
            val start = o.requireDate("startDate", where)
            val end = o.requireDate("endDate", where)
            if (end.isBefore(start)) fail("$where ends before it starts.")
            val income = o.requireMoney("income", where, allowZero = true)
            cycles += ManualCycle(id, month.toInt(), year.toInt(), start, end, income)
        }

        val txs = ArrayList<Transaction>(txJson.length())
        val seenTxIds = HashSet<Long>()
        for (i in 0 until txJson.length()) {
            val where = "Entry ${i + 1}"
            val o = txJson.opt(i) as? JSONObject ?: fail("$where is not an object.")
            val id = o.requireLong("id", where)
            if (id < 1) fail("$where has an invalid id.")
            if (!seenTxIds.add(id)) fail("$where repeats id $id.")
            val category = o.requireString("category", where)
            if (category.isBlank() || category.length > EntryLimits.CATEGORY_MAX) {
                fail("$where has an empty or too long category.")
            }
            val kind = o.requireString("kind", where)
            if (kind !in KINDS) fail("$where has an unknown kind \"$kind\".")
            val note = o.optionalString("note", where) ?: ""
            if (note.length > EntryLimits.NOTE_MAX) fail("$where has a note longer than ${EntryLimits.NOTE_MAX} characters.")
            val color = o.optionalLong("color", where)?.also {
                if (it !in Int.MIN_VALUE..Int.MAX_VALUE) fail("$where has an invalid color.")
            }?.toInt()
            // cycleId may point at a deleted cycle: the app keeps entries when a cycle is removed.
            val cycleId = o.optionalLong("cycleId", where)?.also { if (it < 1) fail("$where has an invalid cycle reference.") }
            txs += Transaction(
                id = id,
                date = o.requireDate("date", where),
                category = category,
                kind = kind,
                amount = o.requireMoney("amount", where, allowZero = true),
                note = note,
                color = color,
                cycleId = cycleId,
            )
        }

        val warnings = ArrayList<String>()
        val settings = decodeSettings(root.opt("settings"), warnings)
        val meta = BackupMeta(
            version = version,
            exportedAt = root.opt("exportedAt") as? String,
            appVersion = root.opt("appVersion") as? String,
        )
        return BackupParse.Success(BackupSnapshot(cycles, txs, settings), meta, warnings)
    }

    private fun decodeSettings(raw: Any?, warnings: MutableList<String>): BackupSettings {
        if (raw == null) {
            warnings += "The backup has no settings; your current settings were kept."
            return BackupSettings()
        }
        val o = raw as? JSONObject ?: run {
            warnings += "The backup's settings could not be read; your current settings were kept."
            return BackupSettings()
        }

        fun bool(key: String): Boolean? = when (val v = o.opt(key)) {
            null, JSONObject.NULL -> null
            is Boolean -> v
            else -> { warnings += "Setting \"$key\" was ignored (expected true/false)."; null }
        }
        fun int(key: String, range: IntRange): Int? = when (val v = o.opt(key)) {
            null, JSONObject.NULL -> null
            is Int, is Long -> (v as Number).toLong().let { n ->
                if (n in range.first.toLong()..range.last.toLong()) n.toInt()
                else { warnings += "Setting \"$key\" was ignored (out of range)."; null }
            }
            else -> { warnings += "Setting \"$key\" was ignored (expected a whole number)."; null }
        }
        fun text(key: String, max: Int, pattern: Regex? = null): String? = when (val v = o.opt(key)) {
            null, JSONObject.NULL -> null
            is String -> if (v.length <= max && (pattern == null || pattern.matches(v))) v
                         else { warnings += "Setting \"$key\" was ignored (invalid value)."; null }
            else -> { warnings += "Setting \"$key\" was ignored (expected text)."; null }
        }
        fun <E> keyed(key: String, parse: (String) -> E?): E? {
            val s = text(key, 64) ?: return null
            return parse(s) ?: run { warnings += "Setting \"$key\" has an unknown value \"$s\" and was ignored."; null }
        }

        return BackupSettings(
            seedHue = int("seedHue", 0..359),
            darkTheme = bool("darkTheme"),
            dynamicColor = bool("dynamicColor"),
            dashboardVariant = keyed("dashboardVariant") { k -> DashboardVariant.entries.firstOrNull { it.key == k } },
            fontOption = keyed("fontOption") { k -> AppFont.entries.firstOrNull { it.key == k } },
            currencyCode = text("currencyCode", 3, Regex("[A-Z]{3}")),
            regionCode = text("regionCode", 2, Regex("[A-Z]{2}")),
            // Calendar.SUNDAY (1), MONDAY (2) and SATURDAY (7) are the only supported week starts.
            weekStartDay = int("weekStartDay", 1..7)?.let { d ->
                if (d == 1 || d == 2 || d == 7) d
                else { warnings += "Setting \"weekStartDay\" was ignored (unsupported value)."; null }
            },
            numberFormat = text("numberFormat", 32, Regex("[A-Z_]+")),
            showDecimals = bool("showDecimals"),
            payday = int("payday", 1..31),
            swipeLeft = keyed("swipeLeft") { k -> SwipeAction.entries.firstOrNull { it.key == k } },
            swipeRight = keyed("swipeRight") { k -> SwipeAction.entries.firstOrNull { it.key == k } },
            userName = text("userName", EntryLimits.PROFILE_NAME_MAX),
            userEmail = text("userEmail", EntryLimits.PROFILE_EMAIL_MAX),
        )
    }

    // ─────────────────────────── strict field helpers ───────────────────────────

    private fun JSONObject.requireString(key: String, where: String): String =
        opt(key) as? String ?: fail("$where: \"$key\" must be text.")

    private fun JSONObject.optionalString(key: String, where: String): String? = when (val v = opt(key)) {
        null, JSONObject.NULL -> null
        is String -> v
        else -> fail("$where: \"$key\" must be text.")
    }

    private fun JSONObject.requireLong(key: String, where: String): Long =
        optionalLong(key, where) ?: fail("$where: \"$key\" is missing.")

    private fun JSONObject.optionalLong(key: String, where: String): Long? = when (val v = opt(key)) {
        null, JSONObject.NULL -> null
        is Int -> v.toLong()
        is Long -> v
        else -> fail("$where: \"$key\" must be a whole number.")
    }

    private fun JSONObject.requireDate(key: String, where: String): LocalDate {
        val s = requireString(key, where)
        return try {
            LocalDate.parse(s)
        } catch (e: DateTimeParseException) {
            fail("$where: \"$key\" is not a valid date (expected yyyy-MM-dd).")
        }
    }

    private fun JSONObject.requireMoney(key: String, where: String, allowZero: Boolean): Double {
        val v = (opt(key) as? Number)?.toDouble() ?: fail("$where: \"$key\" must be a number.")
        if (!v.isFinite() || v > MAX_MONEY || v < 0.0 || (!allowZero && v == 0.0)) {
            fail("$where: \"$key\" is out of range.")
        }
        return v
    }
}
