# Finance Tracker (SC)

Finance Tracker ("SC") is a modern Android personal finance app built with Kotlin and Jetpack Compose. It helps you track expenses, manage budgets, and sync your data with Google Sheets—all while keeping your data private and under your control.

## App Icon

The app features a clean "SC" logo with a professional blue-to-green gradient background, consistent across all themes.

## Features

### Core Features
- **Expense Tracking** - Log daily expenses with category, payment method, description, and tags
- **Transfer Tracking** - Move money between accounts (Cash, Bank, UPI, Credit Card, etc.)
- **Split Transactions** - Divide a single purchase across multiple categories
- **Recurring Entries** - Set up automatic monthly income and expenses
- **Monthly Budget** - Set and track your monthly spending limit
- **Category Budgets** - Set individual budgets per category with progress tracking
- **Category Customization** - Add categories and customize colors

### Dashboard
- Monthly balance overview with budget progress bar
- Quick-add transaction card for fast expense logging
- Top categories summary with spending breakdown
- Sync status card with refresh button
- Transaction list with search, category filter, account filter, and sort options
- Floating action button for adding new transactions

### Reports
- Income vs Spending summary
- Budget trend charts (monthly/yearly views)
- Category breakdown with percentages
- Budget vs Actual comparison
- Month-over-month comparison
- Top merchants analysis
- Recurring plans management
- Month-end forecast based on spending pace

### Settings
- Theme selection (System, Light, Dark)
- Multi-currency support with 10 currencies
- App version info
- Google Sheets setup guide

### Notifications
- Overspending alerts when monthly or category budgets are exceeded
- Push notifications for budget alerts

### Data Sync
- Live sync with Google Sheets when configured
- Cached data fallback when Sheets is unavailable
- Background sync via WorkManager
- Manual refresh from dashboard

## Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Design System | Material 3 |
| Architecture | MVVM with ViewModel |
| Async | Coroutines & StateFlow |
| Navigation | Jetpack Navigation Compose |
| Local Storage | SharedPreferences |
| Background Tasks | WorkManager |
| Cloud Sync | Google Sheets API v4 |
| Image Loading | Coil |

## Minimum Requirements

- Android Studio Hedgehog or newer recommended
- JDK 17
- Android SDK installed
- An emulator or Android phone with USB debugging enabled
- API 24 (Android 7.0) or higher
- A Google account
- A Google Cloud project with Sheets API enabled

## Supported Currencies

| Currency | Symbol | Code |
|---|---|---|
| Indian Rupee | ₹ | INR (Default) |
| US Dollar | $ | USD |
| Euro | € | EUR |
| British Pound | £ | GBP |
| Japanese Yen | ¥ | JPY |
| Chinese Yuan | ¥ | CNY |
| Australian Dollar | A$ | AUD |
| Canadian Dollar | C$ | CAD |
| Singapore Dollar | S$ | SGD |
| UAE Dirham | د.إ | AED |

## Project Structure

```
app/src/main/java/com/financetracker/
├── MainActivity.kt                    # App entry point
├── data/
│   ├── model/
│   │   ├── Category.kt               # Category data model
│   │   ├── CategoryBudget.kt         # Budget tracking model
│   │   ├── Currency.kt               # Multi-currency support
│   │   ├── Expense.kt                # Expense data model
│   │   ├── IncomeEntry.kt            # Monthly income model
│   │   ├── RecurringEntry.kt         # Recurring transactions
│   │   └── TransactionType.kt        # Transaction type enum
│   └── repository/
│       └── GoogleSheetsRepository.kt # Google Sheets sync
├── ui/
│   ├── navigation/
│   │   └── FinanceNavHost.kt         # App navigation
│   ├── screens/
│   │   ├── AddExpenseScreen.kt       # Add transaction
│   │   ├── CategoriesScreen.kt       # Category management
│   │   ├── DashboardScreen.kt        # Home screen
│   │   ├── EditExpenseScreen.kt      # Edit expense
│   │   ├── ReportsScreen.kt          # Charts & reports
│   │   └── SettingsScreen.kt         # App settings
│   ├── theme/
│   │   ├── AppStyle.kt               # Shared styles & formatting
│   │   └── Theme.kt                  # Material theme
│   └── viewmodel/
│       └── ExpenseViewModel.kt       # State management
└── workmanager/
    └── BackgroundSyncWorker.kt       # Background sync
```

