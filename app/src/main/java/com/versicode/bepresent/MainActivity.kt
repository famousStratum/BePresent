package com.versicode.bepresent

import android.Manifest
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.media.MediaPlayer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.versicode.bepresent.notifications.NotificationHelper
import com.versicode.bepresent.notifications.NotificationScheduler
import com.versicode.bepresent.notifications.NotificationSound
import com.versicode.bepresent.ui.theme.BePresentTheme
import kotlin.math.roundToInt
import androidx.core.content.edit

class MainActivity : ComponentActivity() {

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var notificationScheduler: NotificationScheduler

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationHelper = NotificationHelper(this)
        notificationScheduler = NotificationScheduler(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)

        enableEdgeToEdge()
        setContent {
            BePresentTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BePresentScreen(
                        modifier = Modifier.padding(innerPadding),
                        initialIsActive = prefs.getBoolean("isActive", false),
                        initialStartHour = prefs.getInt("startHour", 9),
                        initialStartMinute = prefs.getInt("startMinute", 0),
                        initialEndHour = prefs.getInt("endHour", 21),
                        initialEndMinute = prefs.getInt("endMinute", 0),
                        initialReminderMode = ReminderMode.valueOf(prefs.getString("reminderMode", ReminderMode.RANDOM.name)!!),
                        initialSliderValue = prefs.getFloat("sliderValue", 1f),
                        initialNotificationSound = NotificationSound.valueOf(prefs.getString("notificationSound", NotificationSound.A_SHARP_BOWL.name)!!),
                        onSoundSelected = { sound ->
                            notificationHelper.createNotificationChannel(sound)
                        },
                        onScheduleManual = { count, startH, startM, endH, endM ->
                            prefs.edit { putBoolean("isActive", true) }
                            notificationScheduler.scheduleManualConfigurator(count, startH, startM, endH, endM)
                        },
                        onScheduleRandom = { startH, startM, endH, endM ->
                            prefs.edit { putBoolean("isActive", true) }
                            notificationScheduler.scheduleDailyConfigurator(startH, startM, endH, endM)
                        },
                        onCancel = {
                            prefs.edit { putBoolean("isActive", false) }
                            notificationScheduler.cancelAllReminders()
                        },
                    )
                }
            }
        }
    }
}

enum class ReminderMode {
    RANDOM, MANUAL
}

fun intensityLabel(count: Int): Int = when (count) {
    in 1..3 -> R.string.intensity_quiet
    in 4..6 -> R.string.intensity_normal
    else -> R.string.intensity_insistent
}

