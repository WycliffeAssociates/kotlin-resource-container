package org.wycliffeassociates.resourcecontainer.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Checking(
        @JsonProperty("checking_entity")
        var checkingEntity: List<String> = arrayListOf(),
        @JsonProperty("checking_level")
        var checkingLevel: String = ""
)