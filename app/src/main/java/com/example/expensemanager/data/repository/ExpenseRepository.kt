package com.example.expensemanager.data.repository

import com.example.expensemanager.data.model.Expense
import com.example.expensemanager.data.model.NotificationItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ExpenseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getExpensesStream(roomId: String): Flow<List<Expense>> = callbackFlow {
        if (roomId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val subscription = firestore.collection("rooms").document(roomId)
            .collection("expenses")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.documents?.mapNotNull { it.toObject(Expense::class.java) } ?: emptyList()
                trySend(expenses)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addExpense(roomId: String, expense: Expense, authorName: String): Result<Expense> {
        return try {
            val docRef = firestore.collection("rooms").document(roomId).collection("expenses").document()
            val finalExpense = expense.copy(expenseId = docRef.id, roomId = roomId)
            docRef.set(finalExpense).await()

            // Post in-app notification
            firestore.collection("rooms").document(roomId).collection("notifications").add(
                NotificationItem(
                    roomId = roomId,
                    title = "New Expense Added",
                    message = "$authorName added ${finalExpense.title} (₹${finalExpense.amount})",
                    type = "EXPENSE_ADDED"
                )
            ).await()

            Result.success(finalExpense)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateExpense(roomId: String, expense: Expense, authorName: String): Result<Unit> {
        return try {
            firestore.collection("rooms").document(roomId)
                .collection("expenses")
                .document(expense.expenseId)
                .set(expense)
                .await()

            firestore.collection("rooms").document(roomId).collection("notifications").add(
                NotificationItem(
                    roomId = roomId,
                    title = "Expense Edited",
                    message = "$authorName modified ${expense.title}",
                    type = "EXPENSE_UPDATED"
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(roomId: String, expenseId: String, expenseTitle: String, authorName: String): Result<Unit> {
        return try {
            firestore.collection("rooms").document(roomId)
                .collection("expenses")
                .document(expenseId)
                .delete()
                .await()

            firestore.collection("rooms").document(roomId).collection("notifications").add(
                NotificationItem(
                    roomId = roomId,
                    title = "Expense Deleted",
                    message = "$authorName removed $expenseTitle",
                    type = "EXPENSE_DELETED"
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
