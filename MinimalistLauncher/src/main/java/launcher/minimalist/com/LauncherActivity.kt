package launcher.minimalist.com

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.gesture.Direction
import androidx.compose.ui.gesture.DragObserver
import androidx.compose.ui.gesture.dragGestureFilter
import androidx.compose.ui.gesture.longPressGestureFilter
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.viewModel
import androidx.lifecycle.ViewModel
import java.util.*

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val screenState = remember { mutableStateOf(LauncherScreen.DRAWER) }
            Scaffold(
                    bodyContent = {
                        ShowBodyContent(screenState = screenState, launcherActivity = this@LauncherActivity)
                    }
            )
        }
    }

}

@Composable
private fun ShowBodyContent(screenState: MutableState<LauncherScreen>, launcherActivity: LauncherActivity) {
    val homeViewModel: HomeViewModel = viewModel(HomeViewModel::class.java)

    when (screenState.value) {
        LauncherScreen.HOME -> {
            HomeContent(screenState = screenState, launcherActivity = launcherActivity, homeViewModel = homeViewModel)
        }
        LauncherScreen.DRAWER -> {
            DrawerContent(screenState = screenState, launcherActivity = launcherActivity, homeViewModel = homeViewModel)
        }
    }

}

@Composable
fun HomeContent(screenState: MutableState<LauncherScreen>, launcherActivity: LauncherActivity, homeViewModel: HomeViewModel) {
    val dragObserver: DragObserver = object : DragObserver {
        override fun onDrag(dragDistance: Offset): Offset {
            if (dragDistance.x < 10f) {
                screenState.value = LauncherScreen.DRAWER
            }
            return super.onDrag(dragDistance)
        }
    }

    Box(modifier = Modifier.fillMaxSize()
            .dragGestureFilter(dragObserver = dragObserver, canDrag = { it == Direction.LEFT })) {

        Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight(1 / 2f).align(Alignment.Center)) {
            Column(modifier = Modifier.padding(horizontal = 30.dp)) {
                homeViewModel.favoriteApps.forEach {
                    Text(
                            text = it.appName,
                            style = MaterialTheme.typography.h4,
                            modifier = Modifier
                                    .clickable(
                                            onClick = {
                                                launcherActivity.startActivity(launcherActivity.packageManager.getLaunchIntentForPackage(it.packageName))
                                            })
                                    .padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerContent(screenState: MutableState<LauncherScreen>, launcherActivity: LauncherActivity, homeViewModel: HomeViewModel) {
    val dragObserver: DragObserver = object : DragObserver {
        override fun onDrag(dragDistance: Offset): Offset {
            if (dragDistance.x < 10f) {
                screenState.value = LauncherScreen.HOME
            }
            return super.onDrag(dragDistance)
        }
    }
    val appList by remember { mutableStateOf(mutableListOf<LauncherApplication>()) }
    fetchAppList(appList, launcherActivity)

    val context = ContextAmbient.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumnFor(items = appList, modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
                .dragGestureFilter(
                        dragObserver = dragObserver,
                        canDrag = {
                            it == Direction.RIGHT
                        }
                )) {
            ListItem(modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = {
                        launcherActivity.startActivity(launcherActivity.packageManager.getLaunchIntentForPackage(it.packageName))
                    })
                    .longPressGestureFilter { _: Offset ->
                        homeViewModel.addFavoriteApp(it)
                        Toast.makeText(context, "Favorite Added", Toast.LENGTH_SHORT).show()
                    }) {
                Text(text = it.appName)
            }
        }
    }
}

fun fetchAppList(appList: MutableList<LauncherApplication>, launcherActivity: LauncherActivity) {
    // Start from a clean adapter when refreshing the list
    appList.clear()

    // Query the package manager for all apps
    val activities = launcherActivity.packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0)

    // Sort the applications by alphabetical order and add them to the list
    Collections.sort(activities, ResolveInfo.DisplayNameComparator(launcherActivity.packageManager))
    for (resolver in activities) {

        // Exclude the settings app and this launcher from the list of apps shown
        val appName = resolver.loadLabel(launcherActivity.packageManager) as String
        if (appName == "Settings" || appName == "Minimalist Launcher") continue

        appList.add(LauncherApplication(appName = appName, packageName = resolver.activityInfo.packageName))
    }
}

data class LauncherApplication(val appName: String, val packageName: String)

enum class LauncherScreen {
    HOME, DRAWER
}

class HomeViewModel : ViewModel() {

    var favoriteApps: List<LauncherApplication> by mutableStateOf(listOf())

    //TODO Store in DB
    fun addFavoriteApp(app: LauncherApplication) {
        favoriteApps = favoriteApps + listOf(app)
    }

}