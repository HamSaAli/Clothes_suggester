package ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.clothes_suggester.BuildConfig
import com.example.clothes_suggester.R
import com.example.clothes_suggester.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import data.WeatherConverter
import data.model.CityResponse
import data.model.WeatherResponse
import data.source.LocalDataSource
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import utils.Constant


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val logInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    private val client = OkHttpClient.Builder().apply { addInterceptor(logInterceptor) }.build()
    private val converter = WeatherConverter()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var disposable: Disposable? = null


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

    private fun getCityName(latitude: Double, longitude: Double): Single<String> {
        val request = Request.Builder()
            .url("${Constant.BASE_URL}lat=$latitude&lon=$longitude&appid=${BuildConfig.API_KEY}")
            .build()


        return Single.create { emitter ->

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    emitter.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.string()?.let { jsonString ->
                        val result = Gson().fromJson(jsonString, CityResponse::class.java)
                        Log.i("HAMSA", "result: $result")
                        result.name.let { emitter.onSuccess(it) }
                    }
                }
            })
        }
    }

    private fun getWeather(cityName: String): Single<WeatherResponse> {
        val request = Request.Builder()
            .url("${Constant.BASE_URL}q=${cityName}&appid=${BuildConfig.API_KEY}")
            .build()

        return Single.create { emitter ->
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    emitter.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.string()?.let { jsonString ->
                        val result = Gson().fromJson(jsonString, WeatherResponse::class.java)
                        emitter.onSuccess(result)
                    }
                }

            })
        }
    }

    private fun handleWeatherResponse(result: WeatherResponse) {
        val temperature = converter.convertFahrenheitToCelsius(
            result.main.temperature.toFloatOrNull() ?: 0f
        )
        binding.apply {
            textTempature.text = temperature.toString().plus("°C")
            textCityName.text = result.name
            textPressure.text = (result.main.pressure).plus("hpa")
            textHumidity.text = (result.main.humidity).plus("%")
            textWindSpeed.text = result.main.feelsLike
        }
        setWeatherStatusImage(result)
        setClothingImage(result)
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
                        Toast.makeText(this, "Null Received", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Get Success", Toast.LENGTH_SHORT).show()
                        disposable = getCityName(location.latitude, location.longitude)
                            .flatMap { cityName ->
                                getWeather(cityName)
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                { result ->
                                    handleWeatherResponse(result)
                                },
                                { e ->
                                    e.message?.let { Log.v("ActivityMain", it) }
                                }
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
        val iconCode = weatherResponse.weatherStatus.joinToString { it.iconWeatherStatus }
        binding.imageWeatherStatus.apply {
            setImageResource(
                when (iconCode) {
                    "01d" -> R.drawable.sun
                    "02d" -> R.drawable.few_cloud
                    "03d" -> R.drawable.clouds
                    "04d" -> R.drawable.icon1
                    "09d" -> R.drawable.shower_rain
                    "10d" -> R.drawable.rainy
                    "11d" -> R.drawable.thunderstorm
                    "13d" -> R.drawable.snow
                    "50d" -> R.drawable.icon2
                    "02n" -> R.drawable.scarred
                    "03n" -> R.drawable.clouds
                    "04n" -> R.drawable.icon2
                    "09n" -> R.drawable.rainy
                    "10n" -> R.drawable.rain
                    "11n" -> R.drawable.thunderstorm
                    "13n" -> R.drawable.snow
                    "50n" -> R.drawable.icon2
                    else -> R.drawable.sun
                }
            )
        }
    }

    private fun setClothingImage(weatherResponse: WeatherResponse) {
        val temperature = converter.convertFahrenheitToCelsius(
            weatherResponse.main.temperature.toFloatOrNull() ?: 0f
        )
        val clothingList = when {
            temperature <= 0 -> LocalDataSource.tooHeavyClothes
            temperature in Constant.TEMPERATURE_MIN..Constant.TEMPERATURE_MID -> LocalDataSource.heavyClothes
            temperature in Constant.TEMPERATURE_MAX..Constant.TEMPERATURE_XMAX -> LocalDataSource.springClothes
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
        binding.imageClothesSuggester.setImageResource(randomClothing.imageResourceId)
        sharedPreferences.edit().putInt("lastImageResourceId", imageResourceId).apply()
    }
}