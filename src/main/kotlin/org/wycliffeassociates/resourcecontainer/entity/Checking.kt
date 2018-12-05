package org.wycliffeassociates.resourcecontainer.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Checking(
    @JsonProperty("checking_entity")
    var checkingEntity: List<String> = arrayListOf(),
    @JsonProperty("checking_level")
    var checkingLevel: String = ""
)

fun checking(init: Checking.() -> Unit): Checking {
    val checking = Checking()
    checking.init()
    return checking
}