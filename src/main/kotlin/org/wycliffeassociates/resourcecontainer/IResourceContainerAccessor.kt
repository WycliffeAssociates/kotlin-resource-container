package org.wycliffeassociates.resourcecontainer

import java.io.BufferedReader
import java.io.BufferedWriter

interface IResourceContainerAccessor {
    fun fileExists(filename: String): Boolean
    fun getReader(filename: String): BufferedReader
    fun initWrite()
    fun write(filename: String, writeFunction: (BufferedWriter) -> Unit)
}