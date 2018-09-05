package org.wycliffeassociates.resourcecontainer.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class TableOfContents(
    var title: String = "",
    @JsonProperty("sub-title")
    var subtitle: String = "",
    var link: String = "",
    val sections: List<TableOfContents> = arrayListOf()
)