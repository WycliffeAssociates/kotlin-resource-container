package org.wycliffeassociates.resourcecontainer

import java.io.*

class DirectoryAccessor(private val rootDir: File) : IResourceContainerAccessor {
    override fun getInputStream(filename: String): InputStream {
        return getFile(filename).inputStream()
    }

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

    override fun write(filename: String, writeFunction: (OutputStream) -> Unit) {
        writeFunction(getFile(filename).outputStream())
    }

    override fun write(files: Map<String, (OutputStream) -> Unit>) {
        files.entries.forEach { (filename, writeFunction) ->
            writeFunction(getFile(filename).outputStream())
        }
    }

    override fun close() {
        // Consider closing all readers/writers here, but it doesn't seem to be needed.
    }

    override val root: String? = null
}
