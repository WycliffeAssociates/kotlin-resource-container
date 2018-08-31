package org.wycliffeassociates.resourcecontainer.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class DublinCore(
    var type: String,
    @JsonProperty("conformsto")
    var conformsTo: String,
    var format: String,
    var identifier: String,
    var title: String,
    var subject: String,
    var description: String,
    var language: List<Language>,
    var source: List<Source>,
    var rights: String,
    var creator: String,
    var contributor: List<String>,
    var relation: List<String>,
    var publisher: String,
    var issued: String,
    var modified: String,
    var version: String,
    var checking: Checking
)

data class Language(
        var direction: String = "",
        var identifier: String = "",
        var title: String = ""
)

data class Source(
        var identifier: String,
        var language: String,
        var version: String
)

data class Project(
        var title: String,
        var versification: String,
        var identifier: String,
        var sort: Int,
        var path: String,
        var categories: List<String>
)

data class Checking(
    @JsonProperty("checking_entity")
    var checkingEntity: List<String> = arrayListOf(),
    @JsonProperty("checking_level")
    var checkingLevel: String = ""
)

data class Manifest(
        val dublinCore: DublinCore,
        val projects: List<Project>,
        val checking: Checking
)