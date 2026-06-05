package com.dvide.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.dvide.app.domain.model.DashboardVariant
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
        val KEY_INCOME           = doublePreferencesKey("income")
        val KEY_ANCHOR_DAY       = intPreferencesKey("anchor_day")
        val KEY_SEED_HUE         = intPreferencesKey("seed_hue")
        val KEY_DARK_THEME       = booleanPreferencesKey("dark_theme")
        val KEY_DASHBOARD_VARIANT= stringPreferencesKey("dashboard_variant")
        val KEY_USER_NAME        = stringPreferencesKey("user_name")
        val KEY_SEEDED           = booleanPreferencesKey("seeded")
    }

    val settingsFlow: Flow<AppSettings> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            AppSettings(
                income           = prefs[KEY_INCOME]            ?: 3200.0,
                anchorDay        = prefs[KEY_ANCHOR_DAY]        ?: 25,
                seedHue          = prefs[KEY_SEED_HUE]          ?: 300,
                darkTheme        = prefs[KEY_DARK_THEME]        ?: true,
                dashboardVariant = DashboardVariant.fromKey(prefs[KEY_DASHBOARD_VARIANT] ?: "editorial"),
                userName         = prefs[KEY_USER_NAME]         ?: "Sam",
                seeded           = prefs[KEY_SEEDED]            ?: false,
            )
        }

    suspend fun setIncome(income: Double) = dataStore.edit { it[KEY_INCOME] = income }

    suspend fun setAnchorDay(day: Int) = dataStore.edit { it[KEY_ANCHOR_DAY] = day }

    suspend fun setSeedHue(hue: Int) = dataStore.edit { it[KEY_SEED_HUE] = hue }

    suspend fun setDarkTheme(dark: Boolean) = dataStore.edit { it[KEY_DARK_THEME] = dark }

    suspend fun setDashboardVariant(variant: DashboardVariant) =
        dataStore.edit { it[KEY_DASHBOARD_VARIANT] = variant.key }

    suspend fun setUserName(name: String) = dataStore.edit { it[KEY_USER_NAME] = name }

    suspend fun markSeeded() = dataStore.edit { it[KEY_SEEDED] = true }
}

data class AppSettings(
    val income: Double           = 3200.0,
    val anchorDay: Int           = 25,
    val seedHue: Int             = 300,
    val darkTheme: Boolean       = true,
    val dashboardVariant: DashboardVariant = DashboardVariant.EDITORIAL,
    val userName: String         = "Sam",
    val seeded: Boolean          = false,
)
