package com.example

import com.example.data.TextProcessingUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun naturalSort_ordersChapter2BeforeChapter10() {
        val titles = listOf("الفصل 10", "الفصل 1", "الفصل 2", "الفصل 20")
        val sorted = titles.sortedWith { a, b -> TextProcessingUtils.naturalCompare(a, b) }
        assertEquals(listOf("الفصل 1", "الفصل 2", "الفصل 10", "الفصل 20"), sorted)
    }

    @Test
    fun autoSplitChapters_splitsMultiChapterPasteCorrectly() {
        val raw = """
            الفصل 1: البداية
            نص الفصل الأول هنا بكلمات متعددة.
            
            الفصل 2: المغامرة
            نص الفصل الثاني هنا بكلمات إضافية.
            
            الفصل 10: النهاية
            نص الفصل العاشر.
        """.trimIndent()
        val split = TextProcessingUtils.autoSplitIntoChapters(raw)
        assertEquals(3, split.size)
        assertTrue(split[0].title.contains("الفصل 1"))
        assertTrue(split[1].title.contains("الفصل 2"))
        assertTrue(split[2].title.contains("الفصل 10"))
    }

    @Test
    fun stressTestGenerator_createsOver500kWords() {
        val hugeText = TextProcessingUtils.generate500kWordStressTestContent()
        val paragraphs = TextProcessingUtils.splitIntoParagraphs(hugeText)
        assertTrue(paragraphs.size >= 16000)
        assertTrue(TextProcessingUtils.countWords(hugeText) >= 500_000)
    }
}
