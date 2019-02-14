package org.wycliffeassociates.resourcecontainer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile

class ZipResourceContainer(filepath: File, config: Config? = null)
    : ResourceContainer(filepath, config) {
    val zip = ZipFile(filepath)

    override fun read(): Manifest {
        val manifestEntry = zip.getEntry("manifest.yaml")
                ?: throw IOException("Missing manifest.yaml")

        val mapper = ObjectMapper(YAMLFactory())
        mapper.registerModule(KotlinModule())

        manifest = zip.getInputStream(manifestEntry).use {
            mapper.readValue(it, Manifest::class.java)
        }

        config?.let {
            zip.getEntry("config.yaml")?.let { configEntry ->
                it.readFromZip(zip, configEntry)
            }
        }
        return manifest
    }

    override fun write() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun writeManifest() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
