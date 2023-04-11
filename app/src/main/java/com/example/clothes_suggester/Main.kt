package com.example.clothes_suggester
import com.google.gson.annotations.SerializedName

data class Main(
    @SerializedName("temp") val temperature:String,
)