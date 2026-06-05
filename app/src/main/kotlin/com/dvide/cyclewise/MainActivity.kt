package com.dvide.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dvide.app.ui.MainViewModel
import com.dvide.app.ui.navigation.CyclewiseNavHost
import com.dvide.app.ui.theme.CyclewiseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            CyclewiseTheme(
                seedHue   = settings.seedHue,
                darkTheme = settings.darkTheme,
            ) {
                CyclewiseNavHost(viewModel = viewModel)
            }
        }
    }
}
