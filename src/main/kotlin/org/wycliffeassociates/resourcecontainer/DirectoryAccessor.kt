package org.wycliffeassociates.resourcecontainer

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File

class DirectoryAccessor(private val rootDir: File) : IResourceContainerAccessor {
    override fun getReader(filename: String): BufferedReader {
        return getFile(filename).bufferedReader()
    }

    override fun fileExists(filename: String): Boolean {
        return getFile(filename).exists()
    }

    private fun getFile(filename: String): File = File(rootDir, filename)

    override fun initWrite() {
        rootDir.mkdirs()
    }

    override fun write(filename: String, writeFunction: (BufferedWriter) -> Unit) {
        writeFunction(getFile(filename).bufferedWriter())
    }
}