package com.example.clothes_suggester

import okhttp3.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.clothes_suggester.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    private val client = OkHttpClient()
    private val converter = WeatherConverter()
    private val apiManager = ApiManager(client, converter)

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        apiManager.getWeather(10.33f, 12.44f, ::onWeatherResponse, ::onError)
    }

    private fun onWeatherResponse(temperature : Int){
        runOnUiThread {
            binding.tempText.text = "$temperature C"
            setClothingImage(temperature)
        }
    }

    private fun onError(){

    }

    private fun setClothingImage(temperature: Int) {
        val clothingList = when {
            temperature <= 0 -> listOf(
                Clothing(R.drawable.hoodies),
                Clothing(R.drawable.bag),
                Clothing(R.drawable.hot1),
                Clothing(R.drawable.hoodi)
            )
            temperature in 1..19 -> listOf(
                Clothing(R.drawable.cold),
                Clothing(R.drawable.jeans),
                Clothing(R.drawable.white),
                Clothing(R.drawable.bag)
            )
            temperature in 20..29 -> listOf(
                Clothing(R.drawable.bag),
                Clothing(R.drawable.hoodi),
                Clothing(R.drawable.jeans),
                Clothing(R.drawable.cold)
            )
            else -> listOf(
                Clothing(R.drawable.hoodies),
                Clothing(R.drawable.hot1),
                Clothing(R.drawable.white),
                Clothing(R.drawable.jeans)
            )
        }

        val randomClothing = clothingList.random()
        binding.clothesImage.setImageResource(randomClothing.imageResourceId)
    }
}