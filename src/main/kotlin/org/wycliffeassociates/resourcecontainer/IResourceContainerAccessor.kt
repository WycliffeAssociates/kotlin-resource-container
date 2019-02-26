package org.wycliffeassociates.resourcecontainer

import java.io.BufferedReader
import java.io.BufferedWriter

interface IResourceContainerAccessor {
    fun checkFileExists(filename: String, dirname: String? = null): Boolean
    fun getReader(filename: String, dirname: String? = null): BufferedReader
    fun initWrite()
    fun write(filename: String, dirname: String? = null, writeFcn: (BufferedWriter) -> Unit)
}