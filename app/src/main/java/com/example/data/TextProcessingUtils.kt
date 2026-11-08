package com.example.data

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.math.ceil
import kotlin.math.max

data class SplitChapterPreview(
    val title: String,
    val content: String,
    val wordCount: Int
)

object TextProcessingUtils {

    private val TASHKEEL_REGEX = Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")
    private val WORD_SPLIT_REGEX = Regex("\\s+")
    private val MULTI_SPACE_REGEX = Regex("[ \\t]{2,}")
    private val MULTI_NEWLINE_REGEX = Regex("\\n{3,}")

    // Matches Arabic & English chapter headers at line start:
    // e.g. "الفصل 1", "الفصل ١٢: البداية", "الفصل الأول", "Chapter 12 - The Return", "# الفصل 3"
    private val CHAPTER_HEADER_REGEX = Regex(
        pattern = "^\\s*(?:#+\\s*)?(?:الفصل|فصل|الباب|Chapter|CHAPTER|Ch\\.)\\s*(?:[0-9٠-٩]+|الأول|الاول|الثاني|الثالث|الرابع|الخامس|السادس|السابع|الثامن|التاسع|العاشر|الحادي عشر|الثاني عشر).*$",
        options = setOf(RegexOption.MULTILINE)
    )

    fun countWords(text: String): Int {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return 0
        return trimmed.split(WORD_SPLIT_REGEX).count { it.isNotBlank() }
    }

    fun removeTashkeel(text: String): String {
        return TASHKEEL_REGEX.replace(text, "")
    }

    fun cleanPastedText(text: String): String {
        if (text.isBlank()) return ""
        return text
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .lines()
            .joinToString("\n") { line ->
                MULTI_SPACE_REGEX.replace(line.trim(), " ")
            }
            .let { MULTI_NEWLINE_REGEX.replace(it, "\n\n") }
            .trim()
    }

    fun suggestTitleFromContent(content: String, fallback: String = "فصل جديد"): String {
        val firstNonBlank = content.lineSequence()
            .map { it.trim().removePrefix("#").trim() }
            .firstOrNull { it.isNotBlank() }
            ?: return fallback
        return if (firstNonBlank.length <= 65) {
            firstNonBlank
        } else {
            firstNonBlank.take(62).trim() + "..."
        }
    }

    /**
     * Splits a large pasted text containing multiple chapters into distinct chapters.
     */
    fun autoSplitIntoChapters(rawText: String, cleanWhitespace: Boolean = true): List<SplitChapterPreview> {
        val normalized = if (cleanWhitespace) cleanPastedText(rawText) else rawText.replace("\r\n", "\n")
        if (normalized.isBlank()) return emptyList()

        val matches = CHAPTER_HEADER_REGEX.findAll(normalized).toList()
        if (matches.isEmpty()) {
            val title = suggestTitleFromContent(normalized, "الفصل 1")
            return listOf(
                SplitChapterPreview(
                    title = title,
                    content = normalized,
                    wordCount = countWords(normalized)
                )
            )
        }

        val result = mutableListOf<SplitChapterPreview>()

        // Check if there is a prologue/intro before the first chapter heading
        val firstMatchStart = matches.first().range.first
        if (firstMatchStart > 40) {
            val introContent = normalized.substring(0, firstMatchStart).trim()
            if (introContent.isNotBlank()) {
                result.add(
                    SplitChapterPreview(
                        title = suggestTitleFromContent(introContent, "مقدمة"),
                        content = introContent,
                        wordCount = countWords(introContent)
                    )
                )
            }
        }

        for (i in matches.indices) {
            val currentMatch = matches[i]
            val headerLine = currentMatch.value.trim().removePrefix("#").trim()
            val startIdx = currentMatch.range.first
            val endIdx = if (i + 1 < matches.size) matches[i + 1].range.first else normalized.length
            val sectionText = normalized.substring(startIdx, endIdx).trim()
            val title = headerLine.ifBlank { "الفصل ${i + 1}" }.take(80)
            result.add(
                SplitChapterPreview(
                    title = title,
                    content = sectionText,
                    wordCount = countWords(sectionText)
                )
            )
        }
        return result
    }

