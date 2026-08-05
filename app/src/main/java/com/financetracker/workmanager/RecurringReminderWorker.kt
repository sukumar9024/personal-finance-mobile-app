package com.financetracker.workmanager

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.financetracker.R
import com.financetracker.data.model.Currency
import com.financetracker.data.model.RecurringReminderStatus
import com.financetracker.data.model.RecurringType
import com.financetracker.data.repository.GoogleSheetsRepository
import com.financetracker.notifications.RecurringReminderActionReceiver
import java.time.LocalDate
import java.time.YearMonth

class RecurringReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = GoogleSheetsRepository(context)

    override suspend fun doWork(): Result {
        ensureReminderChannel(applicationContext)
        if (!canPostNotifications(applicationContext)) return Result.success()

        val cachedData = repository.loadCachedData() ?: return Result.success()
        val today = LocalDate.now()
        val currentPeriod = YearMonth.from(today).toString()
        val statuses = repository.loadRecurringReminderOccurrences()
        val statusByEntry = statuses.associateBy { "${it.entryId}|${it.period}" }

        cachedData.recurringEntries
            .filter { it.active && it.reminderEnabled }
            .filter { entry ->
                val dueDay = entry.dayOfMonth.coerceAtMost(YearMonth.from(today).lengthOfMonth())
                val reminderDate = YearMonth.from(today).atDay(dueDay).minusDays(entry.reminderDaysBefore.toLong())
                today == reminderDate
            }
            .filter { entry ->
                statusByEntry["${entry.id}|$currentPeriod"]?.status != RecurringReminderStatus.PAID &&
                    statusByEntry["${entry.id}|$currentPeriod"]?.status != RecurringReminderStatus.SKIPPED
            }
            .forEach { entry ->
                postReminder(entryId = entry.id, period = currentPeriod)
            }

        return Result.success()
    }

    private fun postReminder(entryId: String, period: String) {
        val cachedData = repository.loadCachedData() ?: return
        val entry = cachedData.recurringEntries.firstOrNull { it.id == entryId } ?: return
        val currency = Currency.getDefault()
        val title = "${entry.title} is due soon"
        val amountText = "${currency.symbol}${entry.amount}"
        val body = buildString {
            append(amountText)
            append(" • ")
            append(if (entry.type == RecurringType.INCOME) "Income" else entry.category ?: "Expense")
            if (entry.paymentMethod.isNotBlank()) append(" • ${entry.paymentMethod}")
        }

        val paidIntent = RecurringReminderActionReceiver.intent(
            context = applicationContext,
            action = RecurringReminderActionReceiver.ACTION_MARK_PAID,
            entryId = entry.id,
            period = period
        )
        val skippedIntent = RecurringReminderActionReceiver.intent(
            context = applicationContext,
            action = RecurringReminderActionReceiver.ACTION_MARK_SKIPPED,
            entryId = entry.id,
            period = period
        )
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(
                R.mipmap.ic_launcher,
                "Paid",
                PendingIntent.getBroadcast(applicationContext, "${entry.id}|paid|$period".hashCode(), paidIntent, flags)
            )
            .addAction(
                R.mipmap.ic_launcher,
                "Skip",
                PendingIntent.getBroadcast(applicationContext, "${entry.id}|skip|$period".hashCode(), skippedIntent, flags)
            )
            .build()

        notifyReminder(applicationContext, "${entry.id}|$period".hashCode(), notification)
    }

    companion object {
        const val UNIQUE_WORK_NAME = "recurring_reminder_check"
        const val CHANNEL_ID = "recurring_reminders"

        fun ensureReminderChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recurring reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders before recurring expenses and income are due."
            }
            manager.createNotificationChannel(channel)
        }

        fun canPostNotifications(context: Context): Boolean {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }

        @Suppress("MissingPermission")
        fun notifyReminder(context: Context, notificationId: Int, notification: android.app.Notification) {
            if (canPostNotifications(context)) {
                NotificationManagerCompat.from(context).notify(notificationId, notification)
            }
        }
    }
}
