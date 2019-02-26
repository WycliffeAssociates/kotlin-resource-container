package org.wycliffeassociates.resourcecontainer

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.Project
import org.wycliffeassociates.resourcecontainer.entity.TableOfContents
import org.wycliffeassociates.resourcecontainer.errors.InvalidRCException
import org.wycliffeassociates.resourcecontainer.errors.OutdatedRCException
import org.wycliffeassociates.resourcecontainer.errors.RCException
import org.wycliffeassociates.resourcecontainer.errors.UnsupportedRCException
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

const val MANIFEST_FILENAME = "manifest.yaml"
const val CONFIG_FILENAME = "config.yaml"
const val TOC_FILENAME = "toc.yaml"

interface Config {
    fun read(br: BufferedReader): Config
    fun write(bw: BufferedWriter)
}

class ResourceContainer private constructor(val file: File, var config: Config? = null) {

    lateinit var manifest: Manifest
    private val accessor: IResourceContainerAccessor = when (file.extension) { // TODO: Check
        "zip" -> ZipAccessor(file)
        else -> DirAccessor(file)
    }

    private fun read(): Manifest {
        if (accessor.checkFileExists(MANIFEST_FILENAME)) {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModule(KotlinModule())
            manifest = accessor.getReader(MANIFEST_FILENAME).use {
                mapper.readValue(it, Manifest::class.java)
            }
            config?.let {
                if (accessor.checkFileExists(CONFIG_FILENAME)) {
                    this.config = it.read(accessor.getReader(CONFIG_FILENAME))
                }
            }
            return manifest
        } else {
            throw IOException("Missing manifest.yaml")
        }
    }

    fun write() {
        writeManifest()
        for (p in manifest.projects) {
            if(!p.path.isNullOrEmpty()) {
                //writeTableOfContents(p)
            }
        }
    }

    fun writeManifest() {
        accessor.initWrite()
        accessor.write(MANIFEST_FILENAME) { writeManifest(it) }
    }

    private fun writeManifest(bw: BufferedWriter) {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        mapper.setSerializationInclusion(Include.NON_NULL)
        bw.use {
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
        config?.let { config ->
            if (accessor.checkFileExists(CONFIG_FILENAME)) {
                accessor.write(CONFIG_FILENAME) { config.write(it) }
            }
        }
    }

    fun writeTableOfContents(project: Project) {
        accessor.initWrite()
        accessor.write(project.path) { writeTableOfContents(it, project) }
        // TODO: If file/project.path doesn't exist, it won't be created in the zip implementation
    }

    private fun writeTableOfContents(bw: BufferedWriter, project: Project) {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        mapper.setSerializationInclusion(Include.NON_NULL)
        mapper.setSerializationInclusion(Include.NON_EMPTY)
        bw.use {
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

    fun conformsTo(): String {
        manifest?.let {
            var version = it.dublinCore.conformsTo
            return version.replace(Regex("^rc"), "")
        }
        throw RCException("Resource container lacks required conformsTo field")
    }

    fun toc(): TableOfContents? {
        return toc(null)
    }

    // TODO
    fun toc(identifier: String?): TableOfContents? {
        val pj = project(identifier)
        pj?.let {
            val contentDirname = Paths.get(pj.path).fileName.toString()
            if (accessor.checkFileExists(TOC_FILENAME, contentDirname)) {
                val mapper = ObjectMapper(YAMLFactory())
                mapper.registerModule(KotlinModule())
                return accessor.getReader(TOC_FILENAME, contentDirname).use {
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

        val contentPath = File(file, p.path)
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

        val contentDir = File(file, p.path)
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

        val contentDir = File(file, p.path)
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

        val contentDir = File(file, p.path)
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

    /**
     * Convenience method to get the type of the resource container.
     *
     * @return the RC type
     */
    inline fun type(): String = this.manifest.dublinCore.type


    companion object {

        const val conformsTo = "0.2"

        fun create(file: File, init: ResourceContainer.() -> Unit): ResourceContainer {
            val rc = ResourceContainer(file)
            rc.init()
            if(rc.conformsTo().isNullOrEmpty()) {
                rc.manifest.dublinCore.conformsTo = conformsTo
            }
            return rc
        }

        fun load( dir: File, config: Config, strict: Boolean = true): ResourceContainer =
            load(dir, strict, config)

        fun load(dir: File, strict: Boolean = true, config: Config? = null): ResourceContainer {
            val rc = ResourceContainer(dir, config)
            rc.read()

            if(strict) {
                if(rc.manifest == null) {
                    throw InvalidRCException("Missing manifest.yaml")
                }
                if(Semver.gt(rc.conformsTo(), conformsTo)) {
                    throw UnsupportedRCException("Found " + rc.conformsTo() + " but expected " + conformsTo)
                }
                if(Semver.lt(rc.conformsTo(), conformsTo)) {
                    throw OutdatedRCException("Found " + rc.conformsTo() + " but expected " + conformsTo)
                }
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
