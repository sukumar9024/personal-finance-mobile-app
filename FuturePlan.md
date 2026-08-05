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
