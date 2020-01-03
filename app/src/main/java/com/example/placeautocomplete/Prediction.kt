package com.example.placeautocomplete

class Prediction(
    val id: String,
    val textPrimary: String,
    val textSecondary: String
) {
    override fun toString() = "$textPrimary $textSecondary"
}