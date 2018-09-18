package org.wycliffeassociates.resourcecontainer

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.wycliffeassociates.resourcecontainer.entity.dublincore
import org.wycliffeassociates.resourcecontainer.errors.OutdatedRCException
import org.wycliffeassociates.resourcecontainer.errors.UnsupportedRCException
import java.io.File

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
class ContainerUnitTest {
    @Rule @JvmField
    var resourceDir = TemporaryFolder()

    @Test
    @Throws(Exception::class)
    fun loadSingleBookRC() {
        val classLoader = this.javaClass.classLoader
        val resource = classLoader.getResource("valid_single_book_rc")
        val containerDir = File(resource!!.path)

        val container = ResourceContainer.load(containerDir)

        assertNotNull(container)
        assertEquals(4, container.chapters().size)
        assertEquals(8, container.chunks("01").size)
        assertEquals("Titus", container.readChunk("front", "title").trim())
        //assertTrue(container.config().get("content").size() > 0)
        //assertTrue(container.toc()!!.size() > 0)

        // write to toc and config
        //container.writeTOC("something")
        //container.writeConfig("something_else")

//        assertEquals("something", container.toc()!!.value())
//        assertEquals("something_else", container.config().value())

        // delete toc and config
//        container.writeTOC(null)
//        assertEquals(null, container.toc()!!.value())
//
//        container.writeConfig(null)
//        assertEquals(null, container.config().value())
    }

    @Test
    @Throws(Exception::class)
    fun loadMultiBookRC() {
        val classLoader = this.javaClass.classLoader
        val resource = classLoader.getResource("valid_multi_book_rc")
        val containerDir = File(resource!!.path)


        val container = ResourceContainer.load(containerDir)

        assertNotNull(container)

        assertEquals(4, container.chapters("tit").size)
        assertEquals(8, container.chunks("tit", "01").size)
        assertEquals("Titus", container.readChunk("tit", "front", "title").trim())

        assertEquals(4, container.chapters("gen").size)
        assertEquals(8, container.chunks("gen", "01").size)
        assertEquals("Genesis", container.readChunk("gen", "front", "title").trim())

//        // write to toc and config
//        container.writeTOC("gen", "something")
//        container.writeConfig("gen", "something_else")
//
//        assertEquals("something", container.toc("gen")!!.value())
//        assertEquals("something_else", container.config("gen").value())
//
//        // delete toc and config
//        container.writeTOC("gen", null)
//        assertEquals(null, container.toc("gen")!!.value())
//
//        container.writeConfig("gen", null)
//        assertEquals(null, container.config("gen").value())
//
//        // test exceptions
//        try {
//            container.writeConfig(null)
//            assertTrue(false)
//        } catch (e: RCException) {
//            assertEquals("Multiple projects found. Specify the project identifier.", e.message)
//        }

    }

    @Test
    @Throws(Exception::class)
    fun failToLoadMissingRC() {
        val containerDir = File("missing_rc")

        try {
            val container = ResourceContainer.load(containerDir)
            assertNull(container)
        } catch (e: Exception) {
            assertEquals("Missing manifest.yaml", e.message)
        }

    }

    @Test
    @Throws(Exception::class)
    fun loadMissingRCWhenNotInStrictMode() {
        val containerDir = File("missing_rc")

        val container = ResourceContainer.load(containerDir, false)
        assertNotNull(container)
    }

    @Test
    @Throws(Exception::class)
    fun updateRC() {
        val classLoader = this.javaClass.classLoader
        val resource = classLoader.getResource("valid_single_book_rc")
        val containerDir = File(resource!!.path)

        val container = ResourceContainer.load(containerDir)

        assertNotNull(container)
        assertEquals("Titus", container.readChunk("front", "title").trim())
        container.writeChunk("front", "title", "Titus Updated")
        container.writeChunk("80", "12", "What is this?")
        assertEquals("Titus Updated", container.readChunk("front", "title").trim())
        assertEquals("What is this?", container.readChunk("80", "12").trim())
    }

    @Test
    @Throws(Exception::class)
    fun createNewRC() {
        val classLoader = this.javaClass.classLoader
        val resource = classLoader.getResource("valid_single_book_rc")
        val containerDir = File(File(resource!!.path).parentFile, "new_rc")

        val rc = ResourceContainer.create(containerDir) {
            manifest = org.wycliffeassociates.resourcecontainer.entity.manifest {
                dublinCore = dublincore {
                    type = "book"
                    format = "text/usfm"
                    identifier = "en-me"
                    rights = "CC BY-SA 4.0"
                    language = org.wycliffeassociates.resourcecontainer.entity.language {
                        identifier = "en"
                        title = "English"
                        direction = "ltr"
                    }
                }
            }
        }

        assertNotNull(rc)
        assertEquals(ResourceContainer.conformsTo, rc.conformsTo())
        assertEquals("book", rc.type())
    }