    /**
     * Calculates total or remaining reading minutes.
     */
    fun calculateReadingMinutes(wordCount: Int, progressPercent: Float, wpm: Int): Pair<Int, Boolean> {
        val safeWpm = max(60, wpm)
        val totalMinutes = max(1, ceil(wordCount.toDouble() / safeWpm.toDouble()).toInt())
        return if (progressPercent <= 1f || progressPercent >= 99.5f) {
            totalMinutes to false // false = total time ("⏱ 12 د")
        } else {
            val remainingRatio = (100f - progressPercent).coerceIn(1f, 100f) / 100f
            val remainingMinutes = max(1, ceil((wordCount * remainingRatio) / safeWpm.toDouble()).toInt())
            remainingMinutes to true // true = remaining time ("متبقي 5 د")
        }
    }

    /**
     * Natural sort comparator that understands both Western (0-9) and Arabic-Indic (٠-٩) digits.
     * Ensures "فصل 2" comes before "فصل 10".
     */
    fun naturalCompare(a: String, b: String): Int {
        val s1 = normalizeDigits(a.trim())
        val s2 = normalizeDigits(b.trim())
        var i1 = 0
        var i2 = 0
        val len1 = s1.length
        val len2 = s2.length

        while (i1 < len1 && i2 < len2) {
            val c1 = s1[i1]
            val c2 = s2[i2]

            if (c1.isDigit() && c2.isDigit()) {
                var num1 = 0L
                while (i1 < len1 && s1[i1].isDigit()) {
                    num1 = num1 * 10 + (s1[i1] - '0')
                    i1++
                }
                var num2 = 0L
                while (i2 < len2 && s2[i2].isDigit()) {
                    num2 = num2 * 10 + (s2[i2] - '0')
                    i2++
                }
                if (num1 != num2) {
                    return num1.compareTo(num2)
                }
            } else {
                val cmp = c1.lowercaseChar().compareTo(c2.lowercaseChar())
                if (cmp != 0) return cmp
                i1++
                i2++
            }
        }
        return (len1 - i1).compareTo(len2 - i2)
    }

