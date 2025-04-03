package com.example.bibliobit.data.model

enum class BookStatus {
    PLAN_TO_READ,
    READING,
    FINISH;

    override fun toString(): String {
        return when (this) {
            PLAN_TO_READ -> "plan to read"
            READING -> "reading"
            FINISH -> "finish"
        }
    }
}