package com.example.bibliobit.data.model

enum class BookStatus(val dbValue: String) {
    PLAN_TO_READ("PLAN_TO_READ"),
    READING("READING"),
    FINISH("FINISH");

    override fun toString(): String {
        return when (this) {
            PLAN_TO_READ -> "Plan to Read" // Untuk tampilan di BookDetailScreen
            READING -> "Reading"
            FINISH -> "Finish"
        }
    }
}