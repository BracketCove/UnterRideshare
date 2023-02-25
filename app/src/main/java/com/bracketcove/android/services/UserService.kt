package com.bracketcove.android.services

import com.bracketcove.android.ServiceResult
import com.bracketcove.android.domain.UnterUser

interface UserService {

    suspend fun getUserById(userId: String): ServiceResult<UnterUser?>
    suspend fun updateUser(user: UnterUser): ServiceResult<UnterUser?>

    suspend fun initializeNewUser(user: UnterUser): ServiceResult<UnterUser?>

    suspend fun logOutUser(user: UnterUser): Unit
}