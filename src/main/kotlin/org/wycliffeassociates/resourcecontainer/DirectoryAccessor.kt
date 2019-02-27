package org.wycliffeassociates.resourcecontainer

import java.io.File
import java.io.Reader
import java.io.Writer

class DirectoryAccessor(private val rootDir: File) : IResourceContainerAccessor {
    override fun getReader(filename: String): Reader {
        return getFile(filename).bufferedReader()
    }

    override fun fileExists(filename: String): Boolean {
        return getFile(filename).exists()
    }

    private fun getFile(filename: String): File = File(rootDir, filename)

    override fun initWrite() {
        rootDir.mkdirs()
    }

    override fun write(filename: String, writeFunction: (Writer) -> Unit) {
        writeFunction(getFile(filename).bufferedWriter())
    }
}