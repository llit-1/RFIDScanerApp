package com.example.rfidscaner.model

data class CreateInventorizationRequest(
    val location: String,
    val person: String,
    val warehouseCategoriesId: Int? = null
)