package com.yslee.subwaywhen.feature.search.modal

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LineToKorailCodeMapperTest : FunSpec({

    test("경의중앙 → K4") {
        lineToKorailCode("경의중앙") shouldBe "K4"
    }

    test("수인분당 → K1") {
        lineToKorailCode("수인분당") shouldBe "K1"
    }

    test("경춘 → K2") {
        lineToKorailCode("경춘") shouldBe "K2"
    }

    test("우이 → UI") {
        lineToKorailCode("우이") shouldBe "UI"
    }

    test("신분당 → D1") {
        lineToKorailCode("신분당") shouldBe "D1"
    }

    test("공항 → A1") {
        lineToKorailCode("공항") shouldBe "A1"
    }

    test("1호선 → 빈 문자열") {
        lineToKorailCode("1호선") shouldBe ""
    }

    test("2호선 → 빈 문자열") {
        lineToKorailCode("2호선") shouldBe ""
    }

    test("알 수 없는 노선 → 빈 문자열") {
        lineToKorailCode("알수없음") shouldBe ""
    }
})
