package com.yslee.subwaywhen.core.location

interface LocationManager {
    suspend fun locationAuthCheck(): Boolean
    suspend fun locationRequest(): LocationData?
}
