package org.wycliffeassociates.resourcecontainer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wycliffeassociates.resourcecontainer.errors.OutdatedRCException;
import org.wycliffeassociates.resourcecontainer.errors.RCException;
import org.wycliffeassociates.resourcecontainer.errors.UnsupportedRCException;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ContainerUnitTest {
    @Rule
    public TemporaryFolder resourceDir = new TemporaryFolder();

    @Test
    public void loadSingleBookRC() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("valid_single_book_rc");
        File containerDir = new File(resource.getPath());

        Factory factory = new Factory();
        ResourceContainer container = factory.load(containerDir);

        assertNotNull(container);
        assertEquals(4, container.chapters().length);
        assertEquals(8, container.chunks("01").length);
        assertEquals("Titus", container.readChunk("front", "title").trim());
        assertTrue(container.config().get("content").size() > 0);
        assertTrue(container.toc().size() > 0);

        // write to toc and config
        container.writeTOC("something");
        container.writeConfig("something_else");

        assertEquals("something", container.toc().value());
        assertEquals("something_else", container.config().value());

        // delete toc and config
        container.writeTOC(null);
        assertEquals(null, container.toc().value());

        container.writeConfig(null);
        assertEquals(null, container.config().value());
    }

    @Test
    public void loadMultiBookRC() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("valid_multi_book_rc");
        File containerDir = new File(resource.getPath());

        Factory factory = new Factory();
        ResourceContainer container = factory.load(containerDir);

        assertNotNull(container);

        assertEquals(4, container.chapters("tit").length);
        assertEquals(8, container.chunks("tit", "01").length);
        assertEquals("Titus", container.readChunk("tit", "front", "title").trim());

        assertEquals(4, container.chapters("gen").length);
        assertEquals(8, container.chunks("gen", "01").length);
        assertEquals("Genesis", container.readChunk("gen", "front", "title").trim());

        // write to toc and config
        container.writeTOC("gen", "something");
        container.writeConfig("gen", "something_else");

        assertEquals("something", container.toc("gen").value());
        assertEquals("something_else", container.config("gen").value());

        // delete toc and config
        container.writeTOC("gen", null);
        assertEquals(null, container.toc("gen").value());

        container.writeConfig("gen", null);
        assertEquals(null, container.config("gen").value());

        // test exceptions
        try {
            container.writeConfig(null);
            assertTrue(false);
        } catch (RCException e) {
            assertEquals("Multiple projects found. Specify the project identifier.", e.getMessage());
        }
    }

    @Test
    public void failToLoadMissingRC() throws Exception {
        File containerDir = new File("missing_rc");

        Factory factory = new Factory();
        try {
            ResourceContainer container = factory.load(containerDir);
            assertNull(container);
        } catch (Exception e) {
            assertEquals("Missing manifest.yaml", e.getMessage());
        }
    }

    @Test
    public void loadMissingRCWhenNotInStrictMode() throws Exception {
        File containerDir = new File("missing_rc");

        Factory factory = new Factory();
        ResourceContainer container = factory.load(containerDir, false);
        assertNotNull(container);
    }

    @Test
    public void updateRC() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("valid_single_book_rc");
        File containerDir = new File(resource.getPath());

        Factory factory = new Factory();
        ResourceContainer container = factory.load(containerDir);

        assertNotNull(container);
        assertEquals("Titus", container.readChunk("front", "title").trim());
        container.writeChunk("front", "title", "Titus Updated");
        container.writeChunk("80", "12", "What is this?");
        assertEquals("Titus Updated", container.readChunk("front", "title").trim());
        assertEquals("What is this?", container.readChunk("80", "12").trim());
    }

    @Test
    public void createNewRC() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("valid_single_book_rc");
        File containerDir = new File(new File(resource.getPath()).getParentFile(), "new_rc");

        Factory factory = new Factory();
        HashMap<String, Object> manifest = new HashMap<>();
        HashMap<String, String> language = new HashMap<>();
        language.put("identifier", "en");
        language.put("title", "English");
        language.put("direction", "ltr");

        HashMap<String, Object> dublinCore = new HashMap<>();
        dublinCore.put("type", "book");
        dublinCore.put("format", "text/usfm");
        dublinCore.put("identifier", "en-me");
        dublinCore.put("rights", "CC BY-SA 4.0");
        dublinCore.put("language", language);
        manifest.put("dublin_core", dublinCore);
        ResourceContainer container = factory.create(containerDir, manifest);

        assertNotNull(container);
        assertEquals(factory.conformsTo, container.conformsTo());
        assertEquals("book", container.type());
    }

    @Test
    public void failOpeningOldRC() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("old_rc");
        File containerDir = new File(resource.getPath());

        Factory factory = new Factory();
        try {
            ResourceContainer container = factory.load(containerDir);
            assertNull(container);
        } catch (OutdatedRCException e) {
            assertEquals("Found 0.1 but expected " + Factory.conformsTo, e.getMessage());
        }
    }

    @Test
    public void failOpeningUnsupportedRC() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("unsupported_rc");
        File containerDir = new File(resource.getPath());

        Factory factory = new Factory();
        try {
            ResourceContainer container = factory.load(containerDir);
            assertNull(container);
        } catch (UnsupportedRCException e) {
            assertEquals("Found 9999990.1 but expected " + Factory.conformsTo, e.getMessage());
        }
    }

    @Test
    public void throwErrorWhenNotSpecifyingProjectInMultiProjectRC() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("valid_multi_book_rc");
        File containerDir = new File(resource.getPath());

        Factory factory = new Factory();
        ResourceContainer container = factory.load(containerDir);

        try {
            container.chapters();
            assertTrue(false);
        } catch (Exception e) {
            assertEquals("Multiple projects found. Specify the project identifier.", e.getMessage());
        }
        try {
            container.chunks("01");
            assertTrue(false);
        } catch (Exception e) {
            assertEquals("Multiple projects found. Specify the project identifier.", e.getMessage());
        }
        try {
            container.readChunk("01", "01");
            assertTrue(false);
        } catch (Exception e) {
            assertEquals("Multiple projects found. Specify the project identifier.", e.getMessage());
        }
        try {
            container.writeChunk("01", "01", "test");
            assertTrue(false);
        } catch (Exception e) {
            assertEquals("Multiple projects found. Specify the project identifier.", e.getMessage());
        }
    }

    @Test
    public void semverComparison() throws Exception {
        final int EQUAL = 0;
        final int GREATER_THAN = 1;
        final int LESS_THAN = -1;

        assertEquals(EQUAL, Semver.compare("10.0.1", "10.0.1"));
        assertEquals(EQUAL, Semver.compare("10.0", "10.0.0"));
        assertEquals(EQUAL, Semver.compare("10.*", "10.0.0"));
        assertEquals(EQUAL, Semver.compare("10.*", "10.9.0"));
        assertEquals(EQUAL, Semver.compare("10.0.0", "10.0-alpha.0"));
        assertEquals(EQUAL, Semver.compare("10.0.0", "v10.0.0"));
        assertEquals(EQUAL, Semver.compare("10.*.1", "10.9.1"));
        assertEquals(EQUAL, Semver.compare("0.8.1", "0.8.1"));
        assertEquals(EQUAL, Semver.compare("*", "0.8.1"));
        assertEquals(EQUAL, Semver.compare("0.8.1", "*"));

        assertEquals(GREATER_THAN, Semver.compare("10.0.0", "1.0.0"));
        assertEquals(GREATER_THAN, Semver.compare("10.1.0", "10.0.0"));
        assertEquals(GREATER_THAN, Semver.compare("10", "9.9.0"));
        assertEquals(GREATER_THAN, Semver.compare("10.1-alpha.0", "10.0.0"));
        assertEquals(GREATER_THAN, Semver.compare("10.9.6", "10.*.1"));
        assertEquals(GREATER_THAN, Semver.compare("0.9.6", "0.9.1"));
        assertEquals(GREATER_THAN, Semver.compare("0.10.0", "0.9.*"));

        assertEquals(LESS_THAN, Semver.compare("1.0.0", "10.0.0"));
        assertEquals(LESS_THAN, Semver.compare("10.0.0", "10.1.0"));
        assertEquals(LESS_THAN, Semver.compare("9.9.0", "10"));
        assertEquals(LESS_THAN, Semver.compare("10.0.0", "10.1-alpha.0"));
        assertEquals(LESS_THAN, Semver.compare("10.*.1", "10.9.6"));
        assertEquals(LESS_THAN, Semver.compare("0.9.1", "0.9.6"));
        assertEquals(LESS_THAN, Semver.compare("0.9.*", "0.10.0"));
    }
}