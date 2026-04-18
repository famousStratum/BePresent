package com.versicode.bepresent.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val notificationId = inputData.getInt("notificationId", 1)
        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.sendNotification(notificationId)
        return Result.success()
    }
}