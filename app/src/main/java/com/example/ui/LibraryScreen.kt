package com.example.ui

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ChapterEntity
import com.example.data.NovelEntity
import com.example.data.ReaderSettings
import com.example.data.SplitChapterPreview
import com.example.data.TextProcessingUtils
import com.example.ui.theme.NovelColors
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.SubScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    novels: List<NovelEntity>,
    chapters: List<ChapterEntity>,
    settings: ReaderSettings,
    selectedNovelId: Long?,
    searchQuery: String,
    statusFilter: String,
    tagFilter: String?,
    selectedChapterIds: Set<Long>,
    sharedIncomingText: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var fabExpanded by remember { mutableStateOf(false) }

    // Dialog states
    var showPasteDialog by remember { mutableStateOf(false) }
    var initialPasteContent by remember { mutableStateOf("") }
    var showNewNovelDialog by remember { mutableStateOf(false) }
    var editingNovel by remember { mutableStateOf<NovelEntity?>(null) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var contextMenuChapter by remember { mutableStateOf<ChapterEntity?>(null) }
    var contextMenuNovel by remember { mutableStateOf<NovelEntity?>(null) }
    var showRenameDialogFor by remember { mutableStateOf<ChapterEntity?>(null) }
    var showBatchRenameDialog by remember { mutableStateOf(false) }
    var showConvertFormatDialog by remember { mutableStateOf<List<ChapterEntity>?>(null) }
    var showMoveToNovelDialog by remember { mutableStateOf<List<ChapterEntity>?>(null) }
    var showMergeDialog by remember { mutableStateOf<List<ChapterEntity>?>(null) }
    var showDetailsDialogFor by remember { mutableStateOf<ChapterEntity?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Open Paste dialog automatically if text was shared from another Android app via Share Target
    LaunchedEffect(sharedIncomingText) {
        if (!sharedIncomingText.isNullOrBlank()) {
            initialPasteContent = sharedIncomingText
            showPasteDialog = true
            viewModel.consumeSharedIncomingText()
        }
    }

    // Multiple File Picker Launcher (TXT, MD, HTML, DOCX, EPUB, PDF)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val names = uris.map { uri -> getFileNameFromUri(context, uri) }
            viewModel.importFilesFromUris(
                uris = uris,
                fileNames = names,
                targetNovelId = if (selectedNovelId != null && selectedNovelId > 0) selectedNovelId else null
            )
        }
    }

    val currentNovel = remember(novels, selectedNovelId) {
        novels.find { it.id == selectedNovelId }
    }
    val activeSortBy = currentNovel?.sortBy ?: "NATURAL_NAME"
    val activeSortAsc = currentNovel?.sortAscending ?: true

    // Filter and sort chapters
    val filteredChapters = remember(
        chapters,
        selectedNovelId,
        searchQuery,
        statusFilter,
        tagFilter,
        activeSortBy,
        activeSortAsc
    ) {
        chapters.filter { ch ->
            val matchesNovel = when (selectedNovelId) {
                null -> true
                -1L -> ch.novelId == null
                else -> ch.novelId == selectedNovelId
            }
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                ch.title.contains(searchQuery, ignoreCase = true) ||
                    ch.content.contains(searchQuery, ignoreCase = true) ||
                    ch.tags.contains(searchQuery, ignoreCase = true)
            }
            val matchesStatus = when (statusFilter) {
                "NEW" -> ch.readingStatus == "NEW"
                "READING" -> ch.readingStatus == "READING"
                "COMPLETED" -> ch.readingStatus == "COMPLETED"
                "FAVORITE" -> ch.favorite
                "DRAFT" -> ch.translatorStage == "DRAFT"
                "REVIEWED" -> ch.translatorStage == "REVIEWED"
                "FINAL" -> ch.translatorStage == "FINAL"
                else -> true
            }
            val matchesTag = if (tagFilter.isNullOrBlank()) {
                true
            } else {
                ch.tags.split(",").map { it.trim() }.contains(tagFilter)
            }
            matchesNovel && matchesSearch && matchesStatus && matchesTag
        }.sortedWith { a, b ->
            val cmp = when (activeSortBy) {
                "CREATED_AT" -> a.createdAt.compareTo(b.createdAt)
                "MODIFIED_AT" -> a.modifiedAt.compareTo(b.modifiedAt)
                "LAST_READ" -> a.lastReadAt.compareTo(b.lastReadAt)
                "WORD_COUNT" -> a.wordCount.compareTo(b.wordCount)
                "PROGRESS" -> a.progressPercent.compareTo(b.progressPercent)
                else -> TextProcessingUtils.naturalCompare(a.title, b.title)
            }
            if (activeSortAsc) cmp else -cmp
        }
    }

    // Collect all unique tags for tag filter strip
    val allTags = remember(chapters) {
        chapters.flatMap { it.tags.split(",") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    // Last read chapter for the "متابعة القراءة" top card
    val lastReadChapter = remember(chapters) {
        chapters.filter { it.lastReadAt > 0L }.maxByOrNull { it.lastReadAt }
            ?: chapters.firstOrNull()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Multi-Selection Bar OR Search & Tools Bar
            if (selectedChapterIds.isNotEmpty()) {
                val selectedEntities = chapters.filter { selectedChapterIds.contains(it.id) }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.clearMultiSelection() }) {
                                    Icon(Icons.Default.Close, contentDescription = "إلغاء التحديد")
                                }
                                Text(
                                    text = "تم تحديد ${selectedChapterIds.size} فصل",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row {
                                IconButton(
                                    onClick = { viewModel.selectAllChapters(filteredChapters.map { it.id }) },
                                    modifier = Modifier.testTag("select_all_button")
                                ) {
                                    Icon(Icons.Default.SelectAll, contentDescription = "تحديد الكل")
                                }
                                IconButton(
                                    onClick = { showBatchRenameDialog = true },
                                    modifier = Modifier.testTag("batch_rename_button")
                                ) {
                                    Icon(Icons.Default.DriveFileRenameOutline, contentDescription = "إعادة تسمية جماعية")
                                }
                                IconButton(
                                    onClick = { showConvertFormatDialog = selectedEntities },
                                    modifier = Modifier.testTag("batch_convert_button")
                                ) {
                                    Icon(Icons.Default.SyncAlt, contentDescription = "تغيير الصيغة")
                                }
                                IconButton(
                                    onClick = { showMoveToNovelDialog = selectedEntities },
                                    modifier = Modifier.testTag("batch_move_button")
                                ) {
                                    Icon(Icons.Default.DriveFileMove, contentDescription = "نقل إلى رواية")
                                }
                                if (selectedEntities.size >= 2) {
                                    IconButton(
                                        onClick = { showMergeDialog = selectedEntities },
                                        modifier = Modifier.testTag("batch_merge_button")
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.MergeType, contentDescription = "دمج الفصول")
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.deleteChaptersToTrash(selectedChapterIds.toList()) },
                                    modifier = Modifier.testTag("batch_delete_button")
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "حذف إلى السلة",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Header & Search Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (settings.language == "en") "Chapter Reader" else "قارئ الفصول",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Quick Access Buttons: Glossary, Find & Replace, Annotations, Trash
                            IconButton(
                                onClick = { viewModel.openSubScreen(SubScreen.GLOSSARY) },
                                modifier = Modifier.testTag("open_glossary_button")
                            ) {
                                Icon(Icons.Default.Translate, contentDescription = "قاموس المصطلحات")
                            }
                            IconButton(
                                onClick = { viewModel.openSubScreen(SubScreen.FIND_REPLACE) },
                                modifier = Modifier.testTag("open_find_replace_button")
                            ) {
                                Icon(Icons.Default.FindReplace, contentDescription = "بحث واستبدال")
                            }
                            IconButton(
                                onClick = { viewModel.openSubScreen(SubScreen.ANNOTATIONS) },
                                modifier = Modifier.testTag("open_annotations_button")
                            ) {
                                Icon(Icons.Default.Bookmark, contentDescription = "العلامات والملاحظات")
                            }
                            IconButton(
                                onClick = { viewModel.openSubScreen(SubScreen.TRASH) },
                                modifier = Modifier.testTag("open_trash_button")
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "سلة المحذوفات")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Search Bar + Sort + View Mode
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = {
                                Text(
                                    text = if (settings.language == "en") "Search titles or inside chapters..."
                                    else "ابحث بالاسم أو داخل محتوى كل الفصول...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "بحث")
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "مسح البحث")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("library_search_input")
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Sort Menu Button
                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier.testTag("sort_menu_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "الترتيب")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                val sortOptions = listOf(
                                    "NATURAL_NAME" to "الاسم (ترتيب طبيعي: فصل 2 قبل 10)",
                                    "CREATED_AT" to "تاريخ الإضافة",
                                    "MODIFIED_AT" to "تاريخ التعديل",
                                    "LAST_READ" to "آخر قراءة",
                                    "WORD_COUNT" to "عدد الكلمات",
                                    "PROGRESS" to "نسبة التقدم"
                                )
                                sortOptions.forEach { (key, label) ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = label,
                                                fontWeight = if (activeSortBy == key) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        trailingIcon = {
                                            if (activeSortBy == key) {
                                                Icon(
                                                    imageVector = if (activeSortAsc) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        onClick = {
                                            val newAsc = if (activeSortBy == key) !activeSortAsc else true
                                            if (currentNovel != null) {
                                                viewModel.updateNovelSort(currentNovel, key, newAsc)
                                            } else {
                                                novels.forEach { viewModel.updateNovelSort(it, key, newAsc) }
                                            }
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // View Mode Cycle Button (LIST -> GRID -> COMPACT)
                        IconButton(
                            onClick = {
                                val nextMode = when (settings.libraryViewMode) {
                                    "LIST" -> "GRID"
                                    "GRID" -> "COMPACT"
                                    else -> "LIST"
                                }
                                viewModel.updateSettings { it.copy(libraryViewMode = nextMode) }
                            },
                            modifier = Modifier.testTag("view_mode_toggle")
                        ) {
                            val icon = when (settings.libraryViewMode) {
                                "GRID" -> Icons.Default.GridView
                                "COMPACT" -> Icons.Default.ViewCompact
                                else -> Icons.Default.ViewList
                            }
                            Icon(icon, contentDescription = "تبديل طريقة العرض")
                        }
                    }
                }
            }

            // Novels (Folders) Strip
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedNovelId == null,
                        onClick = { viewModel.setSelectedNovelId(null) },
                        label = { Text("كل المكتبة (${chapters.size})") },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.LibraryBooks,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                items(novels, key = { it.id }) { novel ->
                    val count = chapters.count { it.novelId == novel.id }
                    val novelColor = parseColorSafe(novel.colorHex, MaterialTheme.colorScheme.primary)
                    FilterChip(
                        selected = selectedNovelId == novel.id,
                        onClick = { viewModel.setSelectedNovelId(novel.id) },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.combinedClickable(
                                    onClick = { viewModel.setSelectedNovelId(novel.id) },
                                    onLongClick = { contextMenuNovel = novel }
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(novelColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${novel.title} ($count)")
                            }
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = novelColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                item {
                    val standaloneCount = chapters.count { it.novelId == null }
                    FilterChip(
                        selected = selectedNovelId == -1L,
                        onClick = { viewModel.setSelectedNovelId(-1L) },
                        label = { Text("ملفات مفردة ($standaloneCount)") }
                    )
                }
            }

            // Status & Translator Stage Filters Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filters = listOf(
                    "ALL" to "الكل",
                    "NEW" to "غير مقروء",
                    "READING" to "قيد القراءة",
                    "COMPLETED" to "مكتمل",
                    "FAVORITE" to "★ المفضلة",
                    "DRAFT" to "مسودة ترجمة",
                    "REVIEWED" to "مُراجَع",
                    "FINAL" to "نهائي"
                )
                filters.forEach { (key, label) ->
                    FilterChip(
                        selected = statusFilter == key,
                        onClick = { viewModel.setStatusFilter(key) },
                        label = { Text(label, style = MaterialTheme.typography.labelLarge) }
                    )
                }
                allTags.forEach { tag ->
                    FilterChip(
                        selected = tagFilter == tag,
                        onClick = {
                            viewModel.setTagFilter(if (tagFilter == tag) null else tag)
                        },
                        label = { Text("#$tag", style = MaterialTheme.typography.labelLarge) }
                    )
                }
            }

            // Main Content Area
            if (filteredChapters.isEmpty()) {
                EmptyLibraryView(
                    onPasteClick = {
                        initialPasteContent = readClipboardText(context)
                        showPasteDialog = true
                    },
                    onImportClick = {
                        filePickerLauncher.launch(arrayOf("*/*"))
                    },
                    onStressTestClick = {
                        viewModel.generate500kWordStressTestChapter(
                            if (selectedNovelId != null && selectedNovelId > 0) selectedNovelId else novels.firstOrNull()?.id
                        )
                    }
                )
            } else {
                when (settings.libraryViewMode) {
                    "GRID" -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 165.dp),
                            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredChapters, key = { it.id }) { chapter ->
                                ChapterCardItem(
                                    chapter = chapter,
                                    novel = novels.find { it.id == chapter.novelId },
                                    settings = settings,
                                    isSelected = selectedChapterIds.contains(chapter.id),
                                    isCompact = false,
                                    searchQuery = searchQuery,
                                    onClick = {
                                        if (selectedChapterIds.isNotEmpty()) {
                                            viewModel.toggleChapterSelection(chapter.id)
                                        } else {
                                            viewModel.openChapterForReading(chapter.id)
                                        }
                                    },
                                    onLongClick = {
                                        if (selectedChapterIds.isEmpty()) {
                                            contextMenuChapter = chapter
                                        } else {
                                            viewModel.toggleChapterSelection(chapter.id)
                                        }
                                    },
                                    onMoreClick = { contextMenuChapter = chapter }
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Top "Continue Reading" Card
                            if (lastReadChapter != null && searchQuery.isBlank() && selectedChapterIds.isEmpty()) {
                                item(key = "continue_reading_card") {
                                    ContinueReadingHeroCard(
                                        chapter = lastReadChapter,
                                        novel = novels.find { it.id == lastReadChapter.novelId },
                                        settings = settings,
                                        onContinueClick = {
                                            viewModel.openChapterForReading(lastReadChapter.id)
                                        }
                                    )
                                }
                            }

                            items(filteredChapters, key = { it.id }) { chapter ->
                                ChapterCardItem(
                                    chapter = chapter,
                                    novel = novels.find { it.id == chapter.novelId },
                                    settings = settings,
                                    isSelected = selectedChapterIds.contains(chapter.id),
                                    isCompact = settings.libraryViewMode == "COMPACT",
                                    searchQuery = searchQuery,
                                    onClick = {
                                        if (selectedChapterIds.isNotEmpty()) {
                                            viewModel.toggleChapterSelection(chapter.id)
                                        } else {
                                            viewModel.openChapterForReading(chapter.id)
                                        }
                                    },
                                    onLongClick = {
                                        if (selectedChapterIds.isEmpty()) {
                                            contextMenuChapter = chapter
                                        } else {
                                            viewModel.toggleChapterSelection(chapter.id)
                                        }
                                    },
                                    onMoreClick = { contextMenuChapter = chapter }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Expandable (+) Floating Action Button Menu
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            AnimatedVisibility(visible = fabExpanded) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Paste from Clipboard (Primary Translator Workflow)
                    ExtendedFloatingActionButton(
                        onClick = {
                            fabExpanded = false
                            initialPasteContent = readClipboardText(context)
                            showPasteDialog = true
                        },
                        icon = { Icon(Icons.Default.ContentPaste, contentDescription = null) },
                        text = { Text("لصق نص / تقسيم فصول") },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag("fab_paste_clipboard")
                    )

                    // 2. Import Multiple Files
                    ExtendedFloatingActionButton(
                        onClick = {
                            fabExpanded = false
                            filePickerLauncher.launch(arrayOf("*/*"))
                        },
                        icon = { Icon(Icons.Default.FileOpen, contentDescription = null) },
                        text = { Text("استيراد ملفات (TXT/DOCX/EPUB/PDF)") },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.testTag("fab_import_files")
                    )

                    // 3. New Blank Chapter/File
                    ExtendedFloatingActionButton(
                        onClick = {
                            fabExpanded = false
                            showNewFileDialog = true
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null) },
                        text = { Text("ملف / فصل جديد") },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.testTag("fab_new_chapter")
                    )

                    // 4. New Novel / Folder
                    ExtendedFloatingActionButton(
                        onClick = {
                            fabExpanded = false
                            showNewNovelDialog = true
                        },
                        icon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
                        text = { Text("رواية / مجلد جديد") },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.testTag("fab_new_novel")
                    )

                    // 5. 500k-Word Performance Stress Test Generator
                    SmallFloatingActionButton(
                        onClick = {
                            fabExpanded = false
                            viewModel.generate500kWordStressTestChapter(
                                if (selectedNovelId != null && selectedNovelId > 0) selectedNovelId else novels.firstOrNull()?.id
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("توليد فصل 500 ألف كلمة (اختبار الأداء)", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            ExtendedFloatingActionButton(
                onClick = { fabExpanded = !fabExpanded },
                icon = {
                    Icon(
                        imageVector = if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "إضافة محتوى"
                    )
                },
                text = { Text(if (fabExpanded) "إغلاق" else "إضافة / لصق") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("main_add_fab")
            )
        }
    }

    // --- Dialogs & Bottom Sheets ---

    if (showPasteDialog) {
        PasteAndAutoSplitDialog(
            initialText = initialPasteContent,
            novels = novels,
            defaultNovelId = if (selectedNovelId != null && selectedNovelId > 0) selectedNovelId else novels.firstOrNull()?.id,
            onDismiss = { showPasteDialog = false },
            onSaveSingle = { novelId, title, content, openNow ->
                viewModel.addSingleChapter(
                    novelId = novelId,
                    title = title,
                    content = content,
                    openImmediately = openNow
                )
                showPasteDialog = false
            },
            onSaveSplitBatch = { novelId, previews ->
                viewModel.addSplitChaptersBatch(novelId, previews)
                showPasteDialog = false
            }
        )
    }

    if (showNewFileDialog) {
        PasteAndAutoSplitDialog(
            initialText = "",
            novels = novels,
            defaultNovelId = if (selectedNovelId != null && selectedNovelId > 0) selectedNovelId else null,
            onDismiss = { showNewFileDialog = false },
            onSaveSingle = { novelId, title, content, openNow ->
                viewModel.addSingleChapter(
                    novelId = novelId,
                    title = title,
                    content = content,
                    openImmediately = openNow
                )
                showNewFileDialog = false
            },
            onSaveSplitBatch = { novelId, previews ->
                viewModel.addSplitChaptersBatch(novelId, previews)
                showNewFileDialog = false
            }
        )
    }

    if (showNewNovelDialog || editingNovel != null) {
        NovelFormDialog(
            existingNovel = editingNovel,
            onDismiss = {
                showNewNovelDialog = false
                editingNovel = null
            },
            onConfirm = { title, desc, colorHex, category, tags ->
                val current = editingNovel
                if (current != null) {
                    viewModel.updateNovelDetails(
                        current.copy(
                            title = title,
                            description = desc,
                            colorHex = colorHex,
                            category = category,
                            tags = tags
                        )
                    )
                } else {
                    viewModel.createNovel(title, desc, colorHex, category, tags)
                }
                showNewNovelDialog = false
                editingNovel = null
            }
        )
    }

    // Long-press Context Menu BottomSheet for Chapter
    val activeMenuChapter = contextMenuChapter
    if (activeMenuChapter != null) {
        ModalBottomSheet(onDismissRequest = { contextMenuChapter = null }) {
            ChapterContextSheetContent(
                chapter = activeMenuChapter,
                onSelectMultiple = {
                    viewModel.toggleChapterSelection(activeMenuChapter.id)
                    contextMenuChapter = null
                },
                onRename = {
                    showRenameDialogFor = activeMenuChapter
                    contextMenuChapter = null
                },
                onEditContent = {
                    viewModel.openSubScreen(SubScreen.EDITOR, activeMenuChapter.id)
                    contextMenuChapter = null
                },
                onConvertFormat = {
                    showConvertFormatDialog = listOf(activeMenuChapter)
                    contextMenuChapter = null
                },
                onMoveTo = {
                    showMoveToNovelDialog = listOf(activeMenuChapter)
                    contextMenuChapter = null
                },
                onDuplicate = {
                    viewModel.duplicateChapter(activeMenuChapter)
                    contextMenuChapter = null
                },
                onSplitChapter = {
                    viewModel.splitChapterIntoMultiple(activeMenuChapter)
                    contextMenuChapter = null
                },
                onToggleReadStatus = {
                    val nextStatus = if (activeMenuChapter.readingStatus == "COMPLETED") "NEW" else "COMPLETED"
                    val nextPercent = if (nextStatus == "COMPLETED") 100f else 0f
                    viewModel.updateChapter(
                        activeMenuChapter.copy(
                            readingStatus = nextStatus,
                            progressPercent = nextPercent,
                            paragraphIndex = if (nextStatus == "NEW") 0 else activeMenuChapter.paragraphIndex
                        )
                    )
                    contextMenuChapter = null
                },
                onToggleFavorite = {
                    viewModel.updateChapter(activeMenuChapter.copy(favorite = !activeMenuChapter.favorite))
                    contextMenuChapter = null
                },
                onChangeTranslatorStage = { newStage ->
                    viewModel.updateChapter(activeMenuChapter.copy(translatorStage = newStage))
                    contextMenuChapter = null
                },
                onShareExport = {
                    shareChapterText(context, activeMenuChapter)
                    contextMenuChapter = null
                },
                onShowDetails = {
                    showDetailsDialogFor = activeMenuChapter
                    contextMenuChapter = null
                },
                onDelete = {
                    viewModel.deleteChaptersToTrash(listOf(activeMenuChapter.id))
                    contextMenuChapter = null
                }
            )
        }
    }

    // Long-press Context Menu for Novel Folder
    val activeMenuNovel = contextMenuNovel
    if (activeMenuNovel != null) {
        AlertDialog(
            onDismissRequest = { contextMenuNovel = null },
            title = { Text("إدارة الرواية: ${activeMenuNovel.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            editingNovel = activeMenuNovel
                            contextMenuNovel = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تعديل الاسم واللون والوسوم")
                    }
                    OutlinedButton(
                        onClick = {
                            val novelChapters = chapters.filter { it.novelId == activeMenuNovel.id }
                            val combined = novelChapters.joinToString("\n\n====================\n\n") {
                                "${it.title}\n\n${it.content}"
                            }
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TITLE, activeMenuNovel.title)
                                putExtra(Intent.EXTRA_TEXT, combined)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "تصدير الرواية كاملة"))
                            contextMenuNovel = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تصدير/مشاركة الرواية كاملة كملف واحد")
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.deleteNovelToTrash(activeMenuNovel)
                            contextMenuNovel = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("نقل الرواية إلى سلة المحذوفات", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { contextMenuNovel = null }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Individual Rename Dialog
    val chapterToRename = showRenameDialogFor
    if (chapterToRename != null) {
        var newName by remember(chapterToRename) { mutableStateOf(chapterToRename.title) }
        var newTags by remember(chapterToRename) { mutableStateOf(chapterToRename.tags) }
        AlertDialog(
            onDismissRequest = { showRenameDialogFor = null },
            title = { Text("إعادة تسمية الفصل والوسوم") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("اسم الفصل") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTags,
                        onValueChange = { newTags = it },
                        label = { Text("الوسوم (مفصولة بفاصلة)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateChapter(
                            chapterToRename.copy(
                                title = newName.trim().ifBlank { chapterToRename.title },
                                tags = newTags.trim()
                            )
                        )
                        showRenameDialogFor = null
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialogFor = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Batch Rename Dialog ("الفصل {رقم}" with start number and zero padding)
    if (showBatchRenameDialog) {
        val selectedList = filteredChapters.filter { selectedChapterIds.contains(it.id) }
        var pattern by remember { mutableStateOf("الفصل {رقم}") }
        var startNumStr by remember { mutableStateOf("1") }
        var zeroPadStr by remember { mutableStateOf("1") }

        AlertDialog(
            onDismissRequest = { showBatchRenameDialog = false },
            title = { Text("إعادة تسمية جماعية (${selectedList.size} فصول)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "استخدم {رقم} في النمط ليتم تعويضه تلقائيًا بالترتيب.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = pattern,
                        onValueChange = { pattern = it },
                        label = { Text("نمط التسمية") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startNumStr,
                            onValueChange = { startNumStr = it.filter { c -> c.isDigit() } },
                            label = { Text("رقم البداية") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = zeroPadStr,
                            onValueChange = { zeroPadStr = it.filter { c -> c.isDigit() } },
                            label = { Text("عدد الأرقام (أصفار)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    val previewNum = (startNumStr.toIntOrNull() ?: 1)
                        .toString()
                        .padStart((zeroPadStr.toIntOrNull() ?: 1).coerceIn(1, 5), '0')
                    Text(
                        text = "معاينة أول عنصر: ${pattern.replace("{رقم}", previewNum)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.batchRenameSelectedChapters(
                            chaptersList = selectedList,
                            pattern = pattern,
                            startNumber = startNumStr.toIntOrNull() ?: 1,
                            zeroPadding = (zeroPadStr.toIntOrNull() ?: 1).coerceIn(1, 5)
                        )
                        showBatchRenameDialog = false
                    }
                ) {
                    Text("تطبيق التسمية")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchRenameDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Convert Format Dialog (TXT <-> MD <-> HTML <-> DOCX <-> PDF)
    val chaptersToConvert = showConvertFormatDialog
    if (chaptersToConvert != null) {
        var targetFormat by remember { mutableStateOf("MD") }
        var saveAsNewCopy by remember { mutableStateOf(false) }
        val formats = listOf("TXT", "MD", "HTML", "DOCX", "PDF")

        AlertDialog(
            onDismissRequest = { showConvertFormatDialog = null },
            title = { Text("تغيير صيغة الملف (${chaptersToConvert.size})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("اختر الصيغة الجديدة المطلوبة:")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        formats.forEach { fmt ->
                            FilterChip(
                                selected = targetFormat == fmt,
                                onClick = { targetFormat = fmt },
                                label = { Text(fmt) }
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { saveAsNewCopy = !saveAsNewCopy }
                    ) {
                        Checkbox(checked = saveAsNewCopy, onCheckedChange = { saveAsNewCopy = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ كنسخة جديدة (بدل استبدال الملف الحالي)")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.convertChaptersFormat(chaptersToConvert, targetFormat, saveAsNewCopy)
                        showConvertFormatDialog = null
                    }
                ) {
                    Text(if (saveAsNewCopy) "حفظ نسخة جديدة" else "استبدال الصيغة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConvertFormatDialog = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Move to Novel Dialog
    val chaptersToMove = showMoveToNovelDialog
    if (chaptersToMove != null) {
        AlertDialog(
            onDismissRequest = { showMoveToNovelDialog = null },
            title = { Text("نقل ${chaptersToMove.size} فصل إلى...") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.moveChaptersToNovel(chaptersToMove, null)
                            showMoveToNovelDialog = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("خارج الروايات (ملف منفرد)")
                    }
                    novels.forEach { novel ->
                        Button(
                            onClick = {
                                viewModel.moveChaptersToNovel(chaptersToMove, novel.id)
                                showMoveToNovelDialog = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(novel.title)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMoveToNovelDialog = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Merge Chapters Dialog
    val chaptersToMerge = showMergeDialog
    if (chaptersToMerge != null) {
        var mergedTitle by remember { mutableStateOf("فصل مدمج (${chaptersToMerge.size} فصول)") }
        var deleteOriginals by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showMergeDialog = null },
            title = { Text("دمج ${chaptersToMerge.size} فصول في ملف واحد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mergedTitle,
                        onValueChange = { mergedTitle = it },
                        label = { Text("اسم الفصل المدمج الجديد") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { deleteOriginals = !deleteOriginals }
                    ) {
                        Checkbox(checked = deleteOriginals, onCheckedChange = { deleteOriginals = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نقل الفصول الأصلية إلى السلة بعد الدمج")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.mergeSelectedChapters(
                            chaptersToMerge = chaptersToMerge,
                            mergedTitle = mergedTitle,
                            targetNovelId = chaptersToMerge.firstOrNull()?.novelId,
                            moveOriginalsToTrash = deleteOriginals
                        )
                        showMergeDialog = null
                    }
                ) {
                    Text("دمج الآن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMergeDialog = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // File Details Dialog
    val detailsChapter = showDetailsDialogFor
    if (detailsChapter != null) {
        val sdf = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
        val sizeBytes = remember(detailsChapter) { detailsChapter.content.toByteArray(Charsets.UTF_8).size }
        val sizeKb = String.format(Locale.US, "%.1f KB", sizeBytes / 1024f)

        AlertDialog(
            onDismissRequest = { showDetailsDialogFor = null },
            title = { Text("تفاصيل الملف") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("الاسم: ${detailsChapter.title}", fontWeight = FontWeight.Bold)
                    Text("الصيغة: ${detailsChapter.format}")
                    Text("الحجم التقريبي: $sizeKb ($sizeBytes بايت)")
                    Text("عدد الكلمات: ${detailsChapter.wordCount} كلمة")
                    Text("عدد الأحرف: ${detailsChapter.content.length} حرف")
                    Text("موضع القراءة المحفوظ: فقرة ${detailsChapter.paragraphIndex + 1}، حرف ${detailsChapter.charOffset}")
                    Text("نسبة التقدم: ${detailsChapter.progressPercent.toInt()}%")
                    Text("حالة الترجمة: ${translatorStageLabel(detailsChapter.translatorStage)}")
                    Text("تاريخ الإضافة: ${sdf.format(Date(detailsChapter.createdAt))}")
                    Text("آخر تعديل: ${sdf.format(Date(detailsChapter.modifiedAt))}")
                    if (detailsChapter.lastReadAt > 0L) {
                        Text("آخر قراءة: ${sdf.format(Date(detailsChapter.lastReadAt))}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailsDialogFor = null }) {
                    Text("إغلاق")
                }
            }
        )
    }
}

@Composable
private fun ContinueReadingHeroCard(
    chapter: ChapterEntity,
    novel: NovelEntity?,
    settings: ReaderSettings,
    onContinueClick: () -> Unit
) {
    val (minutes, isRemaining) = TextProcessingUtils.calculateReadingMinutes(
        wordCount = chapter.wordCount,
        progressPercent = chapter.progressPercent,
        wpm = settings.readingSpeedWpm
    )
    ElevatedCard(
        onClick = onContinueClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("continue_reading_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (settings.language == "en") "CONTINUE READING" else "متابعة القراءة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (novel != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = novel.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = chapter.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "الفقرة ${chapter.paragraphIndex + 1} • ${chapter.progressPercent.toInt()}% • " +
                        if (isRemaining) "متبقي $minutes د" else "⏱ $minutes د",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (chapter.progressPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "متابعة القراءة",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChapterCardItem(
    chapter: ChapterEntity,
    novel: NovelEntity?,
    settings: ReaderSettings,
    isSelected: Boolean,
    isCompact: Boolean,
    searchQuery: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val (minutes, isRemaining) = remember(chapter.wordCount, chapter.progressPercent, settings.readingSpeedWpm) {
        TextProcessingUtils.calculateReadingMinutes(
            wordCount = chapter.wordCount,
            progressPercent = chapter.progressPercent,
            wpm = settings.readingSpeedWpm
        )
    }
    val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("chapter_item_${chapter.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompact) 10.dp else 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "محدد",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = chapter.title,
                        style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (settings.libShowReadTime) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isRemaining) "متبقي $minutes د" else "⏱ $minutes د",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chapter.favorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "مفضل",
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "خيارات الفصل",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // If user searched inside content, show a matching snippet!
            if (searchQuery.isNotBlank() && chapter.content.contains(searchQuery, ignoreCase = true)) {
                val idx = chapter.content.indexOf(searchQuery, ignoreCase = true)
                val start = (idx - 30).coerceAtLeast(0)
                val end = (idx + searchQuery.length + 45).coerceAtMost(chapter.content.length)
                val snippet = "..." + chapter.content.substring(start, end).replace("\n", " ") + "..."
                Text(
                    text = snippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (!isCompact) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Format Badge
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = chapter.format,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Reading Status Badge
                    if (settings.libShowStatus) {
                        val (statusText, statusColor) = when (chapter.readingStatus) {
                            "COMPLETED" -> "مكتمل" to Color(0xFF059669)
                            "READING" -> "قيد القراءة" to Color(0xFFD97706)
                            else -> "جديد" to MaterialTheme.colorScheme.secondary
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Translator Stage Badge
                    if (settings.libShowTranslatorStage) {
                        Text(
                            text = "• ${translatorStageLabel(chapter.translatorStage)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Word Count
                    if (settings.libShowWordCount) {
                        Text(
                            text = "• ${chapter.wordCount} كلمة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Last Modified Date
                    if (settings.libShowModifiedDate) {
                        Text(
                            text = dateFormat.format(Date(chapter.modifiedAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // Progress Bar & Percentage
            if (settings.libShowProgress) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { (chapter.progressPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${chapter.progressPercent.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryView(
    onPasteClick: () -> Unit,
    onImportClick: () -> Unit,
    onStressTestClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_reader_hero_1794140263314),
            contentDescription = "مكتب المترجم والقارئ",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(20.dp))
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "لا توجد فصول مطابقة في هذا العرض",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "ألصق أول فصل لك من الحافظة مباشرة أو استورد ملفاتك للبدء فورًا 100% بدون إنترنت.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onPasteClick) {
                Icon(Icons.Default.ContentPaste, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("لصق أول فصل لك")
            }
            OutlinedButton(onClick = onImportClick) {
                Icon(Icons.Default.FileOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("استيراد ملفات")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onStressTestClick) {
            Text("تجربة توليد فصل بحجم 500 ألف كلمة لاختبار السرعة")
        }
    }
}

@Composable
private fun PasteAndAutoSplitDialog(
    initialText: String,
    novels: List<NovelEntity>,
    defaultNovelId: Long?,
    onDismiss: () -> Unit,
    onSaveSingle: (novelId: Long?, title: String, content: String, openImmediately: Boolean) -> Unit,
    onSaveSplitBatch: (novelId: Long?, previews: List<SplitChapterPreview>) -> Unit
) {
    val context = LocalContext.current
    var pastedContent by remember { mutableStateOf(initialText) }
    var chapterTitle by remember(initialText) {
        mutableStateOf(
            if (initialText.isNotBlank()) TextProcessingUtils.suggestTitleFromContent(initialText) else ""
        )
    }
    var selectedNovelId by remember { mutableStateOf(defaultNovelId) }
    var autoSplitMode by remember { mutableStateOf(false) }

    val splitPreviews = remember(pastedContent, autoSplitMode) {
        if (autoSplitMode && pastedContent.isNotBlank()) {
            TextProcessingUtils.autoSplitIntoChapters(pastedContent, true)
        } else {
            emptyList()
        }
    }
    val liveWordCount = remember(pastedContent) { TextProcessingUtils.countWords(pastedContent) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("لصق وإضافة فصل")
                AssistChip(
                    onClick = {
                        val clip = readClipboardText(context)
                        if (clip.isNotBlank()) {
                            pastedContent = clip
                            if (chapterTitle.isBlank()) {
                                chapterTitle = TextProcessingUtils.suggestTitleFromContent(clip)
                            }
                        }
                    },
                    label = { Text("لصق الحافظة") },
                    leadingIcon = {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Novel selector
                Text("الرواية / المجلد:", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(
                        selected = selectedNovelId == null,
                        onClick = { selectedNovelId = null },
                        label = { Text("ملف منفرد") }
                    )
                    novels.forEach { n ->
                        FilterChip(
                            selected = selectedNovelId == n.id,
                            onClick = { selectedNovelId = n.id },
                            label = { Text(n.title) }
                        )
                    }
                }

                // Toggle Auto-Split Multiple Chapters
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("تقسيم تلقائي لعدة فصول", fontWeight = FontWeight.SemiBold)
                        Text(
                            "يكتشف أنماط مثل «الفصل 12» أو «Chapter 12» مع معاينة قبل الحفظ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = autoSplitMode, onCheckedChange = { autoSplitMode = it })
                }

                if (!autoSplitMode) {
                    OutlinedTextField(
                        value = chapterTitle,
                        onValueChange = { chapterTitle = it },
                        label = { Text("اسم الفصل (يُقترح تلقائيًا من أول سطر)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = pastedContent,
                    onValueChange = { newText ->
                        val wasEmpty = pastedContent.isBlank()
                        pastedContent = newText
                        if (wasEmpty && chapterTitle.isBlank() && newText.isNotBlank()) {
                            chapterTitle = TextProcessingUtils.suggestTitleFromContent(newText)
                        }
                    },
                    label = { Text("منطقة لصق النص ($liveWordCount كلمة)") },
                    minLines = 6,
                    maxLines = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paste_content_input")
                )

                // If autoSplitMode is enabled, show Preview of detected chapters
                if (autoSplitMode && splitPreviews.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "معاينة التقسيم التلقائي (${splitPreviews.size} فصول مكتشفة):",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            splitPreviews.take(8).forEachIndexed { i, item ->
                                Text(
                                    text = "${i + 1}. ${item.title} (${item.wordCount} كلمة)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (splitPreviews.size > 8) {
                                Text(
                                    text = "... و ${splitPreviews.size - 8} فصول إضافية",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (!autoSplitMode) {
                    OutlinedButton(
                        onClick = {
                            if (pastedContent.isNotBlank()) {
                                val finalTitle = chapterTitle.ifBlank {
                                    TextProcessingUtils.suggestTitleFromContent(pastedContent)
                                }
                                onSaveSingle(selectedNovelId, finalTitle, pastedContent, true)
                            }
                        }
                    ) {
                        Text("حفظ وفتح للقراءة")
                    }
                }
                Button(
                    onClick = {
                        if (pastedContent.isNotBlank()) {
                            if (autoSplitMode && splitPreviews.isNotEmpty()) {
                                onSaveSplitBatch(selectedNovelId, splitPreviews)
                            } else {
                                val finalTitle = chapterTitle.ifBlank {
                                    TextProcessingUtils.suggestTitleFromContent(pastedContent)
                                }
                                onSaveSingle(selectedNovelId, finalTitle, pastedContent, false)
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_save_pasted_button")
                ) {
                    Text(if (autoSplitMode) "تأكيد حفظ (${splitPreviews.size}) فصول" else "حفظ")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun NovelFormDialog(
    existingNovel: NovelEntity?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, colorHex: String, category: String, tags: String) -> Unit
) {
    var title by remember { mutableStateOf(existingNovel?.title ?: "") }
    var desc by remember { mutableStateOf(existingNovel?.description ?: "") }
    var colorHex by remember { mutableStateOf(existingNovel?.colorHex ?: NovelColors.first()) }
    var category by remember { mutableStateOf(existingNovel?.category ?: "رواية مترجمة") }
    var tags by remember { mutableStateOf(existingNovel?.tags ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingNovel != null) "تعديل الرواية" else "رواية / مجلد جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم الرواية / المجلد") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("المجموعة / التصنيف") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("الوسوم (مفصولة بفاصلة)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("وصف أو ملاحظات المترجم") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("اللون المميز للرواية:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NovelColors.forEach { hex ->
                        val c = parseColorSafe(hex, Color.Gray)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (colorHex == hex) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, desc, colorHex, category, tags)
                    }
                }
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun ChapterContextSheetContent(
    chapter: ChapterEntity,
    onSelectMultiple: () -> Unit,
    onRename: () -> Unit,
    onEditContent: () -> Unit,
    onConvertFormat: () -> Unit,
    onMoveTo: () -> Unit,
    onDuplicate: () -> Unit,
    onSplitChapter: () -> Unit,
    onToggleReadStatus: () -> Unit,
    onToggleFavorite: () -> Unit,
    onChangeTranslatorStage: (String) -> Unit,
    onShareExport: () -> Unit,
    onShowDetails: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = chapter.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${chapter.wordCount} كلمة • صيغة ${chapter.format} • ${translatorStageLabel(chapter.translatorStage)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Translator Review Stage Selector
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("DRAFT" to "مسودة", "REVIEWED" to "مُراجَع", "FINAL" to "نهائي").forEach { (stage, label) ->
                FilterChip(
                    selected = chapter.translatorStage == stage,
                    onClick = { onChangeTranslatorStage(stage) },
                    label = { Text("حالة المترجم: $label") }
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        ContextSheetItem(Icons.Default.Edit, "تحرير ومراجعة النص داخل التطبيق", onEditContent)
        ContextSheetItem(Icons.Default.DriveFileRenameOutline, "إعادة تسمية وتعديل الوسوم", onRename)
        ContextSheetItem(Icons.Default.SelectAll, "تحديد متعدد (إجراءات جماعية)", onSelectMultiple)
        ContextSheetItem(Icons.Default.SyncAlt, "تغيير الصيغة (TXT / MD / HTML / DOCX / PDF)", onConvertFormat)
        ContextSheetItem(Icons.Default.DriveFileMove, "نقل إلى رواية أخرى...", onMoveTo)
        ContextSheetItem(Icons.Default.ContentCopy, "تكرار الفصل", onDuplicate)
        ContextSheetItem(Icons.Default.AutoAwesomeMotion, "تقسيم هذا الملف إلى عدة فصول", onSplitChapter)
        ContextSheetItem(
            Icons.Default.CheckCircle,
            if (chapter.readingStatus == "COMPLETED") "تمييز كغير مقروء" else "تمييز كمقروء بالكامل",
            onToggleReadStatus
        )
        ContextSheetItem(
            if (chapter.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            if (chapter.favorite) "إزالة من المفضلة" else "إضافة للمفضلة",
            onToggleFavorite
        )
        ContextSheetItem(Icons.Default.Share, "تصدير / مشاركة الفصل", onShareExport)
        ContextSheetItem(Icons.Default.Info, "تفاصيل الملف (الحجم، الكلمات، التواريخ)", onShowDetails)
        ContextSheetItem(
            Icons.Default.Delete,
            "حذف (ينتقل لسلة المحذوفات 30 يومًا)",
            onDelete,
            isDestructive = true
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ContextSheetItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = tint
        )
    }
}

fun translatorStageLabel(stage: String): String = when (stage) {
    "REVIEWED" -> "مُراجَع"
    "FINAL" -> "نهائي"
    else -> "مسودة"
}

fun parseColorSafe(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}

private fun readClipboardText(context: Context): String {
    return try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val item = clipboard?.primaryClip?.getItemAt(0)
        item?.coerceToText(context)?.toString() ?: ""
    } catch (_: Exception) {
        ""
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var name = "فصل_مستورد.txt"
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex) ?: name
            }
        }
    } catch (_: Exception) {
    }
    return name
}

private fun shareChapterText(context: Context, chapter: ChapterEntity) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, chapter.title)
        putExtra(Intent.EXTRA_TEXT, "${chapter.title}\n\n${chapter.content}")
    }
    context.startActivity(Intent.createChooser(sendIntent, "مشاركة الفصل"))
}
