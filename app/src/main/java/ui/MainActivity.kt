package ui

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import okhttp3.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import androidx.core.app.ActivityCompat
import com.example.clothes_suggester.R
import com.example.clothes_suggester.databinding.ActivityMainBinding
import android.Manifest.permission.*
import android.content.Intent
import android.location.LocationManager.*
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import data.model.WeatherResponse
import android.provider.Settings
import com.example.clothes_suggester.BuildConfig
import com.example.clothes_suggester.utils.utils.Constant
import data.source.LocalDataSource
import okio.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val client = OkHttpClient()
    private val converter = WeatherConverter()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        setup()
    }

    private fun setup() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()
    }

    private fun getWeather(
        latitude: Double,
        longitude: Double,
    ) {

        val request = Request.Builder()
            .url("${Constant.BASE_URL}/weather?lat=$latitude&lon=$longitude&appid=${BuildConfig.API_KEY}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.v("ActivityMain", "$e.message")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string().let { jsonString ->
                    val result = Gson().fromJson(jsonString, WeatherResponse::class.java)
                    runOnUiThread {
                        val temperature = converter.convertFahrenheitToCelsius(
                            result.main.temperature.toFloatOrNull() ?: 0f
                        )
                        binding.textTempature.text = temperature.toString().plus("°C")
                        binding.textCityName.text = result.name
                        binding.textPressure.text = (result.main.pressure).plus("hpa")
                        binding.textHumidity.text = (result.main.humidity).plus("%")
                        binding.textWindSpeed.text = result.main.feelsLike
                        setWeatherStatusImage(result)
                        setClothingImage(result)
                    }
                }
            }
        })
    }

    private fun getCurrentLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        ACCESS_FINE_LOCATION
                    ) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, ACCESS_COARSE_LOCATION
                    ) !=
                    PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        Toast.makeText(this, "Null Recived", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Get Success", Toast.LENGTH_SHORT).show()
                        getWeather(
                            location.latitude,
                            location.longitude,
                        )
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location ", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(GPS_PROVIDER) || locationManager.isProviderEnabled(
            NETWORK_PROVIDER
        )
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                ACCESS_COARSE_LOCATION,
                ACCESS_FINE_LOCATION
            ),
            Constant.PERMISSION_PEQEST_ACCESS_LOCATION
        )
    }

    private fun checkPermission(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(applicationContext, "Granted", Toast.LENGTH_SHORT).show()
            getCurrentLocation()
        } else {
            Toast.makeText(applicationContext, "Denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setWeatherStatusImage(weatherResponse: WeatherResponse) {
        val iconCode = weatherResponse.weatherStatus.joinToString {
            it.iconWeatherStatus
        }
        return when (iconCode) {
            "01d" -> binding.imageWeatherStatus.setImageResource(R.drawable.sun)
            "02d" -> binding.imageWeatherStatus.setImageResource(R.drawable.few_cloud)
            "03d" -> binding.imageWeatherStatus.setImageResource(R.drawable.clouds)
            "04d" -> binding.imageWeatherStatus.setImageResource(R.drawable.icon1)
            "09d" -> binding.imageWeatherStatus.setImageResource(R.drawable.shower_rain)
            "10d" -> binding.imageWeatherStatus.setImageResource(R.drawable.rainy)
            "11d" -> binding.imageWeatherStatus.setImageResource(R.drawable.thunderstorm)
            "13d" -> binding.imageWeatherStatus.setImageResource(R.drawable.snow)
            "50d" -> binding.imageWeatherStatus.setImageResource(R.drawable.icon2)
            "02n" -> binding.imageWeatherStatus.setImageResource(R.drawable.scarred)
            "03n" -> binding.imageWeatherStatus.setImageResource(R.drawable.clouds)
            "04n" -> binding.imageWeatherStatus.setImageResource(R.drawable.icon2)
            "09n" -> binding.imageWeatherStatus.setImageResource(R.drawable.rainy)
            "10n" -> binding.imageWeatherStatus.setImageResource(R.drawable.rain)
            "11n" -> binding.imageWeatherStatus.setImageResource(R.drawable.thunderstorm)
            "13n" -> binding.imageWeatherStatus.setImageResource(R.drawable.snow)
            "50n" -> binding.imageWeatherStatus.setImageResource(R.drawable.icon2)
            else -> binding.imageWeatherStatus.setImageResource(R.drawable.sun)
        }
    }

    private fun setClothingImage(weatherResponse: WeatherResponse) {
        val temperature = converter.convertFahrenheitToCelsius(
            weatherResponse.main.temperature.toFloatOrNull() ?: 0f
        )
        val clothingList = when {
            temperature <= 0 -> LocalDataSource.tooHeavyClothes
            temperature in 1..19 -> LocalDataSource.heavyClothes
            temperature in 20..29 -> LocalDataSource.springClothes
            else -> LocalDataSource.lightClothes
        }

        val randomClothing = clothingList.random()
        val imageResourceId = randomClothing.imageResourceId


        if (sharedPreferences.contains("lastImageResourceId") && sharedPreferences.getInt(
                "lastImageResourceId",
                0
            ) == imageResourceId
        ) {
            setClothingImage(weatherResponse)
            return
        }
        binding.clothesImage.setImageResource(randomClothing.imageResourceId)
        sharedPreferences.edit().putInt("lastImageResourceId", imageResourceId).apply()
    }
}