package com.example.testdht

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var temperatureTextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var refreshButton: Button

    private val esp8266Ip = "http://192.168.0.88/" // Zastąp swoim adresem IP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        temperatureTextView = findViewById(R.id.temperatureTextView)
        humidityTextView = findViewById(R.id.humidityTextView)
        refreshButton = findViewById(R.id.refreshButton)

        refreshButton.setOnClickListener {
            fetchData()
        }

        // Automatyczne pobranie danych przy uruchomieniu
        fetchData()
    }

    private fun fetchData() {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(esp8266Ip)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    temperatureTextView.text = "Błąd połączenia"
                    humidityTextView.text = ""
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                if (response.isSuccessful && responseData != null) {
                    val json = JSONObject(responseData)
                    val temperature = json.getDouble("temperature")
                    val humidity = json.getDouble("humidity")

                    runOnUiThread {
                        temperatureTextView.text = "Temperatura: $temperature °C"
                        humidityTextView.text = "Wilgotność: $humidity %"
                    }
                } else {
                    runOnUiThread {
                        temperatureTextView.text = "Błąd odczytu danych"
                        humidityTextView.text = ""
                    }
                }
            }
        })
    }
}
