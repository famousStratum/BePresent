package com.versicode.bepresent.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlin.random.Random

class DailyConfiguratorWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val startHour = inputData.getInt("startHour", 9)
        val startMinute = inputData.getInt("startMinute", 0)
        val endHour = inputData.getInt("endHour", 21)
        val endMinute = inputData.getInt("endMinute", 0)

        val scheduler = NotificationScheduler(applicationContext)
        val count = Random.nextInt(1, 10)
        
        scheduler.scheduleRandomNotificationsForToday(count, startHour, startMinute, endHour, endMinute)
        
        return Result.success()
    }
}