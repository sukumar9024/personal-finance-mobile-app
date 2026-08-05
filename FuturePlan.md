# Future Plan

## Calendar Spending Heatmap

Add a calendar heatmap to improve the visual understanding of daily spending patterns.

### Goal

Replace or enhance the current dashboard spending calendar with a pictorial heatmap where each day visually reflects how much was spent on that date.

### User Experience

- Show one month at a time.
- Each day cell displays the day number.
- Days with no spending remain neutral.
- Low-spending days use a light color.
- Medium-spending days use a stronger color.
- High-spending days use the strongest color or a subtle glow.
- Add a small legend explaining the spending intensity levels.
- Tapping a day opens a detail view for that date.

### Day Detail View

When the user taps a day, show:

- Total spent on that day.
- Total spent grouped by currency.
- Number of transactions.
- List of transactions for that date.
- Option to clear that day after confirmation.

### Multi-Currency Behavior

- If a day has expenses in multiple currencies, show currency totals separately.
- If exchange-rate conversion is enabled and all required rates exist, optionally use the converted preferred-currency total for heat intensity.
- If exchange rates are incomplete, fall back to per-currency indicators.

### Placement

The heatmap should live on the Dashboard first. It should enhance the existing Spending Calendar card instead of adding a separate page initially.

### Future UX Improvement

If the dashboard becomes too dense, group dashboard cards into sections or tabs:

- Overview
- Calendar
- Goals
- Data

This keeps the dashboard useful without overwhelming the user.

## Dashboard Section Collapse And Customization

Let users control which dashboard cards are visible and how much space they take.

### Goal

Prevent the dashboard from becoming too long as more features are added.

### Planned Behavior

- Add collapse/expand controls for major dashboard cards.
- Save collapsed state locally.
- Add a Settings section for dashboard card visibility.
- Let users hide cards they do not use often.
- Later, consider drag-and-drop card reorder.

### Example Dashboard Cards

- Monthly Overview
- Savings Dashboard
- Spending Calendar
- Multi-Currency Expenditure
- Exchange Conversion
- Monthly Operating View
- Net Worth
- Savings Goals
- Quick Add
- Recent Transactions

## Transaction Bulk Actions

Add multi-select actions for faster cleanup and organization.

### Planned Actions

- Select multiple transactions.
- Delete selected transactions after confirmation.
- Change category for selected transactions.
- Change payment account for selected transactions.
- Add or replace tags for selected transactions.
- Show selected count and total amount before applying changes.

## Advanced Search Filters

Improve transaction discovery beyond text, category, account, and date range.

### Planned Filters

- Currency.
- Amount range.
- Tags.
- Transaction type: expense, transfer, split.
- Description or merchant.
- Has receipt URL.
- Recurring vs manual transaction.
- Over-budget category transactions.

## Spending Insights

Add automatic insights that explain spending changes in plain language.

### Example Insights

- Food spending increased compared with last month.
- UPI spending is unusually high this month.
- A category is close to exceeding budget.
- A merchant appears more often than usual.
- Savings rate improved compared with the previous month.
- Spending is projected to exceed income before month end.

## Budget Rollover

Allow unused budget from one month to carry into the next month.

### Planned Behavior

- Toggle rollover per category.
- Carry unused category budget forward.
- Optionally carry overspending forward as a negative rollover.
- Show base budget, rollover amount, and effective budget.
- Keep rollover calculations month-specific.

## Monthly Close Review

Add an end-of-month review flow.

### Goal

Help users finish a month, understand what happened, and prepare the next month.

### Review Items

- Final income.
- Final spending by currency.
- Final savings.
- Savings rate.
- Over-budget categories.
- Top merchants.
- Biggest transactions.
- Recurring entries applied.
- Missing monthly income warning.
- Export CSV/PDF from the review screen.
- Optional notes for the month.

## Data Validation Dashboard

Add a diagnostics screen to find data quality issues.

### Validation Checks

- Months missing income.
- Expenses missing category.
- Expenses missing currency.
- Duplicate-like transactions.
- Rows with invalid dates.
- Rows with invalid amounts.
- Category budgets with unknown category names.
- Recurring entries with invalid day of month.
- Google Sheets schema mismatch.

## CSV Import

Support importing bank, card, or wallet statement CSV files.

### Planned Flow

- Pick a CSV file from device storage.
- Preview rows before import.
- Map CSV columns to app fields.
- Choose default category, account, and currency.
- Detect possible duplicates.
- Import only selected rows.
- Save import mapping for future files from the same bank.

## Google Sheets Setup Validator

Add an in-app validation tool for Google Sheets sync setup.

### Validation Checks

- Spreadsheet ID exists.
- Service account JSON exists.
- Google Sheets API authentication works.
- Spreadsheet is shared with the service account.
- Required sheets exist or can be created.
- Required headers match the app schema.
- App can read and write a test row.

## Recurring Reminders

Notify users before expected recurring expenses or income dates.

### Planned Behavior

- Reminder toggle per recurring entry.
- Notify a configurable number of days before the due day.
- Show amount, currency, category, and payment account in the notification.
- Allow marking an entry as already paid or skipped.

## Goal Auto-Contribution

Automatically suggest how to allocate monthly savings into savings goals.

### Planned Behavior

- Set goal priority.
- Allocate monthly savings by priority or percentage.
- Preview allocations before applying.
- Update current saved amount for each goal.
- Show expected completion month.

## Debt Payoff Tracker

Add dedicated debt tracking for loans and credit cards.

### Planned Fields

- Debt name.
- Current balance.
- Interest rate.
- Minimum payment.
- Due date.
- Target payoff date.
- Payment history.

### Planned Metrics

- Remaining balance.
- Payoff progress.
- Estimated payoff month.
- Interest saved with extra payments.
- Upcoming payment reminders.

## Investment Tracking

Add manual investment tracking without requiring brokerage integration.

### Planned Fields

- Investment name.
- Asset type.
- Units or shares.
- Average cost.
- Current value.
- Monthly contribution.
- Currency.

### Planned Metrics

- Total invested.
- Current value.
- Gain or loss.
- Monthly contribution trend.
- Allocation by asset type.
