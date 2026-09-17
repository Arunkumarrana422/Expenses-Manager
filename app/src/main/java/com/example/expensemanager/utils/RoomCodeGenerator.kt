package com.example.expensemanager.utils

import java.security.SecureRandom

object RoomCodeGenerator {
    private const val CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Excludes confusing 0/O, 1/I
    private const val CODE_LENGTH = 6
    private val random = SecureRandom()

    fun generate(): String {
        val sb = StringBuilder(CODE_LENGTH)
        for (i in 0 until CODE_LENGTH) {
            sb.append(CHARACTERS[random.nextInt(CHARACTERS.length)])
        }
        return sb.toString()
    }
}
