package launcher.minimalist.com

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import launcher.minimalist.com.theme.*
import java.time.LocalDate
import java.util.*
import kotlin.math.roundToInt

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {
    private val homeViewModel: HomeViewModel by viewModels()

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupPermissions(this)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContent {
            fetchAppList(this@LauncherActivity, homeViewModel)
            val screenState = remember { mutableStateOf(LauncherScreen.HOME) }
            val colorPalette =
                remember { mutableStateOf(listOfColorPalette.find { it.themeName == homeViewModel.getTheme() }!!.themeColors) }
//            val typography = remember { mutableStateOf(listOfTypes[0].typography) }

            val systemUiController = remember { SystemUiController(window) }
            CompositionLocalProvider(SystemUiControllerAmbient provides systemUiController) {
                MinimalLauncherTheme(colors = colorPalette.value) {
                    val sysUiController = SystemUiControllerAmbient.current
                    sysUiController.setSystemBarsColor(
                        color = MaterialTheme.colors.primary
                    )

                    Scaffold(
                        content = {
                            ShowBodyContent(
                                modifier = Modifier.padding(it),
                                screenState = screenState,
                                launcherActivity = this@LauncherActivity,
                                homeViewModel = homeViewModel,
                                colorPalette = colorPalette
                            )
                        }
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                } else {
                }
            }
        }
    }

}

private fun setupPermissions(activity: Activity) {
    val permission = ContextCompat.checkSelfPermission(
        activity,
        Manifest.permission.RECORD_AUDIO
    )

    if (permission != PackageManager.PERMISSION_GRANTED) {
        makeRequest(activity = activity)
    }
}

