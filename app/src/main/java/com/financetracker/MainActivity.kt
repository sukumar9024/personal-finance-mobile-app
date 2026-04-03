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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.financetracker.ui.navigation.FinanceNavHost
import com.financetracker.ui.theme.FinanceTrackerTheme
import com.financetracker.ui.theme.ThemeMode
import com.financetracker.ui.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ensureNotificationChannel()

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

                    FinanceNavHost(viewModel = viewModel)
                }
            }
        }
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