    private fun normalizeDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                in '٠'..'٩' -> sb.append((ch - '٠' + '0'.code).toChar())
                in '۰'..'۹' -> sb.append((ch - '۰' + '0'.code).toChar())
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Splits content into virtualized paragraphs for high-performance LazyColumn rendering
     * capable of opening 500,000+ words without any UI freeze.
     */
    fun splitIntoParagraphs(content: String, hideTashkeel: Boolean = false): List<String> {
        if (content.isBlank()) return listOf("")
        val processed = if (hideTashkeel) removeTashkeel(content) else content
        val paragraphs = processed
            .split("\n")
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }
        return if (paragraphs.isEmpty()) listOf(processed) else paragraphs
    }

    /**
     * Converts chapter content between formats: TXT, MD, HTML, DOCX, PDF.
     */
    fun convertTextFormat(title: String, currentContent: String, fromFormat: String, toFormat: String): String {
        // First normalize to plain paragraphs
        val plain = stripMarkupIfAny(currentContent, fromFormat)
        val paragraphs = plain.split("\n").map { it.trim() }.filter { it.isNotBlank() }

        return when (toFormat.uppercase()) {
            "MD" -> buildString {
                appendLine("# $title")
                appendLine()
                paragraphs.forEach { p ->
                    appendLine(p)
                    appendLine()
                }
            }.trim()

            "HTML" -> buildString {
                appendLine("<!DOCTYPE html>")
                appendLine("<html lang=\"ar\" dir=\"rtl\">")
                appendLine("<head><meta charset=\"UTF-8\"><title>$title</title></head>")
                appendLine("<body>")
                appendLine("  <h1>$title</h1>")
                paragraphs.forEach { p ->
                    appendLine("  <p>${escapeHtml(p)}</p>")
                }
                appendLine("</body>")
                appendLine("</html>")
            }

            else -> plain // TXT, DOCX, EPUB, PDF store clean readable text inside the database
        }
    }

    fun stripMarkupIfAny(content: String, format: String): String {
        return when (format.uppercase()) {
            "HTML" -> content
                .replace(Regex("<style[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("<script[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("</p>|</div>|</h1>|</h2>|</h3>|<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
                .replace(Regex("<[^>]+>"), "")
                .replace("&nbsp;", " ")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .let { cleanPastedText(it) }

            "MD" -> content
                .lines()
                .joinToString("\n") { line ->
                    line.replace(Regex("^#+\\s*"), "")
                }
                .trim()

            else -> content
        }
    }

    private fun escapeHtml(s: String): String {
        return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }

    /**
     * Reads imported file bytes with automatic encoding detection (UTF-8, UTF-16, Windows-1256 Arabic)
     * and supports TXT, MD, HTML, DOCX (ZIP XML), EPUB (ZIP XHTML), and text-based PDF.
     */
    fun extractTextFromUri(context: Context, uri: Uri, fileName: String): Pair<String, String> {
        val ext = fileName.substringAfterLast('.', "txt").lowercase()
        val detectedFormat = when (ext) {
            "md", "markdown" -> "MD"
            "html", "htm" -> "HTML"
            "docx" -> "DOCX"
            "epub" -> "EPUB"
            "pdf" -> "PDF"
            else -> "TXT"
        }

        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return "" to detectedFormat

        val extracted = when (detectedFormat) {
            "DOCX" -> extractDocxText(ByteArrayInputStream(bytes))
            "EPUB" -> extractEpubText(ByteArrayInputStream(bytes))
            "PDF" -> extractPdfText(bytes)
            "HTML" -> stripMarkupIfAny(decodeBytesAutoCharset(bytes), "HTML")
            else -> decodeBytesAutoCharset(bytes)
        }

        return extracted.ifBlank { decodeBytesAutoCharset(bytes) } to detectedFormat
    }

    fun decodeBytesAutoCharset(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        // Check BOM
        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() &&
            bytes[2] == 0xBF.toByte()
        ) {
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        }

        val utf8Text = String(bytes, Charsets.UTF_8)
        // If UTF-8 contains replacement characters '\uFFFD', fall back to Windows-1256 (Arabic)
        if (utf8Text.contains('\uFFFD')) {
            return try {
                String(bytes, Charset.forName("windows-1256"))
            } catch (e: Exception) {
                utf8Text
            }
        }
        return utf8Text
    }

    private fun extractDocxText(inputStream: InputStream): String {
        val sb = StringBuilder()
        try {
            ZipInputStream(inputStream).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    if (entry.name == "word/document.xml") {
                        val xmlBytes = zis.readBytes()
                        val factory = XmlPullParserFactory.newInstance()
                        val parser = factory.newPullParser()
                        parser.setInput(ByteArrayInputStream(xmlBytes), "UTF-8")
                        var eventType = parser.eventType
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG && parser.name == "t") {
                                sb.append(parser.nextText())
                            } else if (eventType == XmlPullParser.END_TAG && parser.name == "p") {
                                sb.append("\n")
                            }
                            eventType = parser.next()
                        }
                        break
                    }
                    entry = zis.nextEntry
                }
            }
        } catch (_: Exception) {
        }
        return cleanPastedText(sb.toString())
    }

    private fun extractEpubText(inputStream: InputStream): String {
        val sections = mutableListOf<Pair<String, String>>()
        try {
            ZipInputStream(inputStream).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val lower = entry.name.lowercase()
                    if (!entry.isDirectory && (lower.endsWith(".xhtml") || lower.endsWith(".html") || lower.endsWith(".htm"))) {
                        val html = decodeBytesAutoCharset(zis.readBytes())
                        val text = stripMarkupIfAny(html, "HTML")
                        if (text.isNotBlank()) {
                            sections.add(entry.name to text)
                        }
                    }
                    entry = zis.nextEntry
                }
            }
        } catch (_: Exception) {
        }
        return sections.sortedBy { it.first }.joinToString("\n\n") { it.second }
    }

    private fun extractPdfText(bytes: ByteArray): String {
        // Lightweight text stream extractor for text-based PDFs
        val raw = String(bytes, Charsets.ISO_8859_1)
        val parenRegex = Regex("\\(([^()\\\\]*(?:\\\\.[^()\\\\]*)*)\\)\\s*Tj")
        val matches = parenRegex.findAll(raw).map { it.groupValues[1] }.toList()
        return if (matches.isNotEmpty()) {
            matches.joinToString(" ").replace("\\n", "\n")
        } else {
            decodeBytesAutoCharset(bytes)
        }
    }

    /**
     * Generates a downloadable byte array for exporting a chapter or novel in TXT, MD, HTML, DOCX, or PDF.
     */
    fun generateExportBytes(title: String, content: String, targetFormat: String): ByteArray {
        return when (targetFormat.uppercase()) {
            "MD" -> convertTextFormat(title, content, "TXT", "MD").toByteArray(Charsets.UTF_8)
            "HTML" -> convertTextFormat(title, content, "TXT", "HTML").toByteArray(Charsets.UTF_8)
            "DOCX" -> createMinimalDocxBytes(title, content)
            "PDF" -> createPdfBytes(title, content)
            else -> "$title\n\n$content".toByteArray(Charsets.UTF_8)
        }
    }

    private fun createMinimalDocxBytes(title: String, content: String): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zos ->
            // [Content_Types].xml
            zos.putNextEntry(ZipEntry("[Content_Types].xml"))
            zos.write(
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                  <Default Extension="xml" ContentType="application/xml"/>
                  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
                </Types>""".trimIndent().toByteArray(Charsets.UTF_8)
            )
            zos.closeEntry()

            // _rels/.rels
            zos.putNextEntry(ZipEntry("_rels/.rels"))
            zos.write(
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
                </Relationships>""".trimIndent().toByteArray(Charsets.UTF_8)
            )
            zos.closeEntry()

            // word/document.xml
            zos.putNextEntry(ZipEntry("word/document.xml"))
            val paragraphs = (listOf(title) + content.split("\n")).filter { it.isNotBlank() }
            val bodyXml = buildString {
                append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
                append("""<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body>""")
                paragraphs.forEach { p ->
                    append("<w:p><w:r><w:t>${escapeHtml(p)}</w:t></w:r></w:p>")
                }
                append("</w:body></w:document>")
            }
            zos.write(bodyXml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }
        return out.toByteArray()
    }

    private fun createPdfBytes(title: String, content: String): ByteArray {
        val doc = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val paint = Paint().apply {
            textSize = 13f
            isAntiAlias = true
        }
        val lines = (listOf(title, "") + content.split("\n")).flatMap { paragraph ->
            if (paragraph.length <= 75) listOf(paragraph) else paragraph.chunked(75)
        }
        val linesPerPage = 38
        val chunks = lines.chunked(linesPerPage).ifEmpty { listOf(listOf(title)) }

        chunks.forEachIndexed { index, pageLines ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas
            var y = 50f
            pageLines.forEach { line ->
                canvas.drawText(line, 40f, y, paint)
                y += 20f
            }
            doc.finishPage(page)
        }
        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }

    /**
     * Generates a 500,000-word chapter in memory for testing instant virtualized rendering.
     */
    fun generate500kWordStressTestContent(): String {
        val baseParagraph = "في قلب القلعة القديمة وقف آريس يتأمل المخطوطات الذهبية التي ترجمها الحكماء عبر العصور، بينما كانت الرياح الشرقية تحمل همسات الأساطير المنسية فوق أسوار مدينة أركاديا العريقة وتكشف أسرار البوابات السبعة."
        // baseParagraph has ~31 words. 16,200 paragraphs * 31 words ≈ 502,200 words!
        val sb = StringBuilder(16200 * 210)
        for (i in 1..16200) {
            sb.append("فقرة رقم ").append(i).append(": ").append(baseParagraph).append('\n')
        }
        return sb.toString()
    }
}
