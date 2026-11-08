package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val CairoFontFamily = FontFamily(Font(R.font.cairo))
val TajawalFontFamily = FontFamily(Font(R.font.tajawal))
val AmiriFontFamily = FontFamily(Font(R.font.amiri))
val NotoNaskhArabicFontFamily = FontFamily(Font(R.font.noto_naskh_arabic))
val NotoKufiArabicFontFamily = FontFamily(Font(R.font.noto_kufi_arabic))
val AlmaraiFontFamily = FontFamily(Font(R.font.almarai))
val ScheherazadeFontFamily = FontFamily(Font(R.font.scheherazade_new))
val InterFontFamily = FontFamily(Font(R.font.inter))

fun resolveReaderFontFamily(fontKey: String): FontFamily {
    return when (fontKey) {
        "Cairo" -> CairoFontFamily
        "Tajawal" -> TajawalFontFamily
        "Amiri" -> AmiriFontFamily
        "Noto Naskh Arabic" -> NotoNaskhArabicFontFamily
        "Noto Kufi" -> NotoKufiArabicFontFamily
        "Almarai" -> AlmaraiFontFamily
        "Scheherazade" -> ScheherazadeFontFamily
        "Inter" -> InterFontFamily
        "Roboto" -> FontFamily.SansSerif
        "Serif" -> FontFamily.Serif
        else -> FontFamily.Default
    }
}

val AvailableReaderFonts = listOf(
    "Cairo" to "خط القاهرة (Cairo)",
    "Tajawal" to "خط تجوال (Tajawal)",
    "Amiri" to "خط أميري الروائي (Amiri)",
    "Noto Naskh Arabic" to "نوتو نسخ (Noto Naskh)",
    "Noto Kufi" to "نوتو كوفي (Noto Kufi)",
    "Almarai" to "خط المراعي (Almarai)",
    "Scheherazade" to "شهرزاد التراثي (Scheherazade)",
    "Inter" to "إنتر (Inter)",
    "Roboto" to "روبوتو (Roboto)",
    "Serif" to "سيريف (Serif)",
    "System" to "خط النظام الافتراضي"
)

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 23.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = TajawalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)
