package com.example.expensemanager

import com.example.expensemanager.utils.CurrencyUtils
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyUtilsTest {

    @Test
    fun testRupeeSymbolPresent() {
        val formatted = CurrencyUtils.formatRupee(500.0)
        assertTrue(formatted.contains("₹"))
        assertTrue(formatted.contains("500"))
    }
}
