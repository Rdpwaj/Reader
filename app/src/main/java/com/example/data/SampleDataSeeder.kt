package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object SampleDataSeeder {

    suspend fun seedIfEmpty(dao: AppDao) {
        val existingNovels = dao.getAllNovelsSnapshot()
        val existingChapters = dao.getAllChaptersSnapshot()
        if (existingNovels.isNotEmpty() || existingChapters.isNotEmpty()) return

        val now = System.currentTimeMillis()
        val novelId = dao.insertNovel(
            NovelEntity(
                title = "سجلات العرش المفقود",
                description = "رواية خيال ملحمي مترجمة - تحتوي على 3 فصول تجريبية مع قاموس مصطلحات وملاحظات لتجربة جميع أدوات المترجم والقارئ.",
                colorHex = "#D97706",
                category = "خيال ملحمي",
                tags = "مترجمة, خيال, مراجعة",
                sortBy = "NATURAL_NAME",
                sortAscending = true,
                createdAt = now - 86400_000L * 3
            )
        )

        val ch1Content = """
الفصل 1: عودة حارس الأثر

هبت رياح الخريف الباردة فوق أبراج مدينة أركاديا الحجرية، حاملةً معها رائحة المطر القديم وأوراق الشجر المتساقطة. وقف آريس عند شرفة المرصد الأعلى، يمسك بين يديه لفافة الرق القرمزية التي عثر عليها في سرداب القلعة الشرقية بعد ثلاث سنوات من البحث المتواصل.

قال المعلم سولين وهو يقترب بخطوات هادئة: «هذه اللفافة ليست مجرد وثيقة تاريخية يا آريس، بل هي مفتاح البوابة السابعة التي أُغلقت منذ عصر الممالك القديمة. إن ترجمتها تتطلب دقة لا تحتمل الخطأ في حرف واحد».

أومأ آريس برأسه، ووضع اللفافة فوق الطاولة الخشبية المضاءة بقنديل الزيت الدافئ. كانت الحروف المحفورة بالحبر الذهبي تتوهج كلما اقترب منها حجر الأثير الأزرق، وكأن الكلمات نفسها تتنفس وتنتظر من يعيد إليها صوتها المفقود.

فتح آريس دفتر ملاحظاته الجلدي، وبدأ يقارن بين الرموز القديمة ومصطلحات السجلات الإمبراطورية. كان يعلم أن أي التباس بين اسم «حارس الأثر» و«سيد الظلال» قد يغير مسار النبوءة بأكملها، ولذلك حرص على تدوين كل مصطلح في قاموسه الخاص قبل متابعة القراءة والمراجعة.

ومع انتصاف الليل، اكتملت ترجمة المقطع الأول من العهد القديم، وظهرت على الصفحة عبارة واضحة: «حينما يجتمع النور والظل عند قمة أركاديا، ستُفتح الطريق لمن يحمل الكلمة الصادقة».
        """.trimIndent()

        val ch2Content = """
الفصل 2: مجلس الحكماء السبعة

مع بزوغ الفجر الأول، اجتمع أعضاء مجلس الحكماء في القاعة الدائرية الكبرى بوسط أركاديا. كانت الأعمدة الرخامية الشاهقة تعكس أشعة الشمس الذهبية على الخريطة المنحوتة في أرضية القاعة، حيث ظهرت حدود المقاطعات الخمس بتفاصيل مذهلة.

تحدثت القائدة ليانا بصوت حازم: «لقد وصلت رسائل الاستطلاع من الحدود الشمالية. هناك تحركات غير معتادة قرب قلعة الصقيع، ولا بد من معرفة ما تقوله المخطوطة التي ترجمها آريس الليلة الماضية».

تقدم آريس إلى وسط القاعة وعرض عليهم النص المترجم بعد مراجعته وتدقيق أسماء الأماكن والشخصيات. أشار بيده إلى موقع البوابة السابعة على الخريطة الخشبية، موضحًا كيف تتطابق الإحداثيات الفلكية المذكورة في الفصل الأول مع موقع المعبد الغارق في وادي السحاب.

ناقش الحكماء تفاصيل الخطة لساعات طويلة، واتفقوا على إرسال بعثة استكشافية يقودها آريس وليانا برفقة المعلم سولين، على أن يحملوا معهم حجر الأثير لفك الأختام المتبقية على طول الطريق.

قبل مغادرة القاعة، استوقف المعلم سولين تلميذه الشاب وهمس له: «تذكر دائمًا أن القوة الحقيقية في هذه الرحلة لا تكمن في السيوف، بل في فهم النص الأصلي كما كُتب دون تحريف».
        """.trimIndent()

        val ch3Content = """
الفصل 10: بوابة وادي السحاب (فصل لاختبار الترتيب الطبيعي)

بعد مسيرة استمرت سبعة أيام عبر الممرات الجبلية الوعرة، وصلت البعثة أخيرًا إلى مشارف وادي السحاب. لاحظ كيف يظهر "الفصل 10" بعد "الفصل 2" بفضل خوارزمية الترتيب الطبيعي للأرقام في تطبيق قارئ الفصول!

كانت الضباب الكثيف يغطي قاع الوادي، غير أن المنارة القديمة ظهرت شامخةً وسط الغيوم البيضاء. أخرج آريس حجر الأثير من حقيبته، فسطع بضوء أزرق صافٍ اخترق الضباب وكشف عن جسر حجري عتيق يمتد نحو البوابة السابعة.

قالت ليانا وهي تتأمل النقوش الممتدة على جانبي الجسر: «إنها نفس الرموز التي رأيناها في مخطوطة أركاديا! يبدو أن ترجمتك كانت دقيقة تمامًا يا آريس».

تقدم الثلاثة نحو البوابة الكبرى، وبدأ آريس يقرأ الكلمات المترجمة بصوت مسموع متزن. ومع كل جملة ينطقها، كانت الحلقات المعدنية الضخمة تدور ببطء، معلنةً فتح الطريق نحو المكتبة الإمبراطورية المفقودة التي لم تطأها قدم منذ ألف عام.

في الداخل، اصطفت آلاف المجلدات والفصول المحفوظة بعناية فائقة، جاهزةً لمن يقرؤها ويراجعها فصلًا بعد فصل في هدوء تام.
        """.trimIndent()

        val standaloneContent = """
مسودة ترجمة سريعة: ملاحظات المترجم اليومية

هذا ملف منفرد خارج أي رواية لتجربة ميزة الملفات المستقلة في المكتبة.
يمكنك لصق أي نص من الحافظة مباشرة عبر زر (+) في الأسفل، أو تقسيم نص طويل يحتوي على عدة فصول تلقائيًا بضغطة واحدة.

جرّب الضغط المطوّل على أي فصل لإعادة التسمية، أو تغيير الصيغة بين TXT وMD وHTML وDOCX وPDF، أو دمج الفصول وتقسيمها، أو فتح شاشة القراءة لتجربة التمرير السلس وحفظ موضع القراءة بالفقرة والحرف بدقة تامة!
        """.trimIndent()

        val ch1Id = dao.insertChapter(
            ChapterEntity(
                novelId = novelId,
                title = "الفصل 1: عودة حارس الأثر",
                content = ch1Content,
                format = "TXT",
                wordCount = TextProcessingUtils.countWords(ch1Content),
                readingStatus = "COMPLETED",
                translatorStage = "FINAL",
                favorite = true,
                tags = "مهم, مدقق",
                paragraphIndex = 4,
                charOffset = 0,
                progressPercent = 100f,
                createdAt = now - 86400_000L * 3,
                modifiedAt = now - 86400_000L * 2,
                lastReadAt = now - 7200_000L
            )
        )

        val ch2Id = dao.insertChapter(
            ChapterEntity(
                novelId = novelId,
                title = "الفصل 2: مجلس الحكماء السبعة",
                content = ch2Content,
                format = "TXT",
                wordCount = TextProcessingUtils.countWords(ch2Content),
                readingStatus = "READING",
                translatorStage = "REVIEWED",
                favorite = true,
                tags = "قيد المراجعة",
                paragraphIndex = 2,
                charOffset = 18,
                progressPercent = 52f,
                createdAt = now - 86400_000L * 2,
                modifiedAt = now - 86400_000L,
                lastReadAt = now - 1800_000L // Most recently read -> shows in "متابعة القراءة" card
            )
        )

        dao.insertChapter(
            ChapterEntity(
                novelId = novelId,
                title = "الفصل 10: بوابة وادي السحاب",
                content = ch3Content,
                format = "MD",
                wordCount = TextProcessingUtils.countWords(ch3Content),
                readingStatus = "NEW",
                translatorStage = "DRAFT",
                favorite = false,
                tags = "مسودة",
                paragraphIndex = 0,
                charOffset = 0,
                progressPercent = 0f,
                createdAt = now - 86400_000L,
                modifiedAt = now - 3600_000L,
                lastReadAt = 0L
            )
        )

        dao.insertChapter(
            ChapterEntity(
                novelId = null,
                title = "مسودة ترجمة سريعة - ملف مستقل",
                content = standaloneContent,
                format = "TXT",
                wordCount = TextProcessingUtils.countWords(standaloneContent),
                readingStatus = "NEW",
                translatorStage = "DRAFT",
                favorite = false,
                tags = "ملاحظات",
                paragraphIndex = 0,
                charOffset = 0,
                progressPercent = 0f,
                createdAt = now - 1200_000L,
                modifiedAt = now - 1200_000L,
                lastReadAt = 0L
            )
        )

        // Seed Glossary Terms for the Novel
        val glossaryTerms = listOf(
            GlossaryEntity(
                novelId = novelId,
                term = "آريس",
                translation = "Ares - بطل الرواية ومترجم المخطوطات القديمة",
                category = "شخصية",
                note = "يُكتب بالألف الممدودة (آريس) في جميع الفصول لتوحيد الترجمة",
                highlightColorHex = "#FDE047"
            ),
            GlossaryEntity(
                novelId = novelId,
                term = "أركاديا",
                translation = "Arcadia - العاصمة الحجرية العريقة",
                category = "مكان",
                note = "تقع في قلب المقاطعات الخمس",
                highlightColorHex = "#86EFAC"
            ),
            GlossaryEntity(
                novelId = novelId,
                term = "حجر الأثير",
                translation = "Aether Stone - حجر أزرق يكشف الحبر القديم",
                category = "مصطلح",
                note = "أداة أساسية لفتح البوابة السابعة",
                highlightColorHex = "#93C5FD"
            ),
            GlossaryEntity(
                novelId = novelId,
                term = "سولين",
                translation = "Master Solin - المعلم الأكبر في المرصد",
                category = "شخصية",
                note = "يلقب بالحكيم الأول",
                highlightColorHex = "#F9A8D4"
            ),
            GlossaryEntity(
                novelId = novelId,
                term = "ليانا",
                translation = "Commander Liana - قائدة حرس الحدود الشمالية",
                category = "شخصية",
                note = "ترافق آريس في رحلة وادي السحاب",
                highlightColorHex = "#C4B5FD"
            )
        )
        glossaryTerms.forEach { dao.insertGlossaryTerm(it) }

        // Seed Sample Annotations (Bookmark, Highlight, Note)
        dao.insertAnnotation(
            AnnotationEntity(
                chapterId = ch1Id,
                novelId = novelId,
                type = "HIGHLIGHT",
                paragraphIndex = 4,
                startOffset = 0,
                endOffset = 65,
                selectedText = "حينما يجتمع النور والظل عند قمة أركاديا، ستُفتح الطريق لمن يحمل الكلمة الصادقة",
                noteText = "اقتباس محوري يتكرر في نهاية المجلد الأول",
                colorHex = "#FDE047",
                createdAt = now - 3600_000L * 5
            )
        )
        dao.insertAnnotation(
            AnnotationEntity(
                chapterId = ch2Id,
                novelId = novelId,
                type = "BOOKMARK",
                paragraphIndex = 2,
                startOffset = 0,
                endOffset = 45,
                selectedText = "تقدم آريس إلى وسط القاعة وعرض عليهم النص المترجم",
                noteText = "موضع التوقف للمراجعة المسائية",
                colorHex = "#93C5FD",
                createdAt = now - 1800_000L
            )
        )

        // Seed 5 days of Reading Stats for streak and chart visualization
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val sampleMinutes = listOf(28, 35, 22, 40, 18)
        val sampleWords = listOf(4950, 6300, 3960, 7200, 3240)
        for (i in 0..4) {
            val c = cal.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = sdf.format(c.time)
            dao.upsertReadingStats(
                ReadingStatsEntity(
                    dateString = dateStr,
                    minutesRead = sampleMinutes[i],
                    wordsRead = sampleWords[i],
                    chaptersCompleted = if (i % 2 == 0) 2 else 1
                )
            )
        }
    }
}
