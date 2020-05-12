package org.wycliffeassociates.resourcecontainer

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.MediaManifest
import org.wycliffeassociates.resourcecontainer.entity.Project
import org.wycliffeassociates.resourcecontainer.errors.OutdatedRCException
import org.wycliffeassociates.resourcecontainer.errors.RCException
import org.wycliffeassociates.resourcecontainer.errors.UnsupportedRCException
import java.io.File
import java.io.IOException
import java.io.Reader
import java.io.Writer

const val MEDIA_FILENAME = "media.yaml"
const val MANIFEST_FILENAME = "manifest.yaml"
const val CONFIG_FILENAME = "config.yaml"

interface Config {
    fun read(reader: Reader): Config
    fun write(writer: Writer)
}

class ResourceContainer private constructor(val file: File, var config: Config? = null) : AutoCloseable {

    lateinit var manifest: Manifest
    var media: MediaManifest? = null

    val accessor: IResourceContainerAccessor = when (file.extension) {
        "zip" -> ZipAccessor(file)
        else -> DirectoryAccessor(file)
    }

    private fun read(): Manifest {
        if (accessor.fileExists(MANIFEST_FILENAME)) {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModule(KotlinModule())
            manifest = accessor.getReader(MANIFEST_FILENAME).use {
                mapper.readValue(it, Manifest::class.java)
            }
            config?.let {
                if (accessor.fileExists(CONFIG_FILENAME)) {
                    this.config = it.read(accessor.getReader(CONFIG_FILENAME))
                }
            }
            if (accessor.fileExists(MEDIA_FILENAME)) {
                this.media = accessor.getReader(MEDIA_FILENAME).use {
                    mapper.readValue(it, MediaManifest::class.java)
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
            if (p.path.isNotEmpty()) {
                //writeTableOfContents(p)
            }
        }
        media?.let {
            writeMedia()
        }
    }

    fun writeManifest() {
        accessor.initWrite()
        accessor.write(MANIFEST_FILENAME) { writeManifest(it) }
    }

    private fun writeManifest(writer: Writer) {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        mapper.setSerializationInclusion(Include.NON_NULL)
        writer.use {
            mapper.writeValue(it, manifest)
        }
    }

    fun writeMedia() {
        accessor.initWrite()
        accessor.write(MEDIA_FILENAME) { writeMedia(it) }
    }
    
    private fun writeMedia(writer: Writer) {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        mapper.setSerializationInclusion(Include.NON_NULL)
        writer.use {
            mapper.writeValue(it, media)
        }
    }

    fun writeConfig() {
        config?.let { config ->
            if (accessor.fileExists(CONFIG_FILENAME)) {
                accessor.write(CONFIG_FILENAME) { config.write(it) }
            }
        }
    }

    fun resource() = Resource(
        manifest.dublinCore.identifier,
        manifest.dublinCore.title,
        manifest.dublinCore.type,
        manifest.checking.checkingLevel,
        manifest.dublinCore.version
    )

    fun project(identifier: String? = null): Project? {
        if (manifest.projects.isEmpty()) {
            return null
        }

        if (!identifier.isNullOrEmpty()) {
            for (p in manifest.projects) {
                if (p.identifier == identifier) {
                    return p
                }
            }
        } else if (manifest.projects.size == 1) {
            return manifest.projects[0]
        } else {
            throw RCException("Multiple projects found. Specify the project identifier.")
        }

        return null
    }

    fun projectIds(): List<String> = manifest.projects.map(Project::identifier)

    fun projectCount(): Int = manifest.projects.size

    fun conformsTo(): String = manifest.dublinCore.conformsTo.replace(Regex("^rc"), "")

    /**
     * Convenience method to get the type of the resource container.
     *
     * @return the RC type
     */
    fun type(): String = this.manifest.dublinCore.type

    companion object {

        const val conformsTo = "0.2"

        fun create(file: File, init: ResourceContainer.() -> Unit): ResourceContainer {
            val rc = ResourceContainer(file)
            rc.init()
            if (rc.conformsTo().isEmpty()) {
                rc.manifest.dublinCore.conformsTo = conformsTo
            }
            return rc
        }

        fun load(dir: File, config: Config, strict: Boolean = true): ResourceContainer =
            load(dir, strict, config)

        fun load(dir: File, strict: Boolean = true, config: Config? = null): ResourceContainer {
            val rc = ResourceContainer(dir, config)
            rc.read()

            if (strict) {
                if (Semver.gt(rc.conformsTo(), conformsTo)) {
                    throw UnsupportedRCException("Found " + rc.conformsTo() + " but expected " + conformsTo)
                }
                if (Semver.lt(rc.conformsTo(), conformsTo)) {
                    throw OutdatedRCException("Found " + rc.conformsTo() + " but expected " + conformsTo)
                }
            }

            return rc
        }
    }

    override fun close() {
        accessor.close()
    }
}

data class Resource(
    val slug: String,
    val title: String,
    val type: String,
    val checkingLevel: String,
    val version: String
)
