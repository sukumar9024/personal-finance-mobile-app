# Finance Tracker - Quick Setup Guide

This guide will walk you through the exact steps to configure and run the app.

## Step 1: Create Google Cloud Project & Enable Sheets API

1. Go to https://console.cloud.google.com/
2. Create a new project (or use existing)
3. In the navigation menu: **APIs & Services → Library**
4. Search for "Google Sheets API" and click **Enable**
5. Also enable "Google Drive API" (for future receipt upload feature)

## Step 2: Create Service Account

1. In Google Cloud Console: **APIs & Services → Credentials**
2. Click **Create Credentials** → **Service Account**
3. Give it a name: `finance-tracker-sa`
4. Click **Create and Continue** (skip granting permissions for now)
5. Click **Done**

## Step 3: Create Service Account Key

1. Find your new service account in the Credentials list
2. Click on it to open details
3. Go to **Keys** tab
4. Click **Add Key** → **Create new key**
5. Choose **JSON** and click **Create**
6. The key file will download automatically
7. **IMPORTANT**: Keep this file secure! It contains private credentials.

## Step 4: Create Google Spreadsheet

1. Go to https://sheets.google.com/
2. Click **+ Blank** to create new spreadsheet
3. Rename the default sheet to: `categories` (exactly this name)
4. In `categories` sheet, add these headers in **row 1**:
   - Cell A1: `Name`
   - Cell B1: `Color`
   - Cell C1: `Monthly Budget`

5. Add default categories (optional - you can add later):
   ```
   Row 2: Food, #FF5722, 10000
   Row 3: Transport, #2196F3, 5000
   Row 4: Shopping, #E91E63, 8000
   Row 5: Bills, #9C27B0, 15000
   Row 6: Entertainment, #FF9800, 5000
   Row 7: Health, #4CAF50, 3000
   Row 8: Education, #3F51B5, 2000
   Row 9: Other, #607D8B, 0
   ```

6. Note your Spreadsheet URL: `https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit`
7. Copy the **Spreadsheet ID** (the part between `/d/` and `/edit`)
   - Example: `1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms`

## Step 5: Share Spreadsheet with Service Account

1. In your Google Sheet, click the **Share** button (top right)
2. In the "Add people and groups" field, paste the **Service Account email**
   - Find this in your downloaded JSON key file
   - Look for: `"client_email": "your-service-account@your-project.iam.gserviceaccount.com"`
3. Set permissions to **Editor**
4. Click **Send** (or just "Share" without sending email)

## Step 6: Add Credentials to App (Two Options)

### Option A: Place JSON in Assets (Recommended for Development)

1. Rename your downloaded JSON file to: `service-account-key.json`
2. Copy it to: `app/src/main/assets/service-account-key.json`
3. That's it! The app will load it automatically.

**Important**: The `app/src/main/assets/` directory might not exist yet. Create it.

### Option B: Use Build Config Fields

1. Open `app/build.gradle.kts`
2. In the `android.defaultConfig` block, add:
   ```kotlin
   buildConfigField("String", "SPREADSHEET_ID", "\"YOUR_SPREADSHEET_ID_HERE\"")
   ```
3. For the service account JSON, you can either:
   - Use Option A (simpler)
   - Or add the entire JSON as a string (not recommended due to complexity)

## Step 7: Verify Configuration

1. Edit `app/src/main/res/values/strings.xml` if needed (optional)
2. Review the Settings screen will show your configured Spreadsheet ID

## Step 8: Build and Run

### Prerequisites Check:
```bash
# Check Java version (need 17+)
java -version

# Check Android SDK
echo $ANDROID_HOME
```

If ANDROID_HOME is not set, add to your shell profile:
```bash
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

### Build the App:
```bash
# Make gradlew executable
chmod +x gradlew

# Clean and build
./gradlew clean build

# Or build debug APK
./gradlew assembleDebug
```

### Run on Device:
1. Connect Android device (API 24+) with USB debugging enabled
2. Or start an Android emulator from Android Studio
3. Run: `./gradlew installDebug`

## Step 9: First App Launch

1. Open the app on your device
2. Go to **Settings** (gear icon)
3. Verify your Spreadsheet ID is displayed (if using Option A, it will be empty string by default - that's OK)
4. The app will automatically:
   - Create `expenses_2025_04` (current month) sheet if it doesn't exist
   - Load categories from `categories` sheet
5. Tap **+** to add your first expense
6. Check your Google Sheet - data will appear instantly!

## Troubleshooting

### "Failed to load expenses" error:
1. Check Logcat in Android Studio for detailed error
2. Verify spreadsheet ID is correct (if using build config)
3. Ensure spreadsheet is shared with service account email
4. Confirm Google Sheets API is enabled in Cloud Console

### Sheets not auto-creating:
- Service account must have **Editor** permission
- Check service account email matches the one you shared with
- Verify JSON key file is in the correct location: `app/src/main/assets/`

### Categories not loading:
- Create `categories` sheet with exact headers (case-sensitive)
- Wait a few seconds for Google Sheets to propagate permissions
- Restart the app

### Build fails:
1. Ensure you have Java 17: `java -version`
2. Ensure Android SDK is installed
3. Set ANDROID_HOME environment variable
4. Run: `./gradlew --refresh-dependencies`

## Testing without Real Data

If you want to test the UI without connecting to Google Sheets yet:

1. Temporarily modify `GoogleSheetsRepository.kt` to return mock data:
   ```kotlin
   suspend fun fetchExpenses(sheetName: String): List<Expense> {
       return listOf(
           Expense(
               date = LocalDate.now(),
               amount = 100.0,
               category = "Food",
               description = "Test expense"
           )
       )
   }
   ```

2. The app will show test data instead of real sheets

## Next Steps After Setup

1. Add more categories to the `categories` sheet as needed
2. Set monthly budgets for each category
3. Start tracking expenses!
4. Check Reports screen for visual insights
5. Consider backing up your service account key securely

## Security Best Practices

1. **Never commit** `service-account-key.json` to version control
2. The `.gitignore` file already excludes it, but double-check
3. For production release, consider:
   - Using Android Keystore to store credentials
   - Using a backend server as a proxy (instead of direct API access)
   - Implementing OAuth2 user-based authentication
   - Using Google's App Passwords if using 2FA

## Need Help?

- Check README.md for detailed documentation
- Review error messages in the app's Settings screen
- Check Logcat output: `adb logcat | grep FinanceTracker`
- Verify all steps in this guide were followed correctly

---

**Happy Expense Tracking!** 📊