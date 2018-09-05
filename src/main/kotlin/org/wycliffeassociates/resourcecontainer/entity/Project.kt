package org.wycliffeassociates.resourcecontainer.entity

data class Project(
        var title: String = "",
        var versification: String = "",
        var identifier: String = "",
        var sort: Int = 0,
        var path: String = "",
        var categories: List<String> = arrayListOf()
)