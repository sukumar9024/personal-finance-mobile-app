# Finance Tracker

Finance Tracker is a Kotlin and Jetpack Compose Android app for tracking personal income, expenses, transfers, budgets, savings, recurring entries, and Google Sheets-backed finance history.

The app is designed around month-by-month finance tracking. Each month can have its own income, expenses, category budgets, savings amount, and dashboard view.

## Features

### Dashboard

- Monthly income, spending, remaining amount, transaction count, and average transaction.
- Expenditure grouped by transaction currency for mixed-currency months.
- Total savings till now from full history.
- Savings per selected month.
- Monthly savings rate and all-time savings rate.
- Best savings month, worst savings month, and count of months where spending exceeded income.
- Recent monthly savings history.
- Daily burn rate, projected month-end spending, and safe-to-spend per day.
- Payment account spending summary.
- Upcoming recurring income and expenses.
- Manual net-worth tracker for assets and debts.
- Quick-add expense entry.
- Top categories for the selected month.
- Search, category filter, account filter, date range filter, and transaction sorting.
- Month navigation with previous/next buttons and month picker.
- Sync status, manual refresh, export, backup, restore, and data clearing controls.

### Transactions

- Add expenses.
- Add transfers between accounts.
- Split one transaction across multiple categories.
- Edit or delete transactions.
- Tags, descriptions, subcategories, payment methods, and transfer accounts.
- Transaction-level currency selection.
- Transactions are stored in monthly sheets named `expenses_YYYY_MM`.

### Monthly Income And Savings

- Monthly income is saved separately per `YYYY-MM`.
- Editing current month income updates the dashboard immediately.
- Viewing a previous month shows that month's income, expenses, and saved amount.
- Recurring income can still be configured, but manual monthly income values are stored independently in the `monthly_income` sheet.

### Categories And Budgets

- Default categories are seeded automatically.
- Add custom categories.
- Change category colors.
- Set category budgets per month.
- Browse category budgets and spending by month.

### Reports

- Income vs spending summary.
- Budget trend chart.
- Category breakdown.
- Budget vs actual.
- Month-over-month comparison.
- Income history editor.
- Month-end forecast.
- Top merchants.
- Currency totals grouped by transaction currency.
- Recurring plans with active/inactive toggles.
- Optional include/exclude transfers setting.

### Settings And Data

- Theme: System, Light, Dark.
- Multi-currency selection.
- Per-transaction currency support. The Settings currency is the default display/input currency, while each expense can be saved in its own currency.
- Include/exclude transfers in reports.
- CSV export.
- PDF summary export.
- Local backup.
- Restore latest local backup.
- Cached fallback when Google Sheets sync is unavailable.

## Navigation

- **Dashboard**: Main screen. Shows monthly overview, savings, income editor, quick add, filters, and recent transactions.
- **Add Transaction**: Tap the floating `+` button from the dashboard.
- **Edit Transaction**: Tap a transaction from the dashboard list.
- **Reports**: Tap the reports icon in the dashboard top bar or the Reports quick action.
- **Categories**: Tap the Categories quick action. Use this page to manage categories, colors, and monthly category budgets.
- **Settings**: Tap the settings icon in the dashboard top bar or the Settings quick action.

## How To Use The App

1. Open the app.
2. Use the dashboard month controls to select the month you want to work with.
3. Enter the income for that month and tap **Save Income**.
4. Tap `+` to add expenses, transfers, or split transactions.
5. Select the transaction currency when the expense is not in your default currency.
6. Use **Quick Add** for simple expenses, including currency selection.
7. Open **Categories** to set category budgets for the selected month.
8. Open **Reports** to review trends, currency totals, income history, recurring plans, and forecasts.
9. Use **Export** to generate CSV and PDF files.
10. Use **Backup** before risky changes and **Restore Latest Backup** if needed.
11. Use **Clear Day**, **Clear Month**, or **Clear All Transactions** only after checking the confirmation count.

## Tech Stack

