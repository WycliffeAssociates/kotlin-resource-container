package org.wycliffeassociates.resourcecontainer

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.Project
import org.wycliffeassociates.resourcecontainer.errors.InvalidRCException
import org.wycliffeassociates.resourcecontainer.errors.OutdatedRCException
import org.wycliffeassociates.resourcecontainer.errors.RCException
import org.wycliffeassociates.resourcecontainer.errors.UnsupportedRCException
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.IOException

const val MANIFEST_FILENAME = "manifest.yaml"
const val CONFIG_FILENAME = "config.yaml"

interface Config {
    fun read(br: BufferedReader): Config
    fun write(bw: BufferedWriter)
}

class ResourceContainer private constructor(val file: File, var config: Config? = null) {

    lateinit var manifest: Manifest
    private val accessor: IResourceContainerAccessor = when (file.extension) {
        "zip" -> ZipAccessor(file)
        else -> DirectoryAccessor(file)
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

    fun writeConfig() {
        config?.let { config ->
            if (accessor.checkFileExists(CONFIG_FILENAME)) {
                accessor.write(CONFIG_FILENAME) { config.write(it) }
            }
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
