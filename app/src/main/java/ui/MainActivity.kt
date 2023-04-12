package ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import okhttp3.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.clothes_suggester.R
import com.example.clothes_suggester.databinding.ActivityMainBinding
import data.Clothing

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val client = OkHttpClient()
    private val converter = WeatherConverter()
    private val apiManager = ApiManager(client, converter)
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        apiManager.getWeather(10.33f, 12.44f, ::onWeatherResponse, ::onError)

    }

    @SuppressLint("SetTextI18n")
    private fun onWeatherResponse(
        city: String?,
        temperature: Int,
        pressure: String,
        humidity: String,
        fell_like: String
    ) {
        if (city != null) {
            runOnUiThread {
                binding.textTempature.text = "$temperature°C"
                binding.textCityName.text = city
                binding.textPressure.text = "$pressure hpa"
                binding.textHumidity.text = "$humidity%"
                binding.textWindSpeed.text = "$fell_like k"

                setClothingImage(temperature)
            }
        }
    }

    private fun onError() {
        binding.clothesImage.setImageResource(R.drawable.error2)
    }

    private fun setClothingImage(temperature: Int) {
        val clothingList = when {
            temperature <= 0 -> listOf(
                Clothing(R.drawable.hoodie_white),
                Clothing(R.drawable.jacket_brown),
                Clothing(R.drawable.sweater_skyblue),
                Clothing(R.drawable.sweater_green),
                Clothing(R.drawable.jeans_brown),
                Clothing(R.drawable.jeans_black),
            )
            temperature in 1..19 -> listOf(
                Clothing(R.drawable.hoodie_white),
                Clothing(R.drawable.jacket_brown),
                Clothing(R.drawable.sweater_skyblue),
                Clothing(R.drawable.sweater_green),
                Clothing(R.drawable.jeans_brown),
                Clothing(R.drawable.jeans_black),
            )
            temperature in 20..29 -> listOf(
                Clothing(R.drawable.dress_black),
                Clothing(R.drawable.skirt_blue_white_jacket),
                Clothing(R.drawable.skirt_black),
                Clothing(R.drawable.skirt_blue),
                Clothing(R.drawable.jeans_brown),
                Clothing(R.drawable.jeans_black),
            )
            else -> listOf(
                Clothing(R.drawable.tshirt_white),
                Clothing(R.drawable.tshirt_green),
                Clothing(R.drawable.tshirt_pink),
                Clothing(R.drawable.tshirt_brown),
                Clothing(R.drawable.tshirt_striped_red),
                Clothing(R.drawable.jeans_brown),
                Clothing(R.drawable.jeans_black),
                Clothing(R.drawable.shirt),
                Clothing(R.drawable.jeans_black),
                Clothing(R.drawable.dress_black),
                Clothing(R.drawable.dress_orange),
                Clothing(R.drawable.skirt_blue),
                Clothing(R.drawable.skirt_black),
            )
        }

        val randomClothing = clothingList.random()
        val imageResourceId = randomClothing.imageResourceId


        if (sharedPreferences.contains("lastImageResourceId") && sharedPreferences.getInt(
                "lastImageResourceId",
                0
            ) == imageResourceId
        ) {
            setClothingImage(temperature)
            return
        }
        binding.clothesImage.setImageResource(randomClothing.imageResourceId)
        sharedPreferences.edit().putInt("lastImageResourceId", imageResourceId).apply()
    }
}