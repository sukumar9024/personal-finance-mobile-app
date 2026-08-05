package com.financetracker

import android.annotation.SuppressLint
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.financetracker.ui.navigation.FinanceNavHost
import com.financetracker.ui.theme.FinanceTrackerTheme
import com.financetracker.ui.theme.ThemeMode
import com.financetracker.ui.viewmodel.ExpenseViewModel
import com.financetracker.workmanager.RecurringReminderWorker
import java.util.concurrent.TimeUnit

class MainActivity : FragmentActivity() {
    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ensureNotificationChannel()
        RecurringReminderWorker.ensureReminderChannel(this)
        scheduleRecurringReminderChecks()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val darkTheme = when (uiState.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            FinanceTrackerTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var biometricUnlocked by rememberSaveable(uiState.biometricLockEnabled) {
                        mutableStateOf(!uiState.biometricLockEnabled)
                    }
                    val notificationPermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { granted ->
                        if (granted) {
                            uiState.overspendingAlert?.let { alert ->
                                postOverspendingNotification(alert.title, alert.message)
                            }
                        }
                        viewModel.consumeOverspendingAlert()
                    }

                    LaunchedEffect(uiState.overspendingAlert?.token) {
                        val alert = uiState.overspendingAlert ?: return@LaunchedEffect
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            postOverspendingNotification(alert.title, alert.message)
                            viewModel.consumeOverspendingAlert()
                        }
                    }

                    LaunchedEffect(uiState.recurringEntries.any { it.reminderEnabled }) {
                        if (uiState.recurringEntries.any { it.reminderEnabled } &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    LaunchedEffect(uiState.biometricLockEnabled) {
                        if (uiState.biometricLockEnabled && !biometricUnlocked) {
                            showBiometricPrompt(
                                onAuthenticated = { biometricUnlocked = true },
                                onUnavailable = {
                                    viewModel.setBiometricLockEnabled(false)
                                    biometricUnlocked = true
                                }
                            )
                        }
                    }

                    if (biometricUnlocked) {
                        FinanceNavHost(viewModel = viewModel)
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                                Text("Finance Tracker is locked", style = MaterialTheme.typography.titleLarge)
                                Text("Authenticate with your device credential to continue.", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showBiometricPrompt(
        onAuthenticated: () -> Unit,
        onUnavailable: () -> Unit
    ) {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            onUnavailable()
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Finance Tracker")
            .setSubtitle("Use biometrics or device credential")
            .setAllowedAuthenticators(authenticators)
            .build()

        BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthenticated()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS ||
                        errorCode == BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL ||
                        errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE
                    ) {
                        onUnavailable()
                    }
                }
            }
        ).authenticate(promptInfo)
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            OVERSPENDING_CHANNEL_ID,
            "Budget alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Alerts when monthly or category budgets are exceeded."
        }
        manager.createNotificationChannel(channel)
    }

    private fun scheduleRecurringReminderChecks() {
        val request = PeriodicWorkRequestBuilder<RecurringReminderWorker>(12, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            RecurringReminderWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun postOverspendingNotification(title: String, message: String) {
        if (!canPostNotifications()) return

        val notification = NotificationCompat.Builder(this, OVERSPENDING_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notifyOverspending(notification)
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun notifyOverspending(notification: android.app.Notification) {
        NotificationManagerCompat.from(this).notify(OVERSPENDING_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val OVERSPENDING_CHANNEL_ID = "budget_alerts"
        private const val OVERSPENDING_NOTIFICATION_ID = 1001
    }
}