## Setup Instructions

### Before You Run

This repo does not include usable credentials. You need to create your own:
- A Google spreadsheet
- A Google Cloud service account
- A service account JSON key file

### 1. Create The Google Sheet

Create a new spreadsheet in Google Sheets with these sheets:

#### `categories` Sheet

| Column | Header |
|---|---|
| A | Name |
| B | Color |
| C | Monthly Budget |

#### Monthly Expense Sheets

Created automatically as `expenses_YYYY_MM` with columns: Date, Amount, Category, Subcategory, Description, Payment Method, Transaction Type, Split Group ID, Tags, Created At, Modified At.

#### `monthly_income` Sheet

| Column | Header |
|---|---|
| A | Month |
| B | Income |

#### `recurring_entries` Sheet

| Column | Header |
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

### 2. Get The Spreadsheet ID

From URL: `https://docs.google.com/spreadsheets/d/1AbcExampleSpreadsheetId123456/edit#gid=0`
The ID is: `1AbcExampleSpreadsheetId123456`

### 3. Create Google Cloud Credentials

1. Go to Google Cloud Console
2. Create or select a project
3. Enable `Google Sheets API`
4. Create a `Service Account`
5. Generate a `JSON` key file
6. Download the key file

### 4. Share The Sheet

Share the spreadsheet with the service account email as `Editor`.

### 5. Add Credentials To The App

#### Spreadsheet ID

Add to root `local.properties`:
```text
spreadsheet.id=YOUR_REAL_SPREADSHEET_ID
```

#### Service Account Key

Create this file:
```text
app/src/main/assets/service-account-key.json
```

Paste the service account JSON inside. **Do not commit this file.**

## Build & Run

### Open The Project

Open in Android Studio and let Gradle sync.

If SDK path is not detected, add to `local.properties`:
```text
sdk.dir=C:\\Users\\YOUR_NAME\\AppData\\Local\\Android\\Sdk
```

### Build

```powershell
# Windows
.\gradlew.bat assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

### Install

```powershell
# Windows
.\gradlew.bat installDebug

# macOS/Linux
./gradlew installDebug
```

### Run On Physical Device

1. Enable Developer Options on the phone
2. Enable USB debugging
3. Connect the phone
4. Verify with `adb devices`
5. Install: `.\gradlew.bat installDebug`
6. Launch from phone or: `adb shell am start -n com.financetracker/.MainActivity`

### Run On Emulator

```powershell
# List available AVDs
emulator -list-avds

# Start emulator
emulator -avd Pixel_9_Pro

# Install and launch
.\gradlew.bat installDebug
adb shell am start -n com.financetracker/.MainActivity
```

### Helper Scripts

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-emulator-debug.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\run-pixel-debug.ps1
```

## How The App Behaves

### Sync Configured
- Categories and data load from Google Sheets
- Monthly sheets created automatically
- Live sync for all operations
- Reports use live data

### Sync Not Configured
- App opens normally
- Cached data used when available
- Dashboard shows sync status
- Local changes continue for the session

## Troubleshooting

### Sync Not Working
- Verify `spreadsheet.id` in `local.properties`
- Verify `service-account-key.json` exists in `app/src/main/assets/`
- Sheets API enabled in Google Cloud
- Sheet shared with service account as Editor
- Device has internet access
- Rebuild app after changing `local.properties`

### Cached/Old Data
- Use dashboard refresh button
- Check sync status card

### Emulator Black Screen
- Cold boot: `emulator -avd Pixel_9_Pro -no-snapshot-load -gpu swiftshader_indirect`

### Build Fails
- Verify JDK 17 installed
- Verify Android SDK installed
- Verify `local.properties` SDK path
- Verify Gradle wrapper files exist

## Security Notes

- Never commit `service-account-key.json`
- Never share private keys in screenshots or docs
- If a key was ever committed, revoke and regenerate

## License

This project is for personal use.

---

Built with ❤️ using Kotlin and Jetpack Compose