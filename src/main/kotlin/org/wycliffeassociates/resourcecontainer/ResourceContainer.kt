package org.wycliffeassociates.resourcecontainer

import com.esotericsoftware.yamlbeans.YamlReader
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.wycliffeassociates.resourcecontainer.entity.*
import org.wycliffeassociates.resourcecontainer.errors.InvalidRCException
import org.wycliffeassociates.resourcecontainer.errors.RCException
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

class ResourceContainer private constructor(val dir: File) {

    val manifest: Manifest

    init {
        val manifestFile = File(dir, "manifest.yaml")
        if (manifestFile.exists()) {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModule(KotlinModule())
            manifest = manifestFile.bufferedReader().use {
                mapper.readValue(it, Manifest::class.java)
            }
        } else {
            throw IOException()
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

    companion object Factory {

        const val conformsTo = "0.2"

        fun load(dir: File, strict: Boolean = true) = ResourceContainer(dir)

//        fun create(dir: File, manifest: Map<String, Any>): ResourceContainer {
//            if (dir.exists()) throw RCException("Resource container already exists")
//
//            val dc = dublincore {
//                conformsTo = "rc$conformsTo"
//                checking = Checking()
//            }
////
//            if (manifest.containsKey("dublin_core")) {
//                val mdc = manifest.get("dublin_code") as Map<String, Any>
//                val requiredKeys = arrayOf("type", "format", "identifier", "language", "rights")
//                for (key in requiredKeys) {
//                    if (!mdc.containsKey(key) || mdc[key] == null) {
//                        throw InvalidRCException("Missing dublin_core.$key")
//                    }
//                }
//            } else {
//                throw InvalidRCException("Missing dublin_core")
//            }
//
//
//        }

    }
}

data class Resource(
        val slug: String,
        val title: String,
        val type: String,
        val checkingLevel: String,
        val version: String
)