private fun makeRequest(activity: Activity) {
    ActivityCompat.requestPermissions(
        activity,
        arrayOf(Manifest.permission.RECORD_AUDIO),
        101
    )
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
private fun ShowBodyContent(
    screenState: MutableState<LauncherScreen>,
    launcherActivity: LauncherActivity,
    homeViewModel: HomeViewModel,
    colorPalette: MutableState<Colors>,
    modifier: Modifier
) {
    val appList by homeViewModel.launcherApplications.observeAsState()

    appList?.let {
        when (screenState.value) {
            LauncherScreen.HOME -> {
                HomeContent(
                    modifier = modifier,
                    screenState = screenState,
                    launcherActivity = launcherActivity,
                    homeViewModel = homeViewModel,
                    appList = appList!!,
                )
//            }
            }
            LauncherScreen.DRAWER -> {
                DrawerContent(
                    screenState = screenState,
                    launcherActivity = launcherActivity,
                    homeViewModel = homeViewModel,
                    appList = appList!!
                )
            }
            LauncherScreen.SETTINGS -> {


            }
        }
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun HomeContent(
    screenState: MutableState<LauncherScreen>,
    launcherActivity: LauncherActivity,
    homeViewModel: HomeViewModel,
    appList: MutableList<LauncherApplication>,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    var showSettings by remember { mutableStateOf(false) }

    Box() {

        Column(modifier = Modifier.fillMaxSize(), content = {
            Box(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { _: Offset ->
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        showSettings = true
//                    screenState.value = LauncherScreen.SETTINGS
                    })
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(onHorizontalDrag = { _, _ ->
                        screenState.value = LauncherScreen.DRAWER
                    })
                }
            ) {

                SetBackground()

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(1 / 2f)
                        .align(Alignment.Center),
                    color = Color.Transparent
                ) {
                    Column(modifier = Modifier.padding(horizontal = 30.dp)) {
                        if (homeViewModel.favoriteApps.isEmpty()) {
                            Text(
                                text = "Swipe right and long press\non 7 apps! \n\n\n -->",
                                style = MaterialTheme.typography.h5,
                                color = Color.White
                            )
                        } else {
                            homeViewModel.favoriteApps.forEach { appName ->
                                Text(
                                    text = appName,
                                    style = MaterialTheme.typography.h5,
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(vertical = 16.dp)
                                        .pointerInput(Unit) {
                                            detectTapGestures(onLongPress = { _: Offset ->
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.LongPress
                                                )
                                                homeViewModel.removeFavorite(appName)
                                            }, onTap = { offset ->
                                                val app = appList.find { it.appName == appName }
                                                launcherActivity.startActivity(
                                                    launcherActivity.packageManager.getLaunchIntentForPackage(
                                                        app!!.packageName
                                                    )
                                                )
                                            })
                                        }
                                )
                            }
                        }

                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(1 / 4f)
                        .align(Alignment.TopCenter)
                        .padding(top = 40.dp),
                    color = Color.Transparent
                ) {
                    Column(modifier = Modifier.padding(horizontal = 30.dp, vertical = 20.dp)) {
                        AndroidView(factory = { context ->
                            return@AndroidView View.inflate(context, R.layout.text_clock, null)
                        })

                        val weatherData by homeViewModel.weatherData.observeAsState()
                        weatherData?.let {
                            Text(
                                text = it.main.temp.roundToInt().toString() + "°F  " +
                                        "${it.weather[0].description.capitalize(Locale.getDefault())}",
                                color = Color.White, style = MaterialTheme.typography.h6,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Text(
                            text = "${
                                LocalDate.now().dayOfWeek.name.toLowerCase().capitalize()
                            }, " +
                                    "${
                                        LocalDate.now().month.name.toLowerCase().capitalize()
                                    } " +
                                    "${LocalDate.now().dayOfMonth}",
                            color = Color.White, style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(1 / 4f)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 30.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .wrapContentHeight(Alignment.Bottom)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp)
                                .wrapContentSize(),
                            shape = RoundedCornerShape(30.dp),
                            backgroundColor = Color.DarkGray
                        ) {

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_duckduckgo),
                                    contentDescription = "Duck Duck Go Icon",
                                    modifier = Modifier
                                        .requiredSize(48.dp)
                                        .align(Alignment.CenterVertically)
                                        .padding(start = 10.dp)
                                )

                                var searchInput by remember { mutableStateOf(TextFieldValue("")) }

                                BasicTextField(
                                    value = searchInput,
                                    onValueChange = {
                                        searchInput = it
                                    },
                                    textStyle = TextStyle(color = Color.White),
                                    cursorBrush = SolidColor(Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                        .height(20.dp),
//                                    onTextInputStarted = { keyboardController ->
//                                        keyboardController.showSoftwareKeyboard()
//                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onDone = {
                                        val searchIntent = Intent(Intent.ACTION_VIEW)
                                        searchIntent.data =
                                            Uri.parse("https://duckduckgo.com/q=" + searchInput.text)

                                        searchIntent.`package` = "com.duckduckgo.mobile.android"
                                        launcherActivity.startActivity(searchIntent)

                                        searchInput = TextFieldValue("")
                                    })

                                )
                            }
                        }
                    }
                }
            }
        })

        val hideSettings = { showSettings = !showSettings }
        AnimatedVisibility(
            visible = showSettings,
            enter = slideInVertically(
                initialOffsetY = { 100 }
            ) + fadeIn(initialAlpha = 0.3f),
            exit = slideOutVertically(targetOffsetY = {100}) + fadeOut(targetAlpha = 0.3f)
        ) {
            SettingsContent(
                screenState = screenState,
                launcherActivity = launcherActivity,
                homeViewModel = homeViewModel,
                appList = appList!!,
                hideSettings = hideSettings
            )
        }
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun DrawerContent(
    screenState: MutableState<LauncherScreen>,
    launcherActivity: LauncherActivity,
    homeViewModel: HomeViewModel,
    appList: MutableList<LauncherApplication>
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val scrollState = rememberLazyListState(0)

    var appListToShow by remember { mutableStateOf(appList) }

    val gray = Color(0xB3000000)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary)
    ) {

        SetBackground()

        Column(
            Modifier
                .fillMaxSize()
                .background(color = Color(0x4DA9A9A9))
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 60.dp, bottom = 16.dp)
                        .wrapContentSize(),
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(2.dp, Color.White)
                ) {

                    Row(modifier = Modifier.fillMaxWidth()) {
                        var searchInput by remember { mutableStateOf(TextFieldValue("")) }
                        BasicTextField(
                            value = searchInput,
                            onValueChange = {
                                searchInput = it
                                if (searchInput.text != "") {
                                    Log.d("TAG", "FILTERING")
                                    val filteredAppList = appList.filter { launcherApplication ->
                                        launcherApplication.appName.contains(
                                            searchInput.text,
                                            true
                                        )
                                    }.toMutableList()
                                    appListToShow = filteredAppList
                                } else {
                                    appListToShow = appList
                                }
                                homeViewModel.filterAppBySearch(it.text)
                            },
                            textStyle = TextStyle(color = Color.White),
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                                .height(20.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        )
                    }
                }
            }
            val showLauncherIcons by remember { mutableStateOf(homeViewModel.getShowLauncherIcons()) }
            val filterAppList by homeViewModel.filteredLauncherApplications.observeAsState()

            Log.d("", "DrawerContent: ${filterAppList!!.size}")
            Row(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(7 / 8f)
                        .padding(start = 16.dp, end = 40.dp)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                Log.d("TAG", "DrawerContent: change - $change")
                                Log.d("TAG", "DrawerContent: amount - $dragAmount")

                                screenState.value = LauncherScreen.HOME
                            }

