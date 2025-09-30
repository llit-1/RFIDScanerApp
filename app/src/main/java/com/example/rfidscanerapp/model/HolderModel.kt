package com.example.rfidscanner.model

data class HolderModel(
    val id: Int,
    val surname: String,
    val name: String,
    val patronymic: String?,
    val jobTitle: String?,
    val department: String?,
    val actual: Int
)