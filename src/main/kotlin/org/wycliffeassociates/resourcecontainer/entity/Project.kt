package org.wycliffeassociates.resourcecontainer.entity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import org.wycliffeassociates.resourcecontainer.Config

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Project(
    var title: String = "",
    var versification: String = "",
    var identifier: String = "",
    var sort: Int = 0,
    var path: String = "",
    var categories: List<String> = arrayListOf(),
    var config: Config? = null
) {
    // Work around missing-non-nullable bug in Jackson: https://github.com/FasterXML/jackson-module-kotlin/issues/87
    // Use an intermediate creator that will accept nulls and only pass them on if non-null.
    @JsonCreator
    private constructor(
        title: String?,
        versification: String?,
        identifier: String?,
        sort: Int?,
        path: String?,
        categories: List<String>?,
        config: Config?
    ) : this() {
        title?.let { this.title = it }
        versification?.let { this.versification = it }
        identifier?.let { this.identifier = it }
        sort?.let { this.sort = it }
        path?.let { this.path = it }
        categories?.let { this.categories = it }
        config?.let { this.config = it }
    }
}

fun project(init: Project.() -> Unit): Project {
    val project = Project()
    project.init()
    return project
}
