package com.yslee.subwaywhen.feature.search.modal

/**
 * iOS ModalModel.useLineTokorailCode() 포팅.
 * 입력값은 subwayLineDisplayName()이 반환하는 약어 기준.
 * 코레일 코드가 없는 노선은 "" 반환.
 */
fun lineToKorailCode(useLine: String): String = when (useLine) {
    "경의중앙" -> "K4"
    "수인분당" -> "K1"
    "경춘" -> "K2"
    "우이" -> "UI"
    "신분당" -> "D1"
    "공항" -> "A1"
    else -> ""
}
