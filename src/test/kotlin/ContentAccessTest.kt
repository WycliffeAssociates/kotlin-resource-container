package org.wycliffeassociates.resourcecontainer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
    fun getContentFromRC() {
        testCases.forEach { testCase ->
            val rcFile = getRCFile(testCase.path)
            ResourceContainer.load(rcFile).use { rc ->
                val content = rc.getProjectContent(
                    projectIdentifier = "sng", extension = testCase.extension
                )

                assertNotNull(content)
                content!!
                assertEquals(testCase.expectedStreams, content.streams.size)
                assertFalse(
                        "Empty file stream found",
                        content.streams.any { it.value.available() == 0 }
                )

                content.streams.forEach { entry ->
                    tempDir.resolve(File(entry.key).name).outputStream().use { output ->
                        output.write(entry.value.read())
                    }
                }
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun failOnClosedZipFile() {
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
    fun subsequentAccess() {
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
}