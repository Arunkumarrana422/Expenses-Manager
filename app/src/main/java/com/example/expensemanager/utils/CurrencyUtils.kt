package com.example.expensemanager.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    /**
     * Formats amount into Indian Rupee style, e.g. ₹1,50,000.00
     */
    fun formatRupee(amount: Double): String {
        return try {
            val indiaLocale = Locale("en", "IN")
            val format = NumberFormat.getCurrencyInstance(indiaLocale)
            val formatted = format.format(amount)
            // Ensure symbol is consistently ₹
            if (!formatted.startsWith("₹")) {
                "₹" + String.format(Locale.US, "%,.2f", amount)
            } else {
                formatted
            }
        } catch (e: Exception) {
            "₹" + String.format(Locale.US, "%,.2f", amount)
        }
    }

    fun formatRupeeNoDecimals(amount: Double): String {
        return "₹" + String.format(Locale.US, "%,.0f", amount)
    }
}
