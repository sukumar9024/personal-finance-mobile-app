# Finance Tracker

Finance Tracker is an Android personal finance app built with Kotlin and Jetpack Compose. It is meant for people who want a clean expense tracker on their phone while keeping their data in Google Sheets instead of building a custom backend.

The app can:

- track expenses, transfers, monthly income, and recurring entries
- organize spending by category and budget
- show dashboard and report views
- sync live with Google Sheets when credentials are configured
- fall back to cached data when Sheets is not configured or unavailable
- remember theme and currency preferences locally

## What Is In This Project

- `app/`: Android app source
- `app/src/main/java/com/financetracker/`: Kotlin source code
- `app/src/main/res/`: Android resources
- `scripts/run-emulator-debug.ps1`: helper script to build, install, and launch on a chosen device
- `scripts/run-pixel-debug.ps1`: helper script for the Pixel emulator flow
- `gradlew` and `gradlew.bat`: Gradle wrapper

Main screens in the app:

- Dashboard
- Add Transaction
- Edit Expense
- Categories
- Reports
- Settings

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- MVVM with `AndroidViewModel`, `StateFlow`, and coroutines
- Google Sheets API v4
- WorkManager
- SharedPreferences for local cache and preferences

## Requirements

- Android Studio Hedgehog or newer is recommended
- JDK 17
- Android SDK installed
- An emulator or an Android phone with USB debugging enabled
- A Google account
- A Google Cloud project with Sheets API enabled

## Before You Run Anything

This repo does not include usable credentials anymore. You need to create your own:

- a Google spreadsheet
- a Google Cloud service account
- a service account JSON key file

You also need to replace the placeholder spreadsheet ID in `app/build.gradle.kts`.

## 1. Create The Google Sheet

Create a new spreadsheet in Google Sheets. This app expects a few sheets:

### `categories`

Headers:

| Column | Value |
|---|---|
| A | `Name` |
| B | `Color` |
| C | `Monthly Budget` |

Suggested starter rows:

| Name | Color | Monthly Budget |
|---|---|---|
| Food | `#FF5722` | `8000` |
| Transport | `#2196F3` | `4000` |
| Shopping | `#E91E63` | `6000` |
| Bills | `#9C27B0` | `5000` |
| Entertainment | `#FF9800` | `3000` |
| Health | `#4CAF50` | `2500` |
| Education | `#3F51B5` | `3500` |
| Investment | `#10B981` | `7000` |
| Family Support | `#F97316` | `5000` |
| Other | `#607D8B` | `2000` |

### Monthly expense sheets

The app creates these automatically when sync is working:

- `expenses_YYYY_MM`

Headers:

| Column | Value |
|---|---|
| A | Date |
| B | Amount |
| C | Category |
| D | Subcategory |
| E | Description |
| F | Payment Method |
| G | Transfer Account |
| H | Transfer Destination Account |
| I | Transaction Type |
| J | Split Group ID |
| K | Receipt URL |
| L | Tags |
| M | Created At |
| N | Modified At |
| O | Recurring ID |
| P | Occurrence Period |

### `monthly_income`

Headers:

| Column | Value |
|---|---|
| A | Month |
| B | Income |
| C | Recurring ID |

### `recurring_entries`

Headers:

| Column | Value |
|---|---|
| A | ID |
| B | Title |
| C | Amount |
| D | Type |
| E | Day Of Month |
| F | Category |
| G | Description |
| H | Payment Method |
| I | Active |

## 2. Get The Spreadsheet ID

From a URL like:

```text
https://docs.google.com/spreadsheets/d/1AbcExampleSpreadsheetId123456/edit#gid=0
```

the spreadsheet ID is the part between `/d/` and `/edit`.

## 3. Create Google Cloud Credentials

1. Go to Google Cloud Console.
2. Create a project or use an existing one.
3. Enable `Google Sheets API`.
4. Open `APIs & Services` -> `Credentials`.
5. Create a `Service Account`.
6. Open the service account.
7. Add a new key.
8. Choose `JSON`.
9. Download the key file.

The JSON contains the service account email. It looks something like:

```text
something@your-project-id.iam.gserviceaccount.com
```

## 4. Share The Sheet With The Service Account

Open the spreadsheet and share it with the service account email as:

- `Editor`

If you skip this, the app will not be able to read or write data.

## 5. Add The Credentials To The App

### Spreadsheet ID

Open `app/build.gradle.kts` and replace the placeholder with your real spreadsheet ID:

```kotlin
buildConfigField("String", "SPREADSHEET_ID", "\"YOUR_SPREADSHEET_ID_HERE\"")
```

### Service account key

Create this file locally on your machine:

```text
app/src/main/assets/service-account-key.json
```

Put the downloaded service account JSON inside that file.

Important:

- do not commit this file
- if you ever committed a real key before, rotate it in Google Cloud and generate a new one

