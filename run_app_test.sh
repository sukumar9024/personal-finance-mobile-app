#!/bin/bash

# Script to setup and test the personal finance mobile app
# This script demonstrates the steps needed to run and test the app

echo "==============================================="
echo "PERSONAL FINANCE APP SETUP AND TESTING GUIDE"
echo "==============================================="

echo ""
echo "1. VERIFYING PROJECT STRUCTURE"
echo "============================="
if [ -d "app" ] && [ -f "build.gradle.kts" ]; then
    echo "✓ Project structure verified"
else
    echo "✗ Project structure missing"
    exit 1
fi

echo ""
echo "2. CHECKING GRADLE SETUP"
echo "======================="
if [ -f "gradlew" ]; then
    echo "✓ Gradle wrapper found"
    # Make executable
    chmod +x gradlew
else
    echo "✗ Gradle wrapper not found"
    exit 1
fi

echo ""
echo "3. VERIFYING GOOGLE SHEETS CONFIGURATION"
echo "======================================="
echo "Your provided Google Sheets ID: 1rxcpH-jeU5p4P7Pq_CbqdsvyBSvJUDm-WPdq1ZAlpDs"
echo "This ID should be configured in app/build.gradle.kts"

echo ""
echo "4. REQUIRED SETUP STEPS"
echo "======================="
echo "Before running the app, you need to:"
echo "1. Create a service account in Google Cloud Console"
echo "2. Enable Google Sheets API"
echo "3. Download the service account JSON key"
echo "4. Place it in app/src/main/assets/service-account-key.json"
echo "5. Share your Google Sheet with the service account email"

echo ""
echo "5. BUILDING THE APP"
echo "=================="
echo "To build the app, run:"
echo "   ./gradlew assembleDebug"

echo ""
echo "6. RUNNING ON EMULATOR/DEVICE"
echo "============================="
echo "To run on connected device/emulator:"
echo "   ./gradlew installDebug"

echo ""
echo "7. TESTING PROCEDURES"
echo "===================="
echo "After installation, test these features:"
echo "- Launch app and verify dashboard loads"
echo "- Add a test expense and verify it syncs to Google Sheets"
echo "- Navigate to Reports screen to see category breakdown"
echo "- Check Settings screen for spreadsheet configuration"

echo ""
echo "8. EXPECTED BEHAVIOR"
echo "==================="
echo "✓ Monthly sheets should auto-create (expenses_YYYY_MM)"
echo "✓ Expenses should sync to Google Sheets in real-time"
echo "✓ Category-based filtering should work"
echo "✓ Total expense calculation should be accurate"
echo "✓ Reports should show category breakdowns"

echo ""
echo "==============================================="
echo "READY TO TEST - FOLLOW THE STEPS ABOVE"
echo "==============================================="