# Finance Tracker - Android Personal Finance App

A modern Android personal finance application built with Kotlin and Jetpack Compose that syncs directly with Google Sheets.

## Features

### Core Functionality
- **Add/View/Edit/Delete Expenses**: Full CRUD operations on expense records
- **Monthly Tabs**: Automatic creation of monthly expense sheets (expenses_YYYY_MM)
- **Category Management**: Pre-defined categories with color coding
- **Real-time Sync**: Direct integration with Google Sheets API
- **Dashboard**: Overview of total expenses with quick stats
- **Reports**: Visual pie charts and category breakdown with budget tracking

### Advanced Features
- Receipt URL storage (for linking receipt images)
- Payment method tracking (Cash, Card, UPI, etc.)
- Tags for flexible expense organization
- Subcategories for detailed tracking
- Budget monitoring with progress indicators
- Date-based filtering
- Search and sort capabilities (infrastructure in place)

## Architecture

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Google Sheets API v4
- **Async**: Kotlin Coroutines & Flow
- **Navigation**: Jetpack Navigation Compose

## Project Structure

```
app/src/main/java/com/financetracker/
├── MainActivity.kt                    # App entry point
├── data/
│   ├── model/
│   │   ├── Expense.kt                # Expense data model
│   │   └── Category.kt               # Category data model
│   └── repository/
│       └── GoogleSheetsRepository.kt # Google Sheets API integration
├── ui/
│   ├── navigation/
│   │   └── FinanceNavHost.kt        # Navigation setup
│   ├── screens/
│   │   ├── DashboardScreen.kt      # Main dashboard
│   │   ├── AddExpenseScreen.kt     # Add new expense
│   │   ├── EditExpenseScreen.kt    # Edit existing expense
│   │   ├── CategoriesScreen.kt     # View all categories
│   │   ├── ReportsScreen.kt        # Analytics and reports
│   │   └── SettingsScreen.kt       # Configuration
│   ├── theme/
│   │   └── Theme.kt                # App theme and colors
│   └── viewmodel/
│       └── ExpenseViewModel.kt     # ViewModel for expense data
└── res/                             # Android resources
    ├── values/strings.xml
    ├── xml/backup_rules.xml
    └── xml/data_extraction_rules.xml
```

## Google Sheets Structure

The app creates and uses the following sheets:

### 1. Monthly Expense Sheets (auto-created)
**Name**: `expenses_YYYY_MM` (e.g., `expenses_2025_04`)

**Columns**:
| Column | Description |
|--------|-------------|
| A - Date | Expense date (YYYY-MM-DD) |
| B - Amount | Expense amount (decimal) |
| C - Category | Expense category |
| D - Subcategory | Optional subcategory |
| E - Description | Expense description |
| F - Payment Method | Cash, Card, UPI, etc. |
| G - Receipt URL | Link to receipt image (optional) |
| H - Tags | Comma-separated tags |
| I - Created At | Timestamp |
| J - Modified At | Timestamp |

### 2. Categories Sheet (required)
**Name**: `categories`