//                            detectVerticalDragGestures { change, dragAmount ->
//                                if(scrollState.firstVisibleItemIndex == 0){
//                                } else {
//
//                                }
//                            }
                        }, state = scrollState
                ) {
                    items(
                        items = appListToShow,
                        itemContent = {
                            ListItem(modifier = Modifier
                                .height(50.dp)
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectTapGestures(onLongPress = { _: Offset ->
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (appList.size == 6) {
                                            Toast
                                                .makeText(
                                                    context,
                                                    "Only 6 Apps Allowed",
                                                    Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        } else {
                                            homeViewModel.addFavoriteApp(it)
                                            Toast
                                                .makeText(
                                                    context,
                                                    "Favorite Added",
                                                    Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        }
                                    }, onTap = { offset ->
                                        launcherActivity.startActivity(
                                            launcherActivity.packageManager.getLaunchIntentForPackage(
                                                it.packageName
                                            )
                                        )
                                    })
                                }) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    var horizontalPadding = 0.dp
                                    if (showLauncherIcons) {
                                        horizontalPadding = 8.dp
                                        AndroidView(factory = { context ->
                                            val imageView = ImageView(context)
                                            imageView.setImageDrawable(it.launcherIconRes)
                                            return@AndroidView imageView
                                        }, modifier = Modifier.size(36.dp))
                                    }
                                    Text(
                                        text = it.appName, color = Color.White, modifier = Modifier
                                            .padding(horizontal = horizontalPadding)
                                            .align(Alignment.CenterVertically)
                                    )
                                }
                            }
                        }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(7 / 8f)
                        .padding(end = 8.dp), horizontalAlignment = Alignment.End
                ) {
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
                                modifier = Modifier
                                    .pointerInput(Unit) {
                                        detectTapGestures(onTap = {
                                            val appToScrollTo =
                                                getFirstAppFromChar(c.toString(), appList)
                                            val indexToScrollTo = appList.indexOf(appToScrollTo)
                                            selectedIndex = index
                                            CoroutineScope(Dispatchers.Main).launch {
                                                scrollState.scrollToItem(indexToScrollTo, 0)
                                            }
                                        })
                                    }
                                    .padding(vertical = 2.dp, horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetBackground() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        AndroidView(factory = { context ->
            val view = View.inflate(context, R.layout.background, null)
            val background = view.findViewById<AppCompatImageView>(R.id.background)

            val wallpaperManager = WallpaperManager.getInstance(context)

            background.background = wallpaperManager.fastDrawable

            return@AndroidView view
        })
    }
}

