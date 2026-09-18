package com.example.randomquote.model

data class Quote(
    val id: Int,
    val text: String,
    val category: String = "Principle",
    val isFavorite: Boolean = false
)
