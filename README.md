Smart Notification Center

A feature-rich Android application for scheduling, managing, and tracking smart notifications — built with modern Android development practices using Jetpack Compose and a clean MVVM + Clean Architecture approach.


Screenshots

> _Add screenshots of Dashboard, Schedule, History, and Settings screens here._


Features

- `Create & Schedule Notifications` — Set custom title, message, date/time, priority level, and repeat mode (None, Daily, Weekly)
- `Dashboard` — Overview of all notifications with search, filtering by status, and quick actions (edit, cancel, delete)
- `Schedule View` — List of upcoming scheduled notifications
- `History Log` — Full log of triggered notifications with search and bulk-clear support
- `Priority Settings` — Per-priority configuration for sound, vibration, and heads-up display
- `Boot Persistence` — Notifications are automatically rescheduled after device reboot
- `App Settings` — General app preferences stored with DataStore


Tech Stack

Language: Kotlin 2.0.21
UI: Jetpack Compose + Material Design 3
Architecture: MVVM + Clean Architecture (Domain / Data / Presentation)
DI: Hilt 2.51.1
Database: Room 2.6.1
Background Work: WorkManager 2.9.1
Navigation: Navigation Compose 2.8.3
Async: Kotlin Coroutines + Flow 1.9.0
Preferences: DataStore Preferences 1.1.1
Build System: Gradle with KSP (Version Catalogs)


Architecture

The project follows **Clean Architecture** with three main layers:

```
app/
└── com.smartnotification/
    ├── data/
    │   ├── local/          # Room database, DAOs, Entities, Mappers
    │   ├── repository/     # Repository implementations
    │   └── worker/         # WorkManager workers, NotificationScheduler, BootReceiver
    ├── domain/
    │   ├── model/          # Domain models (NotificationItem, HistoryItem, PrioritySettings, enums)
    │   └── usecase/        # Use cases (Create, Update, Delete, Cancel, Search, History, Priority)
    ├── presentation/
    │   ├── dashboard/      # Dashboard screen + ViewModel
    │   ├── create/         # Create/Edit screen + ViewModel
    │   ├── schedule/       # Schedule screen + ViewModel
    │   ├── history/        # History screen + ViewModel
    │   ├── priority/       # Priority settings screen + ViewModel
    │   ├── settings/       # App settings screen + ViewModel
    │   ├── navigation/     # Navigation graph + Bottom Nav items
    │   ├── components/     # Shared UI components
    │   └── theme/          # Material Design 3 theme
    └── di/                 # Hilt modules (Database, WorkManager, Repository)
```

Domain Models

- `NotificationItem` — A scheduled notification with title, message, scheduled time, priority, repeat mode, and status
- `HistoryItem` — A record of a triggered notification
- `PrioritySettings` — Per-priority behaviour configuration (sound, vibration, heads-up)
- `Priority` — `LOW`, `MEDIUM`, `HIGH`
- `RepeatMode` — `NONE`, `DAILY`, `WEEKLY`
- `NotificationStatus` — `SCHEDULED`, `TRIGGERED`, `CANCELLED`

Permissions

The app requests the following permissions:

POST_NOTIFICATIONS: Display system notifications
SCHEDULE_EXACT_ALARM USE_EXACT_ALARM: Deliver notifications at precise times
RECEIVE_BOOT_COMPLETED: Reschedule notifications after reboot
VIBRATE:Vibrate on notification delivery
|FOREGROUND_SERVICE: Run notification delivery reliably in the background

---

Requirements

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Minimum SDK: API 26 (Android 8.0)
- Target SDK: API 35 (Android 15)
- Compile SDK: 35

---

Getting Started

1. Clone the repository
   ```bash
   git clone https://github.com/your-username/SmartNotificationCenter.git
   ```

2. Open in Android Studio
   - Open Android Studio → `File > Open` → select the `SmartNotificationCenter` folder

3. Sync Gradle
   - Let Android Studio sync and download all dependencies automatically

4. Run the app
   - Select a device or emulator running API 26+
   - Click **Run ▶**

> On first launch, the app will request notification permission (required on Android 13+). Grant it to enable all features.

Use Cases

CreateNotificationUseCase: Inserts a notification and schedules it via WorkManager
UpdateNotificationUseCase: Cancels existing schedule, updates DB, and reschedules
DeleteNotificationUseCase: Cancels schedule and removes the notification from DB
CancelNotificationUseCase: Cancels the WorkManager job and marks status as CANCELLED
GetAllNotificationsUseCase: Reactive stream of all notifications
GetScheduledNotificationsUseCase: Reactive stream of scheduled-only notifications
SearchNotificationsUseCase: Filters notifications by query string
GetHistoryUseCase: Reactive stream of triggered notification history
SearchHistoryUseCase: Filters history by query string
ClearHistoryUseCase: Deletes all history entries
GetPrioritySettingsUseCase: Reactive stream of all priority settings
UpdatePrioritySettingsUseCase: Persists changes to a priority's behaviour config

Project Configuration

Dependencies are managed through Gradle **Version Catalogs** (`gradle/libs.versions.toml`), and code generation (Hilt + Room) is powered by **KSP** for faster build times.

License

```
MIT License — feel free to use, modify, and distribute.
```
