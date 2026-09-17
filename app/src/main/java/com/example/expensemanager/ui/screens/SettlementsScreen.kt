package com.example.expensemanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensemanager.data.model.DebtTransaction
import com.example.expensemanager.ui.components.EmptyStateView
import com.example.expensemanager.ui.components.SettlementCard
import com.example.expensemanager.viewmodel.AuthUiState
import com.example.expensemanager.viewmodel.AuthViewModel
import com.example.expensemanager.viewmodel.ExpenseViewModel
import com.example.expensemanager.viewmodel.RoomViewModel
import com.example.expensemanager.viewmodel.SettlementViewModel

@Composable
fun SettlementsScreen(
    settlementViewModel: SettlementViewModel,
    expenseViewModel: ExpenseViewModel,
    roomViewModel: RoomViewModel,
    authViewModel: AuthViewModel
) {
    val currentRoom by roomViewModel.currentRoom.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val calculation by expenseViewModel.calculationResult.collectAsState()
    val recordedSettlements by settlementViewModel.settlements.collectAsState()

    val currentUserName = (authState as? AuthUiState.Authenticated)?.profile?.name ?: "User"
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Pending Settlements", "Payment History")

    LaunchedEffect(currentRoom) {
        currentRoom?.let { room ->
            settlementViewModel.observeSettlements(room.roomId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settlement & Balances",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Automatic debt minimization: Who pays whom to settle all balances",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTabIndex == 0) {
            // Pending Settlements
            if (calculation.suggestedSettlements.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateView(
                        title = "All Settled Up!",
                        message = "Everyone in this room has paid their exact equal share. No pending settlements."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(calculation.suggestedSettlements) { transaction ->
                        SettlementCard(
                            transaction = transaction,
                            isPaid = false,
                            onMarkPaid = {
                                currentRoom?.let { room ->
                                    settlementViewModel.markSettlementPaid(room.roomId, transaction, currentUserName)
                                }
                            }
                        )
                    }
                }
            }
        } else {
            // History of paid settlements
            if (recordedSettlements.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateView(
                        title = "No Settlement Records",
                        message = "Mark settlements as paid when members transfer money."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recordedSettlements) { s ->
                        SettlementCard(
                            transaction = DebtTransaction(
                                fromMemberId = s.payerId,
                                fromMemberName = s.payerName,
                                toMemberId = s.receiverId,
                                toMemberName = s.receiverName,
                                amount = s.amount
                            ),
                            isPaid = true
                        )
                    }
                }
            }
        }
    }
}
