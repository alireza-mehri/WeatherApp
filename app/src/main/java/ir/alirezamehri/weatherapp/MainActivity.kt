package ir.alirezamehri.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import ir.alirezamehri.weatherapp.databinding.ActivityMainBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    //Location the needed Variables
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    val PERMISSION_ID = 1010

    //Location the needed Variables
    private var currentCity = "tehran" //"tehran"
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //------------- Get location ---------------
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        RequestPermission()
        getLastLocation()
        //------------- Get location ---------------
        getData()

    }

    private fun showContent(
        cityName: String,
        weatherDescription: String,
        imageUrl: String,
        sunrise: Int,
        sunset: Int,
        temp: Double,
        feelsLike: Double,
        tempMin: Double,
        tempMax: Double,
        pressure: Int,
        humidity: Int,
        windDeg: Int,
        windSpeed: Int
    ) {
        binding.imageViewInfo.setOnClickListener() {
            val intent = Intent(this, infoActivity::class.java)
            startActivity(intent)
        }

        binding.refresh.setOnClickListener() {
            reLoadData()
        }
        binding.gps.setOnClickListener() {
            reLoadData()
        }
//--------   search by the City ----------------
//        binding.buttonMashhad.setOnClickListener() {
//            currentCity = "mashhad"
//            reLoadData()
//        }
// --------   search by the City ----------------

        binding.refresh.visibility = View.VISIBLE
        binding.progressBar.visibility = View.INVISIBLE

        binding.textViewCityName.text = cityName
        binding.textViewWeatherDescription.text = weatherDescription
        binding.textViewSunrise.text = getTimeFromUnixTime(sunrise)
        binding.textViewSunset.text = getTimeFromUnixTime(sunset)

        binding.textViewTemp.text = " °C دما : ${temp}"
        binding.textViewfeelsLike.text = " °C دما مستقیم : ${feelsLike}"
        binding.textViewtempMin.text = " °C حداقل دما : ${tempMin}"
        binding.textViewtempMax.text = " °C حداکثر دما : ${tempMax}"
        binding.textViewpressure.text = " فشار هوا : ${pressure}"
        binding.textViewhumidity.text = " رطوبت هوا : ${humidity}"

        binding.textViewSpeed.text = " سرعت باد : ${windSpeed}"
        binding.textViewDeg.text = " درجه باد : ${windDeg}"


        Glide.with(this@MainActivity).load(imageUrl).into(binding.imageViewWeather)
    }

    private fun getTimeFromUnixTime(unixTime: Int): String {
        val time = unixTime * 1000.toLong() // int -> long
        val date = Date(time) // mon 2021/05/01 12:15:54 Am
        val formatter = SimpleDateFormat("HH:mm a")
        return formatter.format(date) // 12:15 Am
    }

    private fun getData() {
//------------- Get location ---------------
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        RequestPermission()
        getLastLocation()
        //------------- Get location ---------------
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.openweathermap.org/data/2.5/weather?q=${currentCity}&appid=40c2fae6f3611c044dde04b13bde5451&lang=fa&units=metric")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "ارتباط اینترنت را بررسی کنید",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.refresh.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.INVISIBLE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val rawContent = response.body!!.string()
                getDataToShow(rawContent)
            }

        })
    }

    private fun getDataToShow(rawData: String) {
        val jsonObject = JSONObject(rawData)

        val sunrise = jsonObject.getJSONObject("sys").getInt("sunrise")
        val sunset = jsonObject.getJSONObject("sys").getInt("sunset")

        val temp = jsonObject.getJSONObject("main").getDouble("temp")
        val feelsLike = jsonObject.getJSONObject("main").getDouble("feels_like")
        val tempMin = jsonObject.getJSONObject("main").getDouble("temp_min")
        val tempMax = jsonObject.getJSONObject("main").getDouble("temp_max")
        val pressure = jsonObject.getJSONObject("main").getInt("pressure")
        val humidity = jsonObject.getJSONObject("main").getInt("humidity")

        val windSpeed = jsonObject.getJSONObject("wind").getInt("speed")
        val windDeg = jsonObject.getJSONObject("wind").getInt("deg")

        val weatherArray = jsonObject.getJSONArray("weather")
        val weatherObject = weatherArray.getJSONObject(0)
        val iconId = weatherObject.getString("icon")
        val imageUrl = "https://openweathermap.org/img/wn/${iconId}@2x.png"
        runOnUiThread {
            showContent(
                jsonObject.getString("name"),
                weatherObject.getString("description"),
                imageUrl,
                sunrise,
                sunset,
                temp,
                feelsLike,
                tempMin,
                tempMax,
                pressure,
                humidity,
                windDeg,
                windSpeed
            )
        }
    }

    fun reLoadData() {
        binding.refresh.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE

        binding.textViewTemp.text = "--"
        binding.textViewfeelsLike.text = "--"
        binding.textViewtempMin.text = "--"
        binding.textViewtempMax.text = "--"
        binding.textViewpressure.text = "--"
        binding.textViewhumidity.text = "--"
        binding.textViewSunset.text = "--"
        binding.textViewSunrise.text = "--"
        binding.textViewSpeed.text = "--"
        binding.textViewDeg.text = "--"
        binding.textViewWeatherDescription.text = "--"
        Glide.with(this@MainActivity).load(R.drawable.ic_refresh).into(binding.imageViewWeather)

        getData()
    }


    //------------- Get location ---------------------
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (CheckPermission()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        NewLocationData()
                    } else {
                        currentCity = getCityName(location.latitude,location.longitude)
                    }
                }
            } else {
                Toast.makeText(this, "مکان نما را روشن کنید", Toast.LENGTH_SHORT).show()
            }
        } else {
            RequestPermission()
        }
    }


    @SuppressLint("MissingPermission")
    fun NewLocationData() {
        var locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    }


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var lastLocation: Location = locationResult.lastLocation
            Log.d("Debug:", "your last last location: " + lastLocation.longitude.toString())
            currentCity = getCityName(lastLocation.latitude, lastLocation.longitude)
        }
    }

    private fun CheckPermission(): Boolean {
        //this function will return a boolean
        //true: if we have permission
        //false if not
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false

    }

    fun RequestPermission() {
        //this function will allows us to tell the user to requesut the necessary permsiion if they are not garented
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    fun isLocationEnabled(): Boolean {
        //this function will return to us the state of the location service
        //if the gps or the network provider is enabled then it will return true otherwise it will return false
        var locationManager =
            getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Debug:", "You have the Permission")
            }
        }
    }

    private fun getCityName(lat: Double, long: Double): String {
        var cityName: String = ""
        var countryName = ""
        var geoCoder = Geocoder(this, Locale.getDefault())
        var Adress = geoCoder.getFromLocation(lat, long, 3)

        cityName = Adress.get(0).locality
        countryName = Adress.get(0).countryName
        Log.d("Debug:", "Your City: " + cityName + " ; your Country " + countryName)
        return cityName
    }
//------------- Get location ---------------------

}