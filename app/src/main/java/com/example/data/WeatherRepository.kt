package com.example.data

import com.example.model.WeatherState
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@JsonClass(generateAdapter = true)
data class OpenMeteoForecastResponse(
    @Json(name = "current") val current: CurrentWeatherDto?,
    @Json(name = "daily") val daily: DailyWeatherDto?
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @Json(name = "temperature_2m") val temperature2m: Double?,
    @Json(name = "relative_humidity_2m") val humidity2m: Int?,
    @Json(name = "weather_code") val weatherCode: Int?,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double?
)

@JsonClass(generateAdapter = true)
data class DailyWeatherDto(
    @Json(name = "temperature_2m_max") val maxTemps: List<Double>?,
    @Json(name = "temperature_2m_min") val minTemps: List<Double>?
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    @Json(name = "results") val results: List<GeocodingCityDto>?
)

@JsonClass(generateAdapter = true)
data class GeocodingCityDto(
    @Json(name = "name") val name: String,
    @Json(name = "country") val country: String?,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double
)

interface OpenMeteoForecastApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoForecastResponse
}

interface OpenMeteoGeocodingApi {
    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 1,
        @Query("language") language: String = "en"
    ): GeocodingResponse
}

class WeatherRepository {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val forecastApi: OpenMeteoForecastApi = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(OpenMeteoForecastApi::class.java)

    private val geocodingApi: OpenMeteoGeocodingApi = Retrofit.Builder()
        .baseUrl("https://geocoding-api.open-meteo.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(OpenMeteoGeocodingApi::class.java)

    suspend fun fetchWeatherByCity(cityName: String): WeatherState {
        return try {
            val geo = geocodingApi.searchCity(cityName.trim().ifEmpty { "San Francisco" })
            val place = geo.results?.firstOrNull()
                ?: return WeatherState(
                    city = cityName,
                    isLoading = false,
                    conditionLabel = "City not found",
                    error = "City not found"
                )
            fetchWeatherByCoordinates(
                latitude = place.latitude,
                longitude = place.longitude,
                displayCity = place.name
            )
        } catch (e: Exception) {
            WeatherState(
                city = cityName,
                isLoading = false,
                conditionLabel = "Offline",
                error = e.localizedMessage ?: "Network unavailable"
            )
        }
    }

    suspend fun fetchWeatherByCoordinates(
        latitude: Double,
        longitude: Double,
        displayCity: String
    ): WeatherState {
        return try {
            val response = forecastApi.getForecast(latitude = latitude, longitude = longitude)
            val current = response.current
            val daily = response.daily
            val code = current?.weatherCode ?: 0
            WeatherState(
                city = displayCity,
                tempCelsius = current?.temperature2m?.roundToInt(),
                conditionLabel = mapWmoCodeToLabel(code),
                weatherCode = code,
                highTemp = daily?.maxTemps?.firstOrNull()?.roundToInt(),
                lowTemp = daily?.minTemps?.firstOrNull()?.roundToInt(),
                humidity = current?.humidity2m,
                windKmh = current?.windSpeed10m?.roundToInt(),
                isLoading = false,
                error = null
            )
        } catch (e: Exception) {
            WeatherState(
                city = displayCity,
                isLoading = false,
                conditionLabel = "Unavailable",
                error = e.localizedMessage ?: "Network unavailable"
            )
        }
    }

    private fun mapWmoCodeToLabel(code: Int): String = when (code) {
        0 -> "Clear Sky"
        1 -> "Mostly Clear"
        2 -> "Partly Cloudy"
        3 -> "Overcast"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        66, 67 -> "Freezing Rain"
        71, 73, 75, 77 -> "Snow"
        80, 81, 82 -> "Rain Showers"
        85, 86 -> "Snow Showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Fair"
    }
}
