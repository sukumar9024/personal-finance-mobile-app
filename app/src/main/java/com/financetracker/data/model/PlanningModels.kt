package com.financetracker.data.model

data class DashboardCardPreference(
    val id: String,
    val title: String,
    val group: String = "Overview",
    val sortOrder: Int = 0,
    val visible: Boolean = true
)

enum class RecurringReminderStatus {
    PENDING,
    PAID,
    SKIPPED
}

data class RecurringReminderOccurrence(
    val entryId: String,
    val period: String,
    val status: RecurringReminderStatus = RecurringReminderStatus.PENDING
)

data class CategoryRolloverSetting(
    val category: String,
    val enabled: Boolean = false,
    val carryOverspend: Boolean = true
)

data class MonthlyCloseNote(
    val period: String,
    val notes: String = ""
)

data class CsvImportMapping(
    val name: String,
    val dateColumn: String = "Date",
    val amountColumn: String = "Amount",
    val descriptionColumn: String = "Description",
    val categoryColumn: String = "Category",
    val accountColumn: String = "Account",
    val currencyColumn: String = "Currency"
)

data class DebtAccount(
    val id: String,
    val name: String,
    val currentBalance: Double,
    val interestRate: Double,
    val minimumPayment: Double,
    val dueDay: Int,
    val targetPayoffPeriod: String,
    val currencyCode: String = Currency.getDefault().code,
    val paymentHistory: List<Double> = emptyList()
)

data class InvestmentHolding(
    val id: String,
    val name: String,
    val assetType: String,
    val units: Double,
    val averageCost: Double,
    val currentValue: Double,
    val monthlyContribution: Double,
    val currencyCode: String = Currency.getDefault().code
)
