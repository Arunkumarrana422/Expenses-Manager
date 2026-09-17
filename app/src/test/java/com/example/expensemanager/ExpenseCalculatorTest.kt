package com.example.expensemanager

import com.example.expensemanager.data.model.Expense
import com.example.expensemanager.data.model.RoomMember
import com.example.expensemanager.domain.ExpenseCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseCalculatorTest {

    @Test
    fun testExpenseCalculationAndSettlement() {
        // Arun and Rahul in a room
        val rahul = RoomMember(userId = "user_rahul", name = "Rahul")
        val arun = RoomMember(userId = "user_arun", name = "Arun")
        val members = listOf(rahul, arun)

        // Arun paid 1000 for groceries
        val expenses = listOf(
            Expense(
                expenseId = "exp_1",
                title = "Grocery",
                amount = 1000.0,
                paidBy = "user_arun",
                paidByName = "Arun"
            )
        )

        val result = ExpenseCalculator.calculate(expenses, members)

        // Total = 1000, 2 members -> equal share 500
        assertEquals(1000.0, result.totalExpense, 0.001)
        assertEquals(500.0, result.equalShare, 0.001)

        val rahulShare = result.memberShares.first { it.memberId == "user_rahul" }
        val arunShare = result.memberShares.first { it.memberId == "user_arun" }

        // Rahul paid 0 - 500 = -500 (needs to pay 500)
        assertEquals(-500.0, rahulShare.balance, 0.001)
        // Arun paid 1000 - 500 = +500 (should receive 500)
        assertEquals(500.0, arunShare.balance, 0.001)

        // Suggested Settlement: Rahul -> Arun 500
        assertEquals(1, result.suggestedSettlements.size)
        val transaction = result.suggestedSettlements[0]
        assertEquals("user_rahul", transaction.fromMemberId)
        assertEquals("user_arun", transaction.toMemberId)
        assertEquals(500.0, transaction.amount, 0.001)
    }

    @Test
    fun testBalancedExpensesZeroSettlements() {
        val rahul = RoomMember(userId = "user_rahul", name = "Rahul")
        val arun = RoomMember(userId = "user_arun", name = "Arun")
        val members = listOf(rahul, arun)

        // Both paid 500
        val expenses = listOf(
            Expense(expenseId = "1", amount = 500.0, paidBy = "user_rahul"),
            Expense(expenseId = "2", amount = 500.0, paidBy = "user_arun")
        )

        val result = ExpenseCalculator.calculate(expenses, members)
        assertEquals(1000.0, result.totalExpense, 0.001)
        assertEquals(500.0, result.equalShare, 0.001)
        assertTrue(result.suggestedSettlements.isEmpty())
    }
}
