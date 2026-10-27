package org.andstatus.todoagenda.prefs

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.runner.junit4.KotestTestRunner
import org.junit.runner.RunWith

/**
 * @author yvolk@yurivolkov.com
 */
@RunWith(KotestTestRunner::class)
class KeywordsFilterTest : AnnotationSpec() {
    @Test
    fun testPhrases() {
        var query = "\"do it\""
        val keywordDN = "do it"
        assertOneQueryToKeywords(query, keywordDN)
        val body1 = "Looking for do it"
        assertMatch(query, body1)
        assertNotMatch(query, "Looking for it do")
        query = "word $query"
        val keywordW = "word"
        assertOneQueryToKeywords(query, keywordW, keywordDN)
        val body2 = "$body1 with a word, that is interesting"
        assertMatch("those this that", body2)
        assertNotMatch("something other", body2)
        val query3 = "Hidden \"Smith's\" '. Birthday'"
        assertOneQueryToKeywords(query3, "Hidden", "Smith's", ". Birthday")
        assertMatch(query3, "Smith. Birthday")
        assertNotMatch(query3, "Smith Birthday")
        assertMatch(query3, "Smith's Birthday")
        assertMatch(query3, "Smith Hidden Birthday")
        assertNotMatch(query3, "Smith.Birthday")
    }

    private fun assertOneQueryToKeywords(query: String, vararg keywords: String) {
        val size = keywords.size
        val filter1 = KeywordsFilter(false, query)
        withClue(filter1.toString()) {
            filter1.keywords.size.toLong() shouldBe size.toLong()
        }
        for (ind in 0 until size) {
            withClue(filter1.toString()) {
                filter1.keywords[ind] shouldBe keywords[ind]
            }
        }
    }

    private fun assertMatch(query: String, body: String) {
        withClue("no keywords from '$query' match: '$body'") {
            KeywordsFilter(false, query).matched(body) shouldBe true
        }
    }

    private fun assertNotMatch(query: String, body: String) {
        withClue("Some keyword from '$query' match: '$body'") {
            KeywordsFilter(false, query).matched(body) shouldBe false
        }
    }
}
