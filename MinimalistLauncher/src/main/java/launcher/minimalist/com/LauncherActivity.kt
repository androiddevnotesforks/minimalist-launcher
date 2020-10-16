package launcher.minimalist.com

import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.Transition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.CoreTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.gesture.Direction
import androidx.compose.ui.gesture.longPressGestureFilter
import androidx.compose.ui.gesture.scrollorientationlocking.Orientation
import androidx.compose.ui.gesture.tapGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.HapticFeedBackAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.math.roundToInt

@ExperimentalLayout
@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {
    private val homeViewModel: HomeViewModel by viewModels()

    @ExperimentalFoundationApi
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

@ExperimentalFoundationApi
@ExperimentalLayout
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
        LauncherScreen.SETTINGS -> {
            SettingsContent(screenState = screenState, launcherActivity = launcherActivity, homeViewModel = homeViewModel, appList = appList)
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalLayout
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
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                screenState.value = LauncherScreen.SETTINGS
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
            Column(modifier = Modifier.padding(horizontal = 30.dp, vertical = 20.dp)) {
                AndroidView(viewBlock = { context ->
                    return@AndroidView View.inflate(context, R.layout.text_clock, null)
                })

                val weatherData by homeViewModel.weatherData.observeAsState()
                weatherData?.let {
                    Text(
                            text = it.main.temp.roundToInt().toString() + "°F  " +
                                    "${it.weather[0].description.capitalize(Locale.getDefault())}",
                            color = Color.White, style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(top = 8.dp))
                }

            }
        }

        Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight(1 / 4f).align(Alignment.BottomCenter).padding(bottom = 10.dp), color = Color.Black) {
            Row(modifier = Modifier.align(Alignment.BottomCenter).wrapContentHeight(Alignment.Bottom)) {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp).wrapContentSize(), shape = RoundedCornerShape(30.dp), backgroundColor = Color.DarkGray) {

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Image(
                                asset = vectorResource(id = R.drawable.ic_duckduckgo),
                                modifier = Modifier.preferredSize(48.dp).align(Alignment.CenterVertically).padding(start = 10.dp))

                        var searchInput by remember { mutableStateOf(TextFieldValue("")) }
                        BaseTextField(
                                value = searchInput,
                                onValueChange = {
                                    searchInput = it
                                },
                                textColor = Color.White,
                                modifier = Modifier.fillMaxWidth().padding(20.dp).height(20.dp),
                                imeAction = ImeAction.Search,
                                onImeActionPerformed = {
                                    if (it == ImeAction.Search) {
                                        val searchIntent = Intent(Intent.ACTION_VIEW)
                                        searchIntent.data = Uri.parse("https://duckduckgo.com/q=" + searchInput.text)

                                        searchIntent.`package` = "com.duckduckgo.mobile.android"
                                        launcherActivity.startActivity(searchIntent)

                                        searchInput = TextFieldValue("")

                                    }
                                },
                        )
                    }
                }
            }
        }
    }
}

@ExperimentalLayout
@Composable
fun DrawerContent(screenState: MutableState<LauncherScreen>, launcherActivity: LauncherActivity, homeViewModel: HomeViewModel, appList: MutableList<LauncherApplication>) {
    val context = ContextAmbient.current
    val hapticFeedback = HapticFeedBackAmbient.current

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            LazyColumnFor(items = appList, modifier = Modifier.fillMaxWidth(7 / 8f)
                    .padding(start = 16.dp, end = 40.dp)
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

            Column(modifier = Modifier.fillMaxHeight().fillMaxWidth().padding(end = 8.dp), horizontalAlignment = Alignment.End) {
                val defaultFontSize = MaterialTheme.typography.subtitle2.fontSize
                var textSize by remember { mutableStateOf(defaultFontSize) }

                ('A'..'Z').toMutableList().forEach {
                    Text(
                            text = it.toString(),
                            style = MaterialTheme.typography.subtitle2,
                            fontSize = textSize,
                            color = Color.White,
                    )
                }
            }
        }
    }
}

@ExperimentalLayout
@Composable
fun SettingsContent(screenState: MutableState<LauncherScreen>, launcherActivity: LauncherActivity, homeViewModel: HomeViewModel, appList: MutableList<LauncherApplication>) {
    Scaffold(
            topBar = {
                TopAppBar(title = { Text(text = "Settings", color = Color.White) }, navigationIcon = {
                    IconButton(onClick = {
                        screenState.value = LauncherScreen.HOME
                    }) {
                        Image(asset = Icons.Default.Close, colorFilter = ColorFilter.tint(Color.White))
                    }
                }, backgroundColor = Color.DarkGray)
            },
            bodyContent = {
                Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
                    Text(
                            text = "Weather",
                            style = MaterialTheme.typography.h6,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 8.dp)

                    )

                    var weatherLocation by remember { mutableStateOf(TextFieldValue(homeViewModel.getZipCode())) }
                    OutlinedTextField(
                            value = weatherLocation,
                            onValueChange = {
                                weatherLocation = it
                            },
                            label = { Text(text = "Zipcode (i.e. 78015") },
                            modifier = Modifier.padding(vertical = 8.dp),
                            inactiveColor = Color.White,
                            activeColor = Color.White,
                            imeAction = ImeAction.Done,
                            onImeActionPerformed = { imeAction, softwareKeyboard ->
                                if (imeAction == ImeAction.Done) {
                                    homeViewModel.saveZipCode(weatherLocation.text)
                                    softwareKeyboard?.hideSoftwareKeyboard()
                                }
                            },
                            onTextInputStarted = { softwareKeyboardController ->
                                softwareKeyboardController.showSoftwareKeyboard()
                            }

                    )

                    Text(
                            text = "Theme",
                            style = MaterialTheme.typography.h6,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 8.dp)
                    )

                    FlowRow(crossAxisAlignment = FlowCrossAxisAlignment.Center) {

                    }
                }
            }

    )

}

@ExperimentalLayout
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
    HOME, DRAWER, SETTINGS
}