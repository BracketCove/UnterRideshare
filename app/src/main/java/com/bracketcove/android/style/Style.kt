package com.bracketcove.android.style

import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.bracketcove.android.R


val color_primary = Color(0xFF00BCD4)
val color_black = Color(0xFF000000)
val color_white = Color(0xFFFFFFFF)
val color_light_grey = Color(0xFF616161)

private val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_light, FontWeight.Light),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semi_bold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

val defaultTextStyle = TextStyle(
    fontFamily = Poppins,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

val typography = Typography(
    h1 = defaultTextStyle.copy(
        fontSize = 64.sp,
        fontWeight = FontWeight.Normal,
        color = color_black
    ),
    h2 = defaultTextStyle.copy(
        fontSize = 36.sp,
        fontWeight = FontWeight.Normal,
        color = color_black
    ),
    h3 = defaultTextStyle.copy(
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        color = color_black
    ),
    subtitle1 = defaultTextStyle.copy(
        fontSize = 24.sp,
        fontWeight = FontWeight.Normal,
        color = color_black
    ),
    subtitle2 = defaultTextStyle.copy(
        fontSize = 18.sp,
        fontWeight = FontWeight.Light,
        color = color_black
    ),
    body1 = defaultTextStyle.copy(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        color = color_black
    ),
    body2 = defaultTextStyle.copy(
        fontSize = 16.sp,
        fontWeight = FontWeight.Light,
        color = color_black
    ),
    button = defaultTextStyle.copy(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = color_white
    ),
)

