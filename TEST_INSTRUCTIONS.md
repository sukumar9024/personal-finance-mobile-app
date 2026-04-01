# Testing Instructions for Personal Finance App

## Prerequisites

Before testing the app, ensure you have:
1. Android Studio installed
2. Android SDK with API level 34
3. An Android emulator or physical device connected
4. Your Google Sheets service account JSON file ready

## Setup Steps

### 1. Configure Google Sheets Access

1. **Get your Spreadsheet ID** from your Google Sheets URL:
   ```
   https://docs.google.com/spreadsheets/d/YOUR_SPREADSHEET_ID/edit
   ```
   Your ID is: `1rxcpH-jeU5p4P7Pq_CbqdsvyBSvJUDm-WPdq1ZAlpDs`

2. **Create the categories sheet** in your Google Sheet:
   - Rename the default sheet to "categories"
   - Add headers in row 1:
     - A1: Name
     - B1: Color  
     - C1: Monthly Budget

3. **Add sample categories** (optional):
   ```
   A2: Food, B2: #FF5722, C2: 10000
   A3: Transport, B3: #2196F3, C3: 5000
   A4: Shopping, B4: #E91E63, C4: 8000
   ```

### 2. Configure the App

1. **Update build.gradle.kts**:
   ```kotlin
   android {
       defaultConfig {
           // ... existing config
           buildConfigField("String", "SPREADSHEET_ID", "\"1rxcpH-jeU5p4P7Pq_CbqdsvyBSvJUDm-WPdq1ZAlpDs\"")
           buildConfigField("String", "SERVICE_ACCOUNT_JSON", "\"\"")
       }
   }
   ```

2. **Add service account key**:
   - Create `app/src/main/assets/service-account-key.json`
   - Paste your service account JSON content

### 3. Build and Run

1. **In Android Studio**:
   - Open the project
   - Wait for Gradle sync to complete
   - Click "Run" button (▶️)

2. **Using command line**:
   ```bash
   # Make sure you have the right permissions
   chmod +x gradlew
   
   # Build the app
   ./gradlew assembleDebug
   
   # Install on connected device/emulator
   ./gradlew installDebug
   ```

## Expected Test Results

### After Successful Setup:

1. **App Launch**:
   - Should show dashboard with current month's expenses
   - Display total expenses and category breakdown

2. **Add Expense Test**:
   - Tap "+" button
   - Enter test expense details
   - Confirm expense appears in Google Sheet

3. **Reports Test**:
   - Navigate to Reports screen
   - Verify pie chart visualization
   - Check category spending breakdown

4. **Settings Verification**:
   - Go to Settings
   - Confirm spreadsheet ID is displayed
   - Verify current month sheet name

## Troubleshooting

### Common Issues:

1. **"Failed to load expenses"**:
   - Verify spreadsheet ID is correct
   - Check service account has Editor permissions
   - Ensure internet connection

2. **Sheet not auto-creating**:
   - Confirm service account email is shared with spreadsheet
   - Check Google Sheets API is enabled in Google Cloud Console

3. **Categories not loading**:
   - Verify categories sheet exists with correct headers
   - Check column order matches expected format

## What to Expect

The app will:
- Automatically create monthly sheets (expenses_YYYY_MM)
- Sync all expense data to your Google Sheet
- Provide real-time expense tracking
- Show category-based spending analysis
- Support budget monitoring with visual progress indicators

## Next Steps

1. Complete the configuration steps above
2. Build and run the app in Android Studio
3. Test the core functionality with sample data
4. Verify data synchronization with your Google Sheet