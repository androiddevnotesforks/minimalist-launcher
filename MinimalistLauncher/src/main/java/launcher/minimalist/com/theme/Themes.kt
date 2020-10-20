package launcher.minimalist.com.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightBlue = Color(0xFF059EDC)
private val LightVariantBlue = Color(0xFF459EDC)
private val SecondaryBlue = Color(0xFF459EDC)
private val Blue800 = Color(0xFF001CCF)

private val Red300 = Color(0xFFEA6D7E)
private val Red800 = Color(0xFFD00036)

private val SkyBlue = Color(0xFF059EDC)

val MinimalLauncherLightBlueDarkPalette = darkColors(
        primary = LightBlue,
        primaryVariant = LightVariantBlue,
        onPrimary = Color.Black,
        secondary = SecondaryBlue,
        onSecondary = Color.Black,
        onSurface = Color.White,
        onBackground = Color.White,
        error = Red300,
        onError = Color.Black
)

val MinimalLauncherLightBlackDarkPalette = darkColors(
        primary = Color.Black,
        primaryVariant = LightVariantBlue,
        onPrimary = Color.Black,
        secondary = SecondaryBlue,
        onSecondary = Color.Black,
        onSurface = Color.White,
        onBackground = Color.White,
        error = Red300,
        onError = Color.Black
)

val MinimalLauncherRedDarkPalette = darkColors(
        primary = Red300,
        primaryVariant = Red800,
        onPrimary = Color.Black,
        secondary = SecondaryBlue,
        onSecondary = Color.Black,
        onSurface = Color.White,
        onBackground = Color.White,
        error = Red300,
        onError = Color.Black
)

val MinimalLauncherGreenDarkPalette = darkColors(
        primary = Color.Green,
        primaryVariant = Red800,
        onPrimary = Color.Black,
        secondary = SecondaryBlue,
        onSecondary = Color.Black,
        onSurface = Color.White,
        onBackground = Color.White,
        error = Red300,
        onError = Color.Black
)

val MinimalLauncherCyanDarkPalette = darkColors(
        primary = Color.Cyan,
        primaryVariant = Red800,
        onPrimary = Color.Black,
        secondary = SecondaryBlue,
        onSecondary = Color.Black,
        onSurface = Color.White,
        onBackground = Color.White,
        error = Red300,
        onError = Color.Black
)

val MinimalLauncherBlueDarkPalette = darkColors(
        primary = Color.Blue,
        primaryVariant = Red800,
        onPrimary = Color.Black,
        secondary = SecondaryBlue,
        onSecondary = Color.Black,
        onSurface = Color.White,
        onBackground = Color.White,
        error = Red300,
        onError = Color.Black
)

val MinimalLauncherMagentaDarkPalette = darkColors(
        primary = Color.Magenta,
        primaryVariant = Red800,
        onPrimary = Color.Black,
        secondary = SecondaryBlue,
        onSecondary = Color.Black,
        onSurface = Color.White,
        onBackground = Color.White,
        error = Red300,
        onError = Color.Black
)

val listOfColorPalette = listOf(MinimalLauncherBlueDarkPalette, MinimalLauncherCyanDarkPalette,
        MinimalLauncherGreenDarkPalette, MinimalLauncherLightBlueDarkPalette,
        MinimalLauncherMagentaDarkPalette, MinimalLauncherRedDarkPalette, MinimalLauncherLightBlackDarkPalette)

private val MinimalLauncherLightPalette = lightColors(
        primary = LightBlue,
        primaryVariant = LightVariantBlue,
        onPrimary = Color.White,
        secondary = SecondaryBlue,
        secondaryVariant = SecondaryBlue,
        onSecondary = Color.Black,
        onSurface = Color.Black,
        onBackground = Color.Black,
        error = Red800,
        onError = Color.White
)

@Composable
fun MinimalLauncherTheme(
        isDarkTheme: Boolean = isSystemInDarkTheme(),
        colors: Colors? = MinimalLauncherBlueDarkPalette,
        content: @Composable () -> Unit
) {

//    val myColors =
//        colors ?: if (isDarkTheme) MinimalLauncherDarkPalette else MinimalLauncherLightPalette

    MaterialTheme(
            colors = colors!!,
            content = content,
            shapes = JetchatShapes,
    )
}
