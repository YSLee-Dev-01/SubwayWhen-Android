package com.yslee.subwaywhen.core.notification

import com.yslee.subwaywhen.data.model.SaveSetting
import com.yslee.subwaywhen.data.model.SaveStation

interface NotificationScheduler {
    fun reschedule(setting: SaveSetting, saveStations: List<SaveStation>)
}
