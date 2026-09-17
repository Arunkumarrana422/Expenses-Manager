package com.example.expensemanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.model.CalculationResult
import com.example.expensemanager.data.model.Expense
import com.example.expensemanager.data.model.ExpenseCategory
import com.example.expensemanager.data.model.RoomMember
import com.example.expensemanager.data.repository.ExpenseRepository
import com.example.expensemanager.domain.ExpenseCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ExpenseSortOrder {
    NEWEST,
    OLDEST,
    HIGHEST_AMOUNT,
    LOWEST_AMOUNT
}

class ExpenseViewModel(
    private val repository: ExpenseRepository = ExpenseRepository()
) : ViewModel() {

    private val _rawExpenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _rawExpenses.asStateFlow()

    private val _calculationResult = MutableStateFlow(CalculationResult())
    val calculationResult: StateFlow<CalculationResult> = _calculationResult.asStateFlow()

    // Filter states
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<ExpenseCategory?>(null)
    val selectedMemberId = MutableStateFlow<String?>(null)
    val sortOrder = MutableStateFlow(ExpenseSortOrder.NEWEST)

    fun observeRoomExpenses(roomId: String, members: List<RoomMember>) {
        viewModelScope.launch {
            repository.getExpensesStream(roomId).collect { list ->
                _rawExpenses.value = list
                _calculationResult.value = ExpenseCalculator.calculate(list, members)
            }
        }
    }

    fun addExpense(roomId: String, title: String, amount: Double, category: String, paidBy: String, paidByName: String, createdBy: String, description: String, onSuccess: () -> Unit) {
        val expense = Expense(
            roomId = roomId,
            title = title.trim(),
            amount = amount,
            category = category,
            paidBy = paidBy,
            paidByName = paidByName,
            createdBy = createdBy,
            description = description.trim(),
            date = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.addExpense(roomId, expense, paidByName).onSuccess { onSuccess() }
        }
    }

    fun updateExpense(roomId: String, expense: Expense, authorName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateExpense(roomId, expense, authorName).onSuccess { onSuccess() }
        }
    }

    fun deleteExpense(roomId: String, expenseId: String, title: String, authorName: String) {
        viewModelScope.launch {
            repository.deleteExpense(roomId, expenseId, title, authorName)
        }
    }

    fun getFilteredExpenses(): List<Expense> {
        var list = _rawExpenses.value

        val query = searchQuery.value.trim().lowercase()
        if (query.isNotBlank()) {
            list = list.filter { it.title.lowercase().contains(query) || it.description.lowercase().contains(query) }
        }

        val cat = selectedCategory.value
        if (cat != null) {
            list = list.filter { it.category.equals(cat.name, ignoreCase = true) }
        }

        val mem = selectedMemberId.value
        if (mem != null) {
            list = list.filter { it.paidBy == mem }
        }

        return when (sortOrder.value) {
            ExpenseSortOrder.NEWEST -> list.sortedByDescending { it.date }
            ExpenseSortOrder.OLDEST -> list.sortedBy { it.date }
            ExpenseSortOrder.HIGHEST_AMOUNT -> list.sortedByDescending { it.amount }
            ExpenseSortOrder.LOWEST_AMOUNT -> list.sortedBy { it.amount }
        }
    }
}
