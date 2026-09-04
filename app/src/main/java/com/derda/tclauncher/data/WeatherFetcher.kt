package com.derda.tclauncher.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class WeatherInfo(val tempCelsius: Int, val description: String)

/**
 * API anahtarı gerektirmeyen ücretsiz wttr.in servisinden basit hava durumu çeker.
 * Şehir adı boşsa veya bağlantı başarısız olursa null döner (uygulama çökmez).
 */
object WeatherFetcher {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetch(city: String): WeatherInfo? {
        if (city.isBlank()) return null
        return withContext(Dispatchers.IO) {
            runCatching {
                val encoded = URLEncoder.encode(city, "UTF-8")
                val url = URL("https://wttr.in/$encoded?format=j1")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                connection.setRequestProperty("User-Agent", "TCL-Launcher")
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val parsed = json.decodeFromString<WttrResponse>(body)
                val current = parsed.current_condition?.firstOrNull()
                val temp = current?.temp_C?.toIntOrNull()
                val desc = current?.weatherDesc?.firstOrNull()?.value
                if (temp != null && desc != null) WeatherInfo(temp, desc) else null
            }.getOrNull()
        }
    }
}

@Serializable
private data class WttrResponse(
    val current_condition: List<WttrCurrentCondition>? = null
)

@Serializable
private data class WttrCurrentCondition(
    val temp_C: String? = null,
    val weatherDesc: List<WttrDesc>? = null
)

@Serializable
private data class WttrDesc(
    val value: String? = null
)
