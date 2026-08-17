package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherApiResponse(
  @Json(name = "latitude") val latitude: Double,
  @Json(name = "longitude") val longitude: Double,
  @Json(name = "current") val current: CurrentWeatherDto?,
  @Json(name = "daily") val daily: DailyWeatherDto?
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
  @Json(name = "time") val time: String = "",
  @Json(name = "temperature_2m") val temperature: Double = 0.0,
  @Json(name = "relative_humidity_2m") val relativeHumidity: Int = 0,
  @Json(name = "apparent_temperature") val apparentTemperature: Double = 0.0,
  @Json(name = "precipitation") val precipitation: Double = 0.0,
  @Json(name = "weather_code") val weatherCode: Int = 0,
  @Json(name = "wind_speed_10m") val windSpeed: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class DailyWeatherDto(
  @Json(name = "time") val time: List<String> = emptyList(),
  @Json(name = "weather_code") val weatherCode: List<Int> = emptyList(),
  @Json(name = "temperature_2m_max") val temperatureMax: List<Double> = emptyList(),
  @Json(name = "temperature_2m_min") val temperatureMin: List<Double> = emptyList(),
  @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>? = emptyList()
)

data class ApiaryWeather(
  val temperature: Double,
  val apparentTemperature: Double,
  val humidity: Int,
  val windSpeed: Double,
  val precipitationProbability: Int = 0,
  val weatherCode: Int,
  val conditionDescription: String,
  val conditionIcon: String,
  val lastUpdatedFormatted: String = "",
  val dailyForecast: List<DayForecast> = emptyList(),
  val isBeeFlyingOptimal: Boolean,
  val beeFlightMessage: String
)

data class DayForecast(
  val date: String,
  val dayName: String,
  val tempMax: Double,
  val tempMin: Double,
  val conditionDescription: String,
  val conditionIcon: String,
  val precipitationProb: Int
)

fun interpretWmoWeatherCode(code: Int): Pair<String, String> {
  return when (code) {
    0 -> "Açık ve Güneşli" to "☀️"
    1, 2, 3 -> "Parçalı Bulutlu" to "🌤️"
    45, 48 -> "Sisli / Puslu" to "🌫️"
    51, 53, 55 -> "Hafif Çisenti" to "🌦️"
    61, 63, 65 -> "Yağmurlu" to "🌧️"
    71, 73, 75 -> "Karlı" to "🌨️"
    80, 81, 82 -> "Kuvvetli Sağanak" to "⛈️"
    95, 96, 99 -> "Gök Gürültülü Fırtına" to "🌩️"
    else -> "Bulutlu" to "☁️"
  }
}

fun evaluateBeeFlightCondition(temp: Double, rain: Double, wind: Double): Pair<Boolean, String> {
  return when {
    temp < 12.0 -> false to "Hava soğuk (<12°C). Arılar kovan içinde salkımda, uçuş yok."
    temp in 12.0..16.0 -> true to "Sınırlı uçuş sıcaklığı (12-16°C). Kovan açılırken dikkatli olunmalı."
    temp in 16.0..32.0 && rain <= 0.0 && wind < 20.0 -> true to "Mükemmel arı uçuşu ve kovan kontrol havası! 🐝"
    temp > 35.0 -> true to "Çok sıcak (>35°C). Arılar su ve havalandırma çalışmasında."
    rain > 0.5 -> false to "Yağış var. Kovan kontrolleri ertelenmeli."
    wind >= 25.0 -> false to "Kuvvetli rüzgar (>25 km/s). Arılar hırçınlaşabilir."
    else -> true to "Uygun hava koşulları."
  }
}
