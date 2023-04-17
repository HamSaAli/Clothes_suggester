package data

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("name") val name:String,
    @SerializedName("main") val main: WeatherInformation,
)