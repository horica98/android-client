package com.ocaterinca.ocaterinca.core.model.api

import com.squareup.moshi.Json

data class AvatarUploadBody(
    @Json(name = "token")
    val token: String,

    @Json(name = "image")
    val image: String
)