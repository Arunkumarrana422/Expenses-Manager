package com.example.expensemanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.model.Room
import com.example.expensemanager.data.model.RoomMember
import com.example.expensemanager.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RoomViewModel(
    private val repository: RoomRepository = RoomRepository()
) : ViewModel() {

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _currentRoom = MutableStateFlow<Room?>(null)
    val currentRoom: StateFlow<Room?> = _currentRoom.asStateFlow()

    private val _members = MutableStateFlow<List<RoomMember>>(emptyList())
    val members: StateFlow<List<RoomMember>> = _members.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadUserRooms(userId: String) {
        viewModelScope.launch {
            repository.getUserRooms(userId).collect { roomList ->
                _rooms.value = roomList
                if (_currentRoom.value == null && roomList.isNotEmpty()) {
                    selectRoom(roomList.first())
                }
            }
        }
    }

    fun selectRoom(room: Room) {
        _currentRoom.value = room
        loadMembers(room.roomId)
    }

    private fun loadMembers(roomId: String) {
        viewModelScope.launch {
            repository.getRoomMembers(roomId).collect { memberList ->
                _members.value = memberList
            }
        }
    }

    fun createRoom(name: String, userId: String, userName: String, userEmail: String, onSuccess: (Room) -> Unit) {
        if (name.isBlank()) {
            _errorMessage.value = "Room name cannot be empty"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            repository.createRoom(name, userId, userName, userEmail).fold(
                onSuccess = { room ->
                    _isLoading.value = false
                    _currentRoom.value = room
                    loadMembers(room.roomId)
                    onSuccess(room)
                },
                onFailure = { e ->
                    _isLoading.value = false
                    _errorMessage.value = e.localizedMessage ?: "Failed to create room"
                }
            )
        }
    }

    fun joinRoom(code: String, userId: String, userName: String, userEmail: String, onSuccess: (Room) -> Unit) {
        if (code.length != 6) {
            _errorMessage.value = "Room code must be 6 characters"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            repository.joinRoom(code, userId, userName, userEmail).fold(
                onSuccess = { room ->
                    _isLoading.value = false
                    _currentRoom.value = room
                    loadMembers(room.roomId)
                    onSuccess(room)
                },
                onFailure = { e ->
                    _isLoading.value = false
                    _errorMessage.value = e.localizedMessage ?: "Invalid Room Code"
                }
            )
        }
    }

    fun removeMember(memberId: String) {
        val room = _currentRoom.value ?: return
        viewModelScope.launch {
            repository.removeMember(room.roomId, memberId).fold(
                onSuccess = { /* member list will update via snapshot listener */ },
                onFailure = { e -> _errorMessage.value = e.localizedMessage }
            )
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
