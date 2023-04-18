package data

class WeatherConverter {
    fun convertFahrenheitToCelsius(fahrenheit: Float) = (fahrenheit - 273.15).toInt()
}