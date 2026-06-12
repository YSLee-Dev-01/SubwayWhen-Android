package com.yslee.subwaywhen.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yslee.subwaywhen.core.FixInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var fixInfo: FixInfo
    @Inject lateinit var notificationScheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        CoroutineScope(Dispatchers.IO).launch {
            fixInfo.initialize()
            notificationScheduler.reschedule(
                fixInfo.saveSetting.value,
                fixInfo.saveStations.value,
            )
        }
    }
}
