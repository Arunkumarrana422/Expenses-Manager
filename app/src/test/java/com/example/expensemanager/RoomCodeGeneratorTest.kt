package com.example.expensemanager

import com.example.expensemanager.utils.RoomCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomCodeGeneratorTest {

    @Test
    fun testRoomCodeLengthAndFormat() {
        val code = RoomCodeGenerator.generate()
        assertEquals(6, code.length)
        // Check only contains uppercase alphanumeric characters
        assertTrue(code.all { it in "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" })
    }

    @Test
    fun testRoomCodeRandomness() {
        val codes = (1..100).map { RoomCodeGenerator.generate() }.toSet()
        // 100 randomly generated codes should be unique
        assertEquals(100, codes.size)
    }
}
