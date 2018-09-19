package org.wycliffeassociates.resourcecontainer.entity

import com.fasterxml.jackson.annotation.JsonInclude
import org.wycliffeassociates.resourcecontainer.Config

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Project(
        var title: String = "",
        var versification: String = "",
        var identifier: String = "",
        var sort: Int = 0,
        var path: String = "",
        var categories: List<String> = arrayListOf(),
        var config: Config? = null
)

fun project(init: Project.() -> Unit): Project {
    val project = Project()
    project.init()
    return project
}