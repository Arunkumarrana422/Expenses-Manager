package com.example.expensemanager.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Room(
    @DocumentId
    val roomId: String = "",
    val name: String = "",
    val code: String = "",
    val createdBy: String = "",
    val memberIds: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)
