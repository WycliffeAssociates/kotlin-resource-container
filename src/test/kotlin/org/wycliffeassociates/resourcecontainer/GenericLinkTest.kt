package org.wycliffeassociates.resourcecontainer

import org.junit.Assert.*
import org.junit.Test
import org.wycliffeassociates.resourcecontainer.entity.Link

/**
 * Created by joel on 10/11/16.
 */
class GenericLinkTest {

    // anonymous

    @Test
    @Throws(Exception::class)
    fun anonymousContainerLink() {
        val l = Link.parseLink("[[language/project/resource]]")
        assertEquals(null, l!!.title)
        assertEquals("language", l.language)
        assertEquals("project", l.project)
        assertEquals("resource", l.resource)
        assertEquals(null, l.arguments)
        assertFalse(l.isExternal)
        assertFalse(l.isMedia)
        assertFalse(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun anonymousHttpsLink() {
        val l = Link.parseLink("[[https://www.google.com]]")
        assertEquals(null, l!!.title)
        assertTrue(l.isExternal)
        assertFalse(l.isMedia)
        assertFalse(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun anonymousHttpLink() {
        val l = Link.parseLink("[[http://www.google.com]]")
        assertEquals(null, l!!.title)
        assertTrue(l.isExternal)
        assertFalse(l.isMedia)
        assertFalse(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun anonymousShorthandResourceLink() {
        val l = Link.parseLink("[[language/project]]")
        assertEquals(null, l!!.title)
        assertEquals("language", l.language)
        assertEquals("project", l.project)
        assertEquals("project", l.resource)
        assertEquals(null, l.arguments)
        assertFalse(l.isExternal)
        assertFalse(l.isMedia)
        assertFalse(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun anonymousAbbreviatedLink() {
        val l = Link.parseLink("[[slug]]")
        assertEquals(null, l!!.title)
        assertEquals(null, l.language)
        assertEquals(null, l.project)
        assertEquals(null, l.resource)
        assertEquals("slug", l.arguments)
        assertFalse(l.isExternal)
        assertFalse(l.isMedia)
        assertFalse(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun anonymousAnyLanguageLink() {
        val l = Link.parseLink("[[//project/resource/args]]")
        assertEquals(null, l!!.title)
        assertEquals(null, l.language)
        assertEquals("project", l.project)
        assertEquals("resource", l.resource)
        assertEquals("args", l.arguments)
        assertFalse(l.isExternal)
        assertFalse(l.isMedia)
        assertFalse(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun anonymousAnyLanguageNoArgsLink() {
        val l = Link.parseLink("[[//project/resource]]")
        assertEquals(null, l!!.title)
        assertEquals(null, l.language)
        assertEquals("project", l.project)
        assertEquals("resource", l.resource)
        assertEquals(null, l.arguments)
        assertFalse(l.isExternal)
        assertFalse(l.isMedia)
        assertFalse(l.isPassage)
    }

    // titled

    @Test
    @Throws(Exception::class)
    fun titledContainerLink() {
        val l = Link.parseLink("[Link Title](language/project/resource)")
        assertEquals("Link Title", l!!.title)
        assertEquals("language", l.language)
        assertEquals("project", l.project)
        assertEquals("resource", l.resource)
        assertEquals(null, l.arguments)
        assertFalse(l.isExternal)
        assertFalse(l.isMedia)
        assertFalse(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun titledHttpsLink() {
        val l = Link.parseLink("[Link Title](https://www.google.com)")
        assertEquals("Link Title", l!!.title)
        assertTrue(l.isExternal)
        assertFalse(l.isMedia)
        assertFalse(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun titledHttpLink() {
        val l = Link.parseLink("[Link Title](http://www.google.com)")
        assertEquals("Link Title", l!!.title)
        assertTrue(l.isExternal)
        assertFalse(l.isMedia)
        assertFalse(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun titledShorthandResourceLink() {
        val l = Link.parseLink("[Link Title](language/project)")
        assertEquals("Link Title", l!!.title)
        assertEquals("language", l.language)
        assertEquals("project", l.project)
        assertEquals("project", l.resource)
        assertEquals(null, l.arguments)
        assertFalse(l.isExternal)
        assertFalse(l.isMedia)
        assertFalse(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun titledAbbreviatedLink() {
        val l = Link.parseLink("[Link Title](slug)")
        assertEquals("Link Title", l!!.title)
        assertEquals(null, l.language)
        assertEquals(null, l.project)
        assertEquals(null, l.resource)
        assertEquals("slug", l.arguments)
        assertFalse(l.isExternal)
        assertFalse(l.isMedia)
        assertFalse(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun titledAnyLanguageLink() {
        val l = Link.parseLink("[Link Title](//project/resource/args)")
        assertEquals("Link Title", l!!.title)
        assertEquals(null, l.language)
        assertEquals("project", l.project)
        assertEquals("resource", l.resource)
        assertEquals("args", l.arguments)
        assertFalse(l.isExternal)
        assertFalse(l.isMedia)
        assertFalse(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun titledAnyLanguageNoArgsLink() {
        val l = Link.parseLink("[Link Title](//project/resource)")
        assertEquals("Link Title", l!!.title)
        assertEquals(null, l.language)
        assertEquals("project", l.project)
        assertEquals("resource", l.resource)
        assertEquals(null, l.arguments)
        assertFalse(l.isExternal)
        assertFalse(l.isMedia)
        assertFalse(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun titledMediaLink() {
        // without preceding slash
        val l = Link.parseLink("[Link Title](image:language/project/resource)")
        assertEquals("Link Title", l!!.title)
        assertEquals("language", l.language)
        assertEquals("project", l.project)
        assertEquals("resource", l.resource)
        assertEquals(null, l.arguments)
        assertEquals("image", l.protocol)
        assertFalse(l.isExternal)
        assertTrue(l.isMedia)
        assertFalse(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun titledMediaAltLink() {
        // with perceding slash
        val l = Link.parseLink("[Link Title](image:/language/project/resource)")
        assertEquals("Link Title", l!!.title)
        assertEquals("language", l.language)
        assertEquals("project", l.project)
        assertEquals("resource", l.resource)
        assertEquals(null, l.arguments)
        assertEquals("image", l.protocol)
        assertFalse(l.isExternal)
        assertTrue(l.isMedia)
        assertFalse(l.isPassage)
    }

    @Test
    @Throws(Exception::class)
    fun titledMediaAnyLanguageLink() {
        val l = Link.parseLink("[Link Title](image://project/resource)")
        assertEquals("Link Title", l!!.title)
        assertEquals(null, l.language)
        assertEquals("project", l.project)
        assertEquals("resource", l.resource)
        assertEquals(null, l.arguments)
        assertEquals("image", l.protocol)
        assertFalse(l.isExternal)
        assertTrue(l.isMedia)
        assertFalse(l.isPassage)
    }
}