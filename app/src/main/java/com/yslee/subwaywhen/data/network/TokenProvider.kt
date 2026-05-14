package com.yslee.subwaywhen.data.network

import javax.inject.Inject

interface TokenProvider {
    fun token(key: TokenKey): String
}

class DefaultTokenProvider @Inject constructor() : TokenProvider {
    override fun token(key: TokenKey): String = "" // TODO: 외부 작업 후 채울 자리
}
