package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AnnotationEntity
import com.example.data.AppDatabase
import com.example.data.ChapterEntity
import com.example.data.GlossaryEntity
import com.example.data.NovelEntity
import com.example.data.ReaderRepository
import com.example.data.ReaderSettings
import com.example.data.ReadingStatsEntity
import com.example.data.SplitChapterPreview
import com.example.data.TextProcessingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BottomNavTab {
    LIBRARY, CONTINUE, STATS, SETTINGS
}

enum class SubScreen {
    NONE,
    EDITOR,
    FIND_REPLACE,
    GLOSSARY,
    ANNOTATIONS,
    TRASH
}

data class UndoTrashBanner(
    val message: String,
    val deletedChapterIds: List<Long> = emptyList(),
    val deletedNovel: NovelEntity? = null
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = ReaderRepository(application, database.appDao())

    val globalSettings: StateFlow<ReaderSettings> = repository.globalSettingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ReaderSettings())

    val novels: StateFlow<List<NovelEntity>> = repository.activeNovelsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val deletedNovels: StateFlow<List<NovelEntity>> = repository.deletedNovelsFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Lightweight chapter list (0 bytes of body content in RAM!)
    val chapters: StateFlow<List<ChapterEntity>> = repository.activeChaptersFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val deletedChapters: StateFlow<List<ChapterEntity>> = repository.deletedChaptersFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val annotations: StateFlow<List<AnnotationEntity>> = repository.allAnnotationsFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val glossary: StateFlow<List<GlossaryEntity>> = repository.allGlossaryFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val readingStats: StateFlow<List<ReadingStatsEntity>> = repository.readingStatsFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Navigation & UI State
    private val _currentTab = MutableStateFlow(BottomNavTab.LIBRARY)
    val currentTab: StateFlow<BottomNavTab> = _currentTab.asStateFlow()

    private val _activeChapterId = MutableStateFlow<Long?>(null)
    val activeChapterId: StateFlow<Long?> = _activeChapterId.asStateFlow()

    // Full chapter content loaded ONLY for the single currently active chapter in Reader
    val activeReadingChapter: StateFlow<ChapterEntity?> = _activeChapterId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.observeFullChapterById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _activeSubScreen = MutableStateFlow(SubScreen.NONE)
    val activeSubScreen: StateFlow<SubScreen> = _activeSubScreen.asStateFlow()

    private val _editingChapterId = MutableStateFlow<Long?>(null)
    val editingChapterId: StateFlow<Long?> = _editingChapterId.asStateFlow()

    // Full chapter content loaded ONLY for the single chapter being edited in Editor
    val activeEditingChapter: StateFlow<ChapterEntity?> = _editingChapterId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.observeFullChapterById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Library Filter & Selection State
    private val _selectedNovelId = MutableStateFlow<Long?>(null)
    val selectedNovelId: StateFlow<Long?> = _selectedNovelId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Fast SQLite full-text search matching IDs (debounced so typing is instant)
    val searchMatchedChapterIds: StateFlow<Set<Long>> = _searchQuery
        .debounce(180L)
        .mapLatest { q ->
            if (q.isBlank()) emptySet() else repository.searchActiveChapterIds(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _statusFilter = MutableStateFlow("ALL")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _tagFilter = MutableStateFlow<String?>(null)
    val tagFilter: StateFlow<String?> = _tagFilter.asStateFlow()

    private val _selectedChapterIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedChapterIds: StateFlow<Set<Long>> = _selectedChapterIds.asStateFlow()

    private val _undoBanner = MutableStateFlow<UndoTrashBanner?>(null)
    val undoBanner: StateFlow<UndoTrashBanner?> = _undoBanner.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _sharedIncomingText = MutableStateFlow<String?>(null)
    val sharedIncomingText: StateFlow<String?> = _sharedIncomingText.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.initializeAndSeed()
        }
    }

    fun selectTab(tab: BottomNavTab) {
        _activeSubScreen.value = SubScreen.NONE
        _currentTab.value = tab
    }

    fun openSubScreen(subScreen: SubScreen, chapterIdForEdit: Long? = null) {
        if (chapterIdForEdit != null) {
            _editingChapterId.value = chapterIdForEdit
        }
        _activeSubScreen.value = subScreen
    }

    fun closeSubScreen() {
        _activeSubScreen.value = SubScreen.NONE
    }

    fun openChapterForReading(chapterId: Long) {
        _activeChapterId.value = chapterId
    }

    fun closeReader() {
        _activeChapterId.value = null
    }

    fun setSelectedNovelId(novelId: Long?) {
        _selectedNovelId.value = novelId
        clearMultiSelection()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun setTagFilter(tag: String?) {
        _tagFilter.value = tag
    }

    fun toggleChapterSelection(chapterId: Long) {
        val current = _selectedChapterIds.value.toMutableSet()
        if (current.contains(chapterId)) {
            current.remove(chapterId)
        } else {
            current.add(chapterId)
        }
        _selectedChapterIds.value = current
    }

    fun selectAllChapters(ids: List<Long>) {
        _selectedChapterIds.value = ids.toSet()
    }

    fun clearMultiSelection() {
        _selectedChapterIds.value = emptySet()
    }

    fun showToastMessage(msg: String) {
        _statusMessage.value = msg
    }

    fun clearToastMessage() {
        _statusMessage.value = null
    }

    fun onReceiveSharedText(text: String) {
        if (text.isNotBlank()) {
            _sharedIncomingText.value = text
        }
    }

    fun consumeSharedIncomingText() {
        _sharedIncomingText.value = null
    }

    // --- Settings Updates ---
    fun updateSettings(transform: (ReaderSettings) -> ReaderSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = transform(globalSettings.value)
            repository.saveGlobalSettings(updated)
        }
    }

    fun saveNovelSettingsOverride(novelId: Long, overrideSettings: ReaderSettings?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveNovelSettingsOverride(novelId, overrideSettings)
            showToastMessage(if (overrideSettings != null) "تم تخصيص الإعدادات لهذه الرواية" else "تمت العودة للإعدادات العامة")
        }
    }

    fun resetSettingsToDefault() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentLang = globalSettings.value.language
            repository.saveGlobalSettings(ReaderSettings(language = currentLang, onboardingCompleted = true))
            showToastMessage("تمت إعادة جميع الإعدادات إلى الوضع الافتراضي")
        }
    }

    // --- Novel & Chapter Creation ---
    fun createNovel(title: String, description: String, colorHex: String, category: String, tags: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.createNovel(title, description, colorHex, category, tags)
            _selectedNovelId.value = id
            showToastMessage("تم إنشاء الرواية: $title")
        }
    }

    fun updateNovelSort(novel: NovelEntity, sortBy: String, ascending: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNovel(novel.copy(sortBy = sortBy, sortAscending = ascending))
        }
    }

    fun updateNovelDetails(novel: NovelEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNovel(novel)
            showToastMessage("تم تحديث بيانات الرواية")
        }
    }

    fun addSingleChapter(
        novelId: Long?,
        title: String,
        content: String,
        format: String = "TXT",
        translatorStage: String = "DRAFT",
        tags: String = "",
        openImmediately: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val cleanContent = if (globalSettings.value.autoFixPastedWhitespace) {
                TextProcessingUtils.cleanPastedText(content)
            } else {
                content
            }
            val newId = repository.createChapter(
                novelId = novelId,
                title = title,
                content = cleanContent,
                format = format,
                translatorStage = translatorStage,
                tags = tags
            )
            showToastMessage("تم حفظ الفصل (${TextProcessingUtils.countWords(cleanContent)} كلمة)")
            if (openImmediately) {
                _activeChapterId.value = newId
            }
        }
    }

    fun addSplitChaptersBatch(novelId: Long?, previews: List<SplitChapterPreview>, format: String = "TXT") {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createChaptersBatch(novelId, previews, format)
            showToastMessage("تم تقسيم النص وإضافة ${previews.size} فصول بنجاح")
        }
    }

    fun importFilesFromUris(uris: List<Uri>, fileNames: List<String>, targetNovelId: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            var importedCount = 0
            uris.forEachIndexed { idx, uri ->
                val rawName = fileNames.getOrNull(idx) ?: "فصل مستورد ${idx + 1}.txt"
                val (extractedText, detectedFormat) = TextProcessingUtils.extractTextFromUri(context, uri, rawName)
                if (extractedText.isNotBlank()) {
                    val cleanTitle = rawName.substringBeforeLast('.').ifBlank {
                        TextProcessingUtils.suggestTitleFromContent(extractedText)
                    }
                    repository.createChapter(
                        novelId = targetNovelId,
                        title = cleanTitle,
                        content = extractedText,
                        format = detectedFormat
                    )
                    importedCount++
                }
            }
            showToastMessage("تم استيراد $importedCount ملفات بنجاح")
        }
    }

    fun generate500kWordStressTestChapter(novelId: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            showToastMessage("جاري توليد فصل بحجم 500,000 كلمة لاختبار الأداء الفائق...")
            val hugeText = TextProcessingUtils.generate500kWordStressTestContent()
            val id = repository.createChapter(
                novelId = novelId,
                title = "فصل اختبار الأداء الضخم (500 ألف كلمة)",
                content = hugeText,
                format = "TXT",
                tags = "اختبار أداء"
            )
            showToastMessage("تم إنشاء فصل 500 ألف كلمة! افتحه لتجربة العرض الفوري بدون تعليق")
            _activeChapterId.value = id
        }
    }

    // --- File Management Actions ---
    fun updateChapter(chapter: ChapterEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateChapter(chapter)
        }
    }

    fun duplicateChapter(chapter: ChapterEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.duplicateChapter(chapter)
            showToastMessage("تم تكرار الفصل: ${chapter.title}")
        }
    }

    fun convertChaptersFormat(chaptersToConvert: List<ChapterEntity>, targetFormat: String, saveAsNewCopy: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            chaptersToConvert.forEach { ch ->
                repository.convertChapterFormat(ch, targetFormat, saveAsNewCopy)
            }
            clearMultiSelection()
            showToastMessage(
                if (saveAsNewCopy) "تم حفظ نسخة جديدة بصيغة $targetFormat"
                else "تم تحويل الصيغة إلى $targetFormat"
            )
        }
    }

    fun batchRenameSelectedChapters(chaptersList: List<ChapterEntity>, pattern: String, startNumber: Int, zeroPadding: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.batchRenameChapters(chaptersList, pattern, startNumber, zeroPadding)
            clearMultiSelection()
            showToastMessage("تمت إعادة تسمية ${chaptersList.size} فصول")
        }
    }

    fun moveChaptersToNovel(chaptersToMove: List<ChapterEntity>, targetNovelId: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            chaptersToMove.forEach { ch ->
                repository.updateChapter(ch.copy(novelId = targetNovelId))
            }
            clearMultiSelection()
            showToastMessage("تم نقل ${chaptersToMove.size} فصول")
        }
    }

    fun mergeSelectedChapters(chaptersToMerge: List<ChapterEntity>, mergedTitle: String, targetNovelId: Long?, moveOriginalsToTrash: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.mergeChapters(chaptersToMerge, mergedTitle, targetNovelId, moveOriginalsToTrash)
            clearMultiSelection()
            showToastMessage("تم دمج ${chaptersToMerge.size} فصول في ملف واحد")
        }
    }

    fun splitChapterIntoMultiple(chapter: ChapterEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val full = repository.getFullChapterById(chapter.id) ?: chapter
            val splitParts = TextProcessingUtils.autoSplitIntoChapters(full.content, true)
            if (splitParts.size <= 1) {
                val paras = TextProcessingUtils.splitIntoParagraphs(full.content)
                if (paras.size < 2) {
                    showToastMessage("الفصل قصير جدًا ولا يمكن تقسيمه")
                    return@launch
                }
                val mid = paras.size / 2
                val part1 = paras.subList(0, mid).joinToString("\n\n")
                val part2 = paras.subList(mid, paras.size).joinToString("\n\n")
                repository.createChapter(full.novelId, "${full.title} - جزء 1", part1, full.format)
                repository.createChapter(full.novelId, "${full.title} - جزء 2", part2, full.format)
                showToastMessage("تم تقسيم الفصل إلى جزأين")
            } else {
                repository.createChaptersBatch(full.novelId, splitParts, full.format)
                showToastMessage("تم تقسيم الفصل إلى ${splitParts.size} فصول")
            }
        }
    }

    fun deleteChaptersToTrash(chapterIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.moveChaptersToTrash(chapterIds)
            clearMultiSelection()
            _undoBanner.value = UndoTrashBanner(
                message = "تم نقل ${chapterIds.size} فصل إلى سلة المحذوفات (30 يومًا)",
                deletedChapterIds = chapterIds
            )
        }
    }

    fun deleteNovelToTrash(novel: NovelEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.moveNovelToTrash(novel.id)
            if (_selectedNovelId.value == novel.id) {
                _selectedNovelId.value = null
            }
            _undoBanner.value = UndoTrashBanner(
                message = "تم نقل رواية «${novel.title}» إلى السلة",
                deletedNovel = novel
            )
        }
    }

    fun undoLastTrashAction() {
        val banner = _undoBanner.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            if (banner.deletedChapterIds.isNotEmpty()) {
                repository.restoreChaptersFromTrash(banner.deletedChapterIds)
            }
            if (banner.deletedNovel != null) {
                repository.restoreNovelFromTrash(banner.deletedNovel)
            }
            _undoBanner.value = null
            showToastMessage("تم التراجع واستعادة العناصر بنجاح")
        }
    }

    fun dismissUndoBanner() {
        _undoBanner.value = null
    }

    fun restoreChapterFromTrash(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreChaptersFromTrash(listOf(id))
            showToastMessage("تمت استعادة الفصل من السلة")
        }
    }

    fun restoreNovelFromTrash(novel: NovelEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreNovelFromTrash(novel)
            showToastMessage("تمت استعادة الرواية وفصولها")
        }
    }

    fun permanentlyDeleteChapter(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.permanentlyDeleteChapter(id)
            showToastMessage("تم الحذف النهائي للفصل")
        }
    }

    fun permanentlyDeleteNovel(novelId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.permanentlyDeleteNovel(novelId)
            showToastMessage("تم الحذف النهائي للرواية")
        }
    }

    fun emptyTrash() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.emptyAllTrash()
            showToastMessage("تم إفراغ سلة المحذوفات بالكامل")
        }
    }

    // --- Reading Position & Auto-Learning WPM ---
    fun saveReadingPosition(chapterId: Long, paragraphIndex: Int, charOffset: Int, totalParagraphs: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveReadingPosition(chapterId, paragraphIndex, charOffset, totalParagraphs)
        }
    }

    fun recordSessionMetrics(minutesSpent: Int, wordsReadInSession: Int, chapterCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordReadingProgress(
                minutesDelta = minutesSpent,
                wordsDelta = wordsReadInSession,
                completedChapterDelta = if (chapterCompleted) 1 else 0
            )
            val currentSettings = globalSettings.value
            if (currentSettings.autoLearnReadingSpeed && minutesSpent >= 1 && wordsReadInSession >= 80) {
                val observedWpm = (wordsReadInSession / minutesSpent).coerceIn(90, 450)
                val blendedWpm = ((currentSettings.readingSpeedWpm * 3 + observedWpm) / 4).coerceIn(90, 450)
                repository.saveGlobalSettings(currentSettings.copy(readingSpeedWpm = blendedWpm))
            }
        }
    }

    // --- Annotations & Glossary ---
    fun addAnnotation(
        chapterId: Long,
        novelId: Long?,
        type: String,
        paragraphIndex: Int,
        startOffset: Int,
        endOffset: Int,
        selectedText: String,
        noteText: String,
        colorHex: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addAnnotation(
                AnnotationEntity(
                    chapterId = chapterId,
                    novelId = novelId,
                    type = type,
                    paragraphIndex = paragraphIndex,
                    startOffset = startOffset,
                    endOffset = endOffset,
                    selectedText = selectedText,
                    noteText = noteText,
                    colorHex = colorHex
                )
            )
            val label = when (type) {
                "BOOKMARK" -> "تم حفظ العلامة المرجعية"
                "NOTE" -> "تم حفظ الملاحظة"
                else -> "تم تظليل النص"
            }
            showToastMessage(label)
        }
    }

    fun deleteAnnotation(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAnnotation(id)
        }
    }

    fun saveGlossaryTerm(novelId: Long?, term: String, translation: String, category: String, note: String, colorHex: String, existingId: Long = 0L) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveGlossaryTerm(
                GlossaryEntity(
                    id = existingId,
                    novelId = novelId,
                    term = term.trim(),
                    translation = translation.trim(),
                    category = category,
                    note = note.trim(),
                    highlightColorHex = colorHex
                )
            )
            showToastMessage("تم حفظ المصطلح في القاموس: $term")
        }
    }

    fun deleteGlossaryTerm(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGlossaryTerm(id)
        }
    }

    fun performFindAndReplace(
        targetChapters: List<ChapterEntity>,
        findQuery: String,
        replaceWith: String,
        matchCase: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.replaceInChapters(targetChapters, findQuery, replaceWith, matchCase)
            showToastMessage("تم استبدال $count تطابق في ${targetChapters.size} فصل")
        }
    }
}
