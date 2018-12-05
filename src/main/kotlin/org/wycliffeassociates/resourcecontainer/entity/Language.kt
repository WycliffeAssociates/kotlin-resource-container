package org.wycliffeassociates.resourcecontainer.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Language(
        var direction: String = "",
        var identifier: String = "",
        var title: String = ""
)

fun language(init: Language.() -> Unit): Language {
    val lang = Language()
    lang.init()
    return lang
}