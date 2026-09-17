package com.example.expensemanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expensemanager.data.model.Expense
import com.example.expensemanager.data.model.ExpenseCategory
import com.example.expensemanager.ui.components.ConfirmationDialog
import com.example.expensemanager.ui.components.EmptyStateView
import com.example.expensemanager.ui.components.ExpenseCard
import com.example.expensemanager.ui.navigation.Screen
import com.example.expensemanager.viewmodel.AuthUiState
import com.example.expensemanager.viewmodel.AuthViewModel
import com.example.expensemanager.viewmodel.ExpenseSortOrder
import com.example.expensemanager.viewmodel.ExpenseViewModel
import com.example.expensemanager.viewmodel.RoomViewModel

@Composable
fun ExpensesScreen(
    navController: NavController,
    expenseViewModel: ExpenseViewModel,
    roomViewModel: RoomViewModel,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val currentUserId = (authState as? AuthUiState.Authenticated)?.user?.uid ?: ""
    val currentRoom by roomViewModel.currentRoom.collectAsState()

    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var searchTxt by remember { mutableStateOf("") }
    val selectedCategory by expenseViewModel.selectedCategory.collectAsState()
    val currentSort by expenseViewModel.sortOrder.collectAsState()

    val filteredList = expenseViewModel.getFilteredExpenses()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddExpense.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchTxt,
                onValueChange = {
                    searchTxt = it
                    expenseViewModel.searchQuery.value = it
                },
                placeholder = { Text("Search expenses by title or note...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchTxt.isNotEmpty()) {
                        IconButton(onClick = {
                            searchTxt = ""
                            expenseViewModel.searchQuery.value = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { expenseViewModel.selectedCategory.value = null },
                        label = { Text("All Categories") }
                    )
                }
                items(ExpenseCategory.values()) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            expenseViewModel.selectedCategory.value = if (selectedCategory == cat) null else cat
                        },
                        label = { Text(cat.displayName) }
                    )
                }
            }

            // Sorting Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredList.size} Expenses",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                TextButton(onClick = {
                    val nextSort = when (currentSort) {
                        ExpenseSortOrder.NEWEST -> ExpenseSortOrder.HIGHEST_AMOUNT
                        ExpenseSortOrder.HIGHEST_AMOUNT -> ExpenseSortOrder.LOWEST_AMOUNT
                        ExpenseSortOrder.LOWEST_AMOUNT -> ExpenseSortOrder.OLDEST
                        ExpenseSortOrder.OLDEST -> ExpenseSortOrder.NEWEST
                    }
                    expenseViewModel.sortOrder.value = nextSort
                }) {
                    Icon(Icons.Default.Sort, contentDescription = null)
                    Text(" Sort: ${currentSort.name.replace("_", " ")}")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateView(
                        title = "No Expenses Found",
                        message = "Try changing search or filters to see recorded expenses."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { expense ->
                        val canModify = expense.createdBy == currentUserId || currentRoom?.createdBy == currentUserId
                        ExpenseCard(
                            expense = expense,
                            canModify = canModify,
                            onEdit = {
                                // Navigate to edit screen with expenseId
                            },
                            onDelete = { expenseToDelete = expense }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Native Confirmation Dialog for Deleting Expense
    expenseToDelete?.let { exp ->
        val authorName = (authState as? AuthUiState.Authenticated)?.profile?.name ?: "User"
        ConfirmationDialog(
            title = "Delete Expense?",
            message = "Are you sure you want to delete '${exp.title}' for ₹${exp.amount}? This will recalculate all room totals.",
            confirmButtonText = "Delete",
            isDestructive = true,
            onConfirm = {
                currentRoom?.let { room ->
                    expenseViewModel.deleteExpense(room.roomId, exp.expenseId, exp.title, authorName)
                }
                expenseToDelete = null
            },
            onDismiss = { expenseToDelete = null }
        )
    }
}
