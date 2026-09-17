package com.example.expensemanager.data.model

data class MemberShare(
    val memberId: String,
    val memberName: String,
    val totalPaid: Double,
    val equalShare: Double,
    val balance: Double, // Amount Paid - Equal Share
    val expenseCount: Int = 0
)

data class DebtTransaction(
    val fromMemberId: String,
    val fromMemberName: String,
    val toMemberId: String,
    val toMemberName: String,
    val amount: Double
)

data class CalculationResult(
    val totalExpense: Double = 0.0,
    val memberCount: Int = 0,
    val equalShare: Double = 0.0,
    val memberShares: List<MemberShare> = emptyList(),
    val suggestedSettlements: List<DebtTransaction> = emptyList()
)
