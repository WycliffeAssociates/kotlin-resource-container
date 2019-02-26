package org.wycliffeassociates.resourcecontainer

import java.io.File
import java.io.IOException
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ZipAccessor(file: File) : IResourceContainerAccessor {

    private val zipFile = ZipFile(file)

    init {
        if (!checkFileExists(MANIFEST_FILENAME)) {
            throw IOException("Missing manifest.yaml") // TODO: Check one level down too
        }
    }

    private fun <T> performAction(filename: String, dirname: String?, action: (ZipEntry) -> T): T {
        return when (dirname) {
            null -> filename
            else -> "$dirname/$filename"
        }.run { action(zipFile.getEntry(this)) }
    }

    override fun getReader(filename: String, dirname: String?): BufferedReader {
        return performAction(filename, dirname) {
            zipFile.getInputStream(zipFile.getEntry(filename)).bufferedReader()
        }
    }

    override fun checkFileExists(filename: String, dirname: String?): Boolean {
        return performAction(filename, dirname) {
            zipFile.getEntry(filename) != null
        }
    }

    override fun initWrite() {
        // noop
    }

    override fun write(filename: String, dirname: String?, writeFcn: (BufferedWriter) -> Unit) {
        val zos = ZipOutputStream(FileOutputStream("temp.zip")) // TODO
        var found = false
        zipFile.entries().iterator().forEach {
            if (it.name == filename) {
                zos.write(ZipEntry(filename), writeFcn)
                found = true
            } else {
                zos.putNextEntry(it)
                zipFile.getInputStream(it).copyTo(zos)
            }
            zos.tryCloseEntry()
        }
        if (!found) {
            zos.write(ZipEntry(filename), writeFcn)
            zos.tryCloseEntry()
        }
        zos.close()
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
}