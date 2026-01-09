package com.example.mindfulnessreminder

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mindfulnessreminder.notifications.NotificationHelper
import com.example.mindfulnessreminder.notifications.NotificationScheduler
import com.example.mindfulnessreminder.ui.theme.MindfulnessReminderTheme
import java.util.Locale
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
            MindfulnessReminderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MindfulnessReminderScreen(
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
fun MindfulnessReminderScreen(
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
            title = { Text("About Mindfulness Reminder") },
            text = {
                Text(
                    "This app helps you stay present by sending reminders at random intervals throughout the day within your chosen window.\n\n" +
                            "• Enable Reminders: Turn the service on or off.\n" +
                            "• Notification Window: Define when you want to receive reminders.\n" +
                            "• Random Mode: The app picks a random number of notifications (1-10) each day.\n" +
                            "• Manual Mode: You choose exactly how many notifications you want per day."
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        IconButton(
            onClick = { showInfoDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "About",
                tint = MaterialTheme.colorScheme.primary
            )
        }

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
                Text("Enable Reminders", style = MaterialTheme.typography.titleLarge)
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEnabled) {
                Text("Notification Window", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        TimePickerDialog(context, { _, h, m -> startHour = h; startMinute = m }, startHour, startMinute, true).show()
                    }) {
                        Text(String.format(Locale.getDefault(), "Start: %02d:%02d", startHour, startMinute))
                    }
                    Text("to")
                    Button(onClick = {
                        TimePickerDialog(context, { _, h, m -> endHour = h; endMinute = m }, endHour, endMinute, true).show()
                    }) {
                        Text(String.format(Locale.getDefault(), "End: %02d:%02d", endHour, endMinute))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Reminder Type", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = reminderMode == ReminderMode.RANDOM,
                        onClick = { reminderMode = ReminderMode.RANDOM }
                    )
                    Text("Random", modifier = Modifier.padding(start = 8.dp))
                    Spacer(modifier = Modifier.width(24.dp))
                    RadioButton(
                        selected = reminderMode == ReminderMode.MANUAL,
                        onClick = { reminderMode = ReminderMode.MANUAL }
                    )
                    Text("Manual", modifier = Modifier.padding(start = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (reminderMode == ReminderMode.MANUAL) {
                    Text(
                        "Notifications per day: ${sliderValue.roundToInt()}",
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
                    "Reminders are currently disabled.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MindfulnessReminderScreenPreview() {
    MindfulnessReminderTheme {
        MindfulnessReminderScreen(onScheduleManual = {_,_,_,_,_ ->}, onScheduleRandom = {_,_,_,_ ->}, onCancel = {})
    }
}
