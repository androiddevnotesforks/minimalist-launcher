package launcher.minimalist.com.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.font
import androidx.compose.ui.text.font.fontFamily
import launcher.minimalist.com.R

private val BillionDreamsFontFamily = fontFamily(
    font(R.font.billion_dreams)
)

val BillionDreamsTypography = Typography(
        defaultFontFamily = BillionDreamsFontFamily,
)

private val StayHappyFontFamily = fontFamily(
        font(R.font.stay_happy)
)

val StayHappyTypography = Typography(
        defaultFontFamily = StayHappyFontFamily,
)

private val LittleZombieFontFamily = fontFamily(
        font(R.font.little_zombie)
)

val LittleZombieTypography = Typography(
        defaultFontFamily = LittleZombieFontFamily,
)


val DefaultTypography = Typography(
        defaultFontFamily = FontFamily.Default,
)

val listOfTypes = listOf(
        Type("Default", DefaultTypography),
        Type("Billion Dreams", BillionDreamsTypography),
        Type("Stay Happy", StayHappyTypography),
        Type("Little Zombie", LittleZombieTypography)
)

data class Type(val typeName:String,val typography: Typography)


