package data.model
import com.google.gson.annotations.SerializedName

data class WeatherInformation(
    @SerializedName("temp") val temperature:String,
    @SerializedName("temp_min")  val temperatureMin: String,
    @SerializedName("temp_max") val temperatureMax: String,
    @SerializedName("feels_like")  val feelsLike:String,
    @SerializedName("pressure") val pressure:String,
    @SerializedName("humidity")  val humidity:String,
    @SerializedName("rain") val rain: String,
    @SerializedName("current") val currentTemperature: CurrentTemperature
)