package com.example.expensemanager.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Expense(
    @DocumentId
    val expenseId: String = "",
    val roomId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = ExpenseCategory.OTHER.name,
    val paidBy: String = "", // userId
    val paidByName: String = "",
    val createdBy: String = "",
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)
