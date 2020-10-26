package launcher.minimalist.com

import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.HapticFeedBackAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.loadVectorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpCubed
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import dagger.hilt.android.AndroidEntryPoint
import launcher.minimalist.com.theme.*
import java.time.LocalDate
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
            fetchAppList(this@LauncherActivity, homeViewModel)
            val screenState = remember { mutableStateOf(LauncherScreen.HOME) }
            val colorPalette = remember { mutableStateOf(listOfColorPalette.find { it.themeName == homeViewModel.getTheme() }!!.themeColors) }
            val typography = remember { mutableStateOf(listOfTypes[0].typography) }

            val systemUiController = remember { SystemUiController(window) }
            Providers(SystemUiControllerAmbient provides systemUiController) {
                MinimalLauncherTheme(colors = colorPalette.value, type = typography.value) {
                    val sysUiController = SystemUiControllerAmbient.current
                    sysUiController.setSystemBarsColor(
                            color = MaterialTheme.colors.primary
                    )

                    Scaffold(
                            bodyContent = {
                                ShowBodyContent(screenState = screenState, launcherActivity = this@LauncherActivity, homeViewModel = homeViewModel, colorPalette, typography)
                            }
                    )
                }
            }
        }
    }

    @ExperimentalFoundationApi
    override fun onBackPressed() {
        super.onBackPressed()

        onCreate(null)
    }
}

@ExperimentalFoundationApi
@ExperimentalLayout
@Composable
private fun ShowBodyContent(screenState: MutableState<LauncherScreen>, launcherActivity: LauncherActivity, homeViewModel: HomeViewModel, colorPalette: MutableState<Colors>, typography: MutableState<Typography>) {
    val appList by homeViewModel.launcherApplications.observeAsState()

    appList?.let {
        when (screenState.value) {
            LauncherScreen.HOME -> {
//            Crossfade(current = screenState.value, animation = tween(1000)) {
                HomeContent(screenState = screenState, launcherActivity = launcherActivity, homeViewModel = homeViewModel, appList = appList!!)
//            }
            }
            LauncherScreen.DRAWER -> {
//            Crossfade(current = screenState.value, animation = tween(1000)) {
                DrawerContent(screenState = screenState, launcherActivity = launcherActivity, homeViewModel = homeViewModel, appList = appList!!)
//            }
            }
            LauncherScreen.SETTINGS -> {
                SettingsContent(screenState = screenState, launcherActivity = launcherActivity, homeViewModel = homeViewModel, appList = appList!!, colorPalette, typography)
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalLayout
@Composable
fun HomeContent(screenState: MutableState<LauncherScreen>, launcherActivity: LauncherActivity, homeViewModel: HomeViewModel, appList: MutableList<LauncherApplication>) {
    val context = ContextAmbient.current
    val hapticFeedback = HapticFeedBackAmbient.current

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.primary)
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


        Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight(1 / 2f).align(Alignment.Center), color = MaterialTheme.colors.primary) {
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

        Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight(1 / 4f).align(Alignment.TopCenter), color = MaterialTheme.colors.primary) {
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

                Text(
                        text = "${LocalDate.now().dayOfWeek.name.toLowerCase().capitalize()}, " +
                                "${LocalDate.now().month.name.toLowerCase().capitalize()} " +
                                "${LocalDate.now().dayOfMonth}",
                        color = Color.White, style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(top = 8.dp))
            }
        }

        Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight(1 / 4f).align(Alignment.BottomCenter).padding(bottom = 10.dp), color = MaterialTheme.colors.primary) {
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
                                cursorColor = Color.White,
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

@ExperimentalFoundationApi
@ExperimentalLayout
@Composable
fun DrawerContent(screenState: MutableState<LauncherScreen>, launcherActivity: LauncherActivity, homeViewModel: HomeViewModel, appList: MutableList<LauncherApplication>) {
    val context = ContextAmbient.current
    val hapticFeedback = HapticFeedBackAmbient.current
    val scrollState = rememberScrollState(0f)


    val gray = Color(0xB3000000)

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.primary)) {
        Column(Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 16.dp).wrapContentSize(), shape = RoundedCornerShape(30.dp), backgroundColor = gray) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        var searchInput by remember { mutableStateOf(TextFieldValue("Search apps")) }
                        BaseTextField(
                                value = searchInput,
                                onValueChange = {
                                    searchInput = it
                                    homeViewModel.filterAppBySearch(it.text)
                                },
                                textColor = Color.White,
                                cursorColor = Color.White,
                                modifier = Modifier.fillMaxWidth().padding(20.dp).height(20.dp),
                                imeAction = ImeAction.Search,
                                onImeActionPerformed = {

                                },
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                ScrollableColumn(modifier = Modifier
                        .fillMaxWidth(7 / 8f)
                        .padding(start = 16.dp, end = 40.dp)
                        .draggable(
                                orientation = Orientation.Horizontal,
                                canDrag = { it == Direction.RIGHT },
                                onDrag = {
                                    screenState.value = LauncherScreen.HOME
                                }
                        ), scrollState = scrollState) {

                    val showLauncherIcons by remember { mutableStateOf(homeViewModel.getShowLauncherIcons()) }
                    appList.forEach { app ->
                        ListItem(modifier = Modifier
                                .height(50.dp)
                                .fillMaxWidth()
                                .clickable(onClick = {
                                    launcherActivity.startActivity(launcherActivity.packageManager.getLaunchIntentForPackage(app.packageName))
                                })
                                .longPressGestureFilter { _: Offset ->
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (appList.size == 5) {
                                        Toast.makeText(context, "Only 5 Apps Allowed", Toast.LENGTH_SHORT).show()
                                    } else {
                                        homeViewModel.addFavoriteApp(app)
                                        Toast.makeText(context, "Favorite Added", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                var horizontalPadding = 0.dp
                                if (showLauncherIcons) {
                                    horizontalPadding = 8.dp
                                    AndroidView(viewBlock = { context ->
                                        val imageView = ImageView(context)
                                        imageView.setImageDrawable(app.launcherIconRes)
                                        return@AndroidView imageView
                                    }, modifier = Modifier.size(36.dp))
                                }
                                Text(text = app.appName, color = Color.White, modifier = Modifier.padding(horizontal = horizontalPadding).align(Alignment.CenterVertically))
                            }
                        }
                    }

                    Log.d("TAG", "DrawerContent: ${scrollState.maxValue}")

                }

                Column(modifier = Modifier.fillMaxHeight().fillMaxWidth(7 / 8f).padding(end = 8.dp), horizontalAlignment = Alignment.End) {
                    val defaultFontSize = MaterialTheme.typography.subtitle2.fontSize
                    var selectedIndex by remember { mutableStateOf(-1) }

                    val alphabet = ('A'..'Z').toMutableList()
                    Column(verticalArrangement = Arrangement.Center) {
                        alphabet.forEachIndexed { index, c ->
                            Text(
                                    text = c.toString(),
                                    style = if (index == selectedIndex) {
                                        MaterialTheme.typography.h6
                                    } else {
                                        MaterialTheme.typography.body1
                                    },
                                    fontWeight = if (index == selectedIndex) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                    color = Color.White,
                                    modifier = Modifier.clickable(onClick = {
                                        val appToScrollTo = getFirstAppFromChar(c.toString(), appList)
                                        val indexToScrollTo = appList.indexOf(appToScrollTo)
                                        selectedIndex = index
                                        val scrollHeightPerRow = scrollState.maxValue.div(appList.size)
                                        scrollState.smoothScrollTo((scrollHeightPerRow.times(indexToScrollTo)))
                                    }).padding(vertical = 2.dp, horizontal = 8.dp)

                            )
                        }
                    }
                }
            }
        }
    }
}

