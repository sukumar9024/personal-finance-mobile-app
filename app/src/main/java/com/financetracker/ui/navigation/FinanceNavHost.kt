package com.financetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.financetracker.ui.screens.*
import com.financetracker.ui.viewmodel.ExpenseViewModel

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object AddExpense : Screen("add_expense")
    object EditExpense : Screen("edit_expense/{expenseId}") {
        fun createRoute(expenseId: String) = "edit_expense/$expenseId"
    }
    object Categories : Screen("categories")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}

@Composable
fun FinanceNavHost(
    navController: NavHostController = rememberNavController(),
    viewModel: ExpenseViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onAddExpenseClick = {
                    navController.navigate(Screen.AddExpense.route)
                },
                onExpenseClick = { expense ->
                    navController.navigate(Screen.EditExpense.createRoute(expense.id))
                },
                onCategoriesClick = {
                    navController.navigate(Screen.Categories.route)
                },
                onReportsClick = {
                    navController.navigate(Screen.Reports.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.AddExpense.route) {
            AddExpenseScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EditExpense.route) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getString("expenseId") ?: ""
            EditExpenseScreen(
                viewModel = viewModel,
                expenseId = expenseId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
