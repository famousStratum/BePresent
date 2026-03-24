package com.versicode.bepresent

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.versicode.bepresent.notifications.NotificationHelper
import com.versicode.bepresent.notifications.NotificationScheduler
import com.versicode.bepresent.ui.theme.BePresentTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var notificationScheduler: NotificationScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()
        notificationScheduler = NotificationScheduler(this)

        enableEdgeToEdge()
        setContent {
            BePresentTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BePresentScreen(
                        modifier = Modifier.padding(innerPadding),
                        onScheduleManual = { count, startH, startM, endH, endM ->
                            notificationScheduler.scheduleManualConfigurator(count, startH, startM, endH, endM)
                        },
                        onScheduleRandom = { startH, startM, endH, endM ->
                            notificationScheduler.scheduleDailyConfigurator(startH, startM, endH, endM)
                        },
                        onCancel = {
                            notificationScheduler.cancelAllReminders()
                        }
                    )
                }
            }
        }
    }
}

enum class ReminderMode {
    RANDOM, MANUAL
}

@Composable
fun BePresentScreen(
    modifier: Modifier = Modifier,
    onScheduleManual: (Int, Int, Int, Int, Int) -> Unit,
    onScheduleRandom: (Int, Int, Int, Int) -> Unit,
    onCancel: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(false) }
    var reminderMode by remember { mutableStateOf(ReminderMode.RANDOM) }
    var sliderValue by remember { mutableFloatStateOf(1f) }
    var showInfoDialog by remember { mutableStateOf(false) }

    var startHour by remember { mutableStateOf(9) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(21) }
    var endMinute by remember { mutableStateOf(0) }

    val context = LocalContext.current

    // Automatically update scheduling when settings change
    LaunchedEffect(isEnabled, reminderMode, sliderValue, startHour, startMinute, endHour, endMinute) {
        if (isEnabled) {
            when (reminderMode) {
                ReminderMode.RANDOM -> onScheduleRandom(startHour, startMinute, endHour, endMinute)
                ReminderMode.MANUAL -> onScheduleManual(sliderValue.roundToInt(), startHour, startMinute, endHour, endMinute)
            }
        } else {
            onCancel()
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text(stringResource(R.string.about_title)) },
            text = {
                Text(stringResource(R.string.about_text))
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(stringResource(R.string.got_it))
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.enable_reminders), style = MaterialTheme.typography.titleLarge)
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEnabled) {
                Text(stringResource(R.string.notification_window), style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        TimePickerDialog(context, { _, h, m -> startHour = h; startMinute = m }, startHour, startMinute, true).show()
                    }) {
                        Text(stringResource(R.string.time_format_start, startHour, startMinute))
                    }
                    Text(stringResource(R.string.to))
                    Button(onClick = {
                        TimePickerDialog(context, { _, h, m -> endHour = h; endMinute = m }, endHour, endMinute, true).show()
                    }) {
                        Text(stringResource(R.string.time_format_end, endHour, endMinute))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(stringResource(R.string.reminder_type), style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = reminderMode == ReminderMode.RANDOM,
                        onClick = { reminderMode = ReminderMode.RANDOM }
                    )
                    Text(stringResource(R.string.reminder_type_random), modifier = Modifier.padding(start = 8.dp))
                    Spacer(modifier = Modifier.width(24.dp))
                    RadioButton(
                        selected = reminderMode == ReminderMode.MANUAL,
                        onClick = { reminderMode = ReminderMode.MANUAL }
                    )
                    Text(stringResource(R.string.reminder_type_manual), modifier = Modifier.padding(start = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (reminderMode == ReminderMode.MANUAL) {
                    Text(
                        stringResource(R.string.notifications_per_day, sliderValue.roundToInt()),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
            } else {
                Text(
                    stringResource(R.string.reminders_disabled),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BePresentScreenPreview() {
    BePresentTheme {
        BePresentScreen(onScheduleManual = {_,_,_,_,_ ->}, onScheduleRandom = {_,_,_,_ ->}, onCancel = {})
    }
}
