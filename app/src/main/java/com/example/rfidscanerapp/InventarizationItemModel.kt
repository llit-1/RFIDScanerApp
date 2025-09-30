package com.example.rfidscanner.model

data class InventarizationItemModel(
    val id: Int,
    val obj: String,
    val objectName: String,
    val objectCategory: String,
    val holder: String?,
    var detected: Boolean
)