| Area | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Design | Material 3 |
| State | ViewModel, StateFlow |
| Async | Kotlin Coroutines |
| Navigation | Navigation Compose |
| Local storage | SharedPreferences |
| Background work | WorkManager |
| Cloud sync | Google Sheets API v4 |
| Minimum Android | API 24 |
| Compile SDK | 36 |
| Java target | JDK 17 |

## Project Structure

```text
app/src/main/java/com/financetracker/
├── MainActivity.kt
├── data/
│   ├── model/
│   │   ├── AccountBalance.kt
│   │   ├── Category.kt
│   │   ├── CategoryBudget.kt
│   │   ├── Currency.kt
│   │   ├── Expense.kt
│   │   ├── IncomeEntry.kt
│   │   ├── RecurringEntry.kt
│   │   └── TransactionType.kt
│   └── repository/
│       └── GoogleSheetsRepository.kt
├── ui/
│   ├── navigation/FinanceNavHost.kt
│   ├── screens/
│   │   ├── AddExpenseScreen.kt
│   │   ├── CategoriesScreen.kt
│   │   ├── DashboardScreen.kt
│   │   ├── EditExpenseScreen.kt
│   │   ├── ReportsScreen.kt
│   │   └── SettingsScreen.kt
│   ├── theme/
│   └── viewmodel/ExpenseViewModel.kt
└── workmanager/BackgroundSyncWorker.kt
```

## Developer Setup

### 1. Clone The Repo

```bash
git clone https://github.com/sukumar9024/personal-finance-mobile-app.git
cd personal-finance-mobile-app
```

### 2. Install Requirements

- Android Studio, or Android SDK command-line tools.
- JDK 17.
- Android SDK Platform 36.
- Android SDK Build Tools.
- Android emulator, or a physical Android phone with USB debugging.

Verify:

```bash
java -version
adb version
```

### 3. Create `local.properties`

Create `local.properties` in the repo root.

macOS example:

```properties
sdk.dir=/Users/YOUR_NAME/Library/Android/sdk
spreadsheet.id=YOUR_GOOGLE_SHEET_ID
```

Windows example:

```properties
sdk.dir=C:\\Users\\YOUR_NAME\\AppData\\Local\\Android\\Sdk
spreadsheet.id=YOUR_GOOGLE_SHEET_ID
```

Only `spreadsheet.id` is app-specific. `sdk.dir` is used by Gradle/Android Studio to find your local Android SDK.

## Google Sheets Setup

### 1. Create A Google Sheet

Create a blank spreadsheet in Google Sheets. The app can create the required tabs automatically after sync is configured.

Copy the spreadsheet ID from the URL:

```text
https://docs.google.com/spreadsheets/d/SPREADSHEET_ID_HERE/edit
```

Put that ID in `local.properties`:

```properties
spreadsheet.id=SPREADSHEET_ID_HERE
```

### 2. Create A Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/).
2. Create a new project, or select an existing project.
3. Open **APIs & Services**.
4. Open **Library**.
5. Search for **Google Sheets API**.
6. Enable **Google Sheets API**.

### 3. Create A Service Account

1. In Google Cloud Console, open **IAM & Admin**.
2. Open **Service Accounts**.
3. Click **Create service account**.
4. Give it a name such as `finance-tracker-sync`.
5. Finish creation.
6. Open the service account.
7. Go to **Keys**.
8. Click **Add key**.
9. Choose **Create new key**.
10. Select **JSON**.
11. Download the JSON key file.

### 4. Add The Service Account To The Sheet

1. Open the downloaded JSON key.
2. Find `client_email`.
3. Open your Google Sheet.
4. Click **Share**.
5. Add the `client_email`.
6. Give it **Editor** permission.

Without this step, the app can authenticate but cannot read or write your spreadsheet.

### 5. Add Credentials To The Project

Create this directory if it does not exist:

```text
app/src/main/assets/
```

Copy the downloaded JSON key into:

```text
app/src/main/assets/service-account-key.json
```

Do not commit this file.

## Google Sheets Schema

The app uses these sheets:

### `categories`

| Column | Header |
|---|---|
| A | Name |
| B | Color |

