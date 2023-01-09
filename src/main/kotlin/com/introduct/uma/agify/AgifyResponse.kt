package com.introduct.uma.agify

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AgifyResponse(

    @JsonProperty("age")
    val age: Int?,

    @JsonProperty("count")
    val count: Int?,

    @JsonProperty("name")
    val name: String
)