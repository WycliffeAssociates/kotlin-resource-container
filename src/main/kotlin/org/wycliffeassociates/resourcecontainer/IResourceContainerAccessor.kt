package org.wycliffeassociates.resourcecontainer

import java.io.Reader
import java.io.Writer

interface IResourceContainerAccessor {
    fun fileExists(filename: String): Boolean
    fun getReader(filename: String): Reader
    fun initWrite()
    fun write(filename: String, writeFunction: (Writer) -> Unit)
}