package com.example.expensemanager.domain

import com.example.expensemanager.data.model.CalculationResult
import com.example.expensemanager.data.model.DebtTransaction
import com.example.expensemanager.data.model.Expense
import com.example.expensemanager.data.model.MemberShare
import com.example.expensemanager.data.model.RoomMember
import java.math.BigDecimal
import java.math.RoundingMode

object ExpenseCalculator {

    /**
     * Accurately calculates:
     * Total Expense = Sum of all expenses
     * Equal Share = Total Expense / Number of Members
     * Member Balance = Amount Paid - Equal Share
     * Positive Balance: Paid more than equal share (should receive)
     * Negative Balance: Paid less than equal share (needs to pay)
     * Zero: Exactly balanced
     */
    fun calculate(expenses: List<Expense>, members: List<RoomMember>): CalculationResult {
        if (members.isEmpty()) {
            return CalculationResult()
        }

        // Sum up total expenses with BigDecimal to prevent floating point currency drift
        val totalBigDecimal = expenses.fold(BigDecimal.ZERO) { acc, exp ->
            acc.add(BigDecimal.valueOf(exp.amount))
        }.setScale(2, RoundingMode.HALF_EVEN)

        val memberCount = members.size
        val memberCountBd = BigDecimal.valueOf(memberCount.toLong())

        // Equal Share = Total / Count
        val equalShareBd = if (memberCount > 0) {
            totalBigDecimal.divide(memberCountBd, 2, RoundingMode.HALF_EVEN)
        } else {
            BigDecimal.ZERO
        }

        // Sum payments per member
        val paidMap = mutableMapOf<String, BigDecimal>()
        val expenseCountMap = mutableMapOf<String, Int>()
        members.forEach { m ->
            paidMap[m.userId] = BigDecimal.ZERO
            expenseCountMap[m.userId] = 0
        }

        for (expense in expenses) {
            val current = paidMap[expense.paidBy] ?: BigDecimal.ZERO
            paidMap[expense.paidBy] = current.add(BigDecimal.valueOf(expense.amount)).setScale(2, RoundingMode.HALF_EVEN)
            val currentCount = expenseCountMap[expense.paidBy] ?: 0
            expenseCountMap[expense.paidBy] = currentCount + 1
        }

        // Compute balances: Balance = Paid - Equal Share
        val memberShares = members.map { member ->
            val paid = paidMap[member.userId] ?: BigDecimal.ZERO
            val balance = paid.subtract(equalShareBd).setScale(2, RoundingMode.HALF_EVEN)

            MemberShare(
                memberId = member.userId,
                memberName = member.name.ifBlank { "Member" },
                totalPaid = paid.toDouble(),
                equalShare = equalShareBd.toDouble(),
                balance = balance.toDouble(),
                expenseCount = expenseCountMap[member.userId] ?: 0
            )
        }

        // Greedy Debt Minimization Algorithm to compute who pays whom
        val suggestedSettlements = computeMinimalTransactions(memberShares)

        return CalculationResult(
            totalExpense = totalBigDecimal.toDouble(),
            memberCount = memberCount,
            equalShare = equalShareBd.toDouble(),
            memberShares = memberShares,
            suggestedSettlements = suggestedSettlements
        )
    }

    private fun computeMinimalTransactions(shares: List<MemberShare>): List<DebtTransaction> {
        val transactions = mutableListOf<DebtTransaction>()

        // Debtors have negative balance (need to pay)
        // Creditors have positive balance (should receive)
        data class BalanceItem(val memberId: String, val name: String, var amount: BigDecimal)

        val debtors = mutableListOf<BalanceItem>()
        val creditors = mutableListOf<BalanceItem>()

        for (s in shares) {
            val b = BigDecimal.valueOf(s.balance).setScale(2, RoundingMode.HALF_EVEN)
            if (b.compareTo(BigDecimal.valueOf(-0.01)) < 0) {
                debtors.add(BalanceItem(s.memberId, s.memberName, b.abs()))
            } else if (b.compareTo(BigDecimal.valueOf(0.01)) > 0) {
                creditors.add(BalanceItem(s.memberId, s.memberName, b))
            }
        }

        // Sort descending
        debtors.sortByDescending { it.amount }
        creditors.sortByDescending { it.amount }

        var dIndex = 0
        var cIndex = 0

        while (dIndex < debtors.size && cIndex < creditors.size) {
            val debtor = debtors[dIndex]
            val creditor = creditors[cIndex]

            val settleAmount = debtor.amount.min(creditor.amount).setScale(2, RoundingMode.HALF_EVEN)

            if (settleAmount.compareTo(BigDecimal.ZERO) > 0) {
                transactions.add(
                    DebtTransaction(
                        fromMemberId = debtor.memberId,
                        fromMemberName = debtor.name,
                        toMemberId = creditor.memberId,
                        toMemberName = creditor.name,
                        amount = settleAmount.toDouble()
                    )
                )
            }

            debtor.amount = debtor.amount.subtract(settleAmount)
            creditor.amount = creditor.amount.subtract(settleAmount)

            if (debtor.amount.compareTo(BigDecimal.valueOf(0.01)) <= 0) {
                dIndex++
            }
            if (creditor.amount.compareTo(BigDecimal.valueOf(0.01)) <= 0) {
                cIndex++
            }
        }

        return transactions
    }
}
