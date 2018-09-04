package org.wycliffeassociates.resourcecontainer.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class DublinCore(
    var type: String = "",
    @JsonProperty("conformsto")
    var conformsTo: String = "",
    var format: String = "",
    var identifier: String = "",
    var title: String = "",
    var subject: String = "",
    var description: String = "",
    var language: List<Language> = arrayListOf(),
    var source: List<Source> = arrayListOf(),
    var rights: String = "",
    var creator: String = "",
    var contributor: List<String> = arrayListOf(),
    var relation: List<String> = arrayListOf(),
    var publisher: String = "",
    var issued: String = "",
    var modified: String = "",
    var version: String = "",
    var checking: Checking? = null
)

fun dublincore(init: DublinCore.() -> Unit): DublinCore {
    val dc = DublinCore()
    dc.init()
    return dc
}