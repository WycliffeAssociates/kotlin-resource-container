package org.wycliffeassociates.resourcecontainer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException

class ContentAccessTest {
    private data class GetContentTestCase(
        val path: String,
        val extension: String,
        val expectedStreams: Int
    )

    private lateinit var tempDir: File
    private lateinit var testCases: List<GetContentTestCase>

    @Before
    fun setUp() {
        tempDir = createTempDir()
        val testCaseResource = javaClass.classLoader.getResourceAsStream("GetRCContentTestCases.json")
        testCaseResource.use {
            testCases = jacksonObjectMapper().readValue(it)
        }
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Throws(FileNotFoundException::class)
    private fun getRCFile(name: String): File {
        val resource = javaClass.classLoader.getResource(name)
                ?: throw FileNotFoundException("Test resource not found: $name")
        return File(resource.file)
    }

    @Test
    @Throws(Exception::class)
    fun testGetContentFromRC() {
        testCases.forEach { testCase ->
            val rcFile = getRCFile(testCase.path)
            ResourceContainer.load(rcFile).use { rc ->
                val content = rc.getProjectContent(
                    projectIdentifier = "sng", extension = testCase.extension
                )

                assertNotNull(content)
                content!!
                assertEquals(testCase.expectedStreams, content.streams.size)

                content.streams.forEach { entry ->
                    tempDir.resolve(File(entry.key).name).outputStream().use { output ->
                        output.write(entry.value.readBytes())
                    }
                }
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun testStreamClosedZipRC() {
        val testCase = testCases.first { it.path.endsWith(".zip") }
        val rcFile = getRCFile(testCase.path)
        val rc = ResourceContainer.load(rcFile)
        val content = rc.getProjectContent(extension = testCase.extension)

        assertNotNull(content)
        rc.close() // closing the rc (zip) should properly close all opened streams

        assertFalse(
                "Streams were not properly closed.",
                content!!.streams.any { it.value.available() != 0 }
        )
    }

    @Test
    fun testSubsequentAccess() {
        val testCase = testCases.first { it.path.endsWith(".zip") }
        val rcFile = getRCFile(testCase.path)
        val rc = ResourceContainer.load(rcFile)
        var content = rc.getProjectContent(extension = testCase.extension)

        assertNotNull(content)
        assertEquals(testCase.expectedStreams, content!!.streams.size)

        // repeat accessing content
        content = rc.getProjectContent(extension = testCase.extension)

        assertNotNull(content)
        assertEquals(testCase.expectedStreams, content!!.streams.size)
    }

    @Test
    fun test_randomly_read_content_stream_from_zip_RC() {
        val testCase = testCases.first { it.path.endsWith(".zip") }
        val rcFile = getRCFile(testCase.path)
        val rc = ResourceContainer.load(rcFile)
        var content = rc.getProjectContent(extension = "wav")

        assertNotNull(content)
        content!!
        assertEquals(
                "Expected 3 files from content",
                3, content.streams.size
        )

        val entries = content.streams.keys.toList()
        try {
            content.streams[entries[2]]!!.use { input ->
                tempDir.resolve("file_2.wav").outputStream().use { output ->
                    output.write(input.readBytes())
                }
            }

            content.streams[entries[0]]!!.use { input ->
                tempDir.resolve("file_0.wav").outputStream().use { output ->
                    output.write(input.readBytes())
                }
            }

            content.streams[entries[1]]!!.use { input ->
                tempDir.resolve("file_1.wav").outputStream().use { output ->
                    output.write(input.readBytes())
                }
            }
        } catch (ex: Exception) {
            fail("Random content stream accessing failed")
        }
    }
}