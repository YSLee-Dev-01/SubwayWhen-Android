package com.yslee.subwaywhen.data.remote.dto.vicinityStation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VicinityStationsData(
    val documents: List<VicinityDocumentData>
)

@Serializable
data class VicinityDocumentData(
    @SerialName("place_name") val name: String,
    @SerialName("distance") val distance: String,
    @SerialName("category_group_code") val category: String
)
