package org.wycliffeassociates.resourcecontainer;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by joel on 10/11/16.
 */
public class GenericLinkTest {

    // anonymous

    @Test
    public void anonymousContainerLink() throws Exception {
        Link l = Link.parseLink("[[language/project/resource]]");
        assertEquals(null, l.title);
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals(null, l.arguments);
        assertFalse(l.isExternal());
        assertFalse(l.isMedia());
        assertFalse(l.isPassage());
    }

    @Test
    public void anonymousHttpsLink() throws Exception {
        Link l = Link.parseLink("[[https://www.google.com]]");
        assertEquals(null, l.title);
        assertTrue(l.isExternal());
        assertFalse(l.isMedia());
        assertFalse(l.isPassage());
    }

    @Test
    public void anonymousHttpLink() throws Exception {
        Link l = Link.parseLink("[[http://www.google.com]]");
        assertEquals(null, l.title);
        assertTrue(l.isExternal());
        assertFalse(l.isMedia());
        assertFalse(l.isPassage());
    }

    @Test
    public void anonymousShorthandResourceLink() throws Exception {
        Link l = Link.parseLink("[[language/project]]");
        assertEquals(null, l.title);
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("project", l.resource);
        assertEquals(null, l.arguments);
        assertFalse(l.isExternal());
        assertFalse(l.isMedia());
        assertFalse(l.isPassage());
    }

    @Test
    public void anonymousAbbreviatedLink() throws Exception {
        Link l = Link.parseLink("[[slug]]");
        assertEquals(null, l.title);
        assertEquals(null, l.language);
        assertEquals(null, l.project);
        assertEquals(null, l.resource);
        assertEquals("slug", l.arguments);
        assertFalse(l.isExternal());
        assertFalse(l.isMedia());
        assertFalse(l.isPassage());
    }

    @Test
    public void anonymousAnyLanguageLink() throws Exception {
        Link l = Link.parseLink("[[//project/resource/args]]");
        assertEquals(null, l.title);
        assertEquals(null, l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals("args", l.arguments);
        assertFalse(l.isExternal());
        assertFalse(l.isMedia());
        assertFalse(l.isPassage());
    }

    @Test
    public void anonymousAnyLanguageNoArgsLink() throws Exception {
        Link l = Link.parseLink("[[//project/resource]]");
        assertEquals(null, l.title);
        assertEquals(null, l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals(null, l.arguments);
        assertFalse(l.isExternal());
        assertFalse(l.isMedia());
        assertFalse(l.isPassage());
    }

    // titled

    @Test
    public void titledContainerLink() throws Exception {
        Link l = Link.parseLink("[Link Title](language/project/resource)");
        assertEquals("Link Title", l.title);
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals(null, l.arguments);
        assertFalse(l.isExternal());
        assertFalse(l.isMedia());
        assertFalse(l.isPassage());
    }

    @Test
    public void titledHttpsLink() throws Exception {
        Link l = Link.parseLink("[Link Title](https://www.google.com)");
        assertEquals("Link Title", l.title);
        assertTrue(l.isExternal());
        assertFalse(l.isMedia());
        assertFalse(l.isPassage());
    }

    @Test
    public void titledHttpLink() throws Exception {
        Link l = Link.parseLink("[Link Title](http://www.google.com)");
        assertEquals("Link Title", l.title);
        assertTrue(l.isExternal());
        assertFalse(l.isMedia());
        assertFalse(l.isPassage());
    }

    @Test
    public void titledShorthandResourceLink() throws Exception {
        Link l = Link.parseLink("[Link Title](language/project)");
        assertEquals("Link Title", l.title);
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("project", l.resource);
        assertEquals(null, l.arguments);
        assertFalse(l.isExternal());
        assertFalse(l.isMedia());
        assertFalse(l.isPassage());
    }

    @Test
    public void titledAbbreviatedLink() throws Exception {
        Link l = Link.parseLink("[Link Title](slug)");
        assertEquals("Link Title", l.title);
        assertEquals(null, l.language);
        assertEquals(null, l.project);
        assertEquals(null, l.resource);
        assertEquals("slug", l.arguments);
        assertFalse(l.isExternal());
        assertFalse(l.isMedia());
        assertFalse(l.isPassage());
    }

    @Test
    public void titledAnyLanguageLink() throws Exception {
        Link l = Link.parseLink("[Link Title](//project/resource/args)");
        assertEquals("Link Title", l.title);
        assertEquals(null, l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals("args", l.arguments);
        assertFalse(l.isExternal());
        assertFalse(l.isMedia());
        assertFalse(l.isPassage());
    }

    @Test
    public void titledAnyLanguageNoArgsLink() throws Exception {
        Link l = Link.parseLink("[Link Title](//project/resource)");
        assertEquals("Link Title", l.title);
        assertEquals(null, l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals(null, l.arguments);
        assertFalse(l.isExternal());
        assertFalse(l.isMedia());
        assertFalse(l.isPassage());
    }

    @Test
    public void titledMediaLink() throws Exception {
        // without preceding slash
        Link l = Link.parseLink("[Link Title](image:language/project/resource)");
        assertEquals("Link Title", l.title);
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals(null, l.arguments);
        assertEquals("image", l.protocal);
        assertFalse(l.isExternal());
        assertTrue(l.isMedia());
        assertFalse(l.isPassage());
    }

    @Test
    public void titledMediaAltLink() throws Exception {
        // with perceding slash
        Link l = Link.parseLink("[Link Title](image:/language/project/resource)");
        assertEquals("Link Title", l.title);
        assertEquals("language", l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals(null, l.arguments);
        assertEquals("image", l.protocal);
        assertFalse(l.isExternal());
        assertTrue(l.isMedia());
        assertFalse(l.isPassage());
    }

    @Test
    public void titledMediaAnyLanguageLink() throws Exception {
        Link l = Link.parseLink("[Link Title](image://project/resource)");
        assertEquals("Link Title", l.title);
        assertEquals(null, l.language);
        assertEquals("project", l.project);
        assertEquals("resource", l.resource);
        assertEquals(null, l.arguments);
        assertEquals("image", l.protocal);
        assertFalse(l.isExternal());
        assertTrue(l.isMedia());
        assertFalse(l.isPassage());
    }
}
