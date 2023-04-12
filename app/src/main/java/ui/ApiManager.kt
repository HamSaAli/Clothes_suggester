package ui

import com.example.clothes_suggester.utils.Constant
import com.google.gson.Gson
import data.WeatherResponse
import okhttp3.*
import java.io.IOException
import kotlin.reflect.KFunction5

class ApiManager(private val client: OkHttpClient, private val converter: WeatherConverter) {
    fun getWeather(

        latitude: Float,
        longitude: Float,
        onResult: KFunction5<String?, Int, String, String, String, Unit>,
        onFailure: () -> Unit
    ) {
        val request = Request.Builder()
            .url("${Constant.BASE_URL}/weather?lat=$latitude&lon=$longitude&appid=${Constant.API_KEY}")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    val result = Gson().fromJson(jsonString, WeatherResponse::class.java)
                    val city = result.name
                    val pressure = result.main.pressure
                    val humidity = result.main.humidity
                    val feel = result.main.feels_like
                    val temperature =
                        converter.convertFahrenheitToCelsius(
                            result.main.temperature.toFloatOrNull() ?: 0f
                        )
                    onResult(city, temperature, pressure, humidity, feel)
                }
            }
        })
    }

}