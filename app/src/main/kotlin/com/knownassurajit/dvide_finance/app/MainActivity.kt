package com.knownassurajit.dvide_finance.app

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knownassurajit.dvide_finance.app.ui.MainViewModel
import com.knownassurajit.dvide_finance.app.ui.navigation.DvideNavHost
import com.knownassurajit.dvide_finance.app.ui.theme.DvideTheme
import com.knownassurajit.dvide_finance.app.ui.theme.LocalSwipeConfig
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            // System bars follow the in-app theme, not the OS theme: otherwise a light app theme on a dark
            // system (or vice versa) gets dark icons on a dark 3-button-navigation scrim.
            DisposableEffect(settings.darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT, Color.TRANSPARENT,
                    ) { settings.darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        LIGHT_SCRIM, DARK_SCRIM,
                    ) { settings.darkTheme },
                )
                onDispose {}
            }

            DvideTheme(
                seedHue      = settings.seedHue,
                darkTheme    = settings.darkTheme,
                dynamicColor = settings.dynamicColor,
                currencyCode = settings.currencyCode,
                regionCode   = settings.regionCode,
                weekStartDay = settings.weekStartDay,
                numberFormat = settings.numberFormat,
                showDecimals = settings.showDecimals,
                fontOption   = settings.fontOption,
            ) {
                CompositionLocalProvider(LocalSwipeConfig provides settings.swipeConfig) {
                    DvideNavHost(viewModel = viewModel)
                }
            }
        }

        holdFirstFrameUntilReady()
    }

    override fun onStart() {
        super.onStart()
        // The date may have changed while the app was in the background.
        viewModel.refreshToday()
    }

    /**
     * Cold-start flash fix. Persisted settings and data load asynchronously, so the very first composition used
     * to run with defaults (dark theme, "not set up yet" → onboarding, "no cycle" → empty state) and then jump to
     * the user's real state. Cancelling the pre-draw pass keeps the window on its splash background until the
     * ViewModel reports ready, so the first frame that is actually drawn is already correct. The window system
     * re-attempts the draw every frame, so no manual re-invalidation is needed. [STARTUP_TIMEOUT_MS] bounds the
     * wait so a failed load can never leave the user on a blank screen.
     */
    private fun holdFirstFrameUntilReady() {
        val content = findViewById<View>(android.R.id.content)
        val deadline = SystemClock.uptimeMillis() + STARTUP_TIMEOUT_MS
        content.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                val ready = viewModel.isReady.value || SystemClock.uptimeMillis() >= deadline
                if (ready) content.viewTreeObserver.removeOnPreDrawListener(this)
                return ready
            }
        })
    }

    private companion object {
        const val STARTUP_TIMEOUT_MS = 2_000L
        // Same values androidx.activity uses for its default scrims.
        val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
