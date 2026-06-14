package com.yslee.subwaywhen.feature.home.modal

data class HourlyCongestion(
    val hour: Int,
    val percent: Int,
    val level: Int,
)