@Composable
fun BePresentScreen(
    modifier: Modifier = Modifier,
    initialIsActive: Boolean = false,
    initialStartHour: Int = 9,
    initialStartMinute: Int = 0,
    initialEndHour: Int = 21,
    initialEndMinute: Int = 0,
    initialReminderMode: ReminderMode = ReminderMode.RANDOM,
    initialSliderValue: Float = 1f,
    initialNotificationSound: NotificationSound = NotificationSound.A_SHARP_BOWL,
    onSoundSelected: (NotificationSound) -> Unit,
    onScheduleManual: (Int, Int, Int, Int, Int) -> Unit,
    onScheduleRandom: (Int, Int, Int, Int) -> Unit,
    onCancel: () -> Unit,
) {
    var isActive by remember { mutableStateOf(initialIsActive) }
    var notificationSound by remember { mutableStateOf(initialNotificationSound) }
    var reminderMode by remember { mutableStateOf(initialReminderMode) }
    var sliderValue by remember { mutableFloatStateOf(initialSliderValue) }
    var startHour by remember { mutableIntStateOf(initialStartHour) }
    var startMinute by remember { mutableIntStateOf(initialStartMinute) }
    var endHour by remember { mutableIntStateOf(initialEndHour) }
    var endMinute by remember { mutableIntStateOf(initialEndMinute) }

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    val mediaPlayer = remember { runCatching { MediaPlayer() }.getOrNull() }
    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
    }

    // Accessing resources via the context directly here is the correct way for MediaPlayer
    @SuppressLint("LocalContextResourcesRead")
    fun playPreview(sound: NotificationSound) {
        mediaPlayer?.reset()
        val afd = context.resources.openRawResourceFd(sound.toRawResId())
        mediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        mediaPlayer?.prepare()
        mediaPlayer?.start()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            if (isActive) {
                // Active state — centered message
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.active_message),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                // Inactive state — settings
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 48.dp)
                ) {
                    Text(stringResource(R.string.notification_window), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = {
                            TimePickerDialog(context, { _, h, m ->
                                startHour = h; startMinute = m
                                prefs.edit { putInt("startHour", h).putInt("startMinute", m) }
                            }, startHour, startMinute, true).show()                     }) {
                            Text(stringResource(R.string.time_format_start, startHour, startMinute))
                        }
                        Text(stringResource(R.string.to))
                        Button(onClick = {
                            TimePickerDialog(context, { _, h, m ->
                                endHour = h; endMinute = m
                                prefs.edit { putInt("endHour", h).putInt("endMinute", m) }
                            }, endHour, endMinute, true).show()                    }) {
                            Text(stringResource(R.string.time_format_end, endHour, endMinute))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(stringResource(R.string.notification_sound), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        NotificationSound.A_SHARP_BOWL to stringResource(R.string.a_sharp_bowl),
                        NotificationSound.D_SHARP_BOWL to stringResource(R.string.d_sharp_bowl),
                        NotificationSound.BEE_PRESENT to stringResource(R.string.bee_present)
                    ).forEach { (sound, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = notificationSound == sound,
                                onClick = {
                                    notificationSound = sound
                                    playPreview(sound)
                                    prefs.edit { putString("notificationSound", sound.name) }
                                }
                            )
                            Text(label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(stringResource(R.string.reminder_type), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = reminderMode == ReminderMode.RANDOM,
                            onClick = {
                                reminderMode = ReminderMode.RANDOM
                                prefs.edit { putString("reminderMode", ReminderMode.RANDOM.name) }                            }
                        )
                        Text(stringResource(R.string.reminder_type_random), modifier = Modifier.padding(start = 8.dp))
                        Spacer(modifier = Modifier.width(24.dp))
                        RadioButton(
                            selected = reminderMode == ReminderMode.MANUAL,
                            onClick = {
                                reminderMode = ReminderMode.MANUAL
                                prefs.edit { putString("reminderMode", ReminderMode.MANUAL.name) }
                            }
                        )
                        Text(stringResource(R.string.reminder_type_manual), modifier = Modifier.padding(start = 8.dp))
                    }

                    if (reminderMode == ReminderMode.MANUAL) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            stringResource(R.string.intensity_label, stringResource(intensityLabel(sliderValue.roundToInt()))),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it
                                prefs.edit { putFloat("sliderValue", it) }
                            },
                            valueRange = 1f..9f,
                            steps = 0
                        )
                    }
                }
            }

            // Bottom button
            if (isActive) {
                OutlinedButton(
                    onClick = {
                        onCancel()
                        isActive = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Text(stringResource(R.string.step_away))
                }
            } else {
                Button(
                    onClick = {
                        onSoundSelected(notificationSound)
                        when (reminderMode) {
                            ReminderMode.RANDOM -> onScheduleRandom(startHour, startMinute, endHour, endMinute)
                            ReminderMode.MANUAL -> onScheduleManual(sliderValue.roundToInt(), startHour, startMinute, endHour, endMinute)
                        }
                        isActive = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Text(stringResource(R.string.be_present))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BePresentScreenPreview() {
    BePresentTheme {
        BePresentScreen(initialIsActive = false, onScheduleManual = { _, _, _, _, _ -> }, onScheduleRandom = { _, _, _ ,_ -> }, onSoundSelected = { _ -> }, onCancel = {})
    }
}