fun getFirstAppFromChar(
    char: String,
    appList: MutableList<LauncherApplication>
): LauncherApplication? = appList.find { it.appName.startsWith(char) }

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun SettingsContent(
    screenState: MutableState<LauncherScreen>,
    launcherActivity: LauncherActivity,
    homeViewModel: HomeViewModel,
    appList: MutableList<LauncherApplication>,
    hideSettings: () -> Unit,
) {

//    BackHandler(onBack = {
//        screenState.value = LauncherScreen.HOME
//    })
//
//    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current.onBackPressedDispatcher

    Scaffold(
        modifier = Modifier.padding(top = 56.dp),
        topBar = {
            TopAppBar(title = { Text(text = "Settings", color = Color.White) }, navigationIcon = {
                IconButton(onClick = hideSettings) {
                    Image(
                        imageVector = Icons.Default.Close,
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }, backgroundColor = MaterialTheme.colors.primary)
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp)
//                    .clickable { backPressedDispatcher.onBackPressed() }
            ) {
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        homeViewModel.saveZipCode(weatherLocation.text)
                    }),
                )

                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.h6,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

//                Text(
//                    text = "Background",
//                    style = MaterialTheme.typography.body1,
//                    color = Color.White,
//                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
//                )

//                FlowRow(crossAxisAlignment = FlowCrossAxisAlignment.Center) {
//                    listOfColorPalette.forEach {
//                        Box(
//                            modifier = Modifier
//                                .preferredSize(80.dp)
//                                .padding(10.dp)
//                                .background(color = it.themeColors.primary)
//                                .clickable(onClick = {
//                                    colorPalette.value = it.themeColors
//                                    screenState.value = LauncherScreen.HOME
//                                    homeViewModel.saveTheme(it.themeName)
//                                })
//                        )
//                    }
//                }

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

//                    Text(
//                            text = "Font",
//                            style = MaterialTheme.typography.h6,
//                            color = Color.White,
//                            modifier = Modifier.padding(vertical = 8.dp)
//                    )

//                    listOfTypes.forEach {
//                        Text(
//                                text = it.typeName,
//                                style = it.typography.body1,
//                                color = Color.White,
//                                modifier = Modifier
//                                        .padding(vertical = 8.dp, horizontal = 8.dp)
//                                        .clickable(onClick = {
//                                            typography.value = it.typography
//                                        })
//                        )
//                    }

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
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                            .align(Alignment.CenterVertically)
                    )
                }


            }
        }

    )

}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
fun fetchAppList(launcherActivity: LauncherActivity, homeViewModel: HomeViewModel) {
    val appList: MutableList<LauncherApplication> = mutableListOf()
    // Start from a clean adapter when refreshing the list
    appList.clear()

    // Query the package manager for all apps
    val activities = launcherActivity.packageManager.queryIntentActivities(
        Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0
    )

//    val activities = launcherActivity.packageManager.getInstalledPackages(0)
//
//    val sortedActivities = activities.sortBy { it.applicationInfo.}

    // Sort the applications by alphabetical order and add them to the list
    Collections.sort(activities, ResolveInfo.DisplayNameComparator(launcherActivity.packageManager))
    for (resolver in activities) {

        // Exclude the settings app and this launcher from the list of apps shown
        val appName = resolver.loadLabel(launcherActivity.packageManager) as String
        if (appName == "Settings" || appName == "Minimalist Launcher") continue

        appList.add(
            LauncherApplication(
                appName = appName,
                packageName = resolver.activityInfo.packageName,
                launcherIconRes = resolver.loadIcon(launcherActivity.packageManager)
            )
        )
    }

    homeViewModel.storeLauncherApplications(appList)
}

enum class LauncherScreen {
    HOME, DRAWER, SETTINGS
}