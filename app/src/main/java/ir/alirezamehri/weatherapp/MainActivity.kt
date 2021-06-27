package ir.alirezamehri.weatherapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import com.bumptech.glide.Glide
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import ir.alirezamehri.weatherapp.databinding.ActivityMainBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


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
        humidity: Int
    ) {
        binding.imageViewInfo.setOnClickListener(){
            val intent = Intent(this, infoActivity::class.java)
            startActivity(intent)
        }

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


        Glide.with(this@MainActivity).load(imageUrl).into(binding.imageViewWeather)
    }

    private fun getTimeFromUnixTime(unixTime: Int): String {
        val time = unixTime * 1000.toLong() // int -> long
        val date = Date(time) // mon 2021/05/01 12:15:54 Am
        val formatter = SimpleDateFormat("HH:mm a")
        return formatter.format(date) // 12:15 Am
    }

    private fun getData() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.openweathermap.org/data/2.5/weather?q=tehran&appid=40c2fae6f3611c044dde04b13bde5451&lang=fa&units=metric")
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
                humidity
            )
        }
    }

    fun reLoadData(view: View) {
        binding.refresh.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE

        binding.textViewTemp.text = "--"
        binding.textViewfeelsLike.text ="--"
        binding.textViewtempMin.text ="--"
        binding.textViewtempMax.text ="--"
        binding.textViewpressure.text ="--"
        binding.textViewhumidity.text ="--"
        binding.textViewSunset.text = "--"
        binding.textViewSunrise.text = "--"
        binding.textViewWeatherDescription.text = "--"
        Glide.with(this@MainActivity).load(R.drawable.ic_refresh).into(binding.imageViewWeather)

        getData()
    }


}