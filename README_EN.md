# TenDaysPlan - 10-Day Cycle Tracker

<div align="center">

![Android](https://img.shields.io/badge/Android-34%2B-green)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5+-purple)
![License](https://img.shields.io/badge/License-MIT-yellow)

An Android task management and time tracking app based on the 10-day cycle system

</div>

---

## Project Introduction

**TenDaysPlan** (十日谈) is an Android app that helps users manage themselves through a 10-day cycle system. It divides a year into 36 cycles, each focusing on 3 goals, recording up to 6 tasks per day, and helping users track time allocation and task completion through visual statistics.

> Core Philosophy: Break big goals into small cycles, achieve self-improvement through continuous recording and reflection.

---

## Features

### 📅 Year Overview
- View all 36 cycles at a glance
- Quick navigation by clicking on cycles
- Visual display of annual planning progress

### 📊 Progress Tracking
- Visual representation of each cycle's execution
- Task completion rate statistics
- Support for cycle switching and history review

### ✅ Today's Tasks
- Record up to 6 tasks per day
- Support detailed task information (name, detail, duration)
- One-click completion marking, edit and delete tasks
- Display current cycle day and goals

### 📈 Statistics Analysis
- Intelligent statistics of task frequency
- Automatic accumulation of task duration (supports formats like "hours", "minutes")
- Sort by frequency and duration to discover time allocation patterns

### ⚙️ Settings & Backup
- Export data as Base64-encoded JSON file
- Support full data recovery from backup files
- Support clearing all data
- Automatic adaptation to Android storage permissions

---

## Tech Stack

### Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM + Repository Pattern
- **Dependency Injection**: Hilt
- **Local Storage**: Room Database
- **Serialization**: Kotlinx Serialization
- **Navigation**: Compose Navigation
- **Async Processing**: Kotlin Coroutines + Flow

### Data Models
- **CycleEntity**: Cycle entity (contains 3 goals)
- **DayRecordEntity**: Daily record (contains up to 6 tasks)

---

## Project Structure

```
app/src/main/java/com/example/tendaysplan/
├── data/                           # Data layer
│   ├── dao/                       # Room Data Access Objects
│   │   ├── CycleDao.kt
│   │   └── DayRecordDao.kt
│   ├── model/                     # Data models
│   │   ├── CycleEntity.kt
│   │   └── DayRecordEntity.kt
│   ├── repository/                # Data repositories
│   │   ├── CycleRepository.kt
│   │   └── DayRecordRepository.kt
│   ├── TenDaysPlanDatabase.kt     # Room database
│   └── Converters.kt              # Type converters
├── di/                            # Dependency injection modules
│   └── AppModule.kt
├── ui/                            # UI layer
│   ├── screens/                   # Screens
│   │   ├── YearOverviewScreen.kt
│   │   ├── ProgressScreen.kt
│   │   ├── TodayScreen.kt
│   │   ├── StatisticsScreen.kt
│   │   └── SettingsScreen.kt
│   ├── viewmodel/                 # View models
│   │   ├── YearOverviewViewModel.kt
│   │   ├── ProgressViewModel.kt
│   │   ├── TodayViewModel.kt
│   │   ├── StatisticsViewModel.kt
│   │   └── SettingsViewModel.kt
│   └── theme/                     # Theme configuration
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── utils/                         # Utility classes
│   ├── DateUtils.kt              # Date utilities
│   ├── StatisticsAnalyzer.kt     # Statistics analysis
│   └── DataExportHelper.kt       # Data import/export
├── MainActivity.kt               # Main Activity
└── TenDaysPlanApplication.kt     # Application class
```

---

## Requirements

- **Min SDK**: Android 14 (API 34)
- **Target SDK**: Android 16 (API 36)
- **Compile SDK**: 36

---

## Installation

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or higher
- JDK 11 or higher
- Android SDK 34 or higher

### Build Steps

1. **Clone the repository**
```bash
git clone https://github.com/your-username/TenDaysPlan.git
cd TenDaysPlan
```

2. **Open the project**
- Open the project root directory in Android Studio

3. **Sync Gradle**
- Wait for Gradle to automatically sync dependencies

4. **Run the app**
- Connect an Android device or start an emulator
- Click the Run button or execute: `./gradlew installDebug`

---

## Data Details

### Cycle Division
- A year is divided into **36 cycles**
- Each cycle has a fixed **10 days**
- Each cycle can set **up to 3 goals**

### Task Recording
- Maximum of **6 tasks** can be recorded per day
- Each task includes:
  - Task name (required)
  - Task detail (optional)
  - Task duration (optional, supports formats: `2 hours`, `30 minutes`, `1.5h`, etc.)

### Data Import/Export
- Export format: Base64-encoded JSON text file
- Storage location: Device Documents directory
- File naming: `tendaysplan_backup_[timestamp].txt`

---

## Contributing

Issues and Pull Requests are welcome!

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

---

## Author

TenDaysPlan - Making time management simple and efficient

---

## Roadmap

- [ ] Support custom cycle duration
- [ ] Add home screen widgets
- [ ] Support dark mode toggle
- [ ] Add task reminder feature
- [ ] Support cloud data synchronization
- [ ] Add data visualization charts
- [ ] Support multiple languages

---

<div align="center">

**If this project helps you, please give it a ⭐️ Star!**

</div>
