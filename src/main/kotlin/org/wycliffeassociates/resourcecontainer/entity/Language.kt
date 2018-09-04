package org.wycliffeassociates.resourcecontainer.entity

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