### `category_budgets`

| Column | Header |
|---|---|
| A | Category |
| B | Period |
| C | Amount |

`Period` format is `YYYY-MM`.

### `monthly_income`

| Column | Header |
|---|---|
| A | Month |
| B | Income |
| C | Recurring ID |

`Month` format is `YYYY-MM`.

### `recurring_entries`

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

### `expenses_YYYY_MM`

Each month has a separate expense sheet, for example `expenses_2026_08`.

| Column | Header |
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
| M | Currency |
| N | Created At |
| O | Modified At |
| P | Recurring ID |
| Q | Occurrence Period |

Older sheets without the `Currency` column are normalized automatically. Existing rows without currency default to the app default currency.

## Build And Run

### Android Studio

1. Open the repo in Android Studio.
2. Let Gradle sync.
3. Select an emulator or physical device.
4. Run the `app` configuration.

### Command Line

macOS/Linux:

```bash
export ANDROID_HOME=/Users/YOUR_NAME/Library/Android/sdk
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

If `./gradlew` is not executable:

```bash
chmod +x ./gradlew
```

If the local wrapper has download issues, use an installed Gradle:

```bash
ANDROID_HOME=/Users/YOUR_NAME/Library/Android/sdk JAVA_HOME=$(/usr/libexec/java_home -v 17) gradle :app:assembleDebug
```

Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
```

## Run On An Emulator

List emulators:

```bash
emulator -list-avds
```

Start an emulator:

```bash
emulator -avd YOUR_AVD_NAME
```

Install and launch:

```bash
./gradlew :app:installDebug
adb shell monkey -p com.financetracker -c android.intent.category.LAUNCHER 1
```

Windows helper script:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-emulator-debug.ps1
```

## Run On A Physical Android Phone

1. On the phone, enable **Developer options**.
2. Enable **USB debugging**.
3. Connect the phone with USB.
4. Trust the computer if Android asks.
5. Verify the device:

```bash
adb devices
```

Install and launch:

```bash
./gradlew :app:installDebug
adb shell monkey -p com.financetracker -c android.intent.category.LAUNCHER 1
```

Windows helper script:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-pixel-debug.ps1
```

## Test The Setup

1. Build the debug APK.
2. Launch the app.
3. Confirm the dashboard opens.
4. Confirm sync status is not showing missing `spreadsheet.id` or missing service account JSON.
5. Tap **Refresh**.
6. Add a test transaction.
7. Open Google Sheets and confirm an `expenses_YYYY_MM` tab was created or updated.
8. Save monthly income for the selected month.
9. Confirm the `monthly_income` sheet has the correct `YYYY-MM` row.
10. Switch to another month and confirm income/expenses/savings are month-specific.
11. Add two expenses in different currencies and confirm the dashboard and reports show currency totals separately.

## Troubleshooting

### Sync says spreadsheet ID is missing

- Check root `local.properties`.
- Confirm it contains `spreadsheet.id=...`.
- Rebuild the app after editing `local.properties`.

### Sync says service account key is missing

- Check this file exists:

```text
app/src/main/assets/service-account-key.json
```

- Rebuild the app after adding the file.

### Permission denied from Google Sheets

- Open the service account JSON.
- Copy `client_email`.
- Share the spreadsheet with that email as **Editor**.

### Build fails because Android SDK is missing

- Install Android SDK.
- Set `sdk.dir` in `local.properties`.
- Or set `ANDROID_HOME`.

### Build fails with JDK/JLink errors

Use JDK 17:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

### Old cached data appears

- Use **Refresh** on the dashboard.
- Use **Backup** before clearing data.
- Use **Clear Day**, **Clear Month**, or **Clear All Transactions** only after checking the confirmation count.

## Security Notes

- Never commit `app/src/main/assets/service-account-key.json`.
- Never commit private service account keys.
- Never share screenshots containing private keys.
- If a key is exposed, delete it in Google Cloud Console and create a new key.
- Keep `local.properties` local to your machine.

## License

This project is for personal use.
