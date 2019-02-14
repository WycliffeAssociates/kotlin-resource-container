package org.wycliffeassociates.resourcecontainer

import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.errors.InvalidRCException
import org.wycliffeassociates.resourcecontainer.errors.OutdatedRCException
import org.wycliffeassociates.resourcecontainer.errors.RCException
import org.wycliffeassociates.resourcecontainer.errors.UnsupportedRCException
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

interface Config {
    fun read(configFile: File): Config
    fun readFromZip(zip: ZipFile, zipEntry: ZipEntry): Config
    fun write(configFile: File)
}

abstract class ResourceContainer(val file: File, open var config: Config? = null) {
    lateinit var manifest: Manifest

    protected abstract fun read(): Manifest
    abstract fun write()
    abstract fun writeManifest()

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

        fun load(dir: File, config: Config, strict: Boolean = true): ResourceContainer =
                load(dir, strict, config)

        fun load(file: File, strict: Boolean = true, config: Config? = null): ResourceContainer {
            val rc: ResourceContainer =
                    when {
                        file.isDirectory -> DirResourceContainer(file, config)
                        file.extension == "zip" -> ZipResourceContainer(file, config)
                        else -> throw InvalidRCException("Invalid container directory or zip file")
                    }
                rc.read()

                if (strict) {
                    if (rc.manifest == null) {
                        throw InvalidRCException("Missing manifest.yaml")
                    }
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
}

