package com.example.rfidscanner.model

import com.google.gson.annotations.SerializedName

data class SurplusItemModel(
    @SerializedName("obj")
    val obj: String,

    @SerializedName("objectName")
    val objectName: String,

    @SerializedName("objectCategory")
    val objectCategory: String,

    @SerializedName("holder")
    val holder: String?,

    @SerializedName("location")
    val location: String?,

    @SerializedName("onPlace")
    val onPlace: Boolean
)