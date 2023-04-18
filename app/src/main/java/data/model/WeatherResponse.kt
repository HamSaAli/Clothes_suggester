package data.model

import com.google.gson.annotations.SerializedName
import data.model.WeatherInformation

data class WeatherResponse(
    @SerializedName("name") val name:String,
    @SerializedName("main") val main: WeatherInformation,
)