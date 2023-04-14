package ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import okhttp3.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.clothes_suggester.R
import com.example.clothes_suggester.databinding.ActivityMainBinding
import data.Clothing
import android.Manifest.permission.*
import android.location.LocationManager.*
import com.example.clothes_suggester.utils.Constant

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val client = OkHttpClient()
    private val converter = WeatherConverter()
    private val apiManager = ApiManager(client, converter)
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        apiManager.getWeather(32.5f, 44.0f, ::onWeatherResponse, ::onError)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(ACCESS_FINE_LOCATION),
                Constant.REQUEST_LOCATION_PERMISSION
            )
        } else {
            getLocation()
        }
    }

    private fun getLocation() {
        if (locationManager.isProviderEnabled(GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
            locationManager.requestLocationUpdates(
                GPS_PROVIDER,
                Constant.MIN_TIME_BW_UPDATES,
                Constant.MIN_DISTANCE_CHANGE_FOR_UPDATES,
                object : android.location.LocationListener {

                    override fun onLocationChanged(p0: Location) {
                        val latitude = p0.latitude
                        val longitude = p0.longitude
                        apiManager.getWeather(
                            latitude.toFloat(),
                            longitude.toFloat(),
                            ::onWeatherResponse,
                            ::onError
                        )

                        locationManager.removeUpdates(this)
                    }

                    @Deprecated("Deprecated ")
                    override fun onStatusChanged(
                        provider: String?,
                        status: Int,
                        extras: Bundle?
                    ) {
                    }

                })
        } else {
            Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constant.REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
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

                setCloudImage(temperature)
                setClothingImage(temperature)
            }
        }
    }

    private fun onError() {
        binding.clothesImage.setImageResource(R.drawable.error2)
    }

    private fun setCloudImage(temperature: Int) {
        val cloudImage = when (temperature) {
            in Int.MIN_VALUE..0 -> R.drawable.cloudy
            in 1..19 -> R.drawable.cloud
            in 20..29 -> R.drawable.sunny
            else -> R.drawable.hot
        }
        binding.imageCloud.setImageResource(cloudImage)
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