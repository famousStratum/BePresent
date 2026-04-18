package com.versicode.bepresent.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class NotificationScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun scheduleDailyConfigurator(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        val inputData = Data.Builder()
            .putInt("startHour", startHour)
            .putInt("startMinute", startMinute)
            .putInt("endHour", endHour)
            .putInt("endMinute", endMinute)
            .build()

        // Run immediately for today
        val immediateRequest = OneTimeWorkRequestBuilder<DailyConfiguratorWorker>()
            .setInputData(inputData)
            .build()

        workManager.enqueueUniqueWork(
            IMMEDIATE_CONFIG_WORK,
            ExistingWorkPolicy.REPLACE,
            immediateRequest
        )

        // Then repeat daily, aligned to tomorrow's window start
        val dailyRequest = PeriodicWorkRequestBuilder<DailyConfiguratorWorker>(
            24, TimeUnit.HOURS
        )
            .setInputData(inputData)
            .setInitialDelay(millisUntilTomorrowWindowStart(startHour, startMinute), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            DAILY_CONFIG_WORK,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            dailyRequest
        )
    }

    fun scheduleManualConfigurator(count: Int, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        val inputData = Data.Builder()
            .putInt("count", count)
            .putInt("startHour", startHour)
            .putInt("startMinute", startMinute)
            .putInt("endHour", endHour)
            .putInt("endMinute", endMinute)
            .build()

        // Run immediately for today
        val immediateRequest = OneTimeWorkRequestBuilder<ManualConfiguratorWorker>()
            .setInputData(inputData)
            .build()

        workManager.enqueueUniqueWork(
            IMMEDIATE_CONFIG_WORK,
            ExistingWorkPolicy.REPLACE,
            immediateRequest
        )

        // Then repeat daily, aligned to tomorrow's window start
        val manualRequest = PeriodicWorkRequestBuilder<ManualConfiguratorWorker>(
            24, TimeUnit.HOURS
        )
            .setInputData(inputData)
            .setInitialDelay(millisUntilTomorrowWindowStart(startHour, startMinute), TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            DAILY_CONFIG_WORK,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            manualRequest
        )
    }

    fun scheduleRandomNotificationsForToday(count: Int, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        val startMillis = getMillisOfDay(startHour, startMinute)
        var endMillis = getMillisOfDay(endHour, endMinute)
        
        if (endMillis <= startMillis) {
            endMillis += TimeUnit.DAYS.toMillis(1)
        }

        val windowDuration = endMillis - startMillis
        val currentMillis = System.currentTimeMillis()

        for (i in 1..count) {
            val randomOffset = Random.nextLong(0, windowDuration)
            val targetTime = startMillis + randomOffset
            val delay = targetTime - currentMillis

            if (delay > 0) {
                val inputData = Data.Builder()
                    .putInt("notificationId", i)
                    .build()

                val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag(NOTIFICATION_WORK_TAG)
                    .build()

                workManager.enqueueUniqueWork(
                    "notification_work_$i",
                    ExistingWorkPolicy.REPLACE,
                    notificationRequest
                )
            }
        }
    }

    private fun getMillisOfDay(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun cancelAllReminders() {
        workManager.cancelUniqueWork(IMMEDIATE_CONFIG_WORK)
        workManager.cancelUniqueWork(DAILY_CONFIG_WORK)
        workManager.cancelAllWorkByTag(NOTIFICATION_WORK_TAG)
    }

    private fun millisUntilTomorrowWindowStart(startHour: Int, startMinute: Int): Long {
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return tomorrow.timeInMillis - System.currentTimeMillis()
    }

    companion object {
        const val DAILY_CONFIG_WORK = "daily_config_work"
        const val IMMEDIATE_CONFIG_WORK = "immediate_config_work"
        const val NOTIFICATION_WORK_TAG = "notification_work"
    }
}