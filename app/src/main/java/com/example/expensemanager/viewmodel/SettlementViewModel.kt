package com.example.expensemanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.model.DebtTransaction
import com.example.expensemanager.data.model.Settlement
import com.example.expensemanager.data.model.SettlementStatus
import com.example.expensemanager.data.repository.SettlementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettlementViewModel(
    private val repository: SettlementRepository = SettlementRepository()
) : ViewModel() {

    private val _settlements = MutableStateFlow<List<Settlement>>(emptyList())
    val settlements: StateFlow<List<Settlement>> = _settlements.asStateFlow()

    fun observeSettlements(roomId: String) {
        viewModelScope.launch {
            repository.getSettlementsStream(roomId).collect { list ->
                _settlements.value = list
            }
        }
    }

    fun markSettlementPaid(roomId: String, transaction: DebtTransaction, memberName: String) {
        viewModelScope.launch {
            val settlement = Settlement(
                roomId = roomId,
                payerId = transaction.fromMemberId,
                payerName = transaction.fromMemberName,
                receiverId = transaction.toMemberId,
                receiverName = transaction.toMemberName,
                amount = transaction.amount,
                status = SettlementStatus.PAID.name,
                markedPaidBy = memberName
            )
            repository.saveSettlement(roomId, settlement)
        }
    }
}
