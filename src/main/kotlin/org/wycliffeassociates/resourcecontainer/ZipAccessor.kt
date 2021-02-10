package org.wycliffeassociates.resourcecontainer

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

private val pathSeparators = "/\\".toCharArray()

class ZipAccessor(
    private val file: File,
    private val descendToInterestingFolder: Boolean = true
) : IResourceContainerAccessor {
    private var _zipFile: ZipFile? = null
    private var _root: String? = null

    override val root: String?
        get() {
            openZipFile()
            return _root
        }

    private fun openZipFile(): ZipFile {
        if (_zipFile == null) {
            _zipFile = ZipFile(file)

            _root = if (descendToInterestingFolder) {
                val singleTopLevelEntry = _zipFile!!.entries()
                    .asSequence()
                    .singleOrNull { it.isTopLevel() }
                val topLevelContainsOnlyADir = singleTopLevelEntry?.isDirectory ?: false
                if (topLevelContainsOnlyADir) {
                    singleTopLevelEntry!!.name.trimEnd(*pathSeparators)
                } else null
            } else null
        }

        return _zipFile as ZipFile
    }

    private fun closeZipFile() {
        _zipFile?.let(ZipFile::close)
        _zipFile = null
        _root = null
    }

    private fun ZipEntry.isTopLevel() = name.lastIndexOfAny(pathSeparators, startIndex = name.length - 2) < 0

    private fun String.toForwardSlashes() = this.replace("\\", "/")

    private fun String.toInternalFilepath(separator: Char = '/') =
        listOfNotNull(root, this).joinToString(separator.toString())

    private fun getEntry(filename: String): ZipEntry? {
        return pathSeparators
            .asSequence()
            .map { filename.toInternalFilepath(separator = it) }
            .map { openZipFile().getEntry(it) }
            .firstOrNull()
    }

    override fun getInputStream(filename: String): InputStream {
        return openZipFile().getInputStream(getEntry(filename))
    }

    override fun getInputStreams(path: String, extension: String): Map<String, InputStream> {
        val inputStreamMap: MutableMap<String, InputStream> = mutableMapOf()
        val normalizedPath = File(path).normalize().invariantSeparatorsPath
        val pathPrefix = if (root.isNullOrEmpty()) {
            normalizedPath
        } else {
            File(root).resolve(normalizedPath).invariantSeparatorsPath
        }

        openZipFile()
        _zipFile!!.entries().iterator().forEach { entry ->
            val fileEntry = File(entry.name)
            if (
                    entry.name.startsWith(pathPrefix) && fileEntry.extension == extension
            ) {
                val name = fileEntry.relativeTo(File(pathPrefix)).invariantSeparatorsPath
                inputStreamMap[name] = _zipFile!!.getInputStream(entry)
            }
        }

        return inputStreamMap
    }

    override fun getReader(filename: String): Reader {
        return openZipFile()
            .getInputStream(getEntry(filename))
            .bufferedReader()
    }

    override fun fileExists(filename: String) = getEntry(filename) != null

    override fun initWrite() {
        // noop
    }

    /** Thread safety warning: This is NOT thread safe, and additionally, write() will close any open Readers. */
    override fun write(filename: String, writeFunction: (OutputStream) -> Unit) {
        val doCopy = file.exists()
        val dest = when (doCopy) {
            true -> File.createTempFile("otter", ".zip", file.parentFile)
            false -> file
        }
        ZipOutputStream(FileOutputStream(dest)).use { zos ->
            val internalFilename = when {
                doCopy -> filename.toInternalFilepath().toForwardSlashes()
                else -> filename
            }
            var found = false
            if (doCopy) {
                openZipFile().entries().iterator().forEach {
                    if (it.name.toForwardSlashes() == internalFilename) {
                        zos.putNextEntry(ZipEntry(it.name))
                        writeFunction(zos)
                        found = true
                    } else {
                        // Simply doing zos.putNextEntry(it) resulted in ZipExceptions - invalid entry
                        // compressed size
                        val destEntry = ZipEntry(it.name)
                        zos.putNextEntry(destEntry)
                        openZipFile().getInputStream(destEntry).use { inStream -> inStream.copyTo(zos) }
                    }
                }
            }
            if (!found) {
                zos.putNextEntry(ZipEntry(internalFilename))
                writeFunction(zos)
            }
        }
        if (doCopy) {
            closeZipFile()
            file.delete()
            dest.renameTo(file)
        }
    }

    /**
     * @param files a map of the file path (with respect to the root of the RC) and a lambda to write
     * the file to an output stream.
     *
     * If the rc has already been written and thus its zip already exists, the write will happen in
     * a temporary file and then replace the original upon completing the write.
     *
     * First, all files in the map will be written. If there was an existing zip, then those zip entries
     * will be copied, unless they are a file that was contained in the map and are thus outdated.
     *
     * ** Thread safety warning: This is NOT thread safe, and additionally, write() will close any open Readers. **
     */
    override fun write(files: Map<String, (OutputStream) -> Unit>) {
        val doCopy = file.exists()
        val dest = when (doCopy) {
            true -> File.createTempFile("otter", ".zip", file.parentFile)
            false -> file
        }
        ZipOutputStream(FileOutputStream(dest)).use { zos ->
            val internalFilenames = arrayListOf<String>()
            files.forEach { (filename, writeFunction) ->
                val internalFilename = when {
                    doCopy -> filename.toInternalFilepath().toForwardSlashes()
                    else -> filename
                }
                internalFilenames.add(internalFilename)
                zos.putNextEntry(ZipEntry(internalFilename))
                writeFunction(zos)
            }
            if (doCopy) {
                openZipFile().entries().iterator().forEach {
                    if (!internalFilenames.contains(it.name.toForwardSlashes())) {
                        val destEntry = ZipEntry(it.name)
                        zos.putNextEntry(destEntry)
                        openZipFile().getInputStream(destEntry).use { inStream -> inStream.copyTo(zos) }
                    }
                }
            }
        }
        if (doCopy) {
            closeZipFile()
            file.delete()
            dest.renameTo(file)
        }
    }

    override fun close() {
        closeZipFile()
    }
}
