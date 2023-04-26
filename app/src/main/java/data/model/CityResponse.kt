package data.model

data class CityResponse(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val population: Int,
    val timezone: String
)