**Columns**:
| Column | Description |
|--------|-------------|
| A - Name | Category name (Food, Transport, etc.) |
| B - Color | Hex color code (e.g., #FF5722) |
| C - Monthly Budget | Optional budget amount |

**Default Categories** (if sheet doesn't exist):
- Food (#FF5722)
- Transport (#2196F3)
- Shopping (#E91E63)
- Bills (#9C27B0)
- Entertainment (#FF9800)
- Health (#4CAF50)
- Education (#3F51B5)
- Other (#607D8B)

## Setup Instructions

### 1. Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- Java 17+
- Android SDK with API level 34 (Android 14)
- Google Cloud Platform account

### 2. Create Google Cloud Project & Service Account

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable **Google Sheets API**:
   - APIs & Services → Library → Search "Google Sheets API" → Enable
4. Create Service Account:
   - APIs & Services → Credentials → Create Credentials → Service Account
   - Give it a name (e.g., "finance-tracker-app")
   - Grant permissions: Editor (for simplicity in single-user setup)
   - Complete creation
5. Create Key:
   - In Service Account details → Keys → Add Key → Create New Key
   - Choose **JSON** format
   - Download the key file
6. Note the Service Account email (looks like: `something@project-id.iam.gserviceaccount.com`)

### 3. Create Google Spreadsheet

1. Go to [Google Sheets](https://sheets.google.com/)
2. Create a new spreadsheet
3. **Rename** the default sheet to `categories`
4. In `categories` sheet, add headers in row 1:
   - A1: `Name`
   - B1: `Color`
   - C1: `Monthly Budget`
5. Add your category rows below (optional - can also be managed from app later)

### 4. Share Spreadsheet with Service Account

1. In Google Sheets, click **Share** button
2. Add the **Service Account email** (from step 2)
3. Set permissions to **Editor**
4. Click Share

### 5. Get Spreadsheet ID

From your Google Sheets URL:
```
https://docs.google.com/spreadsheets/d/`1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms`/edit
```
The **Spreadsheet ID** is the part between `/d/` and `/edit`:
```
1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms
```

### 6. Configure App

You have **two options** to add credentials to the app:

#### Option A: Build Config (Recommended for Your Use Case)
Edit `app/build.gradle.kts` and add your credentials in the `defaultConfig` block:

```kotlin
android {
    defaultConfig {
        applicationId = "com.financetracker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // ADD THESE LINES:
        buildConfigField("String", "SPREADSHEET_ID", "\"YOUR_SPREADSHEET_ID_HERE\"")
        
        // Paste the entire JSON content as a single line string (escape quotes)
        // OR use assets method below for easier management
    }
}
```

#### Option B: Assets File (Easier)
1. Place your service account JSON file in: `app/src/main/assets/service-account-key.json`
2. The app will automatically load it from assets if `SERVICE_ACCOUNT_JSON` build config is empty

**For Option B**, modify `app/build.gradle.kts` to NOT require the build config fields, or leave them empty.

### 7. Build & Run

```bash
# Make gradlew executable (Mac/Linux)
chmod +x gradlew

# Build the app
./gradlew assembleDebug

# Or build and install on connected device
./gradlew installDebug
```

Or use Android Studio:
1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Connect an Android device (API 24+) or start emulator
4. Click Run ▶️

## Usage Guide

### First Launch
1. The app will automatically create monthly sheets as needed
2. Go to **Settings** to verify your Spreadsheet ID is configured
3. Categories will load from the `categories` sheet (or use defaults)
4. Start adding expenses!

### Adding Expenses
1. Tap the **+** floating button on the dashboard
2. Select date (defaults to today)
3. Enter amount (in INR ₹)
4. Choose category
5. Optionally add: subcategory, description, payment method, tags
6. Tap **Save Expense**

### Viewing & Editing
- Tap any expense card on the dashboard to edit it
- Swipe/scroll to see all expenses
- Total expenses displayed prominently

### Reports
- View pie chart breakdown by category
- See category spending vs budget (if budget set)
- Progress bars show budget utilization
- Color-coded warnings when approaching/exceeding budget

### Categories
- View all categories with their colors
- Budgets displayed on cards
- Categories are pre-populated from Google Sheet

## Technical Notes

### Google Sheets API Quotas
- Free quota: 500 requests per 100 seconds per project
- 60 requests per minute per user
- This app is single-user and makes minimal requests, so quotas are sufficient

### Data Storage
- **No local storage** - all data lives in Google Sheets
- Each expense operation reads/writes directly to the sheet
- Row indices are tracked for efficient updates
- Sheet names follow pattern: `expenses_YYYY_MM`

### Error Handling
- Failed operations show inline error messages
- Network errors handled gracefully
- Auto-retry for transient failures
- Sheet creation happens automatically

### Security Considerations
- Service account credentials should never be committed to version control
- For production, consider using Android Keystore or build secrets
- The spreadsheet must be shared with the service account email with Editor access
- Anyone with the spreadsheet link can view (if published), but editing requires the service account key

## Customization

### Add New Categories
1. In Google Sheets, go to `categories` sheet
2. Add a new row:
   - Name: Your category name
   - Color: Hex color code (e.g., #FF0000 for red)
   - Monthly Budget: Optional number (e.g., 5000)
3. Refresh the app (or restart) to see changes

### Change Color Scheme
Edit `app/src/main/java/com/financetracker/ui/theme/Theme.kt`

### Modify Columns
The data model (`Expense.kt`) and repository (`GoogleSheetsRepository.kt`) would need updates if you add/remove columns.

## Troubleshooting

### "Failed to load expenses" error
- Verify Google Sheets API is enabled in Google Cloud Console
- Check that spreadsheet ID is correct in settings
- Ensure spreadsheet is shared with service account email
- Confirm internet permission in manifest
- Check Logcat for detailed error messages

### Sheets not auto-creating
- Service account must have Editor permission
- API might not be enabled - check Google Cloud Console
- Invalid credentials - verify JSON key file

### Categories not loading
- Create `categories` sheet with headers
- Or wait - defaults will be used if sheet doesn't exist
- Check column headers match exactly: Name, Color, Monthly Budget

### Build fails
- Ensure you have Java 17+ (`java -version`)
- Check Android SDK is installed and ANDROID_HOME is set
- Run `./gradlew --refresh-dependencies` to refresh
- Ensure you have internet connection for first build (downloads dependencies)

## Future Enhancements (Not Yet Implemented)

- [ ] Receipt photo upload with Google Drive integration
- [ ] Recurring expenses with scheduling
- [ ] Multi-currency support
- [ ] Data export to CSV/JSON
- [ ] Multiple spreadsheets support
- [ ] Dark theme toggle
- [ ] Biometric authentication
- [ ] Backups and restore
- [ ] Custom date range filtering
- [ ] Search across all fields
- [ ] Receipt scanning with OCR

## Contributing

This is a personal project for your specific use case. Feel free to:
- Add new features
- Improve UI/UX
- Optimize performance
- Add unit and UI tests

## License

Free to use for personal purposes. No warranty provided.

## Support

For issues or questions, check:
1. Google Cloud Console for API errors
2. Logcat output in Android Studio
3. Settings screen for current status
4. Ensure your service account has proper permissions

---

**Version**: 1.0  
**Last Updated**: April 2025  
**Platform**: Android (API 24+)