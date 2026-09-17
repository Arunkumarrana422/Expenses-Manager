package com.example.expensemanager.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class RoomMember(
    @DocumentId
    val memberId: String = "",
    val userId: String = "",
    val roomId: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val totalPaid: Double = 0.0,
    val balance: Double = 0.0,
    val isOwner: Boolean = false,
    @ServerTimestamp
    val joinedAt: Date? = null
)
