package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore by preferencesDataStore(name = "reader_preferences")

class ReaderRepository(
    private val context: Context,
    private val dao: AppDao
) {
    private val settingsKey = stringPreferencesKey("global_reader_settings_json")

    val globalSettingsFlow: Flow<ReaderSettings> = context.dataStore.data.map { prefs ->
        ReaderSettings.fromJsonString(prefs[settingsKey])
    }

    val activeNovelsFlow: Flow<List<NovelEntity>> = dao.observeActiveNovels()
    val deletedNovelsFlow: Flow<List<NovelEntity>> = dao.observeDeletedNovels()
    val activeChaptersFlow: Flow<List<ChapterEntity>> = dao.observeActiveChapters()
    val deletedChaptersFlow: Flow<List<ChapterEntity>> = dao.observeDeletedChapters()
    val allAnnotationsFlow: Flow<List<AnnotationEntity>> = dao.observeAllAnnotations()
    val allGlossaryFlow: Flow<List<GlossaryEntity>> = dao.observeAllGlossary()
    val readingStatsFlow: Flow<List<ReadingStatsEntity>> = dao.observeReadingStats()

    fun observeFullChapterById(chapterId: Long): Flow<ChapterEntity?> =
        dao.observeChapterById(chapterId)

    suspend fun getFullChapterById(chapterId: Long): ChapterEntity? = withContext(Dispatchers.IO) {
        dao.getChapterById(chapterId)
    }

    suspend fun searchActiveChapterIds(query: String): Set<Long> = withContext(Dispatchers.IO) {
        if (query.isBlank()) emptySet()
        else dao.searchActiveChapterIds(query.trim()).toSet()
    }

    suspend fun initializeAndSeed() = withContext(Dispatchers.IO) {
        val cutoff = System.currentTimeMillis() - 30L * 24L * 3600L * 1000L
        dao.purgeExpiredTrashChapters(cutoff)
        dao.purgeExpiredTrashNovels(cutoff)
        SampleDataSeeder.seedIfEmpty(dao)
    }

    suspend fun saveGlobalSettings(settings: ReaderSettings) = withContext(Dispatchers.IO) {
        context.dataStore.edit { prefs ->
            prefs[settingsKey] = settings.toJsonString()
        }
    }

    suspend fun saveNovelSettingsOverride(novelId: Long, settings: ReaderSettings?) = withContext(Dispatchers.IO) {
        val novel = dao.getNovelById(novelId) ?: return@withContext
        dao.updateNovel(novel.copy(settingsOverrideJson = settings?.toJsonString()))
    }

    // --- Novel Operations ---
    suspend fun createNovel(title: String, description: String, colorHex: String, category: String, tags: String): Long =
        withContext(Dispatchers.IO) {
            dao.insertNovel(
                NovelEntity(
                    title = title.trim().ifBlank { "رواية جديدة" },
                    description = description.trim(),
                    colorHex = colorHex,
                    category = category.ifBlank { "عام" },
                    tags = tags.trim()
                )
            )
        }

    suspend fun updateNovel(novel: NovelEntity) = withContext(Dispatchers.IO) {
        dao.updateNovel(novel)
    }

    suspend fun moveNovelToTrash(novelId: Long) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val novel = dao.getNovelById(novelId) ?: return@withContext
        dao.updateNovel(novel.copy(deletedAt = now))
        dao.updateNovelChaptersDeletedAt(novelId, now)
    }

    suspend fun restoreNovelFromTrash(novel: NovelEntity) = withContext(Dispatchers.IO) {
        dao.updateNovel(novel.copy(deletedAt = null))
        dao.updateNovelChaptersDeletedAt(novel.id, null)
    }

    suspend fun permanentlyDeleteNovel(novelId: Long) = withContext(Dispatchers.IO) {
        val allChapters = dao.getAllChaptersSnapshot().filter { it.novelId == novelId }
        allChapters.forEach { dao.permanentlyDeleteChapter(it.id) }
        dao.permanentlyDeleteNovel(novelId)
    }

    // --- Chapter Operations ---
    suspend fun createChapter(
        novelId: Long?,
        title: String,
        content: String,
        format: String = "TXT",
        translatorStage: String = "DRAFT",
        tags: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val wordCount = TextProcessingUtils.countWords(content)
        dao.insertChapter(
            ChapterEntity(
                novelId = novelId,
                title = title.trim().ifBlank { TextProcessingUtils.suggestTitleFromContent(content) },
                content = content,
                format = format.uppercase(),
                wordCount = wordCount,
                translatorStage = translatorStage,
                tags = tags
            )
        )
    }

    suspend fun createChaptersBatch(novelId: Long?, previews: List<SplitChapterPreview>, format: String = "TXT"): List<Long> =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val entities = previews.mapIndexed { index, item ->
                ChapterEntity(
                    novelId = novelId,
                    title = item.title,
                    content = item.content,
                    format = format,
                    wordCount = item.wordCount,
                    createdAt = now + index,
                    modifiedAt = now + index
                )
            }
            dao.insertChapters(entities)
        }

    /**
     * Safely updates a chapter. If the passed chapter came from the lightweight Library list
     * (where content is empty to save RAM), it preserves the full content stored in SQLite.
     */
    suspend fun updateChapter(chapter: ChapterEntity) = withContext(Dispatchers.IO) {
        val actualContent = if (chapter.content.isNotEmpty()) {
            chapter.content
        } else {
            dao.getChapterById(chapter.id)?.content ?: ""
        }
        val updatedWords = if (chapter.content.isNotEmpty()) {
            TextProcessingUtils.countWords(actualContent)
        } else {
            chapter.wordCount
        }
        dao.updateChapter(
            chapter.copy(
                content = actualContent,
                wordCount = updatedWords,
                modifiedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun saveReadingPosition(
        chapterId: Long,
        paragraphIndex: Int,
        charOffset: Int,
        totalParagraphs: Int
    ) = withContext(Dispatchers.IO) {
        val safeTotal = totalParagraphs.coerceAtLeast(1)
        val rawPercent = if (safeTotal <= 1) {
            100f
        } else {
            ((paragraphIndex.toFloat() / (safeTotal - 1).toFloat()) * 100f).coerceIn(0f, 100f)
        }
        val status = when {
            rawPercent >= 96f -> "COMPLETED"
            rawPercent > 0f || paragraphIndex > 0 || charOffset > 0 -> "READING"
            else -> "NEW"
        }
        dao.updateReadingPosition(
            chapterId = chapterId,
            paragraphIndex = paragraphIndex,
            charOffset = charOffset,
            progressPercent = if (status == "COMPLETED") 100f else rawPercent,
            readingStatus = status,
            lastReadAt = System.currentTimeMillis()
        )
    }

    suspend fun duplicateChapter(chapter: ChapterEntity): Long = withContext(Dispatchers.IO) {
        val full = dao.getChapterById(chapter.id) ?: chapter
        val now = System.currentTimeMillis()
        dao.insertChapter(
            full.copy(
                id = 0,
                title = "${full.title} (نسخة)",
                createdAt = now,
                modifiedAt = now
            )
        )
    }

    suspend fun convertChapterFormat(
        chapter: ChapterEntity,
        targetFormat: String,
        saveAsNewCopy: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val full = dao.getChapterById(chapter.id) ?: chapter
        val convertedContent = TextProcessingUtils.convertTextFormat(
            title = full.title,
            currentContent = full.content,
            fromFormat = full.format,
            toFormat = targetFormat
        )
        val now = System.currentTimeMillis()
        if (saveAsNewCopy) {
            dao.insertChapter(
                full.copy(
                    id = 0,
                    title = "${full.title} ($targetFormat)",
                    content = convertedContent,
                    format = targetFormat,
                    wordCount = TextProcessingUtils.countWords(convertedContent),
                    createdAt = now,
                    modifiedAt = now
                )
            )
        } else {
            dao.updateChapter(
                full.copy(
                    content = convertedContent,
                    format = targetFormat,
                    wordCount = TextProcessingUtils.countWords(convertedContent),
                    modifiedAt = now
                )
            )
            full.id
        }
    }

    suspend fun batchRenameChapters(
        chapters: List<ChapterEntity>,
        pattern: String,
        startNumber: Int,
        zeroPadding: Int
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        chapters.forEachIndexed { idx, ch ->
            val num = startNumber + idx
            val numStr = num.toString().padStart(zeroPadding.coerceAtLeast(1), '0')
            val newTitle = if (pattern.contains("{رقم}")) {
                pattern.replace("{رقم}", numStr)
            } else {
                "$pattern $numStr"
            }
            dao.updateChapterTitleAndTags(ch.id, newTitle.trim(), ch.tags, now)
        }
    }

    suspend fun mergeChapters(
        chapters: List<ChapterEntity>,
        mergedTitle: String,
        targetNovelId: Long?,
        deleteOriginalsToTrash: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val fullChapters = chapters.mapNotNull { dao.getChapterById(it.id) }
        val combinedContent = fullChapters.joinToString("\n\n* * *\n\n") { ch ->
            "${ch.title}\n\n${ch.content.trim()}"
        }
        val newId = createChapter(
            novelId = targetNovelId,
            title = mergedTitle.ifBlank { "فصل مدمج (${fullChapters.size} فصول)" },
            content = combinedContent,
            format = fullChapters.firstOrNull()?.format ?: "TXT"
        )
        if (deleteOriginalsToTrash) {
            val now = System.currentTimeMillis()
            fullChapters.forEach { ch ->
                dao.updateChapterDeletedAt(ch.id, now)
            }
        }
        newId
    }

    suspend fun moveChaptersToTrash(chapterIds: List<Long>) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        chapterIds.forEach { id ->
            dao.updateChapterDeletedAt(id, now)
        }
    }

    suspend fun restoreChaptersFromTrash(chapterIds: List<Long>) = withContext(Dispatchers.IO) {
        chapterIds.forEach { id ->
            dao.updateChapterDeletedAt(id, null)
        }
    }

    suspend fun permanentlyDeleteChapter(id: Long) = withContext(Dispatchers.IO) {
        dao.permanentlyDeleteChapter(id)
    }

    suspend fun emptyAllTrash() = withContext(Dispatchers.IO) {
        dao.emptyTrashChapters()
        dao.emptyTrashNovels()
    }

    // --- Find and Replace across Chapter or Novel ---
    suspend fun replaceInChapters(
        targetChapters: List<ChapterEntity>,
        findQuery: String,
        replaceWith: String,
        matchCase: Boolean
    ): Int = withContext(Dispatchers.IO) {
        if (findQuery.isEmpty()) return@withContext 0
        var totalReplacements = 0
        val regex = if (matchCase) {
            Regex(Regex.escape(findQuery))
        } else {
            Regex(Regex.escape(findQuery), RegexOption.IGNORE_CASE)
        }
        targetChapters.forEach { summary ->
            val ch = dao.getChapterById(summary.id) ?: return@forEach
            val matchesCount = regex.findAll(ch.content).count()
            if (matchesCount > 0) {
                totalReplacements += matchesCount
                val newContent = regex.replace(ch.content, replaceWith)
                dao.updateChapter(
                    ch.copy(
                        content = newContent,
                        wordCount = TextProcessingUtils.countWords(newContent),
                        modifiedAt = System.currentTimeMillis()
                    )
                )
            }
        }
        totalReplacements
    }

    // --- Annotations & Glossary ---
    suspend fun addAnnotation(annotation: AnnotationEntity): Long = withContext(Dispatchers.IO) {
        dao.insertAnnotation(annotation)
    }

    suspend fun deleteAnnotation(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteAnnotationById(id)
    }

    suspend fun saveGlossaryTerm(term: GlossaryEntity): Long = withContext(Dispatchers.IO) {
        dao.insertGlossaryTerm(term)
    }

    suspend fun deleteGlossaryTerm(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteGlossaryTermById(id)
    }

    // --- Reading Stats Recording ---
    suspend fun recordReadingProgress(minutesDelta: Int, wordsDelta: Int, completedChapterDelta: Int = 0) =
        withContext(Dispatchers.IO) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val current = dao.getStatsForDate(today)
            if (current == null) {
                dao.upsertReadingStats(
                    ReadingStatsEntity(
                        dateString = today,
                        minutesRead = minutesDelta.coerceAtLeast(0),
                        wordsRead = wordsDelta.coerceAtLeast(0),
                        chaptersCompleted = completedChapterDelta.coerceAtLeast(0)
                    )
                )
            } else {
                dao.upsertReadingStats(
                    current.copy(
                        minutesRead = (current.minutesRead + minutesDelta).coerceAtLeast(0),
                        wordsRead = (current.wordsRead + wordsDelta).coerceAtLeast(0),
                        chaptersCompleted = (current.chaptersCompleted + completedChapterDelta).coerceAtLeast(0),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }

    // --- Backup & Restore (JSON) ---
    suspend fun exportFullBackupJson(currentSettings: ReaderSettings): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("settings", JSONObject(currentSettings.toJsonString()))

        val novelsArr = JSONArray()
        dao.getAllNovelsSnapshot().forEach { n ->
            val obj = JSONObject()
            obj.put("id", n.id)
            obj.put("title", n.title)
            obj.put("description", n.description)
            obj.put("colorHex", n.colorHex)
            obj.put("category", n.category)
            obj.put("tags", n.tags)
            obj.put("sortBy", n.sortBy)
            obj.put("sortAscending", n.sortAscending)
            obj.put("settingsOverrideJson", n.settingsOverrideJson ?: "")
            obj.put("createdAt", n.createdAt)
            novelsArr.put(obj)
        }
        root.put("novels", novelsArr)

        val chaptersArr = JSONArray()
        dao.getAllChaptersSnapshot().forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("novelId", c.novelId ?: -1L)
            obj.put("title", c.title)
            obj.put("content", c.content)
            obj.put("format", c.format)
            obj.put("wordCount", c.wordCount)
            obj.put("readingStatus", c.readingStatus)
            obj.put("translatorStage", c.translatorStage)
            obj.put("favorite", c.favorite)
            obj.put("tags", c.tags)
            obj.put("paragraphIndex", c.paragraphIndex)
            obj.put("charOffset", c.charOffset)
            obj.put("progressPercent", c.progressPercent.toDouble())
            obj.put("createdAt", c.createdAt)
            obj.put("modifiedAt", c.modifiedAt)
            obj.put("lastReadAt", c.lastReadAt)
            chaptersArr.put(obj)
        }
        root.put("chapters", chaptersArr)

        val glossaryArr = JSONArray()
        dao.getAllGlossarySnapshot().forEach { g ->
            val obj = JSONObject()
            obj.put("novelId", g.novelId ?: -1L)
            obj.put("term", g.term)
            obj.put("translation", g.translation)
            obj.put("category", g.category)
            obj.put("note", g.note)
            obj.put("highlightColorHex", g.highlightColorHex)
            glossaryArr.put(obj)
        }
        root.put("glossary", glossaryArr)

        val annotationsArr = JSONArray()
        dao.getAllAnnotationsSnapshot().forEach { a ->
            val obj = JSONObject()
            obj.put("chapterId", a.chapterId)
            obj.put("novelId", a.novelId ?: -1L)
            obj.put("type", a.type)
            obj.put("paragraphIndex", a.paragraphIndex)
            obj.put("startOffset", a.startOffset)
            obj.put("endOffset", a.endOffset)
            obj.put("selectedText", a.selectedText)
            obj.put("noteText", a.noteText)
            obj.put("colorHex", a.colorHex)
            annotationsArr.put(obj)
        }
        root.put("annotations", annotationsArr)

        root.toString(2)
    }

    suspend fun importFullBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (root.has("settings")) {
                val settings = ReaderSettings.fromJsonString(root.getJSONObject("settings").toString())
                saveGlobalSettings(settings.copy(lastBackupTimestamp = System.currentTimeMillis()))
            }
            if (root.has("novels")) {
                val arr = root.getJSONArray("novels")
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    dao.insertNovel(
                        NovelEntity(
                            id = o.optLong("id", 0L),
                            title = o.optString("title", "رواية"),
                            description = o.optString("description", ""),
                            colorHex = o.optString("colorHex", "#D97706"),
                            category = o.optString("category", "عام"),
                            tags = o.optString("tags", ""),
                            sortBy = o.optString("sortBy", "NATURAL_NAME"),
                            sortAscending = o.optBoolean("sortAscending", true),
                            settingsOverrideJson = o.optString("settingsOverrideJson", "").ifBlank { null },
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }
            if (root.has("chapters")) {
                val arr = root.getJSONArray("chapters")
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val nId = o.optLong("novelId", -1L)
                    dao.insertChapter(
                        ChapterEntity(
                            id = o.optLong("id", 0L),
                            novelId = if (nId <= 0L) null else nId,
                            title = o.optString("title", "فصل"),
                            content = o.optString("content", ""),
                            format = o.optString("format", "TXT"),
                            wordCount = o.optInt("wordCount", 0),
                            readingStatus = o.optString("readingStatus", "NEW"),
                            translatorStage = o.optString("translatorStage", "DRAFT"),
                            favorite = o.optBoolean("favorite", false),
                            tags = o.optString("tags", ""),
                            paragraphIndex = o.optInt("paragraphIndex", 0),
                            charOffset = o.optInt("charOffset", 0),
                            progressPercent = o.optDouble("progressPercent", 0.0).toFloat(),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                            modifiedAt = o.optLong("modifiedAt", System.currentTimeMillis()),
                            lastReadAt = o.optLong("lastReadAt", 0L)
                        )
                    )
                }
            }
            if (root.has("glossary")) {
                val arr = root.getJSONArray("glossary")
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val nId = o.optLong("novelId", -1L)
                    dao.insertGlossaryTerm(
                        GlossaryEntity(
                            novelId = if (nId <= 0L) null else nId,
                            term = o.optString("term", ""),
                            translation = o.optString("translation", ""),
                            category = o.optString("category", "شخصية"),
                            note = o.optString("note", ""),
                            highlightColorHex = o.optString("highlightColorHex", "#FDE047")
                        )
                    )
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
