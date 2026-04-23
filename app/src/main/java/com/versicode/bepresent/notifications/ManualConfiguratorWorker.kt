package com.versicode.bepresent.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlin.random.Random

class ManualConfiguratorWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val tier = inputData.getInt("count", 1)
        val startHour = inputData.getInt("startHour", 9)
        val startMinute = inputData.getInt("startMinute", 0)
        val endHour = inputData.getInt("endHour", 21)
        val endMinute = inputData.getInt("endMinute", 0)

        val count = when (tier) {
            1 -> Random.nextInt(1, 4)   // 1–3
            2 -> Random.nextInt(4, 7)   // 4–6
            else -> Random.nextInt(7, 10) // 7–9
        }

        val scheduler = NotificationScheduler(applicationContext)
        scheduler.scheduleRandomNotificationsForToday(count, startHour, startMinute, endHour, endMinute)
        
        return Result.success()
    }
}