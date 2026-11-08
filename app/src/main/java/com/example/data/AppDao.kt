package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Novels ---
    @Query("SELECT * FROM novels WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun observeActiveNovels(): Flow<List<NovelEntity>>

    @Query("SELECT * FROM novels WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun observeDeletedNovels(): Flow<List<NovelEntity>>

    @Query("SELECT * FROM novels")
    suspend fun getAllNovelsSnapshot(): List<NovelEntity>

    @Query("SELECT * FROM novels WHERE id = :id LIMIT 1")
    suspend fun getNovelById(id: Long): NovelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNovel(novel: NovelEntity): Long

    @Update
    suspend fun updateNovel(novel: NovelEntity)

    @Query("DELETE FROM novels WHERE id = :id")
    suspend fun permanentlyDeleteNovel(id: Long)

    // --- Chapters ---
    @Query("SELECT * FROM chapters WHERE deletedAt IS NULL ORDER BY createdAt ASC")
    fun observeActiveChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun observeDeletedChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters")
    suspend fun getAllChaptersSnapshot(): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    suspend fun getChapterById(id: Long): ChapterEntity?

    @Query("SELECT * FROM chapters WHERE novelId = :novelId AND deletedAt IS NULL")
    suspend fun getChaptersForNovel(novelId: Long): List<ChapterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>): List<Long>

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query(
        """
        UPDATE chapters 
        SET paragraphIndex = :paragraphIndex, 
            charOffset = :charOffset, 
            progressPercent = :progressPercent, 
            readingStatus = :readingStatus, 
            lastReadAt = :lastReadAt 
        WHERE id = :chapterId
        """
    )
    suspend fun updateReadingPosition(
        chapterId: Long,
        paragraphIndex: Int,
        charOffset: Int,
        progressPercent: Float,
        readingStatus: String,
        lastReadAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun permanentlyDeleteChapter(id: Long)

    @Query("DELETE FROM chapters WHERE deletedAt IS NOT NULL AND deletedAt < :cutoffMillis")
    suspend fun purgeExpiredTrashChapters(cutoffMillis: Long)

    @Query("DELETE FROM novels WHERE deletedAt IS NOT NULL AND deletedAt < :cutoffMillis")
    suspend fun purgeExpiredTrashNovels(cutoffMillis: Long)

    @Query("DELETE FROM chapters WHERE deletedAt IS NOT NULL")
    suspend fun emptyTrashChapters()

    @Query("DELETE FROM novels WHERE deletedAt IS NOT NULL")
    suspend fun emptyTrashNovels()

    // --- Annotations (Bookmarks, Highlights, Notes) ---
    @Query("SELECT * FROM annotations ORDER BY createdAt DESC")
    fun observeAllAnnotations(): Flow<List<AnnotationEntity>>

    @Query("SELECT * FROM annotations")
    suspend fun getAllAnnotationsSnapshot(): List<AnnotationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: AnnotationEntity): Long

    @Update
    suspend fun updateAnnotation(annotation: AnnotationEntity)

    @Query("DELETE FROM annotations WHERE id = :id")
    suspend fun deleteAnnotationById(id: Long)

    // --- Glossary ---
    @Query("SELECT * FROM glossary ORDER BY term ASC")
    fun observeAllGlossary(): Flow<List<GlossaryEntity>>

    @Query("SELECT * FROM glossary")
    suspend fun getAllGlossarySnapshot(): List<GlossaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlossaryTerm(term: GlossaryEntity): Long

    @Update
    suspend fun updateGlossaryTerm(term: GlossaryEntity)

    @Query("DELETE FROM glossary WHERE id = :id")
    suspend fun deleteGlossaryTermById(id: Long)

    // --- Reading Stats ---
    @Query("SELECT * FROM reading_stats ORDER BY dateString DESC")
    fun observeReadingStats(): Flow<List<ReadingStatsEntity>>

    @Query("SELECT * FROM reading_stats")
    suspend fun getAllReadingStatsSnapshot(): List<ReadingStatsEntity>

    @Query("SELECT * FROM reading_stats WHERE dateString = :dateString LIMIT 1")
    suspend fun getStatsForDate(dateString: String): ReadingStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReadingStats(stats: ReadingStatsEntity)

    // --- Clear for Restore ---
    @Query("DELETE FROM novels")
    suspend fun clearAllNovels()

    @Query("DELETE FROM chapters")
    suspend fun clearAllChapters()

    @Query("DELETE FROM annotations")
    suspend fun clearAllAnnotations()

    @Query("DELETE FROM glossary")
    suspend fun clearAllGlossary()
}
