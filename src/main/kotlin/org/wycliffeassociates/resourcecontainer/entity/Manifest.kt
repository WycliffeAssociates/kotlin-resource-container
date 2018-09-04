package org.wycliffeassociates.resourcecontainer.entity

data class Manifest(
        val dublinCore: DublinCore,
        val projects: List<Project>,
        val checking: Checking
)