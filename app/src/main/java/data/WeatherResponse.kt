package data

data class WeatherResponse(
    val name:String,
    val main: WeatherInformation,
    val wind: WindInfo
)