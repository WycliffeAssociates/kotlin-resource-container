package org.wycliffeassociates.resourcecontainer

import org.junit.Assert.*
import org.junit.Test
import org.wycliffeassociates.resourcecontainer.entity.Link

/**
 * Created by joel on 10/11/16.
 */

class BookLinkTest {

    // standard links

    @Test
    @Throws(Exception::class)
    fun chapterVerseLink() {
        val l = Link.parseLink("[[language/project/resource/01:02]]")
        assertEquals("language", l!!.language)
        assertEquals("project", l.project)
        assertEquals("resource", l.resource)
        assertEquals("01:02", l.arguments)
        assertEquals("01", l.chapter)
        assertEquals("02", l.chunk)
        assertFalse(l.isExternal)
        assertFalse(l.isMedia)
        assertTrue(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun chapterLink() {
        val l = Link.parseLink("[[language/project/resource/01]]")
        assertEquals("language", l!!.language)
        assertEquals("project", l.project)
        assertEquals("resource", l.resource)
        assertEquals("01", l.arguments)
        assertEquals("01", l.chapter)
        assertFalse(l.isExternal)
        assertFalse(l.isMedia)
        // TRICKY: we can't actually determine if this is a passage link without a :
        assertFalse(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun chapterVerseRangeLink() {
        val l = Link.parseLink("[[language/project/resource/01:02-06]]")
        assertEquals("language", l!!.language)
        assertEquals("project", l.project)
        assertEquals("resource", l.resource)
        assertEquals("01:02-06", l.arguments)
        assertEquals("01", l.chapter)
        assertEquals("02", l.chunk)
        assertEquals("06", l.lastChunk)
        assertFalse(l.isExternal)
        assertFalse(l.isMedia)
        assertTrue(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun invlaidChapterVerseLink() {
        try {
            val l = Link.parseLink("[[language/project/resource/01:02,06]]")
            assertEquals(null, l)
        } catch (e: Exception) {
            assertNotEquals(null, e)
        }

    }

    // automatic linking

    @Test
    @Throws(Exception::class)
    fun autoLink() {
        val links = Link.findLinks("Genesis 1:1")
        if(links != null) {
            assertEquals(1, links.size.toLong())
            assertEquals("Genesis", links.get(0).project)
            assertEquals("1:1", links.get(0).arguments)
            assertEquals("1", links.get(0).chapter)
            assertEquals("1", links.get(0).chunk)
        } else {
            throw Exception("Links is null.")
        }
    }

    @Test
    @Throws(Exception::class)
    fun autoRangeLink() {
        val links = Link.findLinks("genesis 1:1-3")
        if(links != null) {
            assertEquals(1, links.size.toLong())
            assertEquals("genesis", links.get(0).project)
            assertEquals("1:1-3", links.get(0).arguments)
            assertEquals("1", links.get(0).chapter)
            assertEquals("1", links.get(0).chunk)
            assertEquals("3", links.get(0).lastChunk)
        } else {
            throw Exception("Links is null.")
        }
    }

    @Test
    @Throws(Exception::class)
    fun autoMultipleLinks() {
        val links = Link.findLinks("John 1–3; 3:16; 6:14, 44, 46-47; 7:1-5")
        if(links != null) {
            assertEquals(6, links.size.toLong())
            val l1 = links.get(0)
            assertEquals("John", l1.project)
            assertEquals("1", l1.chapter)
            assertEquals(null, l1.chunk)

            val l2 = links.get(1)
            assertEquals("John", l2.project)
            assertEquals("3", l2.chapter)
            assertEquals("16", l1.chunk)

            val l3 = links.get(2)
            assertEquals("John", l3.project)
            assertEquals("6", l3.chapter)
            assertEquals("14", l1.chunk)

            val l4 = links.get(3)
            assertEquals("John", l4.project)
            assertEquals("6", l4.chapter)
            assertEquals("44", l1.chunk)

            val l5 = links.get(4)
            assertEquals("John", l5.project)
            assertEquals("6", l5.chapter)
            assertEquals("46", l1.chunk)
            assertEquals("47", l1.lastChunk)

            val l6 = links.get(5)
            assertEquals("John", l6.project)
            assertEquals("7", l6.chapter)
            assertEquals("1", l1.chunk)
            assertEquals("5", l1.lastChunk)
        } else {
            throw Exception("Links is null.")
        }
    }

    @Test
    @Throws(Exception::class)
    fun titledMediaPassageLink() {
        val l = Link.parseLink("[Link Title](image:/language/project/resource/01:02)")
        assertEquals("Link Title", l!!.title)
        assertEquals("language", l.language)
        assertEquals("project", l.project)
        assertEquals("resource", l.resource)
        assertEquals("01:02", l.arguments)
        assertEquals("image", l.protocol)
        assertEquals("01", l.chapter)
        assertEquals("02", l.chunk)
        assertFalse(l.isExternal)
        assertTrue(l.isMedia)
        assertTrue(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun titledMediaNoResourcePassageLink() {
        val l = Link.parseLink("[Link Title](image:/language/project/01:02)")
        assertEquals("Link Title", l!!.title)
        assertEquals("language", l.language)
        assertEquals("project", l.project)
        assertEquals("project", l.resource)
        assertEquals("01:02", l.arguments)
        assertEquals("image", l.protocol)
        assertEquals("01", l.chapter)
        assertEquals("02", l.chunk)
        assertFalse(l.isExternal)
        assertTrue(l.isMedia)
        assertTrue(l.isPassage)
    }


    @Test
    @Throws(Exception::class)
    fun titledShorthandPassageLink() {
        val l = Link.parseLink("[Link Title](language/project/01:02)")
        assertEquals("Link Title", l!!.title)
        assertEquals("language", l.language)
        assertEquals("project", l.project)
        assertEquals("project", l.resource)
        assertEquals("01:02", l.arguments)
        assertFalse(l.isExternal)
        assertFalse(l.isMedia)
        assertTrue(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun anonymousShorthandPassageLink() {
        val l = Link.parseLink("[[language/project/01:02]]")
        assertEquals(null, l!!.title)
        assertEquals("language", l.language)
        assertEquals("project", l.project)
        assertEquals("project", l.resource)
        assertEquals("01:02", l.arguments)
        assertFalse(l.isExternal)
        assertFalse(l.isMedia)
        assertTrue(l.isPassage)
    }
}