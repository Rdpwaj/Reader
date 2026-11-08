package com.example.data

import org.json.JSONObject

data class ReaderSettings(
    // Language & Onboarding
    val language: String = "ar", // "ar" or "en"
    val onboardingCompleted: Boolean = false,

    // Font Settings
    val fontFamily: String = "Amiri", // Cairo, Tajawal, Amiri, Noto Naskh Arabic, Noto Kufi, Almarai, Scheherazade, Inter, Roboto, Serif, System
    val fontSizeSp: Float = 20f,
    val isBoldWeight: Boolean = false,
    val customFontName: String = "",

    // Text & Lines
    val lineHeightMultiplier: Float = 1.75f,
    val paragraphSpacingDp: Float = 14f,
    val letterSpacingSp: Float = 0f,
    val wordSpacingSp: Float = 0f,
    val textAlign: String = "JUSTIFY", // RIGHT, LEFT, CENTER, JUSTIFY
    val textDirection: String = "AUTO", // AUTO, RTL, LTR
    val firstLineIndentDp: Float = 16f,
    val marginVerticalDp: Float = 16f,
    val marginHorizontalDp: Float = 20f,
    val columnsCount: Int = 1, // 1 or 2 for tablets/wide screens
    val emptyLineBetweenParagraphs: Boolean = true,
    val hideTashkeel: Boolean = false,
    val autoFixPastedWhitespace: Boolean = true,

    // Colors & Themes
    val themeMode: String = "SEPIA", // LIGHT, DARK, AMOLED, SEPIA, PAPER, SYSTEM, CUSTOM
    val customBgHex: String = "#FAF6EE",
    val customTextHex: String = "#1E293B",
    val customHighlightHex: String = "#FDE047",
    val customLinkHex: String = "#D97706",
    val inAppBrightness: Float = -1f, // -1f = system brightness, 0.05f..1.0f = custom
    val warmEyeFilterIntensity: Float = 0.0f, // 0.0f..0.6f
    val warmFilterScheduleSunset: Boolean = false,

    // Display & Visibility
    val showTopBarInReader: Boolean = true,
    val showBottomBarInReader: Boolean = true,
    val showThinEdgeProgress: Boolean = true,
    val showClockAndBatteryInEdge: Boolean = true,
    val showPercentAndRemaining: Boolean = true,
    val immersiveFullScreen: Boolean = false,

    // Library Item Visibility
    val libShowReadTime: Boolean = true,
    val libShowWordCount: Boolean = true,
    val libShowProgress: Boolean = true,
    val libShowStatus: Boolean = true,
    val libShowModifiedDate: Boolean = true,
    val libShowTranslatorStage: Boolean = true,
    val libraryViewMode: String = "LIST", // LIST, GRID, COMPACT

    // Reading Behavior
    val readingMode: String = "SCROLL", // SCROLL (تمرير مستمر), PAGES (صفحات بتأثير قلب)
    val autoScrollSpeed: Float = 0f, // 0 = off, 1f..10f = speed
    val volumeKeyPaging: Boolean = true,
    val touchZonesMode: String = "STANDARD", // STANDARD, SIDES_FLIP, DISABLED
    val keepScreenOn: Boolean = true,
    val autoOpenNextChapter: Boolean = false,
    val hapticFeedback: Boolean = true,
    val readingSpeedWpm: Int = 180,
    val autoLearnReadingSpeed: Boolean = true,
    val highlightGlossaryInText: Boolean = true,

    // Goals, Backup & Security
    val dailyGoalMinutes: Int = 30,
    val backupReminderDays: Int = 7,
    val lastBackupTimestamp: Long = 0L,
    val appLockPin: String = "" // empty = disabled
) {
    fun toJsonString(): String {
        val obj = JSONObject()
        obj.put("language", language)
        obj.put("onboardingCompleted", onboardingCompleted)
        obj.put("fontFamily", fontFamily)
        obj.put("fontSizeSp", fontSizeSp.toDouble())
        obj.put("isBoldWeight", isBoldWeight)
        obj.put("customFontName", customFontName)
        obj.put("lineHeightMultiplier", lineHeightMultiplier.toDouble())
        obj.put("paragraphSpacingDp", paragraphSpacingDp.toDouble())
        obj.put("letterSpacingSp", letterSpacingSp.toDouble())
        obj.put("wordSpacingSp", wordSpacingSp.toDouble())
        obj.put("textAlign", textAlign)
        obj.put("textDirection", textDirection)
        obj.put("firstLineIndentDp", firstLineIndentDp.toDouble())
        obj.put("marginVerticalDp", marginVerticalDp.toDouble())
        obj.put("marginHorizontalDp", marginHorizontalDp.toDouble())
        obj.put("columnsCount", columnsCount)
        obj.put("emptyLineBetweenParagraphs", emptyLineBetweenParagraphs)
        obj.put("hideTashkeel", hideTashkeel)
        obj.put("autoFixPastedWhitespace", autoFixPastedWhitespace)
        obj.put("themeMode", themeMode)
        obj.put("customBgHex", customBgHex)
        obj.put("customTextHex", customTextHex)
        obj.put("customHighlightHex", customHighlightHex)
        obj.put("customLinkHex", customLinkHex)
        obj.put("inAppBrightness", inAppBrightness.toDouble())
        obj.put("warmEyeFilterIntensity", warmEyeFilterIntensity.toDouble())
        obj.put("warmFilterScheduleSunset", warmFilterScheduleSunset)
        obj.put("showTopBarInReader", showTopBarInReader)
        obj.put("showBottomBarInReader", showBottomBarInReader)
        obj.put("showThinEdgeProgress", showThinEdgeProgress)
        obj.put("showClockAndBatteryInEdge", showClockAndBatteryInEdge)
        obj.put("showPercentAndRemaining", showPercentAndRemaining)
        obj.put("immersiveFullScreen", immersiveFullScreen)
        obj.put("libShowReadTime", libShowReadTime)
        obj.put("libShowWordCount", libShowWordCount)
        obj.put("libShowProgress", libShowProgress)
        obj.put("libShowStatus", libShowStatus)
        obj.put("libShowModifiedDate", libShowModifiedDate)
        obj.put("libShowTranslatorStage", libShowTranslatorStage)
        obj.put("libraryViewMode", libraryViewMode)
        obj.put("readingMode", readingMode)
        obj.put("autoScrollSpeed", autoScrollSpeed.toDouble())
        obj.put("volumeKeyPaging", volumeKeyPaging)
        obj.put("touchZonesMode", touchZonesMode)
        obj.put("keepScreenOn", keepScreenOn)
        obj.put("autoOpenNextChapter", autoOpenNextChapter)
        obj.put("hapticFeedback", hapticFeedback)
        obj.put("readingSpeedWpm", readingSpeedWpm)
        obj.put("autoLearnReadingSpeed", autoLearnReadingSpeed)
        obj.put("highlightGlossaryInText", highlightGlossaryInText)
        obj.put("dailyGoalMinutes", dailyGoalMinutes)
        obj.put("backupReminderDays", backupReminderDays)
        obj.put("lastBackupTimestamp", lastBackupTimestamp)
        obj.put("appLockPin", appLockPin)
        return obj.toString()
    }

    companion object {
        fun fromJsonString(json: String?): ReaderSettings {
            if (json.isNullOrBlank()) return ReaderSettings()
            return try {
                val obj = JSONObject(json)
                val def = ReaderSettings()
                ReaderSettings(
                    language = obj.optString("language", def.language),
                    onboardingCompleted = obj.optBoolean("onboardingCompleted", def.onboardingCompleted),
                    fontFamily = obj.optString("fontFamily", def.fontFamily),
                    fontSizeSp = obj.optDouble("fontSizeSp", def.fontSizeSp.toDouble()).toFloat(),
                    isBoldWeight = obj.optBoolean("isBoldWeight", def.isBoldWeight),
                    customFontName = obj.optString("customFontName", def.customFontName),
                    lineHeightMultiplier = obj.optDouble("lineHeightMultiplier", def.lineHeightMultiplier.toDouble()).toFloat(),
                    paragraphSpacingDp = obj.optDouble("paragraphSpacingDp", def.paragraphSpacingDp.toDouble()).toFloat(),
                    letterSpacingSp = obj.optDouble("letterSpacingSp", def.letterSpacingSp.toDouble()).toFloat(),
                    wordSpacingSp = obj.optDouble("wordSpacingSp", def.wordSpacingSp.toDouble()).toFloat(),
                    textAlign = obj.optString("textAlign", def.textAlign),
                    textDirection = obj.optString("textDirection", def.textDirection),
                    firstLineIndentDp = obj.optDouble("firstLineIndentDp", def.firstLineIndentDp.toDouble()).toFloat(),
                    marginVerticalDp = obj.optDouble("marginVerticalDp", def.marginVerticalDp.toDouble()).toFloat(),
                    marginHorizontalDp = obj.optDouble("marginHorizontalDp", def.marginHorizontalDp.toDouble()).toFloat(),
                    columnsCount = obj.optInt("columnsCount", def.columnsCount),
                    emptyLineBetweenParagraphs = obj.optBoolean("emptyLineBetweenParagraphs", def.emptyLineBetweenParagraphs),
                    hideTashkeel = obj.optBoolean("hideTashkeel", def.hideTashkeel),
                    autoFixPastedWhitespace = obj.optBoolean("autoFixPastedWhitespace", def.autoFixPastedWhitespace),
                    themeMode = obj.optString("themeMode", def.themeMode),
                    customBgHex = obj.optString("customBgHex", def.customBgHex),
                    customTextHex = obj.optString("customTextHex", def.customTextHex),
                    customHighlightHex = obj.optString("customHighlightHex", def.customHighlightHex),
                    customLinkHex = obj.optString("customLinkHex", def.customLinkHex),
                    inAppBrightness = obj.optDouble("inAppBrightness", def.inAppBrightness.toDouble()).toFloat(),
                    warmEyeFilterIntensity = obj.optDouble("warmEyeFilterIntensity", def.warmEyeFilterIntensity.toDouble()).toFloat(),
                    warmFilterScheduleSunset = obj.optBoolean("warmFilterScheduleSunset", def.warmFilterScheduleSunset),
                    showTopBarInReader = obj.optBoolean("showTopBarInReader", def.showTopBarInReader),
                    showBottomBarInReader = obj.optBoolean("showBottomBarInReader", def.showBottomBarInReader),
                    showThinEdgeProgress = obj.optBoolean("showThinEdgeProgress", def.showThinEdgeProgress),
                    showClockAndBatteryInEdge = obj.optBoolean("showClockAndBatteryInEdge", def.showClockAndBatteryInEdge),
                    showPercentAndRemaining = obj.optBoolean("showPercentAndRemaining", def.showPercentAndRemaining),
                    immersiveFullScreen = obj.optBoolean("immersiveFullScreen", def.immersiveFullScreen),
                    libShowReadTime = obj.optBoolean("libShowReadTime", def.libShowReadTime),
                    libShowWordCount = obj.optBoolean("libShowWordCount", def.libShowWordCount),
                    libShowProgress = obj.optBoolean("libShowProgress", def.libShowProgress),
                    libShowStatus = obj.optBoolean("libShowStatus", def.libShowStatus),
                    libShowModifiedDate = obj.optBoolean("libShowModifiedDate", def.libShowModifiedDate),
                    libShowTranslatorStage = obj.optBoolean("libShowTranslatorStage", def.libShowTranslatorStage),
                    libraryViewMode = obj.optString("libraryViewMode", def.libraryViewMode),
                    readingMode = obj.optString("readingMode", def.readingMode),
                    autoScrollSpeed = obj.optDouble("autoScrollSpeed", def.autoScrollSpeed.toDouble()).toFloat(),
                    volumeKeyPaging = obj.optBoolean("volumeKeyPaging", def.volumeKeyPaging),
                    touchZonesMode = obj.optString("touchZonesMode", def.touchZonesMode),
                    keepScreenOn = obj.optBoolean("keepScreenOn", def.keepScreenOn),
                    autoOpenNextChapter = obj.optBoolean("autoOpenNextChapter", def.autoOpenNextChapter),
                    hapticFeedback = obj.optBoolean("hapticFeedback", def.hapticFeedback),
                    readingSpeedWpm = obj.optInt("readingSpeedWpm", def.readingSpeedWpm),
                    autoLearnReadingSpeed = obj.optBoolean("autoLearnReadingSpeed", def.autoLearnReadingSpeed),
                    highlightGlossaryInText = obj.optBoolean("highlightGlossaryInText", def.highlightGlossaryInText),
                    dailyGoalMinutes = obj.optInt("dailyGoalMinutes", def.dailyGoalMinutes),
                    backupReminderDays = obj.optInt("backupReminderDays", def.backupReminderDays),
                    lastBackupTimestamp = obj.optLong("lastBackupTimestamp", def.lastBackupTimestamp),
                    appLockPin = obj.optString("appLockPin", def.appLockPin)
                )
            } catch (e: Exception) {
                ReaderSettings()
            }
        }
    }
}