fun getFirstAppFromChar(char: String, appList: MutableList<LauncherApplication>): LauncherApplication? = appList.find { it.appName.startsWith(char) }

@ExperimentalLayout
@Composable
fun SettingsContent(screenState: MutableState<LauncherScreen>, launcherActivity: LauncherActivity, homeViewModel: HomeViewModel, appList: MutableList<LauncherApplication>, colorPalette: MutableState<Colors>, typography: MutableState<Typography>) {
    Scaffold(
            topBar = {
                TopAppBar(title = { Text(text = "Settings", color = Color.White) }, navigationIcon = {
                    IconButton(onClick = {
                        screenState.value = LauncherScreen.HOME
                    }) {
                        Image(asset = Icons.Default.Close, colorFilter = ColorFilter.tint(Color.White))
                    }
                }, backgroundColor = MaterialTheme.colors.primary)
            },
            bodyContent = {
                Column(modifier = Modifier.fillMaxSize().background(Color.DarkGray).padding(16.dp)) {
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

                    Text(
                            text = "Background",
                            style = MaterialTheme.typography.body1,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                    )

                    FlowRow(crossAxisAlignment = FlowCrossAxisAlignment.Center) {
                        listOfColorPalette.forEach {
                            Box(modifier = Modifier.preferredSize(80.dp).padding(10.dp).background(color = it.themeColors.primary).clickable(onClick = {
                                colorPalette.value = it.themeColors
                                screenState.value = LauncherScreen.HOME
                                homeViewModel.saveTheme(it.themeName)
                            }))
                        }
                    }

//                    Text(
//                            text = "Text Color",
//                            style = MaterialTheme.typography.h6,
//                            color = Color.White,
//                            modifier = Modifier.padding(vertical = 8.dp)
//                    )

//                    FlowRow(crossAxisAlignment = FlowCrossAxisAlignment.Center) {
//                        listOfColorPalette.forEach {
//                            Box(modifier = Modifier.preferredSize(80.dp).padding(10.dp).background(color = it.primary).clickable(onClick = {
//                                colorPalette.value = it
//                                screenState.value = LauncherScreen.HOME
//                            }))
//                        }
//                    }

                    Text(
                            text = "Font",
                            style = MaterialTheme.typography.h6,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 8.dp)
                    )

                    listOfTypes.forEach {
                        Text(
                                text = it.typeName,
                                style = it.typography.body1,
                                color = Color.White,
                                modifier = Modifier
                                        .padding(vertical = 8.dp, horizontal = 8.dp)
                                        .clickable(onClick = {
                                            typography.value = it.typography
                                        })
                        )
                    }

                    var showLauncherIcons by remember { mutableStateOf(homeViewModel.getShowLauncherIcons()) }

                    Row(modifier = Modifier.padding(vertical = 8.dp)) {
                        Switch(checked = showLauncherIcons, onCheckedChange = {
                            showLauncherIcons = it
                            homeViewModel.saveShowLauncherIcons(showLauncherIcons)
                        })

                        Text(
                                text = "Show App Icons",
                                style = MaterialTheme.typography.body1,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp).align(Alignment.CenterVertically)
                        )
                    }


                }
            }

    )

}

@ExperimentalLayout
fun fetchAppList(launcherActivity: LauncherActivity, homeViewModel: HomeViewModel) {
    val appList: MutableList<LauncherApplication> = mutableListOf()
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

        appList.add(LauncherApplication(appName = appName, packageName = resolver.activityInfo.packageName, launcherIconRes = resolver.loadIcon(launcherActivity.packageManager)))
    }

    homeViewModel.storeLauncherApplications(appList)
}

enum class LauncherScreen {
    HOME, DRAWER, SETTINGS
}