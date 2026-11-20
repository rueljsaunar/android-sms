package com.personal.sms

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.personal.sms.di.AppContainer
import com.personal.sms.notification.EXTRA_OPEN_THREAD_ADDRESS
import com.personal.sms.ui.AppNavHost
import com.personal.sms.ui.MainViewModel
import com.personal.sms.ui.Routes
import com.personal.sms.ui.onboarding.DefaultSmsOnboardingScreen
import com.personal.sms.ui.permissions.AppPermissions
import com.personal.sms.ui.permissions.PermissionRationaleDialog
import com.personal.sms.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    private val pendingAddress = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        val container = (application as SmsApp).container
        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot(container = container, pendingAddress = pendingAddress)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val address = intent?.getStringExtra(EXTRA_COMPOSE_ADDRESS)
            ?: intent?.getStringExtra(EXTRA_OPEN_THREAD_ADDRESS)
        if (!address.isNullOrBlank()) pendingAddress.value = address
    }
}

@Composable
private fun AppRoot(container: AppContainer, pendingAddress: MutableState<String?>) {
    val context = LocalContext.current
    val mainViewModel: MainViewModel = viewModel(factory = MainViewModel.factory(container))
    val isDefault by mainViewModel.isDefault.collectAsState()
    val navController = rememberNavController()

    // Re-check default-app status whenever we return to the foreground.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) mainViewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { mainViewModel.refresh() }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { }

    var skippedDefault by rememberSaveable { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }
    val showApp = isDefault || skippedDefault

    // Request runtime permissions (with rationale) once the main app is showing.
    LaunchedEffect(showApp) {
        if (showApp && !AppPermissions.allGranted(context)) showRationale = true
    }

    // Route to the thread when opened from a notification tap or the sendto intent.
    LaunchedEffect(pendingAddress.value, showApp) {
        val address = pendingAddress.value
        if (address != null && showApp) {
            navController.navigate(Routes.thread(0L, address))
            pendingAddress.value = null
        }
    }

    if (!showApp) {
        DefaultSmsOnboardingScreen(
            onSetDefault = { roleLauncher.launch(mainViewModel.requestDefaultIntent()) },
            onNotNow = { skippedDefault = true },
        )
    } else {
        AppNavHost(
            container = container,
            navController = navController,
            onRequestDefault = { roleLauncher.launch(mainViewModel.requestDefaultIntent()) },
        )
    }

    if (showRationale) {
        PermissionRationaleDialog(
            onContinue = {
                showRationale = false
                permissionLauncher.launch(AppPermissions.missing(context).toTypedArray())
            },
            onDismiss = { showRationale = false },
        )
    }
}
