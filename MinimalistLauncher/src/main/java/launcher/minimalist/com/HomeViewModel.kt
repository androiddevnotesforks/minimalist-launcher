package launcher.minimalist.com

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel

class HomeViewModel @ViewModelInject constructor(
        private val homeRepository: HomeRepository
) : ViewModel() {

    var favoriteApps: List<String> by mutableStateOf(emptyList())

    init {
        favoriteApps = homeRepository.fetchPreferences()
    }

    //TODO Store in DB
    fun addFavoriteApp(app: LauncherApplication) {
        favoriteApps = homeRepository.addFavoriteApp(app.appName)
    }

    fun removeFavorite(appName: String) {
        favoriteApps = homeRepository.removeFavoriteApp(appName)
    }
}