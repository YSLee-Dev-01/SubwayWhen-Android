package com.yslee.subwaywhen.navigation

object NavRoutes {
    const val Splash = "splash"
    const val Tutorial = "tutorial"
    const val Root = "root"
    const val Edit = "edit"

    const val ARG_DETAIL_MODEL = "detailModel"
    const val ARG_RESULT_SCHEDULE_MODEL = "resultScheduleModel"

    const val Detail = "detail/{$ARG_DETAIL_MODEL}"
    const val DetailResultSchedule = "detail_result_schedule/{$ARG_RESULT_SCHEDULE_MODEL}"

    fun detailRoute(encodedModel: String) = "detail/$encodedModel"
    fun detailResultScheduleRoute(encodedModel: String) = "detail_result_schedule/$encodedModel"
}
