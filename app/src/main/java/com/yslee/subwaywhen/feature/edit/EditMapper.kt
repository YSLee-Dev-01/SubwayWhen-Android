package com.yslee.subwaywhen.feature.edit

import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.model.SaveStationGroup

fun List<SaveStation>.splitByGroup(): Pair<List<SaveStation>, List<SaveStation>> {
    val groupOne = filter { it.group == SaveStationGroup.ONE }
    val groupTwo = filter { it.group == SaveStationGroup.TWO }
    return Pair(groupOne, groupTwo)
}

fun Pair<List<SaveStation>, List<SaveStation>>.mergeGroups(): List<SaveStation> {
    return first + second
}
