package com.example.expensemanager.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class SettlementStatus {
    PENDING,
    PAID
}

data class Settlement(
    @DocumentId
    val settlementId: String = "",
    val roomId: String = "",
    val payerId: String = "",
    val payerName: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val amount: Double = 0.0,
    val status: String = SettlementStatus.PENDING.name,
    val markedPaidBy: String = "",
    @ServerTimestamp
    val settledAt: Date? = null,
    @ServerTimestamp
    val createdAt: Date? = null
)
