package org.wycliffeassociates.resourcecontainer.entity

import com.fasterxml.jackson.annotation.JsonProperty


data class TableOfContents(
        val contents: Content
)

data class Content(
        val title: String = "",
        @JsonProperty("sub-title")
        val subtitle: String = "",
        val link: String = "",
        val sections: List<Content> = arrayListOf()
)