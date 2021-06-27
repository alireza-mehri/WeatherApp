package ir.alirezamehri.weatherapp


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import ir.alirezamehri.weatherapp.databinding.ActivityMainBinding
import ir.alirezamehri.weatherapp.databinding.ActivitySplashScreenBinding
import okhttp3.*
import java.io.IOException


class SplashScreen : AppCompatActivity() {
    lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        checkInternet()
    }

     private fun checkInternet() {
         val intent = Intent(this, MainActivity::class.java)
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.openweathermap.org/data/2.5/weather?q=tehran&appid=40c2fae6f3611c044dde04b13bde5451&lang=fa&units=metric")
            .build()

        Handler().postDelayed({
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            "ارتباط اینترنت را بررسی کنید",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.buttonReload.visibility = View.VISIBLE
                        binding.progressBarReaload.visibility = View.INVISIBLE
                        binding.buttonReload.setOnClickListener(){
                            binding.buttonReload.visibility = View.INVISIBLE
                            binding.progressBarReaload.visibility = View.VISIBLE
                            checkInternet()
                        }
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    startActivity(intent)
                    finish()
                }

            })

        }, 3000)
    }
}