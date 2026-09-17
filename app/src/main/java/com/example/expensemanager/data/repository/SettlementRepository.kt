package com.example.expensemanager.data.repository

import com.example.expensemanager.data.model.NotificationItem
import com.example.expensemanager.data.model.Settlement
import com.example.expensemanager.data.model.SettlementStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class SettlementRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getSettlementsStream(roomId: String): Flow<List<Settlement>> = callbackFlow {
        if (roomId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val subscription = firestore.collection("rooms").document(roomId)
            .collection("settlements")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val settlements = snapshot?.documents?.mapNotNull { it.toObject(Settlement::class.java) } ?: emptyList()
                trySend(settlements)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun markSettlementPaid(roomId: String, settlementId: String, memberName: String): Result<Unit> {
        return try {
            val docRef = firestore.collection("rooms").document(roomId).collection("settlements").document(settlementId)
            docRef.update(
                mapOf(
                    "status" to SettlementStatus.PAID.name,
                    "markedPaidBy" to memberName,
                    "settledAt" to Date()
                )
            ).await()

            firestore.collection("rooms").document(roomId).collection("notifications").add(
                NotificationItem(
                    roomId = roomId,
                    title = "Settlement Marked Paid",
                    message = "$memberName recorded a payment settlement",
                    type = "SETTLEMENT_PAID"
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveSettlement(roomId: String, settlement: Settlement): Result<Unit> {
        return try {
            val docRef = if (settlement.settlementId.isBlank()) {
                firestore.collection("rooms").document(roomId).collection("settlements").document()
            } else {
                firestore.collection("rooms").document(roomId).collection("settlements").document(settlement.settlementId)
            }
            docRef.set(settlement.copy(settlementId = docRef.id, roomId = roomId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
