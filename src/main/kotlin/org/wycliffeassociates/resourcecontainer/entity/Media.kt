package org.wycliffeassociates.resourcecontainer.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class MediaManifest(
    var resource: Resource? = null,
    var projects: List<MediaProject> = listOf()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Resource(
    var version: String = "",
    var media: List<Media> = listOf()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MediaProject(
    var identifier: String = "",
    var version: String = "",
    var media: List<Media> = listOf()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Media(
    var identifier: String = "",
    var version: String = "",
    //var contributor: Array<String> = arrayOf(),
    var url: String = "",
    var quality: List<String> = listOf(),
    var chapter_url: String= ""
)

fun mediamanifest(init: MediaManifest.() -> Unit): MediaManifest {
    val manifest = MediaManifest()
    manifest.init()
    return manifest
}

fun resource(init: Resource.() -> Unit): Resource {
    val resource = Resource()
    resource.init()
    return resource
}

fun mediaproject(init: MediaProject.() -> Unit): MediaProject {
    val project = MediaProject()
    project.init()
    return project
}

fun media(init: Media.() -> Unit): Media {
    val media = Media()
    media.init()
    return media
}