package launcher.minimalist.com

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import launcher.minimalist.com.data.WeatherData

class HomeViewModel @ViewModelInject constructor(
        private val homeRepository: HomeRepository
) : ViewModel() {

    var favoriteApps: List<String> by mutableStateOf(emptyList())

    val _weatherData: MutableLiveData<WeatherData> = MutableLiveData()
    var weatherData: LiveData<WeatherData> = _weatherData

    val _launcherApplications: MutableLiveData<MutableList<LauncherApplication>> = MutableLiveData()
    var launcherApplications: LiveData<MutableList<LauncherApplication>> = _launcherApplications

    val _filteredLauncherApplications: MutableLiveData<MutableList<LauncherApplication>> = MutableLiveData()
    var filteredLauncherApplications: LiveData<MutableList<LauncherApplication>> = _launcherApplications

    init {
        favoriteApps = homeRepository.fetchPreferences()

        viewModelScope.launch {
            _weatherData.postValue(homeRepository.getWeatherData())
        }
    }

    fun storeLauncherApplications(launcherApps: MutableList<LauncherApplication>) {
        _launcherApplications.postValue(launcherApps)
    }

    //TODO Store in DB
    fun addFavoriteApp(app: LauncherApplication) {
        favoriteApps = homeRepository.addFavoriteApp(app.appName)
    }

    fun removeFavorite(appName: String) {
        favoriteApps = homeRepository.removeFavoriteApp(appName)
    }

    fun saveZipCode(zipCode: String) {
        homeRepository.saveZipCode(zipCode)
        viewModelScope.launch {
            _weatherData.postValue(homeRepository.getWeatherData())
        }
    }

    fun saveTheme(themeName: String) {
        homeRepository.saveTheme(themeName)
    }

    fun saveShowLauncherIcons(showLauncherIcons: Boolean) {
        homeRepository.saveShowLauncherIcons(showLauncherIcons)
    }

    fun getTheme() = homeRepository.getTheme()

    fun getZipCode() = homeRepository.getZipCode()

    fun getShowLauncherIcons(): Boolean = homeRepository.showLauncherIcons()

    fun filterAppBySearch(searchChars : String){
        Log.d("TAG", "filterAppBySearch: $searchChars")
        val filteredList = launcherApplications.value!!.filter { it.appName.contains(searchChars) }
        Log.d("TAG", "filterAppBySearch: ${filteredList.size}")
        _filteredLauncherApplications.postValue(filteredList.toMutableList())
    }
}