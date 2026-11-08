package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "novels")
data class NovelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val colorHex: String = "#D97706",
    val category: String = "عام",
    val tags: String = "", // comma-separated
    val sortBy: String = "NATURAL_NAME", // NATURAL_NAME, CREATED_AT, MODIFIED_AT, LAST_READ, WORD_COUNT, PROGRESS
    val sortAscending: Boolean = true,
    val settingsOverrideJson: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val novelId: Long? = null, // null = standalone file outside any novel
    val title: String,
    val content: String,
    val format: String = "TXT", // TXT, MD, HTML, DOCX, EPUB, PDF
    val wordCount: Int = 0,
    val readingStatus: String = "NEW", // NEW (جديد), READING (قيد القراءة), COMPLETED (مكتمل)
    val translatorStage: String = "DRAFT", // DRAFT (مسودة), REVIEWED (مُراجَع), FINAL (نهائي)
    val favorite: Boolean = false,
    val tags: String = "", // comma-separated
    val paragraphIndex: Int = 0, // Precise reading position: paragraph index
    val charOffset: Int = 0,     // Precise reading position: character offset inside paragraph
    val progressPercent: Float = 0f, // 0f..100f
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val lastReadAt: Long = 0L,
    val deletedAt: Long? = null // non-null = in 30-day trash bin
)

@Entity(tableName = "annotations")
data class AnnotationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val novelId: Long? = null,
    val type: String = "HIGHLIGHT", // BOOKMARK, HIGHLIGHT, NOTE
    val paragraphIndex: Int = 0,
    val startOffset: Int = 0,
    val endOffset: Int = 0,
    val selectedText: String = "",
    val noteText: String = "",
    val colorHex: String = "#FDE047",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "glossary")
data class GlossaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val novelId: Long? = null, // null = global glossary across all novels
    val term: String,
    val translation: String,
    val category: String = "شخصية", // شخصية, مكان, مهارة, مصطلح
    val note: String = "",
    val highlightColorHex: String = "#FDE047",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_stats")
data class ReadingStatsEntity(
    @PrimaryKey val dateString: String, // YYYY-MM-DD
    val minutesRead: Int = 0,
    val wordsRead: Int = 0,
    val chaptersCompleted: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
