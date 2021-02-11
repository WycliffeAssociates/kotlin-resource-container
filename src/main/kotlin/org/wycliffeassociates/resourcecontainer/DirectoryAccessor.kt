package org.wycliffeassociates.resourcecontainer

import java.io.*

class DirectoryAccessor(private val rootDir: File) : IResourceContainerAccessor {
    override fun getInputStream(filename: String): InputStream {
        return getFile(filename).inputStream()
    }

    override fun getInputStreams(path: String, extension: String): Map<String, InputStream> {
        val inputStreamMap: MutableMap<String, InputStream> = mutableMapOf()
        val normalizedPath = File(path).normalize().invariantSeparatorsPath
        val contentDir = rootDir.resolve(normalizedPath)

        contentDir.walk().filter { it.isFile }.forEach { file ->
            if (file.extension == extension) {
                val relativePath = file.relativeTo(rootDir).invariantSeparatorsPath
                inputStreamMap[relativePath] = file.inputStream()
            }
        }

        return inputStreamMap
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
        val file = getFile(filename).also{ it.parentFile.mkdirs() }
        file.outputStream().use { os ->
            writeFunction(os)
        }
    }

    override fun write(files: Map<String, (OutputStream) -> Unit>) {
        files.entries.forEach { (filename, writeFunction) ->
            val file = getFile(filename).also{ it.parentFile.mkdirs() }
            file.outputStream().use { os ->
                writeFunction(os)
            }
        }
    }

    override fun close() {
        // Consider closing all readers/writers here, but it doesn't seem to be needed.
    }

    override val root: String? = null
}
