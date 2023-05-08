package org.wycliffeassociates.resourcecontainer

import org.junit.Assert
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException

class ZipAccessorTest {

    @Test
    fun testListFiles() {
        val rcFile = getResourceFile("sng_book_various_content_rc.zip")

        ZipAccessor(rcFile).use { zip ->
            accessorListFilesTestCases.forEach {
                Assert.assertEquals(it.second, zip.list(it.first).size)
            }
        }
    }

    private fun getResourceFile(name: String): File {
        return javaClass.classLoader.getResource(name)?.file
            ?.let { File(it) } ?: throw FileNotFoundException("Resource not found: $name")
    }
}