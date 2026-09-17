package com.example.expensemanager.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expensemanager.data.model.ExpenseCategory
import com.example.expensemanager.utils.DateUtils
import com.example.expensemanager.viewmodel.AuthUiState
import com.example.expensemanager.viewmodel.AuthViewModel
import com.example.expensemanager.viewmodel.ExpenseViewModel
import com.example.expensemanager.viewmodel.RoomViewModel
import java.util.Calendar

@Composable
fun AddEditExpenseScreen(
    navController: NavController,
    expenseViewModel: ExpenseViewModel,
    roomViewModel: RoomViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val currentRoom by roomViewModel.currentRoom.collectAsState()
    val members by roomViewModel.members.collectAsState()
    val authState by authViewModel.uiState.collectAsState()

    val currentUid = (authState as? AuthUiState.Authenticated)?.user?.uid ?: ""
    val currentProfileName = (authState as? AuthUiState.Authenticated)?.profile?.name ?: "Member"

    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var selectedPaidById by remember { mutableStateOf(currentUid) }
    var selectedPaidByName by remember { mutableStateOf(currentProfileName) }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var categoryDropdownOpen by remember { mutableStateOf(false) }
    var memberDropdownOpen by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add Room Expense",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Expense Title *") },
            placeholder = { Text("e.g. WiFi Bill, Dinner, Milk") },
            leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Amount (Numeric validated)
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount (₹) *") },
            placeholder = { Text("0.00") },
            leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp)
        )

        // Category Picker
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedCategory.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category *") },
                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { categoryDropdownOpen = true },
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { categoryDropdownOpen = true }
            )
            DropdownMenu(
                expanded = categoryDropdownOpen,
                onDismissRequest = { categoryDropdownOpen = false }
            ) {
                ExpenseCategory.values().forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.displayName) },
                        onClick = {
                            selectedCategory = cat
                            categoryDropdownOpen = false
                        }
                    )
                }
            }
        }

        // Paid By Member Picker
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedPaidByName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Paid By *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { memberDropdownOpen = true }
            )
            DropdownMenu(
                expanded = memberDropdownOpen,
                onDismissRequest = { memberDropdownOpen = false }
            ) {
                members.forEach { m ->
                    DropdownMenuItem(
                        text = { Text(m.name) },
                        onClick = {
                            selectedPaidById = m.userId
                            selectedPaidByName = m.name
                            memberDropdownOpen = false
                        }
                    )
                }
            }
        }

        // Date Picker Field
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val c = Calendar.getInstance()
                c.set(year, month, dayOfMonth)
                selectedDate = c.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = DateUtils.formatDate(selectedDate),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { datePickerDialog.show() }
            )
        }

        // Description / Notes
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description / Notes (Optional)") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            shape = RoundedCornerShape(12.dp)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val amount = amountText.toDoubleOrNull()
                if (title.isBlank()) {
                    errorMessage = "Please enter an expense title"
                    return@Button
                }
                if (amount == null || amount <= 0) {
                    errorMessage = "Please enter a valid numeric amount greater than ₹0"
                    return@Button
                }

                currentRoom?.let { room ->
                    expenseViewModel.addExpense(
                        roomId = room.roomId,
                        title = title,
                        amount = amount,
                        category = selectedCategory.name,
                        paidBy = selectedPaidById,
                        paidByName = selectedPaidByName,
                        createdBy = currentUid,
                        description = description
                    ) {
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save Expense", fontWeight = FontWeight.Bold)
        }
    }
}
