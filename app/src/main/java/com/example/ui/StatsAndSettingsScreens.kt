package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AnnotationEntity
import com.example.data.ChapterEntity
import com.example.data.NovelEntity
import com.example.data.ReaderSettings
import com.example.data.ReadingStatsEntity
import com.example.data.TextProcessingUtils
import com.example.ui.theme.AvailableReaderFonts
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.SubScreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 1) "متابعة" (Continue Reading & Translator Review Queue Tab)
 */
@Composable
fun ContinueReadingTabScreen(
    viewModel: MainViewModel,
    chapters: List<ChapterEntity>,
    novels: List<NovelEntity>,
    annotations: List<AnnotationEntity>,
    settings: ReaderSettings,
    modifier: Modifier = Modifier
) {
    val inProgressChapters = remember(chapters) {
        chapters.filter { it.readingStatus == "READING" || it.lastReadAt > 0L }
            .sortedByDescending { it.lastReadAt }
    }
    val translatorDrafts = remember(chapters) {
        chapters.filter { it.translatorStage != "FINAL" }
            .sortedByDescending { it.modifiedAt }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 88.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = if (settings.language == "en") "Continue Reading & Review" else "متابعة القراءة والمراجعة",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "يُحفظ موضع القراءة بالفقرة والحرف تلقائيًا ليبقى دقيقًا حتى بعد تغيير حجم الخط.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (inProgressChapters.isNotEmpty()) {
            item {
                Text(
                    text = "فصول قيد القراءة (${inProgressChapters.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            items(inProgressChapters, key = { "prog_${it.id}" }) { ch ->
                val novel = novels.find { it.id == ch.novelId }
                val (mins, isRem) = TextProcessingUtils.calculateReadingMinutes(
                    ch.wordCount,
                    ch.progressPercent,
                    settings.readingSpeedWpm
                )
                ElevatedCard(
                    onClick = { viewModel.openChapterForReading(ch.id) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            if (novel != null) {
                                Text(
                                    text = novel.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = ch.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "توقفت عند الفقرة ${ch.paragraphIndex + 1} (حرف ${ch.charOffset}) • " +
                                    if (isRem) "متبقي $mins د" else "⏱ $mins د",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (ch.progressPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(onClick = { viewModel.openChapterForReading(ch.id) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("متابعة")
                        }
                    }
                }
            }
        }

        if (translatorDrafts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "قائمة مراجعة المترجم (${translatorDrafts.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            items(translatorDrafts, key = { "draft_${it.id}" }) { ch ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ch.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                text = "الحالة: ${translatorStageLabel(ch.translatorStage)} • ${ch.wordCount} كلمة",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(onClick = { viewModel.openSubScreen(SubScreen.EDITOR, ch.id) }) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تحرير")
                            }
                            Button(onClick = { viewModel.openChapterForReading(ch.id) }) {
                                Text("قراءة")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2) "الإحصائيات" (Reading Stats, Daily Goal, Streak, Learned WPM)
 */
@Composable
fun ReadingStatsScreen(
    viewModel: MainViewModel,
    stats: List<ReadingStatsEntity>,
    chapters: List<ChapterEntity>,
    settings: ReaderSettings,
    modifier: Modifier = Modifier
) {
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val todayStats = stats.find { it.dateString == todayStr }
    val todayMinutes = todayStats?.minutesRead ?: 0
    val goalMinutes = settings.dailyGoalMinutes.coerceAtLeast(5)
    val goalProgress = (todayMinutes.toFloat() / goalMinutes.toFloat()).coerceIn(0f, 1f)

    val totalMinutesAllTime = stats.sumOf { it.minutesRead }
    val totalWordsAllTime = stats.sumOf { it.wordsRead }
    val completedChaptersCount = chapters.count { it.readingStatus == "COMPLETED" }
    val streakDays = stats.count { it.minutesRead > 0 }.coerceAtLeast(1)

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = if (settings.language == "en") "Reading Statistics & Goals" else "إحصائيات القراءة والأهداف",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Streak & Daily Goal Hero Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "هدف القراءة اليومي",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$todayMinutes من أصل $goalMinutes دقيقة اليوم",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$streakDays أيام متواصلة",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { goalProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تعديل الهدف اليومي:", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(15, 30, 45, 60).forEach { mins ->
                                FilterChip(
                                    selected = settings.dailyGoalMinutes == mins,
                                    onClick = { viewModel.updateSettings { it.copy(dailyGoalMinutes = mins) } },
                                    label = { Text("$mins د") }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Summary Metric Cards Grid
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatMetricCard(
                    title = "الكلمات المقروءة",
                    value = "$totalWordsAllTime",
                    subtitle = "كلمة إجمالًا",
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "وقت القراءة",
                    value = "$totalMinutesAllTime د",
                    subtitle = "إجمالي الدقائق",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatMetricCard(
                    title = "الفصول المكتملة",
                    value = "$completedChaptersCount / ${chapters.size}",
                    subtitle = "فصل مكتمل",
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "سرعتك المحسوبة",
                    value = "${settings.readingSpeedWpm}",
                    subtitle = "كلمة / دقيقة",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 7-Day Activity Bar Chart
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "نشاط القراءة في الأيام الأخيرة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    val recentStats = stats.take(7).reversed()
                    val maxMins = (recentStats.maxOfOrNull { it.minutesRead } ?: 30).coerceAtLeast(15)
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        recentStats.forEach { day ->
                            val ratio = (day.minutesRead.toFloat() / maxMins.toFloat()).coerceIn(0.08f, 1f)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Text(
                                    text = "${day.minutesRead}د",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(26.dp)
                                        .fillMaxHeight(ratio * 0.72f)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = day.dateString.takeLast(5),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(subtitle, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/**
 * 3) Comprehensive Settings Screen (Section 5: الخط، النص والأسطر، الألوان والثيمات، العرض والعناصر، السلوك، النسخ الاحتياطي)
 */
@Composable
fun FullSettingsScreen(
    viewModel: MainViewModel,
    settings: ReaderSettings,
    novels: List<NovelEntity>,
    chapters: List<ChapterEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showResetConfirm by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }

    // Create Document Launcher for Exporting Full JSON Backup
    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val json = viewModel.repository.exportFullBackupJson(settings)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
                viewModel.updateSettings { it.copy(lastBackupTimestamp = System.currentTimeMillis()) }
                viewModel.showToastMessage("تم تصدير النسخة الاحتياطية الكاملة بنجاح!")
            }
        }
    }

    // Open Document Launcher for Restoring Full JSON Backup
    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val json = context.contentResolver.openInputStream(uri)?.use {
                    String(it.readBytes(), Charsets.UTF_8)
                } ?: ""
                val ok = viewModel.repository.importFullBackupJson(json)
                viewModel.showToastMessage(
                    if (ok) "تمت استعادة النسخة الاحتياطية بنجاح!" else "فشل قراءة ملف النسخة الاحتياطية"
                )
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 92.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (settings.language == "en") "All Settings" else "الإعدادات الشاملة",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = {
                        val nextLang = if (settings.language == "ar") "en" else "ar"
                        viewModel.updateSettings { it.copy(language = nextLang) }
                    },
                    modifier = Modifier.testTag("toggle_language_button")
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (settings.language == "ar") "English (LTR)" else "العربية (RTL)")
                }
            }
        }

        // Section 1: Font Settings (الخط)
        item {
            SettingsSectionCard(title = "1) الخط (Font & Typography)") {
                Text("عائلة الخط:", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp)
                ) {
                    AvailableReaderFonts.forEach { (key, label) ->
                        FilterChip(
                            selected = settings.fontFamily == key,
                            onClick = { viewModel.updateSettings { it.copy(fontFamily = key) } },
                            label = { Text(label) }
                        )
                    }
                }

                SettingsSliderRow(
                    label = "حجم الخط (${settings.fontSizeSp.toInt()} sp)",
                    value = settings.fontSizeSp,
                    range = 14f..36f,
                    onValueChange = { v -> viewModel.updateSettings { it.copy(fontSizeSp = v) } }
                )

                SettingsSwitchRow(
                    label = "الوزن العريض للخط (Bold)",
                    checked = settings.isBoldWeight,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(isBoldWeight = v) } }
                )
            }
        }

        // Section 2: Text & Lines (النص والأسطر)
        item {
            SettingsSectionCard(title = "2) النص والأسطر (Text & Spacing)") {
                SettingsSliderRow(
                    label = "ارتفاع السطر (${String.format(Locale.US, "%.2f", settings.lineHeightMultiplier)})",
                    value = settings.lineHeightMultiplier,
                    range = 1.2f..2.6f,
                    onValueChange = { v -> viewModel.updateSettings { it.copy(lineHeightMultiplier = v) } }
                )
                SettingsSliderRow(
                    label = "المسافة بين الفقرات (${settings.paragraphSpacingDp.toInt()} dp)",
                    value = settings.paragraphSpacingDp,
                    range = 4f..32f,
                    onValueChange = { v -> viewModel.updateSettings { it.copy(paragraphSpacingDp = v) } }
                )
                SettingsSliderRow(
                    label = "المسافة البادئة للسطر الأول (${settings.firstLineIndentDp.toInt()})",
                    value = settings.firstLineIndentDp,
                    range = 0f..40f,
                    onValueChange = { v -> viewModel.updateSettings { it.copy(firstLineIndentDp = v) } }
                )
                SettingsSliderRow(
                    label = "الهوامش الجانبية (${settings.marginHorizontalDp.toInt()} dp)",
                    value = settings.marginHorizontalDp,
                    range = 8f..48f,
                    onValueChange = { v -> viewModel.updateSettings { it.copy(marginHorizontalDp = v) } }
                )
                SettingsSliderRow(
                    label = "الهوامش العلوية والسفلية (${settings.marginVerticalDp.toInt()} dp)",
                    value = settings.marginVerticalDp,
                    range = 8f..48f,
                    onValueChange = { v -> viewModel.updateSettings { it.copy(marginVerticalDp = v) } }
                )

                Text("محاذاة النص:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "RIGHT" to "يمين",
                        "JUSTIFY" to "ضبط (Justify)",
                        "CENTER" to "وسط",
                        "LEFT" to "يسار"
                    ).forEach { (alignKey, label) ->
                        FilterChip(
                            selected = settings.textAlign == alignKey,
                            onClick = { viewModel.updateSettings { it.copy(textAlign = alignKey) } },
                            label = { Text(label) }
                        )
                    }
                }

                SettingsSwitchRow(
                    label = "فصل الفقرات بسطر فارغ واضح",
                    checked = settings.emptyLineBetweenParagraphs,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(emptyLineBetweenParagraphs = v) } }
                )
                SettingsSwitchRow(
                    label = "إخفاء التشكيل (الحركات) أثناء القراءة",
                    checked = settings.hideTashkeel,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(hideTashkeel = v) } }
                )
                SettingsSwitchRow(
                    label = "إصلاح تلقائي لمسافات وأسطر النص الملصوق",
                    checked = settings.autoFixPastedWhitespace,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(autoFixPastedWhitespace = v) } }
                )
            }
        }

        // Section 3: Colors & Themes (الألوان والثيمات)
        item {
            SettingsSectionCard(title = "3) الألوان والثيمات وراحة العين") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    listOf(
                        "SEPIA" to "سيبيا دافئ",
                        "PAPER" to "ورقي كلاسيكي",
                        "LIGHT" to "فاتح",
                        "DARK" to "داكن",
                        "AMOLED" to "أسود AMOLED",
                        "SYSTEM" to "تلقائي حسب النظام"
                    ).forEach { (mode, label) ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.updateSettings { it.copy(themeMode = mode) } },
                            label = { Text(label) }
                        )
                    }
                }

                SettingsSliderRow(
                    label = if (settings.inAppBrightness < 0f) "سطوع داخل التطبيق (تلقائي حسب النظام)"
                    else "سطوع داخل التطبيق (${(settings.inAppBrightness * 100).toInt()}%)",
                    value = if (settings.inAppBrightness < 0f) 0.5f else settings.inAppBrightness,
                    range = 0.05f..1.0f,
                    onValueChange = { v -> viewModel.updateSettings { it.copy(inAppBrightness = v) } }
                )
                SettingsSliderRow(
                    label = "فلتر دافئ لراحة العين (${(settings.warmEyeFilterIntensity * 100).toInt()}%)",
                    value = settings.warmEyeFilterIntensity,
                    range = 0f..0.5f,
                    onValueChange = { v -> viewModel.updateSettings { it.copy(warmEyeFilterIntensity = v) } }
                )
            }
        }

        // Section 4: Display & Elements Visibility (العرض والعناصر)
        item {
            SettingsSectionCard(title = "4) إظهار وإخفاء عناصر الواجهة") {
                SettingsSwitchRow(
                    label = "شريط التقدم الرفيع الدائم في حافة شاشة القراءة",
                    checked = settings.showThinEdgeProgress,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(showThinEdgeProgress = v) } }
                )
                SettingsSwitchRow(
                    label = "إظهار الساعة والبطارية في حافة القراءة",
                    checked = settings.showClockAndBatteryInEdge,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(showClockAndBatteryInEdge = v) } }
                )
                SettingsSwitchRow(
                    label = "إظهار النسبة المئوية والوقت المتبقي",
                    checked = settings.showPercentAndRemaining,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(showPercentAndRemaining = v) } }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("عناصر بطاقة الفصل في المكتبة:", style = MaterialTheme.typography.labelLarge)
                SettingsSwitchRow(
                    label = "عدّاد وقت القراءة والمتبقي بجانب الاسم (⏱ 12 د)",
                    checked = settings.libShowReadTime,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(libShowReadTime = v) } }
                )
                SettingsSwitchRow(
                    label = "عدد الكلمات",
                    checked = settings.libShowWordCount,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(libShowWordCount = v) } }
                )
                SettingsSwitchRow(
                    label = "شريط ونسبة التقدم",
                    checked = settings.libShowProgress,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(libShowProgress = v) } }
                )
                SettingsSwitchRow(
                    label = "حالة القراءة (جديد / قيد القراءة / مكتمل)",
                    checked = settings.libShowStatus,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(libShowStatus = v) } }
                )
                SettingsSwitchRow(
                    label = "تاريخ آخر تعديل",
                    checked = settings.libShowModifiedDate,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(libShowModifiedDate = v) } }
                )
            }
        }

        // Section 5: Behavior & Reading Speed (السلوك وسرعة القراءة)
        item {
            SettingsSectionCard(title = "5) السلوك وسرعة القراءة") {
                SettingsSliderRow(
                    label = "سرعة القراءة المرجعية لحساب العدّاد (${settings.readingSpeedWpm} كلمة/دقيقة)",
                    value = settings.readingSpeedWpm.toFloat(),
                    range = 80f..400f,
                    onValueChange = { v -> viewModel.updateSettings { it.copy(readingSpeedWpm = v.toInt()) } }
                )
                SettingsSwitchRow(
                    label = "تعلّم سرعتي الفعلية تلقائيًا أثناء القراءة",
                    checked = settings.autoLearnReadingSpeed,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(autoLearnReadingSpeed = v) } }
                )
                SettingsSwitchRow(
                    label = "إبقاء الشاشة مضاءة أثناء القراءة",
                    checked = settings.keepScreenOn,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(keepScreenOn = v) } }
                )
                SettingsSwitchRow(
                    label = "التقليب بأزرار الصوت (Volume Keys)",
                    checked = settings.volumeKeyPaging,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(volumeKeyPaging = v) } }
                )
                SettingsSwitchRow(
                    label = "تفعيل مناطق اللمس الجانبية للتقليب السريع",
                    checked = settings.touchZonesMode == "SIDES_FLIP",
                    onCheckedChange = { v ->
                        viewModel.updateSettings { it.copy(touchZonesMode = if (v) "SIDES_FLIP" else "STANDARD") }
                    }
                )
                SettingsSwitchRow(
                    label = "فتح الفصل التالي تلقائيًا عند النهاية",
                    checked = settings.autoOpenNextChapter,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(autoOpenNextChapter = v) } }
                )
                SettingsSwitchRow(
                    label = "تظليل مصطلحات القاموس داخل النص تلقائيًا",
                    checked = settings.highlightGlossaryInText,
                    onCheckedChange = { v -> viewModel.updateSettings { it.copy(highlightGlossaryInText = v) } }
                )
            }
        }

        // Section 6: Backup, Export, Trash & App Lock (النسخ الاحتياطي والأمان)
        item {
            SettingsSectionCard(title = "6) النسخ الاحتياطي، التصدير، والأمان") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            createBackupLauncher.launch("chapter_reader_backup.json")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_backup_button")
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نسخ احتياطي (JSON)")
                    }
                    OutlinedButton(
                        onClick = {
                            restoreBackupLauncher.launch(arrayOf("*/*"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("restore_backup_button")
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استعادة نسخة")
                    }
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val json = viewModel.repository.exportFullBackupJson(settings)
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية - قارئ الفصول")
                                putExtra(Intent.EXTRA_TEXT, json)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة النسخة الاحتياطية"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مشاركة النسخة الاحتياطية السريعة عبر تطبيقات الجهاز")
                }

                OutlinedButton(
                    onClick = { viewModel.openSubScreen(SubScreen.TRASH) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("فتح سلة المحذوفات (30 يومًا)")
                }

                OutlinedButton(
                    onClick = { showPinDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (settings.appLockPin.isBlank()) "تعيين رمز قفل للتطبيق (PIN)"
                        else "تغيير أو إلغاء رمز قفل التطبيق (مفعل)"
                    )
                }

                TextButton(
                    onClick = { showResetConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إعادة ضبط جميع الإعدادات للوضع الافتراضي")
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("إعادة الضبط للافتراضي") },
            text = { Text("هل تريد استعادة جميع إعدادات الخطوط والألوان والعرض للوضع الافتراضي؟ (لن تتأثر فصولك أو رواياتك)") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSettingsToDefault()
                        showResetConfirm = false
                    }
                ) {
                    Text("تأكيد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showPinDialog) {
        var newPin by remember { mutableStateOf(settings.appLockPin) }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("قفل التطبيق برمز سري (PIN)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("اترك الحقل فارغًا لإلغاء القفل، أو أدخل 4 أرقام لقفل التطبيق عند الفتح:")
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it.filter { c -> c.isDigit() }.take(6) },
                        label = { Text("رمز PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSettings { it.copy(appLockPin = newPin) }
                        showPinDialog = false
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun SettingsSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * 4) 3-Screen Onboarding Tour ("جولة تعريفية قصيرة 3 شاشات عند أول فتح")
 */
@Composable
fun OnboardingTourDialog(
    onFinish: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    val titles = listOf(
        "1. لصق وتقسيم الفصول بضغطة واحدة",
        "2. حفظ دقيق لموضع القراءة (فقرة + حرف)",
        "3. أدوات المترجم الكاملة 100% بدون إنترنت"
    )
    val descriptions = listOf(
        "ألصق ترجمتك مباشرة من الحافظة مع اقتراح العنوان من أول سطر، أو ألصق عدة فصول معًا ليقسمها التطبيق تلقائيًا مع معاينة فورية.",
        "لا يضيع موضع قراءتك أبدًا حتى لو أغلقت التطبيق فجأة أو غيّرت نوع وحجم الخط. يعمل مع الملفات الضخمة (500 ألف كلمة) بدون أي تعليق.",
        "استمتع بقاموس المصطلحات مع تظليل الكلمات داخل النص، والبحث والاستبدال الشامل لتوحيد أسماء الشخصيات، والقراءة الصوتية العربية (TTS)."
    )

    AlertDialog(
        onDismissRequest = onFinish,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.img_reader_hero_1794140263314),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(titles[step], style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = descriptions[step],
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 0..2) {
                        Box(
                            modifier = Modifier
                                .size(if (step == i) 22.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (step == i) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step < 2) step++ else onFinish()
                },
                modifier = Modifier.testTag("onboarding_next_button")
            ) {
                Text(if (step < 2) "التالي" else "ابدأ القراءة الآن")
            }
        },
        dismissButton = {
            TextButton(onClick = onFinish) {
                Text("تخطي")
            }
        }
    )
}
