package launcher.minimalist.com.network

import launcher.minimalist.com.data.WeatherData
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("data/2.5/weather")
    suspend fun getLatLongByCity(
            @Query("lat") lat: String,
            @Query("lon") long: String,
            @Query("units") units: String = "Imperial",
            @Query("appid") key: String = "4e29ca8c4547fc74c003c8e58652d710"
    ): WeatherData

    @GET("data/2.5/weather")
    suspend fun getWeatherDataByCity(
            @Query("q") city: String,
            @Query("units") units: String = "Imperial",
            @Query("appid") key: String = "4e29ca8c4547fc74c003c8e58652d710"
    ): WeatherData

    @GET("data/2.5/weather")
    suspend fun getWeatherDataByZipCode(
            @Query("zip") zipCode: String,
            @Query("units") units: String = "Imperial",
            @Query("appid") key: String = "4e29ca8c4547fc74c003c8e58652d710"
    ): WeatherData

    @GET("data/2.5/onecall")
    suspend fun getAllWeatherData(
            @Query("lat") lat: String,
            @Query("lon") long: String,
            @Query("exclude") exclude: String = "minutely",
            @Query("units") units: String = "Imperial",
            @Query("appid") key: String = "4e29ca8c4547fc74c003c8e58652d710"
    ): WeatherData

}