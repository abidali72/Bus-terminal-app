# 🚌 Bus Terminal Management System

A comprehensive, multi-platform bus terminal management solution built with **Jetpack Compose**, **Kotlin**, and **Firebase**. This application provides a seamless experience for passengers to find routes, bus companies to manage their fleet, and administrators to oversee terminal operations.

## 🌟 Key Features

### 👨‍👩‍👧‍👦 Passenger App
- **Smart Route Search**: Find buses between cities with real-time availability.
- **Advanced Filtering**: Filter by bus type (AC, Non-AC, Sleeper, Luxury).
- **Company Directory**: View and follow your favorite bus companies.
- **Detailed Route Info**: View all stops, distance, and estimated travel time.
- **Seamless Booking**: Interactive UI for viewing available buses and schedules.
- **Modern UI**: Clean, responsive design using Material 3 and Jetpack Compose.

### 🏢 Company Management
- **Fleet Management**: Add and manage buses in the company fleet.
- **Route Scheduling**: Create and maintain bus schedules.
- **Dashboard**: Track routes and passenger engagement.

### 🛡️ Admin Panel
- **Terminal Overview**: Centralized control for all companies and routes.
- **User Management**: Oversee passenger and company accounts.
- **System Monitoring**: Ensure smooth terminal operations.

## 🛠️ Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (100% Kotlin)
- **Architecture**: MVVM with Clean Architecture principles.
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Backend**: [Firebase](https://firebase.google.com/) (Auth, Firestore, Realtime Database, Analytics)
- **Local Cache**: [Room Database](https://developer.android.com/training/data-storage/room)
- **Async Programming**: Kotlin Coroutines & Flow
- **Image Loading**: Coil
- **Preferences**: DataStore

## 🚀 Getting Started

### Prerequisites
- Android Studio Iguana or newer
- JDK 17+
- A Firebase project

### Setup
1. **Clone the repository**:
   ```bash
   git clone https://github.com/abidali72/Bus-terminal-app.git
   ```
2. **Firebase Configuration**:
   - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.busterminal.app`.
   - Download the `google-services.json` file.
   - Place `google-services.json` in the `app/` directory of the project.
3. **Build and Run**:
   - Open the project in Android Studio.
   - Wait for Gradle sync to complete.
   - Press **Run** to launch on your device or emulator.

## 📁 Project Structure
```text
app/src/main/java/com/busterminal/app/
├── data/          # Repositories, Data Sources, Models
├── di/            # Hilt Dependency Injection Modules
├── domain/        # Use Cases and Business Logic
├── ui/            # Compose Screens, ViewModels, and Theme
│   ├── admin/     # Admin-specific UI
│   ├── auth/      # Login and Signup
│   ├── company/   # Bus Company-specific UI
│   ├── passenger/ # Passenger-specific UI
│   └── theme/     # Material 3 Theme definition
└── util/          # Utility classes and Extensions
```

## 📄 License
This project is for demonstration purposes. [MIT License](LICENSE) (Replace with your own license if needed).

---
Developed with ❤️ by [Abid Ali](https://github.com/abidali72)
