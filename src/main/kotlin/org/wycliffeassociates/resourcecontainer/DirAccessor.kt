package org.wycliffeassociates.resourcecontainer

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.IOException

class DirAccessor(private val file: File) : IResourceContainerAccessor {
    init {
        if (!File(file, MANIFEST_FILENAME).exists()) {
            throw IOException("Missing manifest.yaml")
        }
    }

    private fun <T> performAction(filename: String, dirname: String?, action: (File) -> T): T {
        return when (dirname) {
            null -> getFile(filename)
            else -> File(getFile(dirname), filename)
        }.run { action(this) }
    }

    override fun getReader(filename: String, dirname: String?): BufferedReader {
        return performAction(filename, dirname) { it.bufferedReader() }
    }

    override fun checkFileExists(filename: String, dirname: String?): Boolean {
        return performAction(filename, dirname) { it.exists() }
    }

    private fun getFile(filename: String): File = File(file, filename)

    override fun initWrite() {
        file.mkdirs()
    }

    override fun write(filename: String, dirname: String?, writeFcn: (BufferedWriter) -> Unit) {
        writeFcn(getFile(filename).bufferedWriter())
    }
}