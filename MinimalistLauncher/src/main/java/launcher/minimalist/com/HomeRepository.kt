package launcher.minimalist.com

import android.content.Context
import android.content.SharedPreferences
import launcher.minimalist.com.data.WeatherData
import launcher.minimalist.com.network.WeatherApi
import javax.inject.Inject

class HomeRepository @Inject constructor(
        val context: Context,
        private val weatherApi: WeatherApi,
) {

    private val preferences: SharedPreferences = context.getSharedPreferences(
            "favorites",
            Context.MODE_PRIVATE
    )

    fun fetchPreferences(): List<String> =
            preferences.getStringSet("favoriteApps", emptySet())!!.map { it }

    fun addFavoriteApp(appName: String): List<String> {
        val favorites = preferences.getStringSet("favoriteApps", emptySet())!!.map { it }

        val combined = favorites + listOf(appName)

        preferences.edit().putStringSet("favoriteApps", combined.toSet()).apply()

        return fetchPreferences()
    }

    fun removeFavoriteApp(appName: String): List<String> {
        var favorites = preferences.getStringSet("favoriteApps", emptySet())!!.map { it }

        favorites = favorites.minus(appName)

        preferences.edit().putStringSet("favoriteApps", favorites.toSet()).apply()

        return fetchPreferences()

    }

    suspend fun getWeatherData(): WeatherData? {
//        val cityDataByLatLong = locationApi.getLatLongByCity(search)
//        return weatherApi.getAllWeatherData(
//                cityDataByLatLong.results[0].geometry.lat.toString(),
//                cityDataByLatLong.results[0].geometry.lng.toString()
//        )
        return if (getZipCode().isEmpty()) {
            null
        } else {
            weatherApi.getWeatherDataByZipCode(zipCode = getZipCode())
        }
    }

    fun saveZipCode(zipCode: String) {
        preferences.edit().putString("zipCode", zipCode).apply()
    }

    fun getZipCode(): String {
        return preferences.getString("zipCode", "")!!
    }
}