package com.example.rfidscanner.model

data class CategoryModel(
    val id: Int,
    val name: String,
    val parent: Int?,
    val img: String?,
    val actual: Int
)