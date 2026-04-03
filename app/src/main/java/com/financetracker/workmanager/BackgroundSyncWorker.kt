package com.financetracker.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.financetracker.data.repository.GoogleSheetsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackgroundSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = GoogleSheetsRepository(context)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Perform background sync
                repository.syncData()
                Result.success()
            } catch (e: Exception) {
                if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        }
    }
}