The app also supports a `SERVICE_ACCOUNT_JSON` build config string, but the assets file is much easier to manage during development. Keep it local and out of git.

## 6. Open The Project

You can open the repo in Android Studio and let Gradle sync, or use the command line.

If your SDK path is not detected automatically, make sure `local.properties` contains:

```text
sdk.dir=C:\\Users\\YOUR_NAME\\AppData\\Local\\Android\\Sdk
```

Typical useful environment variables on Windows:

```text
ANDROID_HOME=C:\Users\YOUR_NAME\AppData\Local\Android\Sdk
ANDROID_SDK_ROOT=C:\Users\YOUR_NAME\AppData\Local\Android\Sdk
```

Also add these to `Path`:

```text
C:\Users\YOUR_NAME\AppData\Local\Android\Sdk\platform-tools
C:\Users\YOUR_NAME\AppData\Local\Android\Sdk\emulator
```

## 7. Build The App

Windows:

```powershell
.\gradlew.bat assembleDebug
```

macOS/Linux:

```bash
./gradlew assembleDebug
```

Install on a connected device or running emulator:

Windows:

```powershell
.\gradlew.bat installDebug
```

macOS/Linux:

```bash
./gradlew installDebug
```

## 8. Run It On A Phone

1. Enable Developer Options on the phone.
2. Enable USB debugging.
3. Connect the phone.
4. Confirm it appears in:

```powershell
adb devices
```

5. Install the app:

```powershell
.\gradlew.bat installDebug
```

6. Launch it from the phone or with:

```powershell
adb shell am start -n com.financetracker/.MainActivity
```

## 9. Run It On An Emulator

List AVDs:

```powershell
emulator -list-avds
```

Start one:

```powershell
emulator -avd Pixel_9_Pro
```

If the emulator window is black, start it with a cold boot and software rendering:

```powershell
emulator -avd Pixel_9_Pro -no-snapshot-load -gpu swiftshader_indirect
```

Install and launch:

```powershell
.\gradlew.bat installDebug
adb shell am start -n com.financetracker/.MainActivity
```

You can also use the included helper scripts:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-emulator-debug.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\run-pixel-debug.ps1
```

## How The App Behaves

### When sync is configured correctly

- categories load from Google Sheets
- monthly sheets are created automatically
- expenses, income, and recurring entries sync with Sheets
- reports use live data

### When sync is missing or broken

- the app still opens
- cached data is used when available
- the dashboard shows sync status
- actions continue locally as cached changes in memory for the current session

## Main Features

- expense tracking
- transfer tracking
- split transactions
- monthly income tracking
- recurring income and recurring expenses
- category budgets
- dashboard summaries
- reports and comparisons
- theme mode selection
- currency selection with persistence across app restarts
- notification-based overspending alerts

## Project Structure At A Glance

```text
app/src/main/java/com/financetracker/
|-- MainActivity.kt
|-- data/
|   |-- model/
|   `-- repository/
|-- ui/
|   |-- navigation/
|   |-- screens/
|   |-- theme/
|   `-- viewmodel/
`-- workmanager/
```

Key files:

- `app/src/main/java/com/financetracker/MainActivity.kt`
- `app/src/main/java/com/financetracker/data/repository/GoogleSheetsRepository.kt`
- `app/src/main/java/com/financetracker/ui/viewmodel/ExpenseViewModel.kt`
- `app/src/main/java/com/financetracker/ui/navigation/FinanceNavHost.kt`
- `app/src/main/java/com/financetracker/ui/screens/`

## Troubleshooting

### The app opens but sync does not work

Check all of these:

- `SPREADSHEET_ID` in `app/build.gradle.kts` has been replaced and is not still `YOUR_SPREADSHEET_ID_HERE`
- `service-account-key.json` exists in `app/src/main/assets/`
- Sheets API is enabled in Google Cloud
- the spreadsheet is shared with the service account email as `Editor`
- the phone or emulator has internet access

### The app installs but shows old or cached data

- use the dashboard refresh action
- check the sync status card on the home screen
- confirm the app is pointing to the spreadsheet you expect

### The emulator window is black

- cold boot the emulator
- run with `-gpu swiftshader_indirect`
- wake the virtual device if the Android guest is booted but the screen is off

### Build fails

Check:

- JDK 17 is installed
- Android SDK is installed
- `local.properties` points to the SDK
- Gradle wrapper files exist

## Security Notes

- never commit `service-account-key.json`
- never share a real private key in screenshots, docs, or chat
- if a real service account key was ever committed, revoke it and generate a new one

## Final Notes

This project is set up so someone else can clone it, add their own local credentials, point it at their own spreadsheet, and run it on either a physical Android phone or a laptop emulator without needing any extra backend.

If you are handing this project to another developer, the only local pieces they need to create are:

- `local.properties`
- `app/src/main/assets/service-account-key.json`
- their own spreadsheet ID in `app/build.gradle.kts`
