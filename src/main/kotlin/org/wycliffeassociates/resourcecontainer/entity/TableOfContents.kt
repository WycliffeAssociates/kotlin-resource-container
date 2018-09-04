package org.wycliffeassociates.resourcecontainer.entity


data class TableOfContents(
        val contents: List<Content> = arrayListOf()
)

data class Content(
        val title: String,
        val subtitle: String,
        val link: String,
        val sections: List<Content>
)