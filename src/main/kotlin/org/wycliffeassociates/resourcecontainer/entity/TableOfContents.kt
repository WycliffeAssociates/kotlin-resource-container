package org.wycliffeassociates.resourcecontainer.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


data class TableOfContents(
    val contents: Content
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Content(
    var title: String = "",
    @JsonProperty("sub-title")
    var subtitle: String = "",
    var link: String = "",
    val sections: List<Content> = arrayListOf()
)