package com.yslee.subwaywhen.ui.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SubwayLineMapperTest : FunSpec({

    context("subwayLineUpDownText - 2호선") {
        test("isUp=true → 내선") {
            subwayLineUpDownText("02호선", isUp = true) shouldBe "내선"
        }
        test("isUp=false → 외선") {
            subwayLineUpDownText("02호선", isUp = false) shouldBe "외선"
        }
    }

    context("subwayLineUpDownText - 1호선") {
        test("isUp=true → 상행") {
            subwayLineUpDownText("01호선", isUp = true) shouldBe "상행"
        }
        test("isUp=false → 하행") {
            subwayLineUpDownText("01호선", isUp = false) shouldBe "하행"
        }
    }

    context("subwayLineUpDownText - 그 외 노선") {
        test("수인분당선 isUp=true → 상행") {
            subwayLineUpDownText("수인분당선", isUp = true) shouldBe "상행"
        }
        test("신분당선 isUp=false → 하행") {
            subwayLineUpDownText("신분당선", isUp = false) shouldBe "하행"
        }
    }
})
