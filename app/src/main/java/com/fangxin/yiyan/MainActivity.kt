package com.fangxin.yiyan

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionConfig
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fangxin.yiyan.domain.UserRole
import com.fangxin.yiyan.service.ScreenShareService
import com.fangxin.yiyan.speech.SpeechController
import com.fangxin.yiyan.ui.components.AppSheet
import com.fangxin.yiyan.ui.components.HelpSheetContent
import com.fangxin.yiyan.ui.components.HistorySheetContent
import com.fangxin.yiyan.ui.components.PrivacySheetContent
import com.fangxin.yiyan.ui.screens.CameraScreen
import com.fangxin.yiyan.ui.screens.CheckScreen
import com.fangxin.yiyan.ui.screens.AssistantScreen
import com.fangxin.yiyan.ui.screens.GuardianAlertDetailScreen
import com.fangxin.yiyan.ui.screens.GuardianHomeScreen
import com.fangxin.yiyan.ui.screens.GuardianSettingsScreen
import com.fangxin.yiyan.ui.screens.GuardianWatchScreen
import com.fangxin.yiyan.ui.screens.GuideDetailScreen
import com.fangxin.yiyan.ui.screens.GuideListScreen
import com.fangxin.yiyan.ui.screens.HomeScreen
import com.fangxin.yiyan.ui.screens.ResultScreen
import com.fangxin.yiyan.ui.screens.RoleSelectScreen
import com.fangxin.yiyan.ui.screens.SamplesScreen
import com.fangxin.yiyan.ui.screens.SettingsScreen
import com.fangxin.yiyan.ui.screens.TextCheckScreen
import com.fangxin.yiyan.ui.theme.Card
import com.fangxin.yiyan.ui.theme.FangxinTheme
import com.fangxin.yiyan.ui.theme.Ink
import com.fangxin.yiyan.ui.theme.InkMute
import com.fangxin.yiyan.ui.theme.InkSoft
import com.fangxin.yiyan.ui.theme.Paper
import com.fangxin.yiyan.util.Intents
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FangxinApp() }
    }
}

private object Routes {
    const val HOME = "home"
    const val CHECK = "check"
    const val CAMERA = "camera"
    const val TEXT = "text"
    const val RESULT = "result"
    const val GUIDES = "guides"
    const val GUIDE = "guide/{guideId}"
    const val SAMPLES = "samples"
    const val SETTINGS = "settings"
    const val ASSISTANT = "assistant"

    fun guide(id: String): String = "guide/$id"
}

private object GuardianRoutes {
    const val HOME = "guardian_home"
    const val ALERT = "guardian_alert"
    const val WATCH = "guardian_watch"
    const val SETTINGS = "guardian_settings"
}

