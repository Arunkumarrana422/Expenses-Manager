package com.example.expensemanager.data.repository

import com.example.expensemanager.data.model.NotificationItem
import com.example.expensemanager.data.model.Room
import com.example.expensemanager.data.model.RoomMember
import com.example.expensemanager.utils.RoomCodeGenerator
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RoomRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun createRoom(roomName: String, userId: String, userName: String, userEmail: String): Result<Room> {
        return try {
            val roomRef = firestore.collection("rooms").document()
            val code = RoomCodeGenerator.generate()

            val room = Room(
                roomId = roomRef.id,
                name = roomName.trim(),
                code = code,
                createdBy = userId,
                memberIds = listOf(userId)
            )

            // Write room
            roomRef.set(room).await()

            // Add creator as first member & owner
            val member = RoomMember(
                memberId = userId,
                userId = userId,
                roomId = roomRef.id,
                name = userName.trim(),
                email = userEmail.trim(),
                isOwner = true
            )
            roomRef.collection("members").document(userId).set(member).await()

            // Update user's currentRoomId
            firestore.collection("users").document(userId).update("currentRoomId", roomRef.id).await()

            Result.success(room)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinRoom(roomCode: String, userId: String, userName: String, userEmail: String): Result<Room> {
        return try {
            val trimmedCode = roomCode.trim().uppercase()
            val querySnapshot = firestore.collection("rooms")
                .whereEqualTo("code", trimmedCode)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("Invalid Room Code. Please verify with room owner."))
            }

            val doc = querySnapshot.documents[0]
            val room = doc.toObject(Room::class.java) ?: throw Exception("Room data corrupt")

            // Prevent duplicate membership
            val membersSnapshot = doc.reference.collection("members").document(userId).get().await()
            if (!membersSnapshot.exists()) {
                val member = RoomMember(
                    memberId = userId,
                    userId = userId,
                    roomId = room.roomId,
                    name = userName.trim(),
                    email = userEmail.trim(),
                    isOwner = (room.createdBy == userId)
                )
                doc.reference.collection("members").document(userId).set(member).await()
                doc.reference.update("memberIds", FieldValue.arrayUnion(userId)).await()

                // Notify room members
                doc.reference.collection("notifications").add(
                    NotificationItem(
                        roomId = room.roomId,
                        title = "New Member Joined",
                        message = "$userName joined the room",
                        type = "MEMBER_JOINED"
                    )
                ).await()
            }

            // Update user's active room
            firestore.collection("users").document(userId).update("currentRoomId", room.roomId).await()

            Result.success(room)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserRooms(userId: String): Flow<List<Room>> = callbackFlow {
        val subscription = firestore.collection("rooms")
            .whereArrayContains("memberIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val rooms = snapshot?.documents?.mapNotNull { it.toObject(Room::class.java) } ?: emptyList()
                trySend(rooms)
            }
        awaitClose { subscription.remove() }
    }

    fun getRoomMembers(roomId: String): Flow<List<RoomMember>> = callbackFlow {
        if (roomId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val subscription = firestore.collection("rooms").document(roomId)
            .collection("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val members = snapshot?.documents?.mapNotNull { it.toObject(RoomMember::class.java) } ?: emptyList()
                trySend(members)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun removeMember(roomId: String, memberUserId: String): Result<Unit> {
        return try {
            val roomRef = firestore.collection("rooms").document(roomId)
            roomRef.collection("members").document(memberUserId).delete().await()
            roomRef.update("memberIds", FieldValue.arrayRemove(memberUserId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
