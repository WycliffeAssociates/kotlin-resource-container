package org.wycliffeassociates.resourcecontainer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
        val expectedStreams: Int,
        val content: String
    )

    private lateinit var testCases: List<GetContentTestCase>

    @Before
    fun setUp() {
        val testCaseResource = javaClass.classLoader.getResourceAsStream("GetRCContentTestCases.json")
        testCaseResource.use {
            testCases = jacksonObjectMapper().readValue(it)
        }
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
                    val contentAsString = entry.value.bufferedReader().readText()
                    assertEquals(testCase.content, contentAsString)
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
    fun testRepeatedAccess() {
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
        var content = rc.getProjectContent(extension = testCase.extension)

        assertNotNull(content)
        content!!
        assertEquals(
                "Expected 3 files from content",
                3, content.streams.size
        )

        val entries = content.streams.keys.toList()
        try {
            content.streams[entries[2]]!!.use { input ->
                val contentAsString = input.bufferedReader().readText()
                assertEquals(testCase.content, contentAsString)
            }

            content.streams[entries[0]]!!.use { input ->
                val contentAsString = input.bufferedReader().readText()
                assertEquals(testCase.content, contentAsString)
            }

            content.streams[entries[1]]!!.use { input ->
                val contentAsString = input.bufferedReader().readText()
                assertEquals(testCase.content, contentAsString)
            }
        } catch (ex: Exception) {
            fail("Random content stream accessing failed")
        }
    }
}