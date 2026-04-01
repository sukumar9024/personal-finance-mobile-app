# Configuration Guide for Personal Finance Mobile App

This guide will help you configure the app to work with your Google Sheets setup.

## 1. Extract Spreadsheet ID from Your Google Sheets Link

Your provided Google Sheets link:
https://docs.google.com/spreadsheets/d/1rxcpH-jeU5p4P7Pq_CbqdsvyBSvJUDm-WPdq1ZAlpDs/edit?usp=sharing

The Spreadsheet ID is: `1rxcpH-jeU5p4P7Pq_CbqdsvyBSvJUDm-WPdq1ZAlpDs`

## 2. Configure Build Settings

Edit `app/build.gradle.kts` and update the buildConfigField values:

```kotlin
android {
    defaultConfig {
        // ... existing configuration
        
        // ADD THESE LINES with your actual values:
        buildConfigField("String", "SPREADSHEET_ID", "\"1rxcpH-jeU5p4P7Pq_CbqdsvyBSvJUDm-WPdq1ZAlpDs\"")
        
        // For service account JSON, you can either:
        // Option A: Set it directly in build config (not recommended for production)
        // buildConfigField("String", "SERVICE_ACCOUNT_JSON", "\"{\\\"type\\\":\\\"service_account\\\",\\\"project_id\\\":\\\"your-project-id\\\",...}\"")
        
        // Option B: Use assets method (recommended)
        // Leave SERVICE_ACCOUNT_JSON empty and use assets method below
    }
}
```

## 3. Set Up Service Account (Recommended Method)

### Step 1: Create Service Account JSON File
1. Create a file named `service-account-key.json` with your service account key
2. Place it in: `app/src/main/assets/service-account-key.json`

### Step 2: Update build.gradle.kts
```kotlin
android {
    defaultConfig {
        // ... existing configuration
        
        // Make sure SERVICE_ACCOUNT_JSON is empty to use assets method
        buildConfigField("String", "SERVICE_ACCOUNT_JSON", "\"\"")
    }
}
```

## 4. Share Spreadsheet with Service Account

1. Open your Google Sheet
2. Click the "Share" button
3. Add the service account email (from your JSON key) with "Editor" permissions
4. Make sure the spreadsheet is accessible to the service account

## 5. Build and Run

```bash
./gradlew clean build
# Or use Android Studio to build and run
```

## 6. Verify Configuration

1. Open the app
2. Go to Settings screen
3. Check that the current month sheet is displayed correctly
4. Verify that categories load properly
5. Try adding a test expense to verify the connection

## Troubleshooting

### Common Issues:
- **"Failed to load expenses"**: Check spreadsheet ID, service account permissions, and internet connection
- **Sheet not auto-creating**: Ensure service account has Editor permissions
- **Categories not loading**: Verify `categories` sheet exists with correct headers

### Testing Your Setup:
1. Add a test expense through the app
2. Check that it appears in your Google Sheet
3. Verify the monthly sheet is created automatically
4. Test the reports screen to see category breakdowns