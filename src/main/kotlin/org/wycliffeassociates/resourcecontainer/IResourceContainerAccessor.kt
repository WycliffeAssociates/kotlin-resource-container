package org.wycliffeassociates.resourcecontainer

import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.io.Writer

interface IResourceContainerAccessor: AutoCloseable {
    fun fileExists(filename: String): Boolean

    /**
     * Returns a list of file paths that are under the specified path (recursive) in the resource container.
     * If the path denotes a file, an empty list will be returned.
     *
     * @param path the path of the resource inside the container. It will include the recursive children of this path.
     */
    fun list(path: String): List<String>
    fun getInputStream(filename: String): InputStream
    fun getInputStreams(path: String, extension: String): Map<String, InputStream>
    /**
     * Get the input streams from container.
     * @param path the path of the resource inside the container. It will include the recursive children of this path.
     * @param extensions the list of filter extensions. Empty list will accept all extensions.
     */
    fun getInputStreams(
        path: String,
        extensions: List<String> = listOf()
    ): Map<String, InputStream>
    fun getReader(filename: String): Reader
    fun initWrite()
    fun write(filename: String, writeFunction: (OutputStream) -> Unit)
    fun write(files: Map<String, (OutputStream) -> Unit>)
    /** The RC's root relative to the root of the filesystem, or null if they are the same. */
    val root: String?
}