package com.knownassurajit.dvide_finance.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.knownassurajit.dvide_finance.app.data.backup.BackupSettings
import com.knownassurajit.dvide_finance.app.domain.model.AppFont
import com.knownassurajit.dvide_finance.app.domain.model.DashboardVariant
import com.knownassurajit.dvide_finance.app.domain.model.SwipeAction
import com.knownassurajit.dvide_finance.app.domain.model.SwipeConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        val KEY_SEED_HUE         = intPreferencesKey("seed_hue")
        val KEY_DARK_THEME       = booleanPreferencesKey("dark_theme")
        val KEY_DASHBOARD_VARIANT= stringPreferencesKey("dashboard_variant")
        val KEY_USER_NAME        = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL       = stringPreferencesKey("user_email")
        val KEY_SEEDED           = booleanPreferencesKey("seeded")
        val KEY_CURRENCY_CODE    = stringPreferencesKey("currency_code")
        val KEY_REGION_CODE      = stringPreferencesKey("region_code")
        val KEY_WEEK_START_DAY   = intPreferencesKey("week_start_day")
        val KEY_NUMBER_FORMAT    = stringPreferencesKey("number_format")
        val KEY_SHOW_DECIMALS    = booleanPreferencesKey("show_decimals")
        val KEY_DYNAMIC_COLOR    = booleanPreferencesKey("dynamic_color")
        val KEY_PAYDAY           = intPreferencesKey("payday")
        val KEY_FONT_OPTION      = stringPreferencesKey("font_option")
        val KEY_PROFILE_PICTURE_URI = stringPreferencesKey("profile_picture_uri")
        val KEY_SWIPE_LEFT       = stringPreferencesKey("swipe_left_action")
        val KEY_SWIPE_RIGHT      = stringPreferencesKey("swipe_right_action")

        private fun defaultCurrencyCode(): String {
            return try {
                java.util.Currency.getInstance(java.util.Locale.getDefault()).currencyCode
            } catch (e: Exception) {
                "GBP"
            }
        }

        private fun defaultRegionCode(): String {
            val country = java.util.Locale.getDefault().country
            return if (country.isNullOrEmpty()) "GB" else country
        }
    }

    val settingsFlow: Flow<AppSettings> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            AppSettings(
                seedHue          = prefs[KEY_SEED_HUE]          ?: 300,
                darkTheme        = prefs[KEY_DARK_THEME]        ?: true,
                dashboardVariant = DashboardVariant.fromKey(prefs[KEY_DASHBOARD_VARIANT] ?: "editorial"),
                userName         = prefs[KEY_USER_NAME]         ?: "",
                userEmail        = prefs[KEY_USER_EMAIL]        ?: "",
                profilePictureUri = prefs[KEY_PROFILE_PICTURE_URI],
                seeded           = prefs[KEY_SEEDED]            ?: false,
                currencyCode     = prefs[KEY_CURRENCY_CODE]     ?: defaultCurrencyCode(),
                regionCode       = prefs[KEY_REGION_CODE]       ?: defaultRegionCode(),
                weekStartDay     = prefs[KEY_WEEK_START_DAY]     ?: 2, // Monday
                numberFormat     = prefs[KEY_NUMBER_FORMAT]     ?: "DEFAULT",
                showDecimals     = prefs[KEY_SHOW_DECIMALS]     ?: true,
                dynamicColor     = prefs[KEY_DYNAMIC_COLOR]     ?: false,
                payday           = prefs[KEY_PAYDAY]            ?: 25,
                fontOption       = AppFont.fromKey(prefs[KEY_FONT_OPTION]),
                swipeLeft        = SwipeAction.fromKey(prefs[KEY_SWIPE_LEFT], SwipeConfig.DEFAULT_LEFT),
                swipeRight       = SwipeAction.fromKey(prefs[KEY_SWIPE_RIGHT], SwipeConfig.DEFAULT_RIGHT),
                isLoaded         = true,
            )
        }


    suspend fun setSeedHue(hue: Int) = dataStore.edit { it[KEY_SEED_HUE] = hue }

    suspend fun setDarkTheme(dark: Boolean) = dataStore.edit { it[KEY_DARK_THEME] = dark }

    suspend fun setDashboardVariant(variant: DashboardVariant) =
        dataStore.edit { it[KEY_DASHBOARD_VARIANT] = variant.key }

    suspend fun setUserName(name: String) = dataStore.edit { it[KEY_USER_NAME] = name }

    suspend fun setUserEmail(email: String) = dataStore.edit { it[KEY_USER_EMAIL] = email }

    suspend fun setProfilePictureUri(uri: String?) = dataStore.edit {
        if (uri != null) it[KEY_PROFILE_PICTURE_URI] = uri
        else it.remove(KEY_PROFILE_PICTURE_URI)
    }

    suspend fun markSeeded() = dataStore.edit { it[KEY_SEEDED] = true }

    suspend fun setCurrencyCode(code: String) = dataStore.edit { it[KEY_CURRENCY_CODE] = code }

    suspend fun setRegionCode(code: String) = dataStore.edit { it[KEY_REGION_CODE] = code }

    suspend fun setWeekStartDay(day: Int) = dataStore.edit { it[KEY_WEEK_START_DAY] = day }

    suspend fun setNumberFormat(format: String) = dataStore.edit { it[KEY_NUMBER_FORMAT] = format }

    suspend fun setShowDecimals(show: Boolean) = dataStore.edit { it[KEY_SHOW_DECIMALS] = show }

    suspend fun setDynamicColor(enabled: Boolean) = dataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }

    suspend fun setPayday(day: Int) = dataStore.edit { it[KEY_PAYDAY] = day.coerceIn(1, 31) }

    suspend fun setFontOption(font: AppFont) = dataStore.edit { it[KEY_FONT_OPTION] = font.key }

    suspend fun setSwipeLeft(action: SwipeAction) = dataStore.edit { it[KEY_SWIPE_LEFT] = action.key }

    suspend fun setSwipeRight(action: SwipeAction) = dataStore.edit { it[KEY_SWIPE_RIGHT] = action.key }

    /** Preferences that belong in a backup file (no profile photo reference, no one-time flags). */
    suspend fun exportForBackup(): BackupSettings {
        val s = settingsFlow.first()
        return BackupSettings(
            seedHue = s.seedHue,
            darkTheme = s.darkTheme,
            dynamicColor = s.dynamicColor,
            dashboardVariant = s.dashboardVariant,
            fontOption = s.fontOption,
            currencyCode = s.currencyCode,
            regionCode = s.regionCode,
            weekStartDay = s.weekStartDay,
            numberFormat = s.numberFormat,
            showDecimals = s.showDecimals,
            payday = s.payday,
            swipeLeft = s.swipeLeft,
            swipeRight = s.swipeRight,
            userName = s.userName,
            userEmail = s.userEmail,
        )
    }

    /**
     * Applies a restored backup in one atomic DataStore edit. Fields that are null in [settings] are left as they
     * are on this device. Marks the app as set up, because a restored backup replaces onboarding.
     */
    suspend fun applyBackup(settings: BackupSettings) = dataStore.edit { p ->
        settings.seedHue?.let { p[KEY_SEED_HUE] = it }
        settings.darkTheme?.let { p[KEY_DARK_THEME] = it }
        settings.dynamicColor?.let { p[KEY_DYNAMIC_COLOR] = it }
        settings.dashboardVariant?.let { p[KEY_DASHBOARD_VARIANT] = it.key }
        settings.fontOption?.let { p[KEY_FONT_OPTION] = it.key }
        settings.currencyCode?.let { p[KEY_CURRENCY_CODE] = it }
        settings.regionCode?.let { p[KEY_REGION_CODE] = it }
        settings.weekStartDay?.let { p[KEY_WEEK_START_DAY] = it }
        settings.numberFormat?.let { p[KEY_NUMBER_FORMAT] = it }
        settings.showDecimals?.let { p[KEY_SHOW_DECIMALS] = it }
        settings.payday?.let { p[KEY_PAYDAY] = it.coerceIn(1, 31) }
        settings.swipeLeft?.let { p[KEY_SWIPE_LEFT] = it.key }
        settings.swipeRight?.let { p[KEY_SWIPE_RIGHT] = it.key }
        settings.userName?.let { p[KEY_USER_NAME] = it }
        settings.userEmail?.let { p[KEY_USER_EMAIL] = it }
        p[KEY_SEEDED] = true
    }
}

data class AppSettings(
    val seedHue: Int             = 300,
    val darkTheme: Boolean       = true,
    val dashboardVariant: DashboardVariant = DashboardVariant.EDITORIAL,
    val userName: String         = "",
    val userEmail: String        = "",
    val profilePictureUri: String? = null,
    val seeded: Boolean          = false,
    val currencyCode: String     = "GBP",
    val regionCode: String       = "GB",
    val weekStartDay: Int        = 2, // Monday
    val numberFormat: String     = "DEFAULT",
    val showDecimals: Boolean    = true,
    val dynamicColor: Boolean    = false,
    val payday: Int              = 25,
    val fontOption: AppFont      = AppFont.GOOGLE_SANS,
    val swipeLeft: SwipeAction   = SwipeConfig.DEFAULT_LEFT,
    val swipeRight: SwipeAction  = SwipeConfig.DEFAULT_RIGHT,
    /**
     * False only for the placeholder value used before DataStore has delivered its first snapshot. Startup code
     * must not draw (or pick onboarding vs. dashboard) from an unloaded instance, or the user sees default
     * values flash before their real ones.
     */
    val isLoaded: Boolean        = false,
) {
    val swipeConfig: SwipeConfig get() = SwipeConfig(swipeLeft, swipeRight)
}
