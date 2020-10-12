package launcher.minimalist.com

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.Transition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.draggable
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
import androidx.compose.ui.gesture.longPressGestureFilter
import androidx.compose.ui.gesture.scrollorientationlocking.Orientation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.HapticFeedBackAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val screenState = remember { mutableStateOf(LauncherScreen.HOME) }
            Scaffold(
                    bodyContent = {
                        ShowBodyContent(screenState = screenState, launcherActivity = this@LauncherActivity, homeViewModel = homeViewModel)
                    }
            )
        }
    }
}

@Composable
private fun ShowBodyContent(screenState: MutableState<LauncherScreen>, launcherActivity: LauncherActivity, homeViewModel: HomeViewModel) {
    val appList by remember { mutableStateOf(mutableListOf<LauncherApplication>()) }
    fetchAppList(appList, launcherActivity)

    when (screenState.value) {
        LauncherScreen.HOME -> {
//            Crossfade(current = screenState.value, animation = tween(1000)) {
                HomeContent(screenState = screenState, launcherActivity = launcherActivity, homeViewModel = homeViewModel, appList = appList)
//            }
        }
        LauncherScreen.DRAWER -> {
//            Crossfade(current = screenState.value, animation = tween(1000)) {
            DrawerContent(screenState = screenState, launcherActivity = launcherActivity, homeViewModel = homeViewModel, appList = appList)
//            }
        }
    }
}

@Composable
fun HomeContent(screenState: MutableState<LauncherScreen>, launcherActivity: LauncherActivity, homeViewModel: HomeViewModel, appList: MutableList<LauncherApplication>) {
    val context = ContextAmbient.current
    val hapticFeedback = HapticFeedBackAmbient.current

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)
            .draggable(
                    orientation = Orientation.Horizontal,
                    canDrag = { it == Direction.LEFT },
                    onDrag = {
                        screenState.value = LauncherScreen.DRAWER
                    }
            ).longPressGestureFilter { _: Offset ->
                Toast.makeText(context, "Launch Settings", Toast.LENGTH_SHORT).show()
            }) {


        Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight(1 / 2f).align(Alignment.Center), color = Color.Black) {
            Column(modifier = Modifier.padding(horizontal = 30.dp)) {
                if (homeViewModel.favoriteApps.isEmpty()) {
                    Text(text = "Swipe right and long press\non 5 apps! \n\n\n -->", style = MaterialTheme.typography.h6, color = Color.White)
                } else {
                    homeViewModel.favoriteApps.forEach { appName ->
                        Text(
                                text = appName,
                                style = MaterialTheme.typography.h4,
                                color = Color.White,
                                modifier = Modifier
                                        .clickable(
                                                onClick = {
                                                    val app = appList.find { it.appName == appName }
                                                    launcherActivity.startActivity(launcherActivity.packageManager.getLaunchIntentForPackage(app!!.packageName))
                                                })
                                        .padding(vertical = 16.dp)
                                        .longPressGestureFilter { _: Offset ->
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            homeViewModel.removeFavorite(appName)
                                        }
                        )
                    }
                }

            }
        }

        Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight(1 / 4f).align(Alignment.TopCenter), color = Color.Black) {
            AndroidView(viewBlock = { context ->
                return@AndroidView View.inflate(context, R.layout.text_clock, null)
            }, modifier = Modifier.padding(horizontal = 30.dp, vertical = 20.dp))
        }

        Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight(1 / 4f).align(Alignment.BottomCenter), color = Color.Black) {

        }
    }
}

@Composable
fun DrawerContent(screenState: MutableState<LauncherScreen>, launcherActivity: LauncherActivity, homeViewModel: HomeViewModel, appList: MutableList<LauncherApplication>) {
    val context = ContextAmbient.current
    val hapticFeedback = HapticFeedBackAmbient.current

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        LazyColumnFor(items = appList, modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
                .draggable(
                        orientation = Orientation.Horizontal,
                        canDrag = { it == Direction.RIGHT },
                        onDrag = {
                            screenState.value = LauncherScreen.HOME
                        }
                )
        ) {
            ListItem(modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = {
                        launcherActivity.startActivity(launcherActivity.packageManager.getLaunchIntentForPackage(it.packageName))
                    })
                    .longPressGestureFilter { _: Offset ->
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (appList.size == 5) {
                            Toast.makeText(context, "Only 5 Apps Allowed", Toast.LENGTH_SHORT).show()
                        } else {
                            homeViewModel.addFavoriteApp(it)
                            Toast.makeText(context, "Favorite Added", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                Text(text = it.appName, color = Color.White)
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

enum class LauncherScreen {
    HOME, DRAWER
}