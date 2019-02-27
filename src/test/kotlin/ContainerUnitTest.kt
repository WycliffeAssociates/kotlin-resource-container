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
    @Rule
    @JvmField
    var resourceDir = TemporaryFolder()


    private val loadSingleBookRcCases = listOf(
            "valid_single_book_rc",
            "valid_single_book_rc.zip"
    )

    @Test
    @Throws(Exception::class)
    fun loadSingleBookRC() {
        loadSingleBookRcCases.forEach {
            val classLoader = this.javaClass.classLoader
            val resource = classLoader.getResource(it)
            val containerDir = File(resource!!.toURI().path)

            val container = ResourceContainer.load(containerDir)

            assertNotNull(container)
        }
    }

    private val loadMultiBookRcCases = listOf(
            "valid_multi_book_rc",
            "valid_multi_book_rc.zip"
    )

    @Test
    @Throws(Exception::class)
    fun loadMultiBookRC() {
        loadMultiBookRcCases.forEach {
            val classLoader = this.javaClass.classLoader
            val resource = classLoader.getResource(it)
            val containerDir = File(resource!!.toURI().path)

            val container = ResourceContainer.load(containerDir)

            assertNotNull(container)
        }
    }

    private val failToLoadMissingManifestCases = listOf(
            "missing_manifest",
            "missing_manifest.zip"
    )

    @Test
    @Throws(Exception::class)
    fun failToLoadMissingManifest() {
        failToLoadMissingManifestCases.forEach {
            val classLoader = this.javaClass.classLoader
            val resource = classLoader.getResource(it)
            val containerDir = File(resource!!.toURI().path)

            try {
                val container = ResourceContainer.load(containerDir)
                assertNull(container)
            } catch (e: Exception) {
                assertEquals("Missing manifest.yaml", e.message)
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun loadMissingRCWhenNotInStrictMode() {
        val containerDir = File("missing_rc")

        val container = ResourceContainer.load(containerDir, false)
        assertNotNull(container)
    }

    private val updateRcTestCases = listOf(
            "valid_single_book_rc",
            "valid_single_book_rc.zip"
    )

    @Test
    @Throws(Exception::class)
    fun updateRC() {
        val classLoader = this.javaClass.classLoader
        updateRcTestCases.forEach {
            val resource = classLoader.getResource(it)
            val containerDir = File(resource!!.toURI().path)

            val container = ResourceContainer.load(containerDir)

            assertNotNull(container)
        }
    }

    private val overwriteRcTestCases = listOf(
            "overwrite_manifest_rc",
            "overwrite_manifest_rc.zip"
    )

    @Test
    @Throws(Exception::class)
    fun overwriteManifest() {
        val classLoader = this.javaClass.classLoader
        overwriteRcTestCases.forEach {
            val resource = classLoader.getResource(it)
            val containerFile = File(resource!!.toURI().path)

            val rc = ResourceContainer.load(containerFile)

            assertNotNull(rc)
            assertEquals("book", rc.type())

            rc.manifest = org.wycliffeassociates.resourcecontainer.entity.manifest {
                dublinCore = dublincore {
                    type = "testType"
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

            assertNotNull(rc)
            assertEquals("testType", rc.type())

            rc.writeManifest()
            val loaded = ResourceContainer.load(containerFile, strict = false)
            assertNotNull(loaded)
            assertEquals("testType", loaded.type())
        }
    }

    private val createNewRcTestCases = listOf(
            "new_rc",
            "new_rc.zip"
    )

    @Test
    @Throws(Exception::class)
    fun createNewRC() {
        val classLoader = this.javaClass.classLoader
        val resource = classLoader.getResource("valid_single_book_rc")

        createNewRcTestCases.forEach {
            val containerFile = File(File(resource!!.toURI().path).parentFile, it)

            ResourceContainer.create(containerFile) {
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
            }.use { rc ->
                assertNotNull(rc)
                assertEquals("book", rc.type())

                rc.write()
                val loaded = ResourceContainer.load(containerFile)
                assertNotNull(loaded)
                assertEquals(ResourceContainer.conformsTo, loaded.conformsTo())
                assertEquals("book", loaded.type())
            }
        }
    }

    private val failOpeningOldRCCases = listOf(
            "old_rc",
            "old_rc.zip"
    )

    @Test
    @Throws(Exception::class)
    fun failOpeningOldRC() {
        failOpeningOldRCCases.forEach {
            val classLoader = this.javaClass.classLoader
            val resource = classLoader.getResource(it)
            val containerDir = File(resource!!.toURI().path)

            try {
                val container = ResourceContainer.load(containerDir)
                assertNull(container)
            } catch (e: OutdatedRCException) {
                assertEquals("Found 0.1 but expected " + ResourceContainer.conformsTo, e.message)
            }
        }
    }

    private val failOpeningUnsupportedRCCases = listOf(
            "unsupported_rc",
            "unsupported_rc.zip"
    )

    @Test
    @Throws(Exception::class)
    fun failOpeningUnsupportedRC() {
        failOpeningUnsupportedRCCases.forEach {
            val classLoader = this.javaClass.classLoader
            val resource = classLoader.getResource(it)
            val containerDir = File(resource!!.toURI().path)

            try {
                val container = ResourceContainer.load(containerDir)
                assertNull(container)
            } catch (e: UnsupportedRCException) {
                assertEquals("Found 9999990.1 but expected " + ResourceContainer.conformsTo, e.message)
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun semverComparison() {
        val EQUAL = 0
        val GREATER_THAN = 1
        val LESS_THAN = -1

        assertEquals(EQUAL, Semver.compare("10.0.1", "10.0.1"))
        assertEquals(EQUAL, Semver.compare("10.0", "10.0.0"))
        assertEquals(EQUAL, Semver.compare("10.*", "10.0.0"))
        assertEquals(EQUAL, Semver.compare("10.*", "10.9.0"))
        assertEquals(EQUAL, Semver.compare("10.0.0", "10.0-alpha.0"))
        assertEquals(EQUAL, Semver.compare("10.0.0", "v10.0.0"))
        assertEquals(EQUAL, Semver.compare("10.*.1", "10.9.1"))
        assertEquals(EQUAL, Semver.compare("0.8.1", "0.8.1"))
        assertEquals(EQUAL, Semver.compare("*", "0.8.1"))
        assertEquals(EQUAL, Semver.compare("0.8.1", "*"))

        assertEquals(GREATER_THAN, Semver.compare("10.0.0", "1.0.0"))
        assertEquals(GREATER_THAN, Semver.compare("10.1.0", "10.0.0"))
        assertEquals(GREATER_THAN, Semver.compare("10", "9.9.0"))
        assertEquals(GREATER_THAN, Semver.compare("10.1-alpha.0", "10.0.0"))
        assertEquals(GREATER_THAN, Semver.compare("10.9.6", "10.*.1"))
        assertEquals(GREATER_THAN, Semver.compare("0.9.6", "0.9.1"))
        assertEquals(GREATER_THAN, Semver.compare("0.10.0", "0.9.*"))

        assertEquals(LESS_THAN, Semver.compare("1.0.0", "10.0.0"))
        assertEquals(LESS_THAN, Semver.compare("10.0.0", "10.1.0"))
        assertEquals(LESS_THAN, Semver.compare("9.9.0", "10"))
        assertEquals(LESS_THAN, Semver.compare("10.0.0", "10.1-alpha.0"))
        assertEquals(LESS_THAN, Semver.compare("10.*.1", "10.9.6"))
        assertEquals(LESS_THAN, Semver.compare("0.9.1", "0.9.6"))
        assertEquals(LESS_THAN, Semver.compare("0.9.*", "0.10.0"))
    }
}