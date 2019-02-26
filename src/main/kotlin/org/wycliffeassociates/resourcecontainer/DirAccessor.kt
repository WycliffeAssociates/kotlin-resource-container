package org.wycliffeassociates.resourcecontainer

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File

class DirAccessor(private val file: File) : IResourceContainerAccessor {
    override fun getReader(filename: String): BufferedReader {
        return getFile(filename).bufferedReader()
    }

    override fun checkFileExists(filename: String): Boolean {
        return getFile(filename).exists()
    }

    private fun getFile(filename: String): File = File(file, filename)

    override fun initWrite() {
        file.mkdirs()
    }

    override fun write(filename: String, writeFcn: (BufferedWriter) -> Unit) {
        writeFcn(getFile(filename).bufferedWriter())
    }
}