package com.example.expensemanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expensemanager.ui.components.EmptyStateView
import com.example.expensemanager.ui.components.ExpenseCard
import com.example.expensemanager.ui.navigation.Screen
import com.example.expensemanager.ui.theme.NegativeRed
import com.example.expensemanager.ui.theme.PositiveGreen
import com.example.expensemanager.utils.CurrencyUtils
import com.example.expensemanager.viewmodel.AuthUiState
import com.example.expensemanager.viewmodel.AuthViewModel
import com.example.expensemanager.viewmodel.ExpenseViewModel
import com.example.expensemanager.viewmodel.RoomViewModel
import kotlin.math.abs

@Composable
fun DashboardScreen(
    navController: NavController,
    roomViewModel: RoomViewModel,
    expenseViewModel: ExpenseViewModel,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val rooms by roomViewModel.rooms.collectAsState()
    val currentRoom by roomViewModel.currentRoom.collectAsState()
    val members by roomViewModel.members.collectAsState()
    val calculation by expenseViewModel.calculationResult.collectAsState()
    val allExpenses by expenseViewModel.expenses.collectAsState()

    val currentUserId = (authState as? AuthUiState.Authenticated)?.user?.uid ?: ""
    var roomMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            roomViewModel.loadUserRooms(currentUserId)
        }
    }

    LaunchedEffect(currentRoom, members) {
        currentRoom?.let { r ->
            expenseViewModel.observeRoomExpenses(r.roomId, members)
        }
    }

    if (rooms.isEmpty()) {
        Scaffold { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    title = "No Rooms Found",
                    message = "Create a new shared room or join using a 6-character room code to start managing expenses.",
                    icon = Icons.Default.MeetingRoom,
                    actionButtonText = "+ Create or Join Room",
                    onActionClick = { navController.navigate(Screen.CreateRoom.route) }
                )
            }
        }
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddExpense.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Expense", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Room Selector Bar
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { roomMenuExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentRoom?.name ?: "Select Room",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = roomMenuExpanded,
                            onDismissRequest = { roomMenuExpanded = false }
                        ) {
                            rooms.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r.name) },
                                    onClick = {
                                        roomViewModel.selectRoom(r)
                                        roomMenuExpanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("+ Create New Room", color = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    roomMenuExpanded = false
                                    navController.navigate(Screen.CreateRoom.route)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("+ Join with Code", color = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    roomMenuExpanded = false
                                    navController.navigate(Screen.JoinRoom.route)
                                }
                            )
                        }
                    }

                    // Room Code Chip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Code: ${currentRoom?.code ?: ""}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Summary Dashboard Card
            item {
                val myShare = calculation.memberShares.firstOrNull { it.memberId == currentUserId }
                val myPaid = myShare?.totalPaid ?: 0.0
                val myEqualShare = calculation.equalShare
                val myBalance = myShare?.balance ?: 0.0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Room Expense",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = CurrencyUtils.formatRupee(calculation.totalExpense),
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${members.size} Members",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // 3 Column Metric Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("You Paid", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                                Text(CurrencyUtils.formatRupeeNoDecimals(myPaid), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Your Share", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                                Text(CurrencyUtils.formatRupeeNoDecimals(myEqualShare), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Balance", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                                val balanceTxt = when {
                                    myBalance > 0.01 -> "+${CurrencyUtils.formatRupeeNoDecimals(myBalance)}"
                                    myBalance < -0.01 -> "-${CurrencyUtils.formatRupeeNoDecimals(abs(myBalance))}"
                                    else -> "₹0"
                                }
                                Text(balanceTxt, color = if (myBalance >= 0) Color.White else Color(0xFFFFB4AB), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Quick Settlement Teaser
            item {
                if (calculation.suggestedSettlements.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(Screen.Settlements.route) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Settlement Pending", fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${calculation.suggestedSettlements.size} payments required to balance",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Text("View >", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Recent Expenses Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Expenses",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { navController.navigate(Screen.Expenses.route) }) {
                        Text("See All (${allExpenses.size})")
                    }
                }
            }

            if (allExpenses.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Expenses Yet",
                        message = "Start tracking by tapping the + Add Expense button below.",
                        actionButtonText = "+ Add First Expense",
                        onActionClick = { navController.navigate(Screen.AddExpense.route) }
                    )
                }
            } else {
                items(allExpenses.take(5)) { expense ->
                    ExpenseCard(
                        expense = expense,
                        canModify = false
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}
