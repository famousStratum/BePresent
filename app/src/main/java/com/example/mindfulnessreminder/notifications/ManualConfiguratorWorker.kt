package com.example.mindfulnessreminder.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ManualConfiguratorWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val count = inputData.getInt("count", 1)
        val startHour = inputData.getInt("startHour", 9)
        val startMinute = inputData.getInt("startMinute", 0)
        val endHour = inputData.getInt("endHour", 21)
        val endMinute = inputData.getInt("endMinute", 0)

        val scheduler = NotificationScheduler(applicationContext)
        scheduler.scheduleRandomNotificationsForToday(count, startHour, startMinute, endHour, endMinute)
        
        return Result.success()
    }
}