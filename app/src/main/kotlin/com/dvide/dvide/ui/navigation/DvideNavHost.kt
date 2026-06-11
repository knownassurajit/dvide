package com.dvide.app.ui.navigation

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
import com.dvide.app.ui.MainViewModel
import com.dvide.app.ui.components.CwIcons
import com.dvide.app.ui.cycle.CycleDetailScreen
import com.dvide.app.ui.dashboard.DashboardScreen
import com.dvide.app.ui.entry.AddTransactionSheet
import com.dvide.app.ui.settings.SettingsScreen
import com.dvide.app.ui.settings.ProfileScreen
import com.dvide.app.ui.onboarding.OnboardingScreen
import com.dvide.app.ui.theme.ShapeSheet

private const val ROUTE_HOME     = "home"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_CYCLE    = "cycle"
private const val ROUTE_PROFILE  = "profile"

@Composable
fun DvideNavHost(
    viewModel: MainViewModel = hiltViewModel(),
) {
    val settings    by viewModel.settings.collectAsStateWithLifecycle()
    val metrics     by viewModel.metrics.collectAsStateWithLifecycle()
    val highlightId by viewModel.highlightId.collectAsStateWithLifecycle()
    val viewWeekly  by viewModel.viewIsWeekly.collectAsStateWithLifecycle()
    val pastCycles  by viewModel.pastCycles.collectAsStateWithLifecycle()

    if (!settings.seeded) {
        OnboardingScreen(
            onComplete = { name, email, income, anchorDay, currencyCode, regionCode, weekStartDay, numberFormat ->
                viewModel.completeOnboarding(
                    name = name,
                    email = email,
                    income = income,
                    anchorDay = anchorDay,
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

    // Slide transitions: enter from right, exit to left
    val enterPush  = slideInHorizontally(spring(stiffness = 300f)) { it }  + fadeIn()
    val exitPush   = slideOutHorizontally(spring(stiffness = 300f)) { -it / 4 } + fadeOut()
    val enterPop   = fadeIn()
    val exitPop    = slideOutHorizontally(spring(stiffness = 300f)) { it } + fadeOut()

    Scaffold(
        floatingActionButton = {
            // FAB only on home screen
            AnimatedVisibility(
                visible = currentRoute == ROUTE_HOME,
                enter   = scaleIn(spring(stiffness = 400f)) + fadeIn(),
                exit    = scaleOut() + fadeOut(),
            ) {
                LargeFab(onClick = { showAddSheet = true })
            }
        },
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
                        onIncomeChange      = viewModel::setIncome,
                        onAnchorDayChange   = viewModel::setAnchorDay,
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
                    route               = ROUTE_CYCLE,
                    enterTransition     = { enterPush },
                    exitTransition      = { exitPush },
                    popEnterTransition  = { enterPop },
                    popExitTransition   = { exitPop },
                ) {
                    CycleDetailScreen(
                        metrics    = metrics,
                        income     = settings.income,
                        anchorDay  = settings.anchorDay,
                        archive    = pastCycles,
                        onClose    = { navController.popBackStack() },
                    )
                }
            }
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

@Composable
private fun LargeFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick        = onClick,
        modifier       = Modifier
            .navigationBarsPadding()
            .padding(end = 4.dp, bottom = 12.dp),
        shape          = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor   = MaterialTheme.colorScheme.onPrimary,
        elevation      = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
    ) {
        Icon(
            imageVector        = CwIcons.Plus,
            contentDescription = "Add transaction",
            modifier           = Modifier.size(28.dp),
        )
    }
}
