package com.example.expensemanager.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class NotificationItem(
    @DocumentId
    val id: String = "",
    val roomId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "INFO", // EXPENSE_ADDED, EXPENSE_UPDATED, EXPENSE_DELETED, MEMBER_JOINED, SETTLEMENT_PAID
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    @ServerTimestamp
    val createdAt: Date? = null
)
