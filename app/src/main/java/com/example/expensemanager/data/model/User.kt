package com.example.expensemanager.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    @DocumentId
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val currentRoomId: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
)
