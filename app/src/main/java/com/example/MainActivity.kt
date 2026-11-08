package com.example

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TextProcessingUtils
import com.example.ui.AnnotationsHubScreen
import com.example.ui.ContinueReadingTabScreen
import com.example.ui.FindAndReplaceScreen
import com.example.ui.FullSettingsScreen
import com.example.ui.GlossaryScreen
import com.example.ui.InAppEditorScreen
import com.example.ui.LibraryScreen
import com.example.ui.OnboardingTourDialog
import com.example.ui.ReaderScreen
import com.example.ui.ReadingStatsScreen
import com.example.ui.TrashBinScreen
import com.example.ui.theme.ChapterReaderTheme
import com.example.viewmodel.BottomNavTab
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.SubScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)

        setContent {
            val settings by viewModel.globalSettings.collectAsStateWithLifecycle()

            ChapterReaderTheme(
                themeMode = settings.themeMode,
                language = settings.language
            ) {
                ChapterReaderRoot(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (!sharedText.isNullOrBlank()) {
                    viewModel.onReceiveSharedText(sharedText)
                } else {
                    val streamUri = intent.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM)
                    if (streamUri != null) {
                        val (text, _) = TextProcessingUtils.extractTextFromUri(this, streamUri, "shared.txt")
                        viewModel.onReceiveSharedText(text)
                    }
                }
            }
            Intent.ACTION_VIEW -> {
                val dataUri = intent.data
                if (dataUri != null) {
                    val (text, _) = TextProcessingUtils.extractTextFromUri(this, dataUri, "opened.txt")
                    viewModel.onReceiveSharedText(text)
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val activeChapterId = viewModel.activeChapterId.value
        val settings = viewModel.globalSettings.value
        if (activeChapterId != null && settings.volumeKeyPaging) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                val ch = viewModel.activeReadingChapter.value
                if (ch != null) {
                    val paras = TextProcessingUtils.splitIntoParagraphs(ch.content, settings.hideTashkeel)
                    val delta = if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) 4 else -4
                    val nextPara = (ch.paragraphIndex + delta).coerceIn(0, (paras.size - 1).coerceAtLeast(0))
                    viewModel.saveReadingPosition(ch.id, nextPara, 0, paras.size)
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun ChapterReaderRoot(viewModel: MainViewModel) {
    val settings by viewModel.globalSettings.collectAsStateWithLifecycle()
    val novels by viewModel.novels.collectAsStateWithLifecycle()
    val deletedNovels by viewModel.deletedNovels.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()
    val deletedChapters by viewModel.deletedChapters.collectAsStateWithLifecycle()
    val annotations by viewModel.annotations.collectAsStateWithLifecycle()
    val glossary by viewModel.glossary.collectAsStateWithLifecycle()
    val readingStats by viewModel.readingStats.collectAsStateWithLifecycle()

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeChapterId by viewModel.activeChapterId.collectAsStateWithLifecycle()
    val activeReadingChapter by viewModel.activeReadingChapter.collectAsStateWithLifecycle()
    val activeSubScreen by viewModel.activeSubScreen.collectAsStateWithLifecycle()
    val editingChapterId by viewModel.editingChapterId.collectAsStateWithLifecycle()
    val activeEditingChapter by viewModel.activeEditingChapter.collectAsStateWithLifecycle()

    val selectedNovelId by viewModel.selectedNovelId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchMatchedChapterIds by viewModel.searchMatchedChapterIds.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val tagFilter by viewModel.tagFilter.collectAsStateWithLifecycle()
    val selectedChapterIds by viewModel.selectedChapterIds.collectAsStateWithLifecycle()
    val undoBanner by viewModel.undoBanner.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val sharedIncomingText by viewModel.sharedIncomingText.collectAsStateWithLifecycle()

    var pinUnlocked by remember { mutableStateOf(false) }

    // Auto-clear status toast message after 3.5 seconds
    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            delay(3500L)
            viewModel.clearToastMessage()
        }
    }

    // 1. Optional PIN Lock Gate
    if (settings.appLockPin.isNotBlank() && !pinUnlocked) {
        var enteredPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf(false) }
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("التطبيق مقفل برمز PIN", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = {
                        enteredPin = it
                        pinError = false
                        if (it == settings.appLockPin) {
                            pinUnlocked = true
                        }
                    },
                    label = { Text("أدخل رمز PIN") },
                    isError = pinError,
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (enteredPin == settings.appLockPin) {
                            pinUnlocked = true
                        } else {
                            pinError = true
                        }
                    }
                ) {
                    Text("فتح التطبيق")
                }
            }
        }
        return
    }

    // 2. Full-Screen Reader Mode
    if (activeChapterId != null) {
        val readingChapter = activeReadingChapter
        if (readingChapter != null && readingChapter.id == activeChapterId) {
            val novel = novels.find { it.id == readingChapter.novelId }
            val sameNovelChapters = remember(chapters, readingChapter.novelId) {
                chapters.filter { it.novelId == readingChapter.novelId }
                    .sortedWith { a, b -> TextProcessingUtils.naturalCompare(a.title, b.title) }
            }
            ReaderScreen(
                viewModel = viewModel,
                chapter = readingChapter,
                novel = novel,
                novelChapters = sameNovelChapters,
                annotations = annotations,
                glossaryTerms = glossary,
                globalSettings = settings,
                onBack = { viewModel.closeReader() },
                onOpenAllSettings = {
                    viewModel.closeReader()
                    viewModel.selectTab(BottomNavTab.SETTINGS)
                }
            )
        } else {
            Surface(modifier = Modifier.fillMaxSize()) {}
        }
        return
    }

    // 3. Main App Scaffold with Bottom Navigation & SubScreens
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (activeSubScreen == SubScreen.NONE) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentTab == BottomNavTab.LIBRARY,
                        onClick = { viewModel.selectTab(BottomNavTab.LIBRARY) },
                        icon = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null) },
                        label = { Text(if (settings.language == "en") "Library" else "المكتبة") },
                        modifier = Modifier.testTag("nav_tab_library")
                    )
                    NavigationBarItem(
                        selected = currentTab == BottomNavTab.CONTINUE,
                        onClick = { viewModel.selectTab(BottomNavTab.CONTINUE) },
                        icon = { Icon(Icons.Default.PlayCircleOutline, contentDescription = null) },
                        label = { Text(if (settings.language == "en") "Continue" else "متابعة") },
                        modifier = Modifier.testTag("nav_tab_continue")
                    )
                    NavigationBarItem(
                        selected = currentTab == BottomNavTab.STATS,
                        onClick = { viewModel.selectTab(BottomNavTab.STATS) },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                        label = { Text(if (settings.language == "en") "Stats" else "الإحصائيات") },
                        modifier = Modifier.testTag("nav_tab_stats")
                    )
                    NavigationBarItem(
                        selected = currentTab == BottomNavTab.SETTINGS,
                        onClick = { viewModel.selectTab(BottomNavTab.SETTINGS) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text(if (settings.language == "en") "Settings" else "الإعدادات") },
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeSubScreen) {
                SubScreen.EDITOR -> {
                    val chToEdit = activeEditingChapter
                    if (chToEdit != null) {
                        InAppEditorScreen(
                            viewModel = viewModel,
                            chapter = chToEdit,
                            glossaryTerms = glossary,
                            onBack = { viewModel.closeSubScreen() }
                        )
                    } else if (editingChapterId == null) {
                        val fallbackId = chapters.firstOrNull()?.id
                        if (fallbackId != null) {
                            LaunchedEffect(fallbackId) {
                                viewModel.openSubScreen(SubScreen.EDITOR, fallbackId)
                            }
                        } else {
                            viewModel.closeSubScreen()
                        }
                    }
                }

                SubScreen.FIND_REPLACE -> {
                    FindAndReplaceScreen(
                        viewModel = viewModel,
                        novels = novels,
                        chapters = chapters,
                        initialChapterId = editingChapterId,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }

                SubScreen.GLOSSARY -> {
                    GlossaryScreen(
                        viewModel = viewModel,
                        novels = novels,
                        glossary = glossary,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }

                SubScreen.ANNOTATIONS -> {
                    AnnotationsHubScreen(
                        viewModel = viewModel,
                        annotations = annotations,
                        chapters = chapters,
                        novels = novels,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }

                SubScreen.TRASH -> {
                    TrashBinScreen(
                        viewModel = viewModel,
                        deletedNovels = deletedNovels,
                        deletedChapters = deletedChapters,
                        onBack = { viewModel.closeSubScreen() }
                    )
                }

                SubScreen.NONE -> {
                    when (currentTab) {
                        BottomNavTab.LIBRARY -> {
                            LibraryScreen(
                                viewModel = viewModel,
                                novels = novels,
                                chapters = chapters,
                                settings = settings,
                                selectedNovelId = selectedNovelId,
                                searchQuery = searchQuery,
                                searchMatchedChapterIds = searchMatchedChapterIds,
                                statusFilter = statusFilter,
                                tagFilter = tagFilter,
                                selectedChapterIds = selectedChapterIds,
                                sharedIncomingText = sharedIncomingText
                            )
                        }

                        BottomNavTab.CONTINUE -> {
                            ContinueReadingTabScreen(
                                viewModel = viewModel,
                                chapters = chapters,
                                novels = novels,
                                annotations = annotations,
                                settings = settings
                            )
                        }

                        BottomNavTab.STATS -> {
                            ReadingStatsScreen(
                                viewModel = viewModel,
                                stats = readingStats,
                                chapters = chapters,
                                settings = settings
                            )
                        }

                        BottomNavTab.SETTINGS -> {
                            FullSettingsScreen(
                                viewModel = viewModel,
                                settings = settings,
                                novels = novels,
                                chapters = chapters
                            )
                        }
                    }
                }
            }

            // Instant Undo Trash Banner ("وشريط تراجع فوريًا")
            val activeUndo = undoBanner
            AnimatedVisibility(
                visible = activeUndo != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                if (activeUndo != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = activeUndo.message,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Row {
                                TextButton(onClick = { viewModel.undoLastTrashAction() }) {
                                    Text(
                                        text = "تراجع",
                                        color = MaterialTheme.colorScheme.inversePrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                TextButton(onClick = { viewModel.dismissUndoBanner() }) {
                                    Text("إغلاق", color = MaterialTheme.colorScheme.inverseOnSurface)
                                }
                            }
                        }
                    }
                }
            }

            // Status / Feedback Toast Banner
            AnimatedVisibility(
                visible = statusMessage != null && undoBanner == null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    tonalElevation = 6.dp
                ) {
                    Text(
                        text = statusMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }

    // 3-Screen Onboarding Tour on First Launch
    if (!settings.onboardingCompleted) {
        OnboardingTourDialog(
            onFinish = {
                viewModel.updateSettings { it.copy(onboardingCompleted = true) }
            }
        )
    }
}
