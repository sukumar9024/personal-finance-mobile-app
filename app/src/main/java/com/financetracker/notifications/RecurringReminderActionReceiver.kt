package com.financetracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.financetracker.data.repository.GoogleSheetsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecurringReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val entryId = intent.getStringExtra(EXTRA_ENTRY_ID).orEmpty()
        val period = intent.getStringExtra(EXTRA_PERIOD).orEmpty()
        if (entryId.isBlank() || period.isBlank()) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val repository = GoogleSheetsRepository(context.applicationContext)
                when (intent.action) {
                    ACTION_MARK_PAID -> repository.markRecurringOccurrencePaid(entryId, period)
                    ACTION_MARK_SKIPPED -> repository.markRecurringOccurrenceSkipped(entryId, period)
                }
                NotificationManagerCompat.from(context).cancel("$entryId|$period".hashCode())
            }
            pendingResult.finish()
        }
    }

    companion object {
        const val ACTION_MARK_PAID = "com.financetracker.recurring.MARK_PAID"
        const val ACTION_MARK_SKIPPED = "com.financetracker.recurring.MARK_SKIPPED"
        private const val EXTRA_ENTRY_ID = "entry_id"
        private const val EXTRA_PERIOD = "period"

        fun intent(
            context: Context,
            action: String,
            entryId: String,
            period: String
        ): Intent {
            return Intent(context, RecurringReminderActionReceiver::class.java).apply {
                this.action = action
                putExtra(EXTRA_ENTRY_ID, entryId)
                putExtra(EXTRA_PERIOD, period)
            }
        }
    }
}
