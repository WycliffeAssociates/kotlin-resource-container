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

    override fun getReader(filename: String): BufferedReader {
            return zipFile.getInputStream(zipFile.getEntry(filename)).bufferedReader()
    }

    override fun checkFileExists(filename: String): Boolean {
            return zipFile.entries().toList().map{it.name}.contains(filename)
    }

    override fun initWrite() {
        // noop
    }

    override fun write(filename: String, writeFcn: (BufferedWriter) -> Unit) {
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