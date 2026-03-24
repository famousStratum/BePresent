# Be Present

Be Present is a simple Android application designed to help you stay present throughout your day. It sends gentle reminders at intervals within a custom time window, encouraging moments of mindfulness.

## Features

- **Custom Notification Window**: Set a start and end time for when you want to receive reminders (e.g., 9:00 AM to 9:00 PM).
- **Two Reminder Modes**:
    - **Random Mode**: The app automatically schedules a random number of notifications (between 1 and 10) at different times each day.
    - **Manual Mode**: You choose exactly how many reminders you want to receive daily.
- **Gentle Alerts**: Notifications feature a calming "bowl" sound to minimize distraction while grabbing your attention.
- **Modern UI**: Built with Jetpack Compose and Material Design 3 for a clean, intuitive experience.
- **Battery Efficient**: Uses Android's `WorkManager` for reliable background scheduling with minimal battery impact.

## Screenshots

*(Add screenshots here)*

## Getting Started

### Prerequisites

- Android device or emulator running Android 7.0 (API level 24) or higher.
- Android Studio Ladybug or newer.

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/famousStratum/BePresent.git
   ```
2. Open the project in Android Studio.
3. Build and run the app on your device or emulator.

## Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Background Tasks**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- **Architecture**: Material 3 Design

## Project Structure

- `ui/`: Contains the theme and UI components.
- `notifications/`: Logic for scheduling and displaying notifications.
    - `NotificationHelper`: Manages notification channels and posting.
    - `NotificationScheduler`: Entry point for scheduling WorkManager tasks.
    - `NotificationWorker`: The task that triggers the notification.
    - `DailyConfiguratorWorker`: Handles random daily scheduling logic.
    - `ManualConfiguratorWorker`: Handles manual fixed-count scheduling.

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.
