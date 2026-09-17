package com.example.expensemanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensemanager.data.model.ExpenseCategory
import com.example.expensemanager.ui.components.CategorySpendingBar
import com.example.expensemanager.ui.components.MemberBalanceCard
import com.example.expensemanager.utils.CurrencyUtils
import com.example.expensemanager.viewmodel.ExpenseViewModel
import com.example.expensemanager.viewmodel.RoomViewModel

@Composable
fun ReportsScreen(
    expenseViewModel: ExpenseViewModel,
    roomViewModel: RoomViewModel
) {
    val calculation by expenseViewModel.calculationResult.collectAsState()
    val allExpenses by expenseViewModel.expenses.collectAsState()

    // Calculate category spending breakdown
    val categoryTotals = ExpenseCategory.values().mapNotNull { cat ->
        val catSum = allExpenses.filter { it.category.equals(cat.name, ignoreCase = true) }.sumOf { it.amount }
        if (catSum > 0) Pair(cat, catSum) else null
    }.sortedByDescending { it.second }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Expense Reports",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Detailed analytics, member shares, and category spending",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Room Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Group Expense", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(CurrencyUtils.formatRupee(calculation.totalExpense), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Expenses Logged", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text("${allExpenses.size}", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Equal Share Per Member", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(CurrencyUtils.formatRupee(calculation.equalShare), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Category Breakdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Category Spending Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (categoryTotals.isEmpty()) {
                        Text("No spending data available yet.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        categoryTotals.forEach { (cat, sum) ->
                            val pct = if (calculation.totalExpense > 0) (sum / calculation.totalExpense).toFloat() else 0f
                            CategorySpendingBar(category = cat, amount = sum, percentage = pct)
                        }
                    }
                }
            }
        }

        // Member Contributions
        item {
            Text("Member Contributions & Balances", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(calculation.memberShares) { share ->
            MemberBalanceCard(share = share)
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
