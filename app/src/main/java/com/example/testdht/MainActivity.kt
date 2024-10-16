package com.example.testdht

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var temperatureTextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var refreshButton: Button
    private lateinit var ipEditText: EditText
    private lateinit var saveIpButton: Button

    private var esp8266Ip: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        temperatureTextView = findViewById(R.id.temperatureTextView)
        humidityTextView = findViewById(R.id.humidityTextView)
        refreshButton = findViewById(R.id.refreshButton)
        ipEditText = findViewById(R.id.ipEditText)
        saveIpButton = findViewById(R.id.saveIpButton)

        // Wczytaj zapisany adres IP z SharedPreferences
        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        esp8266Ip = sharedPref.getString("ESP_IP", null)

        // Jeśli adres IP jest zapisany, ustaw go w polu EditText
        if (esp8266Ip != null) {
            ipEditText.setText(esp8266Ip)
        }

        saveIpButton.setOnClickListener {
            val ipInput = ipEditText.text.toString().trim()
            if (ipInput.isNotEmpty()) {
                // Dodaj protokół, jeśli nie został podany
                esp8266Ip = if (ipInput.startsWith("http://") || ipInput.startsWith("https://")) {
                    ipInput
                } else {
                    "http://$ipInput"
                }
                // Zapisz adres IP w SharedPreferences
                with(sharedPref.edit()) {
                    putString("ESP_IP", esp8266Ip)
                    apply()
                }
                // Informuj użytkownika, że adres IP został zapisany (opcjonalnie)
            } else {
                // Poinformuj użytkownika o konieczności wprowadzenia adresu IP (opcjonalnie)
            }
        }

        refreshButton.setOnClickListener {
            fetchData()
        }

        // Automatyczne pobranie danych przy uruchomieniu, jeśli adres IP jest ustawiony
        if (esp8266Ip != null) {
            fetchData()
        }
    }

    private fun fetchData() {
        if (esp8266Ip == null) {
            runOnUiThread {
                temperatureTextView.text = "Adres IP nie jest ustawiony"
                humidityTextView.text = ""
            }
            return
        }

        val client = OkHttpClient()

        val request = Request.Builder()
            .url(esp8266Ip!!)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    temperatureTextView.text = "Błąd połączenia: ${e.message}"
                    humidityTextView.text = ""
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                if (response.isSuccessful && responseData != null) {
                    try {
                        val json = JSONObject(responseData)
                        val temperature = json.getDouble("temperature")
                        val humidity = json.getDouble("humidity")

                        runOnUiThread {
                            temperatureTextView.text = "Temperatura: $temperature °C"
                            humidityTextView.text = "Wilgotność: $humidity %"
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            temperatureTextView.text = "Błąd parsowania danych"
                            humidityTextView.text = ""
                        }
                    }
                } else {
                    runOnUiThread {
                        temperatureTextView.text = "Błąd odczytu danych: ${response.code}"
                        humidityTextView.text = ""
                    }
                }
            }
        })
    }
}
