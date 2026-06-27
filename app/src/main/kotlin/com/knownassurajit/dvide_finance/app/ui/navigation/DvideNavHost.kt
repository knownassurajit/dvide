package com.knownassurajit.dvide_finance.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.knownassurajit.dvide_finance.app.ui.MainViewModel
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.cycle.CycleDetailScreen
import com.knownassurajit.dvide_finance.app.ui.cycle.ArchiveScreen
import com.knownassurajit.dvide_finance.app.ui.dashboard.DashboardScreen
import com.knownassurajit.dvide_finance.app.ui.entry.AddTransactionSheet
import com.knownassurajit.dvide_finance.app.ui.settings.SettingsScreen
import com.knownassurajit.dvide_finance.app.ui.settings.ProfileScreen
import com.knownassurajit.dvide_finance.app.ui.onboarding.OnboardingScreen
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeSheet
import com.knownassurajit.dvide_finance.app.ui.cycle.AddCycleSheet

private const val ROUTE_HOME     = "home"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_CYCLE    = "cycle"
private const val ROUTE_PROFILE  = "profile"
private const val ROUTE_ARCHIVE  = "archive"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DvideNavHost(
    viewModel: MainViewModel = hiltViewModel(),
) {
    val settings    by viewModel.settings.collectAsStateWithLifecycle()
    val metrics     by viewModel.metrics.collectAsStateWithLifecycle()
    val highlightId by viewModel.highlightId.collectAsStateWithLifecycle()
    val viewWeekly  by viewModel.viewIsWeekly.collectAsStateWithLifecycle()
    val pastCycles  by viewModel.pastCycles.collectAsStateWithLifecycle()
    val allCycles   by viewModel.cycles.collectAsStateWithLifecycle()

    if (!settings.seeded) {
        OnboardingScreen(
            onComplete = { name, email, currencyCode, regionCode, weekStartDay, numberFormat ->
                viewModel.completeOnboarding(
                    name = name,
                    email = email,
                    currencyCode = currencyCode,
                    regionCode = regionCode,
                    weekStartDay = weekStartDay,
                    numberFormat = numberFormat
                )
            }
        )
        return
    }

    val navController   = rememberNavController()
    val navBackStack    by navController.currentBackStackEntryAsState()
    val currentRoute    = navBackStack?.destination?.route

    var showAddSheet by remember { mutableStateOf(false) }
    var showAddCycleSheet by remember { mutableStateOf(false) }

    // Slide transitions: enter from right, exit to left
    val enterPush  = slideInHorizontally(spring(stiffness = 300f)) { it }  + fadeIn()
    val exitPush   = slideOutHorizontally(spring(stiffness = 300f)) { -it / 4 } + fadeOut()
    val enterPop   = fadeIn()
    val exitPop    = slideOutHorizontally(spring(stiffness = 300f)) { it } + fadeOut()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            NavHost(
                navController    = navController,
                startDestination = ROUTE_HOME,
                modifier         = Modifier.fillMaxSize(),
            ) {
                composable(
                    route               = ROUTE_HOME,
                    enterTransition     = { enterPop },
                    exitTransition      = { exitPush },
                    popEnterTransition  = { enterPop },
                    popExitTransition   = { exitPop },
                ) {
                    DashboardScreen(
                        metrics       = metrics,
                        variant       = settings.dashboardVariant,
                        viewIsWeekly  = viewWeekly,
                        onViewChange  = viewModel::setViewIsWeekly,
                        userName      = settings.userName,
                        darkTheme     = settings.darkTheme,
                        highlightId   = highlightId,
                        onToggleTheme = viewModel::toggleTheme,
                        onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
                        onOpenCycle    = { navController.navigate(ROUTE_CYCLE) },
                        onOpenProfile  = { navController.navigate(ROUTE_PROFILE) },
                        onDeleteTransaction = viewModel::deleteTransaction,
                        onAddCycle = { showAddCycleSheet = true },
                        onAddTransaction = { showAddSheet = true },
                        onOpenArchive = { navController.navigate(ROUTE_ARCHIVE) }
                    )
                }

                composable(
                    route               = ROUTE_SETTINGS,
                    enterTransition     = { enterPush },
                    exitTransition      = { exitPush },
                    popEnterTransition  = { enterPop },
                    popExitTransition   = { exitPop },
                ) {
                    SettingsScreen(
                        settings            = settings,
                        onClose             = { navController.popBackStack() },
                        onToggleTheme       = viewModel::toggleTheme,
                        onVariantChange     = viewModel::setVariant,
                        onSeedHueChange     = viewModel::setSeedHue,
                        onOpenProfile       = { navController.navigate(ROUTE_PROFILE) },
                        onCurrencyChange    = viewModel::setCurrencyCode,
                        onRegionChange      = viewModel::setRegionCode,
                        onWeekStartChange   = viewModel::setWeekStartDay,
                        onNumberFormatChange = viewModel::setNumberFormat,
                    )
                }

                composable(
                    route               = ROUTE_PROFILE,
                    enterTransition     = { enterPush },
                    exitTransition      = { exitPush },
                    popEnterTransition  = { enterPop },
                    popExitTransition   = { exitPop },
                ) {
                    ProfileScreen(
                        settings        = settings,
                        onSave          = viewModel::updateProfile,
                        onClose         = { navController.popBackStack() },
                    )
                }

                 composable(
                    route               = ROUTE_ARCHIVE,
                    enterTransition     = { enterPush },
                    exitTransition      = { exitPush },
                    popEnterTransition  = { enterPop },
                    popExitTransition   = { exitPop },
                ) {
                    ArchiveScreen(
                        pastCycles = pastCycles,
                        onClose = { navController.popBackStack() }
                    )
                }

                composable(
                    route               = ROUTE_CYCLE,
                    enterTransition     = { enterPush },
                    exitTransition      = { exitPush },
                    popEnterTransition  = { enterPop },
                    popExitTransition   = { exitPop },
                ) {
                    CycleDetailScreen(
                        metrics    = metrics,
                        archive    = pastCycles,
                        onClose    = { navController.popBackStack() },
                    )
                }
            }
        }
    }

    if (showAddCycleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddCycleSheet = false },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape            = ShapeSheet,
            containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
            dragHandle       = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 38.dp, height = 5.dp),
                )
                BottomSheetDefaults.DragHandle()
            },
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text  = "New Cycle",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = { showAddCycleSheet = false }) {
                    Icon(CwIcons.Back, contentDescription = "Close")
                }
            }
            AddCycleSheet(
                existingCycles = allCycles,
                onAdd = { cycle ->
                    viewModel.addCycle(cycle)
                    showAddCycleSheet = false
                }
            )
        }
    }

    // Modal bottom sheet for Add Transaction
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape            = ShapeSheet,
            containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
            dragHandle       = {
                // Custom grip handle
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 38.dp, height = 5.dp),
                    // RoundedCornerShape drawn via Surface
                )
                BottomSheetDefaults.DragHandle()
            },
        ) {
            // Sheet header
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text  = "New entry",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = { showAddSheet = false }) {
                    Icon(CwIcons.Back, contentDescription = "Close")
                }
            }

            AddTransactionSheet(
                onAdd = { tx ->
                    viewModel.addTransaction(tx)
                    showAddSheet = false
                },
            )
        }
    }
}
