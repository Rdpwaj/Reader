package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AnnotationEntity
import com.example.data.ChapterEntity
import com.example.data.GlossaryEntity
import com.example.data.NovelEntity
import com.example.data.TextProcessingUtils
import com.example.ui.theme.HighlightColors
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlin.math.max

/**
 * 1) In-App Translator Text Editor with Auto-Save, Undo/Redo, and Live Word Count
 */
@Composable
fun InAppEditorScreen(
    viewModel: MainViewModel,
    chapter: ChapterEntity,
    glossaryTerms: List<GlossaryEntity>,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var title by remember(chapter.id) { mutableStateOf(chapter.title) }
    var content by remember(chapter.id) { mutableStateOf(chapter.content) }
    var stage by remember(chapter.id) { mutableStateOf(chapter.translatorStage) }
    var autoSavedStatus by remember { mutableStateOf("تم الحفظ تلقائيًا") }

    val undoStack = remember { mutableStateListOf<String>() }
    val redoStack = remember { mutableStateListOf<String>() }

    val liveWords = remember(content) { TextProcessingUtils.countWords(content) }
    val liveChars = content.length

    // Auto-save every 3 seconds when edited
    LaunchedEffect(title, content, stage) {
        autoSavedStatus = "جاري الحفظ التلقائي..."
        delay(2000L)
        viewModel.updateChapter(
            chapter.copy(
                title = title.trim().ifBlank { chapter.title },
                content = content,
                translatorStage = stage,
                wordCount = liveWords
            )
        )
        autoSavedStatus = "تم الحفظ تلقائيًا ✓"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Editor Top Bar
        Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "محرر المترجم",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$liveWords كلمة • $liveChars حرف • $autoSavedStatus",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Undo Button
                IconButton(
                    onClick = {
                        if (undoStack.isNotEmpty()) {
                            val previous = undoStack.removeAt(undoStack.lastIndex)
                            redoStack.add(content)
                            content = previous
                        }
                    },
                    enabled = undoStack.isNotEmpty(),
                    modifier = Modifier.testTag("editor_undo_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "تراجع")
                }

                // Redo Button
                IconButton(
                    onClick = {
                        if (redoStack.isNotEmpty()) {
                            val next = redoStack.removeAt(redoStack.lastIndex)
                            undoStack.add(content)
                            content = next
                        }
                    },
                    enabled = redoStack.isNotEmpty(),
                    modifier = Modifier.testTag("editor_redo_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "إعادة")
                }

                // Clean formatting button
                IconButton(
                    onClick = {
                        undoStack.add(content)
                        content = TextProcessingUtils.cleanPastedText(content)
                        viewModel.showToastMessage("تم تنظيف المسافات والأسطر الزائدة")
                    }
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = "إصلاح المسافات")
                }

                // Save & Open in Reader
                IconButton(
                    onClick = {
                        viewModel.updateChapter(
                            chapter.copy(
                                title = title.trim().ifBlank { chapter.title },
                                content = content,
                                translatorStage = stage,
                                wordCount = liveWords
                            )
                        )
                        onBack()
                        viewModel.openChapterForReading(chapter.id)
                    }
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = "قراءة الفصل")
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Translator Stage Chips + Quick Glossary Chips
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                listOf("DRAFT" to "مسودة", "REVIEWED" to "مُراجَع", "FINAL" to "نهائي").forEach { (key, label) ->
                    FilterChip(
                        selected = stage == key,
                        onClick = { stage = key },
                        label = { Text("الحالة: $label") }
                    )
                }
                glossaryTerms.take(8).forEach { term ->
                    AssistChip(
                        onClick = {
                            undoStack.add(content)
                            content = "$content ${term.term}"
                        },
                        label = { Text("+ ${term.term}") }
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان الفصل") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { newText ->
                    if (undoStack.isEmpty() || (newText.length - content.length).let { it > 10 || it < -10 }) {
                        undoStack.add(content)
                        if (undoStack.size > 40) undoStack.removeAt(0)
                        redoStack.clear()
                    }
                    content = newText
                },
                label = { Text("محتوى الفصل المترجم") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("editor_content_input")
            )
        }
    }
}

/**
 * 2) Find and Replace Screen (across single chapter or entire novel with preview)
 */
@Composable
fun FindAndReplaceScreen(
    viewModel: MainViewModel,
    novels: List<NovelEntity>,
    chapters: List<ChapterEntity>,
    initialChapterId: Long?,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var matchCase by remember { mutableStateOf(false) }
    var scopeMode by remember {
        mutableStateOf(if (initialChapterId != null) "SINGLE_CHAPTER" else "ALL_LIBRARY")
    }
    var selectedNovelId by remember { mutableStateOf(novels.firstOrNull()?.id) }

    val targetChapters = remember(scopeMode, initialChapterId, selectedNovelId, chapters) {
        when (scopeMode) {
            "SINGLE_CHAPTER" -> chapters.filter { it.id == initialChapterId }
            "NOVEL" -> chapters.filter { it.novelId == selectedNovelId }
            else -> chapters
        }
    }

    // Live Match Preview before applying
    val matchPreviews = remember(targetChapters, findText, matchCase) {
        if (findText.isBlank()) emptyList()
        else {
            val regex = if (matchCase) Regex(Regex.escape(findText))
            else Regex(Regex.escape(findText), RegexOption.IGNORE_CASE)

            targetChapters.mapNotNull { ch ->
                val matches = regex.findAll(ch.content).toList()
                if (matches.isEmpty()) null
                else {
                    val firstRange = matches.first().range
                    val s = (firstRange.first - 25).coerceAtLeast(0)
                    val e = (firstRange.last + 35).coerceAtMost(ch.content.length)
                    Triple(ch, matches.size, ch.content.substring(s, e).replace("\n", " "))
                }
            }
        }
    }
    val totalMatchesCount = matchPreviews.sumOf { it.second }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
                Text(
                    text = "بحث واستبدال (توحيد أسماء الشخصيات والمصطلحات)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text("نطاق البحث والاستبدال:", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    if (initialChapterId != null) {
                        FilterChip(
                            selected = scopeMode == "SINGLE_CHAPTER",
                            onClick = { scopeMode = "SINGLE_CHAPTER" },
                            label = { Text("الفصل الحالي فقط") }
                        )
                    }
                    FilterChip(
                        selected = scopeMode == "NOVEL",
                        onClick = { scopeMode = "NOVEL" },
                        label = { Text("رواية محددة كاملة") }
                    )
                    FilterChip(
                        selected = scopeMode == "ALL_LIBRARY",
                        onClick = { scopeMode = "ALL_LIBRARY" },
                        label = { Text("كل الفصول في المكتبة") }
                    )
                }

                if (scopeMode == "NOVEL") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        novels.forEach { n ->
                            FilterChip(
                                selected = selectedNovelId == n.id,
                                onClick = { selectedNovelId = n.id },
                                label = { Text(n.title) }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = findText,
                    onValueChange = { findText = it },
                    label = { Text("ابحث عن الكلمة أو الاسم (مثال: اريس)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("find_query_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = replaceText,
                    onValueChange = { replaceText = it },
                    label = { Text("استبدل بـ (مثال: آريس)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("replace_with_input")
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { matchCase = !matchCase }
                ) {
                    Checkbox(checked = matchCase, onCheckedChange = { matchCase = it })
                    Text("مطابقة حالة الأحرف (للنصوص الإنجليزية)")
                }
            }

            item {
                Button(
                    onClick = {
                        if (findText.isNotBlank() && totalMatchesCount > 0) {
                            viewModel.performFindAndReplace(
                                targetChapters = targetChapters,
                                findQuery = findText,
                                replaceWith = replaceText,
                                matchCase = matchCase
                            )
                        }
                    },
                    enabled = findText.isNotBlank() && totalMatchesCount > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("execute_replace_button")
                ) {
                    Icon(Icons.Default.FindReplace, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تطبيق الاستبدال ($totalMatchesCount تطابق في ${matchPreviews.size} فصول)")
                }
            }

            item {
                Text(
                    text = "معاينة النتائج قبل التطبيق ($totalMatchesCount تطابق):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(matchPreviews) { (ch, count, snippet) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(ch.title, fontWeight = FontWeight.Bold)
                            Text("$count تطابق", color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("...$snippet...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

/**
 * 3) Glossary Screen (قاموس المصطلحات وأسماء الشخصيات لكل رواية)
 */
@Composable
fun GlossaryScreen(
    viewModel: MainViewModel,
    novels: List<NovelEntity>,
    glossary: List<GlossaryEntity>,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var selectedNovelFilter by remember { mutableStateOf<Long?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddOrEditDialog by remember { mutableStateOf(false) }
    var editingTerm by remember { mutableStateOf<GlossaryEntity?>(null) }

    val filteredGlossary = remember(glossary, selectedNovelFilter, searchQuery) {
        glossary.filter { item ->
            val novelMatch = selectedNovelFilter == null || item.novelId == selectedNovelFilter
            val searchMatch = searchQuery.isBlank() ||
                item.term.contains(searchQuery, ignoreCase = true) ||
                item.translation.contains(searchQuery, ignoreCase = true) ||
                item.note.contains(searchQuery, ignoreCase = true)
            novelMatch && searchMatch
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "قاموس المصطلحات والأسماء (${filteredGlossary.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تُظلّل كلمات القاموس تلقائيًا داخل فصول الرواية لتوحيد الترجمة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث في المصطلحات أو الأسماء...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(
                        selected = selectedNovelFilter == null,
                        onClick = { selectedNovelFilter = null },
                        label = { Text("كل الروايات") }
                    )
                    novels.forEach { n ->
                        FilterChip(
                            selected = selectedNovelFilter == n.id,
                            onClick = { selectedNovelFilter = n.id },
                            label = { Text(n.title) }
                        )
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredGlossary, key = { it.id }) { item ->
                    val novelName = novels.find { it.id == item.novelId }?.title ?: "قاموس عام"
                    val badgeColor = parseColorSafe(item.highlightColorHex, Color(0xFFFDE047))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.term,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${item.category} • $novelName",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = item.translation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (item.note.isNotBlank()) {
                                    Text(
                                        text = item.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = {
                                editingTerm = item
                                showAddOrEditDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل")
                            }
                            IconButton(onClick = { viewModel.deleteGlossaryTerm(item.id) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingTerm = null
                showAddOrEditDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_glossary_term_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة مصطلح")
        }
    }

    if (showAddOrEditDialog) {
        var term by remember { mutableStateOf(editingTerm?.term ?: "") }
        var translation by remember { mutableStateOf(editingTerm?.translation ?: "") }
        var category by remember { mutableStateOf(editingTerm?.category ?: "شخصية") }
        var note by remember { mutableStateOf(editingTerm?.note ?: "") }
        var colorHex by remember { mutableStateOf(editingTerm?.highlightColorHex ?: HighlightColors.first()) }
        var novelId by remember { mutableStateOf(editingTerm?.novelId ?: novels.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { showAddOrEditDialog = false },
            title = { Text(if (editingTerm != null) "تعديل مصطلح" else "إضافة مصطلح أو اسم جديد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = term,
                        onValueChange = { term = it },
                        label = { Text("الكلمة المترجمة في النص (مثال: آريس)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = translation,
                        onValueChange = { translation = it },
                        label = { Text("الأصل أو المعنى (مثال: Ares - بطل الرواية)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("شخصية", "مكان", "مهارة", "مصطلح").forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("ملاحظة للمترجم") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("لون التظليل داخل النص:", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HighlightColors.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(parseColorSafe(hex, Color.Yellow))
                                    .border(
                                        width = if (colorHex == hex) 2.dp else 0.dp,
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
                        if (term.isNotBlank()) {
                            viewModel.saveGlossaryTerm(
                                novelId = novelId,
                                term = term,
                                translation = translation,
                                category = category,
                                note = note,
                                colorHex = colorHex,
                                existingId = editingTerm?.id ?: 0L
                            )
                            showAddOrEditDialog = false
                        }
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddOrEditDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

/**
 * 4) Unified Bookmarks, Highlights & Notes Screen
 */
@Composable
fun AnnotationsHubScreen(
    viewModel: MainViewModel,
    annotations: List<AnnotationEntity>,
    chapters: List<ChapterEntity>,
    novels: List<NovelEntity>,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var typeFilter by remember { mutableStateOf("ALL") } // ALL, BOOKMARK, HIGHLIGHT, NOTE
    val filtered = remember(annotations, typeFilter) {
        annotations.filter { typeFilter == "ALL" || it.type == typeFilter }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
                Text(
                    text = "العلامات والتظليلات والملاحظات (${filtered.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            listOf(
                "ALL" to "الكل",
                "BOOKMARK" to "العلامات المرجعية",
                "HIGHLIGHT" to "التظليلات",
                "NOTE" to "الملاحظات"
            ).forEach { (key, label) ->
                FilterChip(
                    selected = typeFilter == key,
                    onClick = { typeFilter = key },
                    label = { Text(label) }
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered, key = { it.id }) { ann ->
                val ch = chapters.find { it.id == ann.chapterId }
                val novel = novels.find { it.id == ann.novelId }
                Card(
                    onClick = {
                        if (ch != null) {
                            viewModel.saveReadingPosition(ch.id, ann.paragraphIndex, ann.startOffset, 100)
                            viewModel.closeSubScreen()
                            viewModel.openChapterForReading(ch.id)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${ch?.title ?: "فصل"} • فقرة #${ann.paragraphIndex + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { viewModel.deleteAnnotation(ann.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(18.dp))
                            }
                        }
                        if (novel != null) {
                            Text(novel.title, style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (ann.selectedText.isNotBlank()) {
                            Surface(
                                color = parseColorSafe(ann.colorHex, Color.Yellow).copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "«${ann.selectedText}»",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                        if (ann.noteText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📝 ${ann.noteText}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 5) 30-Day Trash Bin Screen (سلة المحذوفات 30 يومًا مع استعادة وحذف نهائي بتأكيد)
 */
@Composable
fun TrashBinScreen(
    viewModel: MainViewModel,
    deletedNovels: List<NovelEntity>,
    deletedChapters: List<ChapterEntity>,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var showEmptyTrashConfirm by remember { mutableStateOf(false) }
    var chapterToDeleteForever by remember { mutableStateOf<ChapterEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                    Column {
                        Text(
                            text = "سلة المحذوفات (تُحفظ 30 يومًا)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${deletedChapters.size} فصول • ${deletedNovels.size} روايات",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                if (deletedChapters.isNotEmpty() || deletedNovels.isNotEmpty()) {
                    TextButton(
                        onClick = { showEmptyTrashConfirm = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إفراغ السلة")
                    }
                }
            }
        }

        if (deletedChapters.isEmpty() && deletedNovels.isEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("سلة المحذوفات فارغة تمامًا", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(deletedNovels, key = { "novel_${it.id}" }) { novel ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("رواية: ${novel.title}", fontWeight = FontWeight.Bold)
                                Text("قابلة للاستعادة خلال 30 يومًا", style = MaterialTheme.typography.labelSmall)
                            }
                            Row {
                                IconButton(onClick = { viewModel.restoreNovelFromTrash(novel) }) {
                                    Icon(Icons.Default.RestoreFromTrash, contentDescription = "استعادة")
                                }
                                IconButton(onClick = { viewModel.permanentlyDeleteNovel(novel.id) }) {
                                    Icon(
                                        Icons.Default.DeleteForever,
                                        contentDescription = "حذف نهائي",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                items(deletedChapters, key = { "ch_${it.id}" }) { ch ->
                    val daysLeft = remember(ch.deletedAt) {
                        val elapsedDays = ((System.currentTimeMillis() - (ch.deletedAt ?: System.currentTimeMillis())) / 86400_000L).toInt()
                        max(1, 30 - elapsedDays)
                    }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ch.title,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${ch.wordCount} كلمة • متبقي $daysLeft يوم قبل الحذف التلقائي",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row {
                                IconButton(onClick = { viewModel.restoreChapterFromTrash(ch.id) }) {
                                    Icon(
                                        Icons.Default.RestoreFromTrash,
                                        contentDescription = "استعادة",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { chapterToDeleteForever = ch }) {
                                    Icon(
                                        Icons.Default.DeleteForever,
                                        contentDescription = "حذف نهائي",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEmptyTrashConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashConfirm = false },
            title = { Text("تأكيد إفراغ سلة المحذوفات نهائيًا") },
            text = { Text("هل أنت متأكد؟ لا يمكن استرجاع الفصول بعد الحذف النهائي.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.emptyTrash()
                        showEmptyTrashConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف نهائي للكل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    val targetDeleteCh = chapterToDeleteForever
    if (targetDeleteCh != null) {
        AlertDialog(
            onDismissRequest = { chapterToDeleteForever = null },
            title = { Text("حذف نهائي للفصل") },
            text = { Text("هل تريد حذف «${targetDeleteCh.title}» نهائيًا دون إمكانية استعادة؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.permanentlyDeleteChapter(targetDeleteCh.id)
                        chapterToDeleteForever = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف نهائي")
                }
            },
            dismissButton = {
                TextButton(onClick = { chapterToDeleteForever = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