@Composable
private fun FangxinApp(viewModel: FangxinViewModel = viewModel()) {
    val settings by viewModel.settings.collectAsState()

    FangxinTheme(largeText = settings.largeText) {
        when (settings.role) {
            null -> RoleSelectScreen(
                onElder = { viewModel.setRole(UserRole.ELDER) },
                onGuardian = { viewModel.setRole(UserRole.GUARDIAN) },
            )
            UserRole.ELDER -> ElderApp(viewModel)
            UserRole.GUARDIAN -> GuardianApp(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ElderApp(viewModel: FangxinViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val history by viewModel.history.collectAsState()
    val currentResult by viewModel.currentResult.collectAsState()
    val screenRequested by viewModel.screenRequested.collectAsState()
    val screenSharing by viewModel.elderScreenSharing.collectAsState()
    val owl by viewModel.owl.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val speech = remember { SpeechController(context) }
    var sheet by remember { mutableStateOf<AppSheet?>(null) }

    val projectionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            ScreenShareService.start(context, result.resultCode, data)
        } else {
            viewModel.showMessage("不给看也行，让家人打电话问您。")
        }
    }
    val shareScreen: () -> Unit = {
        val manager = context.getSystemService(MediaProjectionManager::class.java)
        if (manager == null) {
            viewModel.showMessage("这个手机不能给家人看屏幕。")
        } else if (Build.VERSION.SDK_INT >= 34) {
            projectionLauncher.launch(
                manager.createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay()),
            )
        } else {
            projectionLauncher.launch(manager.createScreenCaptureIntent())
        }
    }

    DisposableEffect(Unit) {
        onDispose { speech.shutdown() }
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val popBack: () -> Unit = { navController.popBackStack() }
    val openResult: () -> Unit = {
        navController.navigate(Routes.RESULT) {
            popUpTo(Routes.HOME) { inclusive = false }
            launchSingleTop = true
        }
    }
    val goHome: () -> Unit = {
        navController.popBackStack(Routes.HOME, inclusive = false)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Paper,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            history = history,
                            largeText = settings.largeText,
                            onToggleText = { viewModel.setLargeText(!settings.largeText) },
                            onHelp = { sheet = AppSheet.HELP },
                            onCamera = { navController.navigate(Routes.CAMERA) },
                            onText = { navController.navigate(Routes.TEXT) },
                            onSample = { navController.navigate(Routes.SAMPLES) },
                            onGuides = { navController.navigate(Routes.GUIDES) },
                            onGuide = { id -> navController.navigate(Routes.guide(id)) },
                            onHistory = { sheet = AppSheet.HISTORY },
                            onSettings = { navController.navigate(Routes.SETTINGS) },
                            onPrivacy = { sheet = AppSheet.PRIVACY },
                            onDialHotline = { Intents.dial(context, Intents.HOTLINE) },
                            onAssistant = { navController.navigate(Routes.ASSISTANT) },
                            onOpenHistoryItem = { item ->
                                viewModel.openHistory(item)
                                openResult()
                            },
                        )
                    }

                    composable(Routes.ASSISTANT) {
                        AssistantScreen(
                            state = owl,
                            onBack = {
                                viewModel.resetOwl()
                                navController.popBackStack()
                            },
                            onAsk = viewModel::askOwl,
                            onReset = viewModel::resetOwl,
                            onDialHotline = { Intents.dial(context, Intents.HOTLINE) },
                        )
                    }

                    composable(Routes.CHECK) {
                        CheckScreen(
                            viewModel = viewModel,
                            onBack = popBack,
                            onCamera = { navController.navigate(Routes.CAMERA) },
                            onText = { navController.navigate(Routes.TEXT) },
                            onMessage = viewModel::showMessage,
                            onDone = openResult,
                        )
                    }

                    composable(Routes.CAMERA) {
                        CameraScreen(
                            viewModel = viewModel,
                            onBack = popBack,
                            onMessage = viewModel::showMessage,
                            onDone = openResult,
                        )
                    }

                    composable(Routes.TEXT) {
                        TextCheckScreen(
                            viewModel = viewModel,
                            onBack = popBack,
                            onMessage = viewModel::showMessage,
                            onDone = openResult,
                        )
                    }

                    composable(Routes.RESULT) {
                        ResultScreen(
                            result = currentResult,
                            speech = speech,
                            onHome = goHome,
                            onCheck = { navController.navigate(Routes.CHECK) },
                            onCamera = { navController.navigate(Routes.CAMERA) },
                            onText = { navController.navigate(Routes.TEXT) },
                            onGuides = { navController.navigate(Routes.GUIDES) },
                            onHelp = { sheet = AppSheet.HELP },
                            onDialHotline = { Intents.dial(context, Intents.HOTLINE) },
                            onMessage = viewModel::showMessage,
                            screenSharing = screenSharing,
                            onShareScreen = shareScreen,
                            onStopShareScreen = { ScreenShareService.stop(context) },
                        )
                    }

                    composable(Routes.GUIDES) {
                        GuideListScreen(
                            onBack = popBack,
                            onGuide = { id -> navController.navigate(Routes.guide(id)) },
                        )
                    }

                    composable(
                        route = Routes.GUIDE,
                        arguments = listOf(navArgument("guideId") { type = NavType.StringType }),
                    ) { entry ->
                        val guideId = entry.arguments?.getString("guideId").orEmpty()
                        LaunchedEffect(guideId) { viewModel.trackGuide(guideId) }
                        GuideDetailScreen(
                            guideId = guideId,
                            speech = speech,
                            onBack = popBack,
                            onGuideList = popBack,
                            onHome = goHome,
                            onHelp = { sheet = AppSheet.HELP },
                            onMessage = viewModel::showMessage,
                        )
                    }

                    composable(Routes.SAMPLES) {
                        SamplesScreen(
                            onBack = popBack,
                            onPick = { sample ->
                                viewModel.openSample(sample, onDone = openResult)
                            },
                        )
                    }

                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            settings = settings,
                            onBack = popBack,
                            onSavePhone = viewModel::saveFamilyPhone,
                            onToggleLargeText = viewModel::setLargeText,
                            onClearHistory = viewModel::clearHistory,
                            onRegenerateCode = viewModel::regeneratePairingCode,
                            onSwitchRole = { viewModel.setRole(UserRole.GUARDIAN) },
                            onToggleFloating = viewModel::setFloating,
                            onMessage = viewModel::showMessage,
                            onPrivacy = { sheet = AppSheet.PRIVACY },
                            onHelp = { sheet = AppSheet.HELP },
                        )
                    }
                }
            }
        }

        val activeSheet = sheet
        if (activeSheet != null) {
            ModalBottomSheet(
                onDismissRequest = { sheet = null },
                containerColor = Card,
                contentColor = Ink,
            ) {
                when (activeSheet) {
                    AppSheet.HELP -> HelpSheetContent(
                        familyPhone = settings.familyPhone,
                        onDialFamily = { phone -> Intents.dial(context, phone) },
                        onDialHotline = { Intents.dial(context, Intents.HOTLINE) },
                        onDialSocial = { Intents.dial(context, Intents.SOCIAL_SECURITY) },
                        onSettings = {
                            sheet = null
                            navController.navigate(Routes.SETTINGS)
                        },
                        onClose = { sheet = null },
                    )

                    AppSheet.HISTORY -> HistorySheetContent(
                        items = history,
                        onOpen = { item ->
                            sheet = null
                            viewModel.openHistory(item)
                            openResult()
                        },
                        onGoCheck = {
                            sheet = null
                            navController.navigate(Routes.CHECK)
                        },
                        onClear = viewModel::clearHistory,
                        onClose = { sheet = null },
                    )

                    AppSheet.PRIVACY -> PrivacySheetContent(onClose = { sheet = null })
                }
            }
        }

        if (screenRequested) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissScreenRequest() },
                containerColor = Card,
                title = {
                    Text("家人想看看您的屏幕", fontSize = 23.sp, fontWeight = FontWeight.Black, color = Ink)
                },
                text = {
                    Text(
                        "给您看一眼，好教您怎么办。最多 5 分钟，随时能停，画面不留。",
                        fontSize = 18.sp,
                        lineHeight = 27.sp,
                        color = InkSoft,
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.dismissScreenRequest()
                            shareScreen()
                        },
                    ) {
                        Text("同意", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissScreenRequest() }) {
                        Text("先不用", fontSize = 20.sp, color = InkMute)
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuardianApp(viewModel: FangxinViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val status by viewModel.linkStatus.collectAsState()
    val alerts by viewModel.guardianAlerts.collectAsState()
    val currentAlert by viewModel.currentAlert.collectAsState()
    val frame by viewModel.guardianFrame.collectAsState()
    val screenOn by viewModel.guardianScreenOn.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    var sheet by remember { mutableStateOf<AppSheet?>(null) }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val popBack: () -> Unit = { navController.popBackStack() }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Paper,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = GuardianRoutes.HOME,
                ) {
                    composable(GuardianRoutes.HOME) {
                        GuardianHomeScreen(
                            paired = settings.guardianPaired,
                            room = settings.guardianRoom,
                            connected = status.connected,
                            elderOnline = status.peerOnline,
                            alerts = alerts,
                            onConnect = viewModel::pairGuardian,
                            onOpenAlert = { alert ->
                                viewModel.openGuardianAlert(alert)
                                navController.navigate(GuardianRoutes.ALERT)
                            },
                            onWatch = { navController.navigate(GuardianRoutes.WATCH) },
                            onSettings = { navController.navigate(GuardianRoutes.SETTINGS) },
                            onPrivacy = { sheet = AppSheet.PRIVACY },
                        )
                    }

                    composable(GuardianRoutes.ALERT) {
                        val alert = currentAlert
                        if (alert == null) {
                            LaunchedEffect(Unit) { navController.popBackStack() }
                        } else {
                            GuardianAlertDetailScreen(
                                alert = alert,
                                screenOn = screenOn,
                                onBack = popBack,
                                onWatch = { navController.navigate(GuardianRoutes.WATCH) },
                                onDialHotline = { Intents.dial(context, Intents.HOTLINE) },
                            )
                        }
                    }

                    composable(GuardianRoutes.WATCH) {
                        GuardianWatchScreen(
                            screenOn = screenOn,
                            frame = frame,
                            onBack = popBack,
                            onRequestScreen = viewModel::requestGuardianScreen,
                        )
                    }

                    composable(GuardianRoutes.SETTINGS) {
                        GuardianSettingsScreen(
                            settings = settings,
                            onBack = popBack,
                            onUnpair = viewModel::unpairGuardian,
                            onClearAlerts = viewModel::clearGuardianAlerts,
                            onSwitchRole = { viewModel.setRole(UserRole.ELDER) },
                            onToggleFloating = viewModel::setFloating,
                            onMessage = viewModel::showMessage,
                            onPrivacy = { sheet = AppSheet.PRIVACY },
                        )
                    }
                }
            }
        }

        if (sheet == AppSheet.PRIVACY) {
            ModalBottomSheet(
                onDismissRequest = { sheet = null },
                containerColor = Card,
                contentColor = Ink,
            ) {
                PrivacySheetContent(onClose = { sheet = null })
            }
        }
    }
}
