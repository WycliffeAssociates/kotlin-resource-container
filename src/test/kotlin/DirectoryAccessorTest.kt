package org.wycliffeassociates.resourcecontainer

import org.junit.Assert
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException

class DirectoryAccessorTest {

    @Test
    fun testListFiles() {
        val rcDir = getResourceFile("sng_book_various_content_rc")
        DirectoryAccessor(rcDir).use { da ->
            accessorListFilesTestCases.forEach {
                Assert.assertEquals(it.second, da.list(it.first).size)
            }
        }
    }

    private fun getResourceFile(name: String): File {
        return javaClass.classLoader.getResource(name)?.file
            ?.let { File(it) } ?: throw FileNotFoundException("Resource not found: $name")
    }
}