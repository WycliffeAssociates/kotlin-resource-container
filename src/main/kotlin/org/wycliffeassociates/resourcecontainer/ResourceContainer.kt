package org.wycliffeassociates.resourcecontainer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.Project
import org.wycliffeassociates.resourcecontainer.entity.TableOfContents
import org.wycliffeassociates.resourcecontainer.errors.RCException
import java.io.File
import java.io.IOException
import java.nio.file.Paths


interface Config {
    fun read(dir: File): Config
    fun write(dir: File)
}

class ResourceContainer private constructor(val dir: File) {

    var config: Config? = null

    var manifest: Manifest

    init {
        manifest = read()
    }

    private fun read(): Manifest {
        val manifestFile = File(dir, "manifest.yaml")
        if (manifestFile.exists()) {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModule(KotlinModule())
            return manifestFile.bufferedReader().use {
                mapper.readValue(it, Manifest::class.java)
            }
            config?.let {
                val configFile = File(dir, "config.yaml")
                if(configFile.exists()) {
                    this.config = it.read(configFile)
                }
            }
        } else {
            throw IOException()
        }
    }

    fun write() {
        writeManifest()
        for (p in manifest.projects) {
            writeTableOfContents(p)
        }
    }

    fun writeManifest() {
        writeManifest(File(dir, "manifest.yaml"))
    }

    private fun writeManifest(out: File) {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
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
            if(configFile.exists()) {
                it.write(configFile)
            }
        }
    }

    fun writeTableOfContents(project: Project) {
        writeTableOfContents(File(dir, project.path), project)
    }

    private fun writeTableOfContents(out: File, project: Project) {
        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())
        val manifestFile = out.bufferedWriter().use {
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



    companion object {

        const val conformsTo = "0.2"

        fun create(dir: File, init: ResourceContainer.() -> Unit): ResourceContainer {
            val rc = ResourceContainer(dir)
            rc.init()
            return rc
        }

        fun load(config: Config, dir: File, strict: Boolean = true): ResourceContainer {
            val rc = load(dir, strict)
            rc.config = config
            return rc
        }

        fun load(dir: File, strict: Boolean = true) = ResourceContainer(dir)

    }
}

data class Resource(
        val slug: String,
        val title: String,
        val type: String,
        val checkingLevel: String,
        val version: String
)
