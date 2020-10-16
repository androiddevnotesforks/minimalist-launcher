package launcher.minimalist.com

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

    val _weatherData : MutableLiveData<WeatherData> = MutableLiveData()
    var weatherData: LiveData<WeatherData> = _weatherData

    init {
        favoriteApps = homeRepository.fetchPreferences()

        viewModelScope.launch {
            _weatherData.postValue(homeRepository.getWeatherData())
        }
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

    fun getZipCode() = homeRepository.getZipCode()
}