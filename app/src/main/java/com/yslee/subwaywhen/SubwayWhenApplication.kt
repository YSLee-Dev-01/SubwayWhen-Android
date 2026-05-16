package com.yslee.subwaywhen

import android.app.Application
import com.yslee.subwaywhen.core.FixInfo
import com.yslee.subwaywhen.di.ApplicationScope
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SubwayWhenApplication : Application() {

    @Inject lateinit var fixInfo: FixInfo
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch { fixInfo.initialize() }
    }
}
