package data
import com.google.gson.annotations.SerializedName

data class WeatherInformation(
    @SerializedName("temp") val temperature:String,
    @SerializedName("temp_min")  val temperature_min: String,
    @SerializedName("temp_max") val temperature_max: String,
    @SerializedName("feels_like")  val feels_like:String,
    @SerializedName("pressure") val pressure:String,
    @SerializedName("humidity")  val humidity:String,
    @SerializedName("rain") val rain: String
)