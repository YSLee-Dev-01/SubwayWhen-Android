package com.yslee.subwaywhen.data.network

import com.yslee.subwaywhen.BuildConfig
import javax.inject.Inject

interface TokenProvider {
    fun token(key: TokenKey): String
}

class DefaultTokenProvider @Inject constructor() : TokenProvider {
    override fun token(key: TokenKey): String = when (key) {
        TokenKey.LIVE -> BuildConfig.LIVE_TOKEN
        TokenKey.SEOUL -> BuildConfig.SEOUL_TOKEN
        TokenKey.KORAIL -> BuildConfig.KORAIL_TOKEN
        TokenKey.KAKAO -> BuildConfig.KAKAO_TOKEN
        TokenKey.REALTIME -> BuildConfig.REALTIME_TOKEN
    }
}
