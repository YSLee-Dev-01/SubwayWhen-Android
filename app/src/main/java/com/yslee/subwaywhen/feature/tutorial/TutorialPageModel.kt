package com.yslee.subwaywhen.feature.tutorial

import com.yslee.subwaywhen.R

sealed class TutorialPageType {
    object First : TutorialPageType()
    data class Middle(val imageRes: Int, val titleRes: Int) : TutorialPageType()
    object Last : TutorialPageType()
}

data class TutorialPage(
    val type: TutorialPageType,
    val buttonLabelRes: Int,
)

val tutorialPages: List<TutorialPage> = listOf(
    TutorialPage(
        type = TutorialPageType.First,
        buttonLabelRes = R.string.tutorial_page_0_button,
    ),
    TutorialPage(
        type = TutorialPageType.Middle(
            imageRes = R.drawable.tutorial_one,
            titleRes = R.string.tutorial_page_1_title,
        ),
        buttonLabelRes = R.string.tutorial_page_1_button,
    ),
    TutorialPage(
        type = TutorialPageType.Middle(
            imageRes = R.drawable.tutorial_two,
            titleRes = R.string.tutorial_page_2_title,
        ),
        buttonLabelRes = R.string.tutorial_page_2_button,
    ),
    TutorialPage(
        type = TutorialPageType.Middle(
            imageRes = R.drawable.tutorial_three,
            titleRes = R.string.tutorial_page_3_title,
        ),
        buttonLabelRes = R.string.tutorial_page_3_button,
    ),
    TutorialPage(
        type = TutorialPageType.Middle(
            imageRes = R.drawable.tutorial_four,
            titleRes = R.string.tutorial_page_4_title,
        ),
        buttonLabelRes = R.string.tutorial_page_4_button,
    ),
    TutorialPage(
        type = TutorialPageType.Middle(
            imageRes = R.drawable.tutorial_five,
            titleRes = R.string.tutorial_page_5_title,
        ),
        buttonLabelRes = R.string.tutorial_page_5_button,
    ),
    TutorialPage(
        type = TutorialPageType.Last,
        buttonLabelRes = R.string.tutorial_page_6_button,
    ),
)
