package com.knownassurajit.dvide_finance.app.ui.navigation

import android.content.Intent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.knownassurajit.dvide_finance.app.data.model.ManualCycle
import com.knownassurajit.dvide_finance.app.domain.model.ExportFormat
import com.knownassurajit.dvide_finance.app.data.model.Transaction
import com.knownassurajit.dvide_finance.app.ui.MainViewModel
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.cycle.AddCycleSheet
import com.knownassurajit.dvide_finance.app.ui.cycle.AddFundsSheet
import com.knownassurajit.dvide_finance.app.ui.cycle.ArchiveScreen
import com.knownassurajit.dvide_finance.app.ui.cycle.CycleDetailScreen
import com.knownassurajit.dvide_finance.app.ui.dashboard.DashboardScreen
import com.knownassurajit.dvide_finance.app.ui.entry.AddTransactionSheet
import com.knownassurajit.dvide_finance.app.ui.onboarding.OnboardingScreen
import com.knownassurajit.dvide_finance.app.ui.settings.ProfileScreen
import com.knownassurajit.dvide_finance.app.ui.settings.SettingsScreen
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import com.knownassurajit.dvide_finance.app.ui.theme.M3XMotion
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeSheet
import com.knownassurajit.dvide_finance.app.ui.theme.dvideBottomBarPadding
import com.knownassurajit.dvide_finance.app.ui.theme.m3xPressEffect

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
    var showAddFundsSheet by remember { mutableStateOf(false) }
    var showFabActionsSheet by remember { mutableStateOf(false) }
    var editingCycle by remember { mutableStateOf<ManualCycle?>(null) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    val enterPush = M3XMotion.enterPush()
    val exitPush  = M3XMotion.exitPush()
    val enterPop  = M3XMotion.enterPop()
    val exitPop   = M3XMotion.exitPop()

    fun openCycle(cycleId: Long) {
        navController.navigate("cycle/$cycleId")
    }

    fun shareExport(format: ExportFormat = ExportFormat.CSV) {
        val content = viewModel.exportLedger(format)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_SUBJECT, "DVIDE ledger (${format.extension.uppercase()})")
            putExtra(Intent.EXTRA_TEXT, content)
        }
        context.startActivity(Intent.createChooser(send, "Export ledger (${format.label})"))
    }

    fun shareCycleExport(cycleId: Long, cycleLabel: String?, format: ExportFormat = ExportFormat.CSV) {
        val content = viewModel.exportCycle(cycleId, format) ?: return
        val send = Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_SUBJECT, "DVIDE cycle - ${cycleLabel ?: "cycle-$cycleId"} (${format.extension.uppercase()})")
            putExtra(Intent.EXTRA_TEXT, content)
        }
        context.startActivity(Intent.createChooser(send, "Export cycle (${format.label})"))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        // Keep zero so nested TopAppBar screens still receive system insets.
        // The FAB is lifted separately — zeroing this also zeros Scaffold's FAB inset.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (currentRoute == ROUTE_HOME && metrics != null) {
                Surface(
                    modifier = Modifier
                        .padding(bottom = dvideBottomBarPadding())
                        .m3xPressEffect()
                        .clip(CircleShape)
                        .combinedClickable(
                            role = Role.Button,
                            onClickLabel = "Add entry",
                            onLongClickLabel = "More options",
                            onClick = {
                                editingTransaction = null
                                showAddSheet = true
                            },
                            onLongClick = {
                                showFabActionsSheet = true
                            },
                        ),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shadowElevation = 6.dp,
                ) {
                    Box(
                        modifier = Modifier.size(56.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = CwIcons.Plus,
                            contentDescription = "Add entry",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
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
                        profilePictureUri = settings.profilePictureUri,
                        onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
                        onOpenCycle    = {
                            val id = metrics?.sourceCycleId
                            if (id != null && id != 0L) openCycle(id)
                        },
                        onOpenProfile  = { navController.navigate(ROUTE_PROFILE) },
                        onDeleteTransaction = viewModel::deleteTransaction,
                        onEditTransaction = { tx ->
                            editingTransaction = tx
                            showAddSheet = true
                        },
                        onAddCycle = { showAddCycleSheet = true },
                        onAddTransaction = {
                            editingTransaction = null
                            showAddSheet = true
                        },
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
                        onShowDecimalsChange = viewModel::setShowDecimals,
                        onOpenProfile       = { navController.navigate(ROUTE_PROFILE) },
                        onCurrencyChange    = viewModel::setCurrencyCode,
                        onRegionChange      = viewModel::setRegionCode,
                        onWeekStartChange   = viewModel::setWeekStartDay,
                        onNumberFormatChange = viewModel::setNumberFormat,
                        onFontChange        = viewModel::setFontOption,
                        onPaydayChange      = viewModel::setPayday,
                        onExport            = { shareExport(ExportFormat.CSV) },
                        onExportFormat      = { format: ExportFormat -> shareExport(format) },
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
                        onSave          = { name, email, pic -> viewModel.updateProfile(name, email, pic) },
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
                        onExport   = {
                            val label = cycle?.let { "${it.startDate} – ${it.endDate}" }
                            shareCycleExport(cycleId, label)
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
            onDismissRequest = {
                showAddSheet = false
                editingTransaction = null
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
                    text  = if (editingTransaction != null) "Edit entry" else "New entry",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = {
                    showAddSheet = false
                    editingTransaction = null
                }) {
                    Icon(CwIcons.Close, contentDescription = "Close")
                }
            }

            AddTransactionSheet(
                cycleId = currentCycle?.id,
                cycleStart = currentCycle?.startDate,
                cycleEnd = currentCycle?.endDate,
                editing = editingTransaction,
                onUpdate = { updatedTx ->
                    viewModel.updateTransaction(updatedTx)
                    showAddSheet = false
                    editingTransaction = null
                },
                onAdd = { tx ->
                    viewModel.addTransaction(tx)
                    showAddSheet = false
                    editingTransaction = null
                },
            )
            }
        }
    }

    if (showFabActionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFabActionsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = ShapeSheet,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            contentWindowInsets = { WindowInsets.safeContent.only(WindowInsetsSides.Bottom) },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DvideDimens.screen)
                    .padding(bottom = DvideDimens.section),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Quick actions",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            showFabActionsSheet = false
                            editingTransaction = null
                            showAddSheet = true
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(
                            imageVector = CwIcons.Plus,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column {
                            Text(
                                text = "New entry",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Log an expense or set-aside allocation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            showFabActionsSheet = false
                            showAddFundsSheet = true
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(
                            imageVector = CwIcons.Wallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column {
                            Text(
                                text = "Add funds to cycle",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Top up your spendable pool with bonus or income",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            showFabActionsSheet = false
                            editingCycle = null
                            showAddCycleSheet = true
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(
                            imageVector = CwIcons.Calendar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column {
                            Text(
                                text = "New pay cycle",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Configure next salary cycle dates and income",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddFundsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddFundsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = ShapeSheet,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            contentWindowInsets = { WindowInsets.safeContent.only(WindowInsetsSides.Bottom) },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = DvideDimens.screen, end = DvideDimens.barInset),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Add funds to cycle",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    IconButton(onClick = { showAddFundsSheet = false }) {
                        Icon(CwIcons.Close, contentDescription = "Close")
                    }
                }
                AddFundsSheet(
                    onAddFunds = { amount, note ->
                        viewModel.addFundsToCurrentCycle(amount, note)
                        showAddFundsSheet = false
                    },
                )
            }
        }
    }
}
