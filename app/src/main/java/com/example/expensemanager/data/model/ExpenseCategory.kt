package com.example.expensemanager.data.model

enum class ExpenseCategory(val displayName: String, val iconName: String, val colorHex: Long) {
    FOOD("Food", "Restaurant", 0xFFF97316),
    GROCERY("Grocery", "ShoppingCart", 0xFF10B981),
    RENT("Rent", "Home", 0xFF6366F1),
    ELECTRICITY("Electricity", "Bolt", 0xFFF59E0B),
    WATER("Water", "WaterDrop", 0xFF06B6D4),
    INTERNET("Internet", "Wifi", 0xFF3B82F6),
    TRANSPORT("Transport", "DirectionsCar", 0xFF8B5CF6),
    SHOPPING("Shopping", "ShoppingBag", 0xFFEC4899),
    MEDICAL("Medical", "LocalHospital", 0xFFEF4444),
    ENTERTAINMENT("Entertainment", "Movie", 0xFF14B8A6),
    OTHER("Other", "Category", 0xFF64748B);

    companion object {
        fun fromName(name: String): ExpenseCategory {
            return values().firstOrNull { 
                it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) 
            } ?: OTHER
        }
    }
}
