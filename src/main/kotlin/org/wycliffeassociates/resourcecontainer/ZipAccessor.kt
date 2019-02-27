package org.wycliffeassociates.resourcecontainer

import java.io.File
import java.io.IOException
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ZipAccessor(private var file: File) : IResourceContainerAccessor {

    private val zipFile
        get() = ZipFile(file)

    override fun getReader(filename: String): BufferedReader {
        return zipFile.getInputStream(zipFile.getEntry(filename)).bufferedReader()
    }

    override fun checkFileExists(filename: String): Boolean {
        return zipFile.entries().toList().map { it.name }.contains(filename)
    }

    override fun initWrite() {
        // noop
    }

    override fun write(filename: String, writeFcn: (BufferedWriter) -> Unit) {
        val doCopy = file.exists()
        val dest = when (doCopy) {
            true -> File(file.parent, "temp.zip")
            false -> file
        }
        ZipOutputStream(FileOutputStream(dest)).use { zos ->
            var found = false
            if (doCopy) {
                zipFile.entries().iterator().forEach {
                    try {
                        if (it.name == filename) {
                            zos.write(ZipEntry(filename), writeFcn)
                            found = true
                        } else {
                            // Simply doing zos.putNextEntry(it) resulted in ZipExceptions - invalid entry
                            // compressed size
                            val destEntry = ZipEntry(it.name)
                            zos.putNextEntry(destEntry)
                            zipFile.getInputStream(destEntry).copyTo(zos)
                        }
                        zos.tryCloseEntry()
                    } catch (e: IOException) {
                        throw e
                    }
                }
            }
            if (!found) {
                zos.write(ZipEntry(filename), writeFcn)
                zos.tryCloseEntry()
            }
        }
        if (doCopy) {
            val parent = file.parent
            val name = file.name
            Files.delete(file.toPath())
            dest.renameTo(File(parent, name))
            file = dest
        }
    }
}

private fun ZipOutputStream.write(zipEntry: ZipEntry, writeFcn: (BufferedWriter) -> Unit) {
    putNextEntry(zipEntry)
    writeFcn(bufferedWriter())
}

private fun ZipOutputStream.tryCloseEntry() {
    try {
        closeEntry()
    } catch (e: IOException) {
        println(e)
    }
}
