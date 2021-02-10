package org.wycliffeassociates.resourcecontainer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class ContentAccessTest {
    private data class GetContentTestCase(
        val path: String,
        val extension: String,
        val numberOfStreams: Int
    )

    private val tempDir = createTempDir()
    private lateinit var testCases: List<GetContentTestCase>

    @Before
    fun setUp() {
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
                ?: throw FileNotFoundException()
        return File(resource.file)
    }

    @Test
    @Throws(Exception::class)
    fun getContentFromRC() {
        testCases.forEach { testCase ->
            val rcFile = getRCFile(testCase.path)
            val rc = ResourceContainer.load(rcFile).use {
                val content = it.getProjectContent(
                        projectIdentifier = "sng", extension = testCase.extension
                )

                assertNotNull(content)
                assertEquals(testCase.numberOfStreams, content!!.streams.size)

                content.streams.forEach { entry ->
                    tempDir.resolve(File(entry.key).name).outputStream().use { output ->
                        output.write(entry.value.read())
                    }
                }
            }
        }
    }

    @Test
    fun failOnClosedRC() {
        val testCase = testCases[0]
        val rcFile = getRCFile(testCase.path)
        val rc = ResourceContainer.load(rcFile)
        val content = rc.getProjectContent(extension = testCase.extension)

        rc.close()
        try {
            content!!.streams.forEach { entry ->
                tempDir.resolve(File(entry.key).name).outputStream().use { output ->
                    output.write(entry.value.read())
                }
            }
        } catch (ex: IOException) {
            assertEquals("Stream closed", ex.message)
        }
    }
}