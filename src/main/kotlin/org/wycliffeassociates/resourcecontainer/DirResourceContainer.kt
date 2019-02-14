package org.wycliffeassociates.resourcecontainer

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.Project
import org.wycliffeassociates.resourcecontainer.entity.TableOfContents
import org.wycliffeassociates.resourcecontainer.errors.RCException
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


class DirResourceContainer(filepath: File, config: Config? = null)
    : ResourceContainer(filepath, config) {

    val dir = filepath

    override fun read(): Manifest {
        val manifestFile = File(dir, "manifest.yaml")
        if (manifestFile.exists()) {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModule(KotlinModule())
            manifest = manifestFile.bufferedReader().use {
                mapper.readValue(it, Manifest::class.java)
            }
            config?.let {
                val configFile = File(dir, "config.yaml")
                if (configFile.exists()) {
                    this.config = it.read(configFile)
                }
            }
            return manifest
        } else {
            throw IOException("Missing manifest.yaml")
        }
    }

    override fun write() {
        dir.mkdirs()
        writeManifest()
        for (p in manifest.projects) {
            if (!p.path.isNullOrEmpty()) {
                //writeTableOfContents(p)
            }
        }
    }

    override fun writeManifest() {
        dir.mkdirs()
        writeManifest(File(dir, "manifest.yaml"))
    }

    private fun writeManifest(out: File) {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        mapper.setSerializationInclusion(Include.NON_NULL)
        val manifestFile = out.bufferedWriter().use {
            mapper.writeValue(it, manifest)
        }
    }

    fun writeTableOfContents(projectId: String) {
        val p = project(projectId)
        p?.let {
            writeTableOfContents(p)
        }
    }

    fun writeConfig() {
        config?.let {
            val configFile = File(dir, "config.yaml")
            if (configFile.exists()) {
                it.write(configFile)
            }
        }
    }

    fun writeTableOfContents(project: Project) {
        dir.mkdirs()
        writeTableOfContents(File(dir, project.path), project)
    }

    private fun writeTableOfContents(out: File, project: Project) {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        mapper.setSerializationInclusion(Include.NON_NULL)
        mapper.setSerializationInclusion(Include.NON_EMPTY)
        out.bufferedWriter().use {
            mapper.writeValue(it, project)
        }
    }

    fun resource(): Resource {
        manifest?.let {
            return Resource(
                    manifest.dublinCore.identifier,
                    manifest.dublinCore.title,
                    manifest.dublinCore.type,
                    manifest.checking.checkingLevel,
                    manifest.dublinCore.version
            )
        }
        throw Exception("Manifest is null")
    }

    fun project(): Project? {
        return project(null)
    }

    fun project(identifier: String?): Project? {
        manifest?.let {
            if (it.projects.isEmpty()) {
                return null
            }

            if (!identifier.isNullOrEmpty()) {
                for (p in it.projects) {
                    if (p.identifier == identifier) {
                        return p
                    }
                }
            } else if (it.projects.size == 1) {
                return it.projects[0]
            } else {
                throw RCException("Multiple projects found. Specify the project identifier.")
            }
        }

        return null
    }

    fun projectIds(): List<String> {
        val list = arrayListOf<String>()
        manifest?.let {
            for (p in it.projects) {
                list.add(p.identifier)
            }
        }
        return list
    }

    fun projectCount(): Int {
        return manifest?.projects?.size ?: 0
    }

    fun toc(): TableOfContents? {
        return toc(null)
    }

    fun toc(identifier: String?): TableOfContents? {
        val pj = project(identifier)
        pj?.let {
            val contentDir = File(dir, Paths.get(pj.path).fileName.toString())
            val tocFile = File(contentDir, "toc.yaml")
            if (tocFile.exists()) {
                val mapper = ObjectMapper(YAMLFactory())
                mapper.registerModule(KotlinModule())
                return tocFile.bufferedReader().use {
                    mapper.readValue(it, TableOfContents::class.java)
                }
            }
        }
        return null
    }


    /**
     * Returns an un-ordered list of chapter slugs in this resource container
     *
     * @return an array of chapter identifiers
     */
    @Throws(Exception::class)
    fun chapters(): Array<String> {
        return chapters(null)
    }

    /**
     * Returns an un-ordered list of chapter slugs in this resource container
     *
     * @param projectIdentifier the project who's chapters will be returned
     * @return an array of chapter identifiers
     */
    @Throws(Exception::class)
    fun chapters(projectIdentifier: String?): Array<String> {
        val p = project(projectIdentifier) ?: return arrayOf()

        val contentPath = File(dir, p.path)
        var chapters = contentPath.list { dir, filename -> File(dir, filename).isDirectory }
        if (chapters == null) chapters = arrayOfNulls<String>(0)
        return chapters
    }

    /**
     * Returns an un-ordered list of chunk slugs in the chapter
     *
     * @param chapterSlug the chapter who's chunks will be returned
     * @return an array of chunk identifiers
     */
    @Throws(Exception::class)
    fun chunks(chapterSlug: String): Array<String> {
        return chunks(null, chapterSlug)
    }

    /**
     * Returns an un-ordered list of chunk slugs in the chapter
     *
     * @param projectIdentifier the project who's chunks will be returned
     * @param chapterSlug the chapter who's chunks will be returned
     * @return an array of chunk identifiers
     */
    @Throws(Exception::class)
    fun chunks(projectIdentifier: String?, chapterSlug: String): Array<String> {
        val p = project(projectIdentifier) ?: return arrayOf()

        val contentDir = File(dir, p.path)
        val chapterDir = File(contentDir, chapterSlug)
        val chunks = ArrayList<String>()
        chapterDir.list { _, filename ->
            chunks.add(filename.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
            false
        }
        return chunks.toTypedArray()
    }


    /**
     * Returns the contents of a chunk.
     *
     * @param chapterSlug the chapter who's chunk will be read
     * @param chunkSlug the contents of the chunk or an empty string if it does not exist
     * @return the chunk contents
     */
    @Throws(Exception::class)
    fun readChunk(chapterSlug: String, chunkSlug: String): String {
        return readChunk(null, chapterSlug, chunkSlug)
    }

    /**
     * Returns the contents of a chunk.
     *
     * @param projectIdentifier the project who's chunk will be read
     * @param chapterSlug the chapter who's chunk will be read
     * @param chunkSlug the contents of the chunk or an empty string if it does not exist
     * @return the chunk contents
     */
    @Throws(Exception::class)
    fun readChunk(projectIdentifier: String?, chapterSlug: String, chunkSlug: String): String {
        val p = project(projectIdentifier) ?: return ""

        val contentDir = File(dir, p.path)
        val chunkFile = File(File(contentDir, chapterSlug), chunkSlug + "." + chunkExt())
        return if (chunkFile.exists() && chunkFile.isFile()) {
            chunkFile.readText()
        } else ""
    }

    /**
     * Writes content to a chunk.
     * The path will be created if it does not already exist.
     *
     * @param chapterIdentifier the chapter who's chunk will be written to
     * @param chunkIdentifier the chunk that will be created
     * @param content the content to be written to the chunk
     * @throws Exception
     */
    @Throws(Exception::class)
    fun writeChunk(chapterIdentifier: String, chunkIdentifier: String, content: String) {
        writeChunk(null, chapterIdentifier, chunkIdentifier, content)
    }

    /**
     * Writes content to a chunk.
     * The path will be created if it does not already exist.
     *
     * @param projectIdentifier the project who's chunk will be written to
     * @param chapterIdentifier the chapter who's chunk will be written to
     * @param chunkIdentifier the chunk that will be created
     * @param content the content to be written to the chunk
     * @throws Exception
     */
    @Throws(Exception::class)
    fun writeChunk(projectIdentifier: String?, chapterIdentifier: String, chunkIdentifier: String, content: String) {
        val p = project(projectIdentifier) ?: return

        val contentDir = File(dir, p.path)
        val chunkFile = File(File(contentDir, chapterIdentifier), chunkIdentifier + "." + chunkExt())
        if (content.isEmpty()) {
            Files.deleteIfExists(chunkFile.toPath())
        } else {
            chunkFile.parentFile.mkdirs()
            chunkFile.writeText(content)
        }
    }

    /**
     * Returns the file extension to use for content files (chunks)
     * @return the extension name
     */
    private fun chunkExt(): String {
        // TODO: 1/26/17 I'd rather not hard code the file extensions in here.
        // it would be better if the library can just figure it out.
        val defaultExt = "txt"
        return when (manifest.dublinCore.format) {
            "text/usx" -> "usx"
            "text/usfm" -> "usfm"
            "text/markdown" -> "md"
            "audio/mp3" -> "mp3"
            "video/mp4" -> "mp4"
            else ->
                // unknown format
                defaultExt
        }
    }

    companion object {

        fun create(dir: File, init: DirResourceContainer.() -> Unit): DirResourceContainer {
            val rc = DirResourceContainer(dir)
            rc.init()
            if (rc.conformsTo().isNullOrEmpty()) {
                rc.manifest.dublinCore.conformsTo = conformsTo
            }
            return rc
        }
    }
}

data class Resource(
        val slug: String,
        val title: String,
        val type: String,
        val checkingLevel: String,
        val version: String
)
