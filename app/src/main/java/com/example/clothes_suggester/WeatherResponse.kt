package com.example.clothes_suggester

data class WeatherResponse(
    val name:String,
    val main:Main,
    val wind:WindInfo
)