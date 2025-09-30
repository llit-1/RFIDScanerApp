package com.example.rfidscanner.model

data class InventorizationItem(
    val id: Int,
    val locationGuid: String,
    val location: String,
    val datetime: String,
    val person: String,
    val status: Int
)