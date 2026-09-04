package com.knownassurajit.dvide_finance.app.ui.navigation

import android.content.Intent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import com.knownassurajit.dvide_finance.app.ui.MainViewModel
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.cycle.AddCycleSheet
import com.knownassurajit.dvide_finance.app.ui.cycle.ArchiveScreen
import com.knownassurajit.dvide_finance.app.ui.cycle.CycleDetailScreen
import com.knownassurajit.dvide_finance.app.ui.dashboard.DashboardScreen
import com.knownassurajit.dvide_finance.app.ui.entry.AddTransactionSheet
import com.knownassurajit.dvide_finance.app.ui.onboarding.OnboardingScreen
import com.knownassurajit.dvide_finance.app.ui.settings.ProfileScreen
import com.knownassurajit.dvide_finance.app.ui.settings.SettingsScreen
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeSheet
import com.knownassurajit.dvide_finance.app.ui.theme.dvideBottomBarPadding

private const val ROUTE_HOME     = "home"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_CYCLE    = "cycle/{cycleId}"
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
    val currentCycle by viewModel.currentCycle.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (!settings.seeded) {
        OnboardingScreen(
            onComplete = { name, email, currencyCode, regionCode, weekStartDay, numberFormat, payday, income ->
                viewModel.completeOnboarding(
                    name = name,
                    email = email,
                    currencyCode = currencyCode,
                    regionCode = regionCode,
                    weekStartDay = weekStartDay,
                    numberFormat = numberFormat,
                    payday = payday,
                    income = income,
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
    var editingCycle by remember { mutableStateOf<ManualCycle?>(null) }

    val enterPush  = slideInHorizontally(spring(stiffness = 300f)) { it }  + fadeIn()
    val exitPush   = slideOutHorizontally(spring(stiffness = 300f)) { -it / 4 } + fadeOut()
    val enterPop   = fadeIn()
    val exitPop    = slideOutHorizontally(spring(stiffness = 300f)) { it } + fadeOut()

    fun openCycle(cycleId: Long) {
        navController.navigate("cycle/$cycleId")
    }

    fun shareExport() {
        val csv = viewModel.exportCsv()
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "DVIDE ledger")
            putExtra(Intent.EXTRA_TEXT, csv)
        }
        context.startActivity(Intent.createChooser(send, "Export ledger"))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        // Keep zero so nested TopAppBar screens still receive system insets.
        // The FAB is lifted separately — zeroing this also zeros Scaffold's FAB inset.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (currentRoute == ROUTE_HOME && metrics != null) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.padding(bottom = dvideBottomBarPadding()),
                    onClick = { showAddSheet = true },
                    icon = { Icon(CwIcons.Plus, contentDescription = null) },
                    text = { Text("Add entry") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        },
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
                        highlightId   = highlightId,
                        onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
                        onOpenCycle    = {
                            val id = metrics?.sourceCycleId
                            if (id != null && id != 0L) openCycle(id)
                        },
                        onOpenProfile  = { navController.navigate(ROUTE_PROFILE) },
                        onDeleteTransaction = viewModel::deleteTransaction,
                        onAddCycle = { showAddCycleSheet = true },
                        onAddTransaction = { showAddSheet = true },
                        onOpenArchive = { navController.navigate(ROUTE_ARCHIVE) },
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
                        onDarkThemeChange   = viewModel::setDarkTheme,
                        onDynamicColorChange = viewModel::setDynamicColor,
                        onVariantChange     = viewModel::setVariant,
                        onSeedHueChange     = viewModel::setSeedHue,
                        onOpenProfile       = { navController.navigate(ROUTE_PROFILE) },
                        onCurrencyChange    = viewModel::setCurrencyCode,
                        onRegionChange      = viewModel::setRegionCode,
                        onWeekStartChange   = viewModel::setWeekStartDay,
                        onNumberFormatChange = viewModel::setNumberFormat,
                        onPaydayChange      = viewModel::setPayday,
                        onExport            = { shareExport() },
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
                        onClose = { navController.popBackStack() },
                        onOpenCycle = { past ->
                            if (past.cycleId != 0L) openCycle(past.cycleId)
                        },
                    )
                }

                composable(
                    route               = ROUTE_CYCLE,
                    arguments           = listOf(navArgument("cycleId") { type = NavType.LongType }),
                    enterTransition     = { enterPush },
                    exitTransition      = { exitPush },
                    popEnterTransition  = { enterPop },
                    popExitTransition   = { exitPop },
                ) { entry ->
                    val cycleId = entry.arguments?.getLong("cycleId") ?: 0L
                    val cycle = allCycles.firstOrNull { it.id == cycleId }
                    val detailMetrics = remember(cycle, transactions) {
                        cycle?.let {
                            com.knownassurajit.dvide_finance.app.domain.engine.CycleEngine.computeMetrics(
                                it, transactions, java.time.LocalDate.now()
                            )
                        }
                    }
                    CycleDetailScreen(
                        metrics    = detailMetrics,
                        cycle      = cycle,
                        onClose    = { navController.popBackStack() },
                        onEdit     = {
                            if (cycle != null) {
                                editingCycle = cycle
                                showAddCycleSheet = true
                            }
                        },
                        onDelete   = {
                            if (cycle != null) {
                                viewModel.deleteCycle(cycle)
                                navController.popBackStack()
                            }
                        },
                    )
                }
            }
        }
    }

    if (showAddCycleSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddCycleSheet = false
                editingCycle = null
            },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape            = ShapeSheet,
            containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
            dragHandle       = { BottomSheetDefaults.DragHandle() },
            contentWindowInsets = {
                WindowInsets.safeContent.only(WindowInsetsSides.Bottom)
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
            ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(start = DvideDimens.screen, end = DvideDimens.barInset),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text  = if (editingCycle != null) "Edit cycle" else "New cycle",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = {
                    showAddCycleSheet = false
                    editingCycle = null
                }) {
                    Icon(CwIcons.Close, contentDescription = "Close")
                }
            }
            AddCycleSheet(
                existingCycles = allCycles,
                editing = editingCycle,
                payday = settings.payday,
                weekStartDay = settings.weekStartDay,
                onAdd = { cycle ->
                    if (editingCycle != null) viewModel.updateCycle(cycle)
                    else viewModel.addCycle(cycle)
                    showAddCycleSheet = false
                    editingCycle = null
                },
            )
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape            = ShapeSheet,
            containerColor   = MaterialTheme.colorScheme.surfaceContainerHigh,
            dragHandle       = { BottomSheetDefaults.DragHandle() },
            contentWindowInsets = {
                WindowInsets.safeContent.only(WindowInsetsSides.Bottom)
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
            ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(start = DvideDimens.screen, end = DvideDimens.barInset),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text  = "New entry",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = { showAddSheet = false }) {
                    Icon(CwIcons.Close, contentDescription = "Close")
                }
            }

            AddTransactionSheet(
                cycleStart = currentCycle?.startDate,
                cycleEnd = currentCycle?.endDate,
                onAdd = { tx ->
                    viewModel.addTransaction(tx)
                    showAddSheet = false
                },
            )
            }
        }
    }
}
