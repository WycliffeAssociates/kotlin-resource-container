package org.wycliffeassociates.resourcecontainer.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Source(
        var identifier: String = "",
        var language: String = "",
        var version: String = ""
)

fun source(init: Source.() -> Unit): Source {
    val source = Source()
    source.init()
    return source
}