    @Test
    @Throws(Exception::class)
    fun failOpeningOldRC() {
        val classLoader = this.javaClass.classLoader
        val resource = classLoader.getResource("old_rc")
        val containerDir = File(resource!!.path)

        try {
            val container = ResourceContainer.load(containerDir)
            assertNull(container)
        } catch (e: OutdatedRCException) {
            assertEquals("Found 0.1 but expected " + ResourceContainer.conformsTo, e.message)
        }

    }

    @Test
    @Throws(Exception::class)
    fun failOpeningUnsupportedRC() {
        val classLoader = this.javaClass.classLoader
        val resource = classLoader.getResource("unsupported_rc")
        val containerDir = File(resource!!.path)

        try {
            val container = ResourceContainer.load(containerDir)
           // assertNull(container)
        } catch (e: UnsupportedRCException) {
            assertEquals("Found 9999990.1 but expected " + ResourceContainer.conformsTo, e.message)
        }

    }

    @Test
    @Throws(Exception::class)
    fun throwErrorWhenNotSpecifyingProjectInMultiProjectRC() {
        val classLoader = this.javaClass.classLoader
        val resource = classLoader.getResource("valid_multi_book_rc")
        val containerDir = File(resource!!.path)

        val container = ResourceContainer.load(containerDir)

        try {
            container.chapters()
            assertTrue(false)
        } catch (e: Exception) {
            assertEquals("Multiple projects found. Specify the project identifier.", e.message)
        }

        try {
            container.chunks("01")
            assertTrue(false)
        } catch (e: Exception) {
            assertEquals("Multiple projects found. Specify the project identifier.", e.message)
        }

        try {
            container.readChunk("01", "01")
            assertTrue(false)
        } catch (e: Exception) {
            assertEquals("Multiple projects found. Specify the project identifier.", e.message)
        }

        try {
            container.writeChunk("01", "01", "test")
            assertTrue(false)
        } catch (e: Exception) {
            assertEquals("Multiple projects found. Specify the project identifier.", e.message)
        }

    }

    @Test
    @Throws(Exception::class)
    fun semverComparison() {
        val EQUAL = 0
        val GREATER_THAN = 1
        val LESS_THAN = -1

//        assertEquals(EQUAL, Semver.compare("10.0.1", "10.0.1"))
//        assertEquals(EQUAL, Semver.compare("10.0", "10.0.0"))
//        assertEquals(EQUAL, Semver.compare("10.*", "10.0.0"))
//        assertEquals(EQUAL, Semver.compare("10.*", "10.9.0"))
//        assertEquals(EQUAL, Semver.compare("10.0.0", "10.0-alpha.0"))
//        assertEquals(EQUAL, Semver.compare("10.0.0", "v10.0.0"))
//        assertEquals(EQUAL, Semver.compare("10.*.1", "10.9.1"))
//        assertEquals(EQUAL, Semver.compare("0.8.1", "0.8.1"))
//        assertEquals(EQUAL, Semver.compare("*", "0.8.1"))
//        assertEquals(EQUAL, Semver.compare("0.8.1", "*"))
//
//        assertEquals(GREATER_THAN, Semver.compare("10.0.0", "1.0.0"))
//        assertEquals(GREATER_THAN, Semver.compare("10.1.0", "10.0.0"))
//        assertEquals(GREATER_THAN, Semver.compare("10", "9.9.0"))
//        assertEquals(GREATER_THAN, Semver.compare("10.1-alpha.0", "10.0.0"))
//        assertEquals(GREATER_THAN, Semver.compare("10.9.6", "10.*.1"))
//        assertEquals(GREATER_THAN, Semver.compare("0.9.6", "0.9.1"))
//        assertEquals(GREATER_THAN, Semver.compare("0.10.0", "0.9.*"))
//
//        assertEquals(LESS_THAN, Semver.compare("1.0.0", "10.0.0"))
//        assertEquals(LESS_THAN, Semver.compare("10.0.0", "10.1.0"))
//        assertEquals(LESS_THAN, Semver.compare("9.9.0", "10"))
//        assertEquals(LESS_THAN, Semver.compare("10.0.0", "10.1-alpha.0"))
//        assertEquals(LESS_THAN, Semver.compare("10.*.1", "10.9.6"))
//        assertEquals(LESS_THAN, Semver.compare("0.9.1", "0.9.6"))
//        assertEquals(LESS_THAN, Semver.compare("0.9.*", "0.10.0"))
    }
}