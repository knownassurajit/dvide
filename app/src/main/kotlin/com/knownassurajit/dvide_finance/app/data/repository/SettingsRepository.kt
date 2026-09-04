package com.knownassurajit.dvide_finance.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.knownassurajit.dvide_finance.app.domain.model.DashboardVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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
        val KEY_DYNAMIC_COLOR    = booleanPreferencesKey("dynamic_color")
        val KEY_PAYDAY           = intPreferencesKey("payday")

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
                seeded           = prefs[KEY_SEEDED]            ?: false,
                currencyCode     = prefs[KEY_CURRENCY_CODE]     ?: defaultCurrencyCode(),
                regionCode       = prefs[KEY_REGION_CODE]       ?: defaultRegionCode(),
                weekStartDay     = prefs[KEY_WEEK_START_DAY]     ?: 2, // Monday
                numberFormat     = prefs[KEY_NUMBER_FORMAT]     ?: "DEFAULT",
                dynamicColor     = prefs[KEY_DYNAMIC_COLOR]     ?: false,
                payday           = prefs[KEY_PAYDAY]            ?: 25,
            )
        }


    suspend fun setSeedHue(hue: Int) = dataStore.edit { it[KEY_SEED_HUE] = hue }

    suspend fun setDarkTheme(dark: Boolean) = dataStore.edit { it[KEY_DARK_THEME] = dark }

    suspend fun setDashboardVariant(variant: DashboardVariant) =
        dataStore.edit { it[KEY_DASHBOARD_VARIANT] = variant.key }

    suspend fun setUserName(name: String) = dataStore.edit { it[KEY_USER_NAME] = name }

    suspend fun setUserEmail(email: String) = dataStore.edit { it[KEY_USER_EMAIL] = email }

    suspend fun markSeeded() = dataStore.edit { it[KEY_SEEDED] = true }

    suspend fun setCurrencyCode(code: String) = dataStore.edit { it[KEY_CURRENCY_CODE] = code }

    suspend fun setRegionCode(code: String) = dataStore.edit { it[KEY_REGION_CODE] = code }

    suspend fun setWeekStartDay(day: Int) = dataStore.edit { it[KEY_WEEK_START_DAY] = day }

    suspend fun setNumberFormat(format: String) = dataStore.edit { it[KEY_NUMBER_FORMAT] = format }

    suspend fun setDynamicColor(enabled: Boolean) = dataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }

    suspend fun setPayday(day: Int) = dataStore.edit { it[KEY_PAYDAY] = day.coerceIn(1, 31) }
}

data class AppSettings(
    val seedHue: Int             = 300,
    val darkTheme: Boolean       = true,
    val dashboardVariant: DashboardVariant = DashboardVariant.EDITORIAL,
    val userName: String         = "",
    val userEmail: String        = "",
    val seeded: Boolean          = false,
    val currencyCode: String     = "GBP",
    val regionCode: String       = "GB",
    val weekStartDay: Int        = 2, // Monday
    val numberFormat: String     = "DEFAULT",
    val dynamicColor: Boolean    = false,
    val payday: Int              = 25,
)
