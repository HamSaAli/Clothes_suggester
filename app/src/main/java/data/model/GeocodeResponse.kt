package data.model

data class GeocodeResponse(
    val results: List<GeocodeResult>,
    val status: String
)

data class GeocodeResult(
    val formatted_address: String,
    val geometry: GeocodeGeometry,
    val types: List<String>
)

data class GeocodeGeometry(
    val location: GeocodeLocation,
    val location_type: String,
    val viewport: GeocodeViewport
)

data class GeocodeLocation(
    val lat: Double,
    val lng: Double
)

data class GeocodeViewport(
    val northeast: GeocodeLocation,
    val southwest: GeocodeLocation
)