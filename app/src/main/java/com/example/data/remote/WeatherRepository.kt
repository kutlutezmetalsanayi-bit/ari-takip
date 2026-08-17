package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class WeatherRepository {
  private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

  private val logging = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BASIC
  }

  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .addInterceptor(logging)
    .build()

  private val api: WeatherApiService by lazy {
    Retrofit.Builder()
      .baseUrl("https://api.open-meteo.com/")
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(WeatherApiService::class.java)
  }

  // Cache entry: weather data + timestamp in millis (TTL: 30 minutes)
  private data class CacheEntry(val weather: ApiaryWeather, val timestamp: Long)
  private val weatherCache = mutableMapOf<String, CacheEntry>()
  private val CACHE_TTL_MILLIS = 30 * 60 * 1000L // 30 minutes

  suspend fun getApiaryWeather(
    latitude: Double,
    longitude: Double,
    forceRefresh: Boolean = false
  ): Result<ApiaryWeather> = withContext(Dispatchers.IO) {
    val cacheKey = "${"%.4f".format(Locale.US, latitude)}_${"%.4f".format(Locale.US, longitude)}"
    val now = System.currentTimeMillis()

    if (!forceRefresh) {
      val cached = weatherCache[cacheKey]
      if (cached != null && (now - cached.timestamp) < CACHE_TTL_MILLIS) {
        return@withContext Result.success(cached.weather)
      }
    }

    try {
      val response = api.getForecast(latitude = latitude, longitude = longitude)
      if (response.isSuccessful && response.body() != null) {
        val body = response.body()!!
        val current = body.current
        if (current != null) {
          val (desc, icon) = interpretWmoWeatherCode(current.weatherCode)
          val (isOptimal, flightMsg) = evaluateBeeFlightCondition(
            temp = current.temperature,
            rain = current.precipitation,
            wind = current.windSpeed
          )

          val dailyList = mutableListOf<DayForecast>()
          val daily = body.daily
          if (daily != null) {
            val count = minOf(daily.time.size, daily.weatherCode.size, daily.temperatureMax.size)
            for (i in 0 until minOf(count, 5)) {
              val dateStr = daily.time[i]
              val dayName = getDayNameFromDate(dateStr)
              val (dDesc, dIcon) = interpretWmoWeatherCode(daily.weatherCode[i])
              val prob = daily.precipitationProbabilityMax?.getOrNull(i) ?: 0
              dailyList.add(
                DayForecast(
                  date = dateStr,
                  dayName = dayName,
                  tempMax = daily.temperatureMax[i],
                  tempMin = daily.temperatureMin[i],
                  conditionDescription = dDesc,
                  conditionIcon = dIcon,
                  precipitationProb = prob
                )
              )
            }
          }

          val todayRainProb = body.daily?.precipitationProbabilityMax?.firstOrNull() ?: 0
          val timeFormat = SimpleDateFormat("HH:mm", Locale("tr", "TR"))
          val updatedStr = timeFormat.format(Date(now))

          val weatherData = ApiaryWeather(
            temperature = current.temperature,
            apparentTemperature = current.apparentTemperature,
            humidity = current.relativeHumidity,
            windSpeed = current.windSpeed,
            precipitationProbability = todayRainProb,
            weatherCode = current.weatherCode,
            conditionDescription = desc,
            conditionIcon = icon,
            lastUpdatedFormatted = updatedStr,
            dailyForecast = dailyList,
            isBeeFlyingOptimal = isOptimal,
            beeFlightMessage = flightMsg
          )

          weatherCache[cacheKey] = CacheEntry(weatherData, now)
          Result.success(weatherData)
        } else {
          // If network failed but cache exists (even expired), gracefully fallback to cached
          weatherCache[cacheKey]?.let { return@withContext Result.success(it.weather) }
          Result.failure(Exception("Hava durumu şu anda alınamadı."))
        }
      } else {
        weatherCache[cacheKey]?.let { return@withContext Result.success(it.weather) }
        Result.failure(Exception("Hava durumu servisine ulaşılamadı (${response.code()})."))
      }
    } catch (e: Exception) {
      weatherCache[cacheKey]?.let { return@withContext Result.success(it.weather) }
      Result.failure(Exception("Hava durumu şu anda alınamadı: ${e.localizedMessage ?: "Bağlantı hatası"}"))
    }
  }

  private fun getDayNameFromDate(dateStr: String): String {
    return try {
      val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
      val date = parser.parse(dateStr) ?: return dateStr
      val formatter = SimpleDateFormat("EEEE", Locale("tr", "TR"))
      formatter.format(date).replaceFirstChar { it.uppercase() }
    } catch (e: Exception) {
      dateStr
    }
  }
}
