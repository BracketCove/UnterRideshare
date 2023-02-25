package com.bracketcove.android.services

import com.bracketcove.android.ServiceResult


interface PhotoService {
    suspend fun attemptUserAvatarUpdate(url: String): ServiceResult<String>
}