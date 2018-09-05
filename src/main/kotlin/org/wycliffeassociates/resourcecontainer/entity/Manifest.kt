package org.wycliffeassociates.resourcecontainer.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Manifest(
        @JsonProperty("dublin_core")
        val dublinCore: DublinCore,
        val projects: List<Project>,
        val checking: Checking
)