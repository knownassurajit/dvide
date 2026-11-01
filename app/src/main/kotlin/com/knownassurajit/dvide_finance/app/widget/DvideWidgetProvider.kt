package com.knownassurajit.dvide_finance.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.knownassurajit.dvide_finance.app.MainActivity
import com.knownassurajit.dvide_finance.app.R
import com.knownassurajit.dvide_finance.app.data.repository.CycleRepository
import com.knownassurajit.dvide_finance.app.data.repository.SettingsRepository
import com.knownassurajit.dvide_finance.app.data.repository.TransactionRepository
import com.knownassurajit.dvide_finance.app.domain.engine.CycleEngine
import com.knownassurajit.dvide_finance.app.ui.theme.CurrencyFormatter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class DvideWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun cycleRepository(): CycleRepository
        fun transactionRepository(): TransactionRepository
        fun settingsRepository(): SettingsRepository
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, DvideWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            if (ids.isNotEmpty()) {
                updateWidgets(context, appWidgetManager, ids)
            }
        }

        private fun updateWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        WidgetEntryPoint::class.java,
                    )
                    val cycleRepo = entryPoint.cycleRepository()
                    val txRepo = entryPoint.transactionRepository()
                    val settingsRepo = entryPoint.settingsRepository()

                    val settings = settingsRepo.settingsFlow.first()
                    val cycles = cycleRepo.getAll()
                    val transactions = txRepo.getAll()
                    val today = LocalDate.now()

                    val currentCycle = CycleEngine.resolveCurrentCycle(cycles, today)
                    val metrics = currentCycle?.let { CycleEngine.computeMetrics(it, transactions, today) }

                    val formatter = CurrencyFormatter(
                        currencyCode = settings.currencyCode,
                        regionCode = settings.regionCode,
                        numberFormat = settings.numberFormat,
                        showDecimals = settings.showDecimals,
                    )

                    for (widgetId in appWidgetIds) {
                        val views = RemoteViews(context.packageName, R.layout.widget_dvide_balance)

                        val intent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                        )
                        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                        if (metrics != null) {
                            views.setTextViewText(R.id.widget_days_left, metrics.daysLeftShort)
                            views.setTextViewText(R.id.widget_cycle_balance, formatter.format(metrics.balance))
                            views.setTextViewText(R.id.widget_daily_quota, formatter.format(metrics.safeToSpend))
                            views.setTextViewText(R.id.widget_weekly_quota, formatter.format(metrics.safeToSpend * 7.0))
                        } else {
                            views.setTextViewText(R.id.widget_days_left, "No cycle")
                            views.setTextViewText(R.id.widget_cycle_balance, "--")
                            views.setTextViewText(R.id.widget_daily_quota, "--")
                            views.setTextViewText(R.id.widget_weekly_quota, "--")
                        }

                        appWidgetManager.updateAppWidget(widgetId, views)
                    }
                } catch (_: Exception) {
                    // Ignore background widget update failures
                }
            }
        }
    }
}
