package com.bracketcove.android.usecase

import com.bracketcove.android.ServiceResult
import com.bracketcove.android.domain.UnterUser
import com.bracketcove.android.services.AuthorizationService
import com.bracketcove.android.services.UserService

class LogOutUser(
    val authService: AuthorizationService,
    val userService: UserService
) {

    suspend fun logout(user: UnterUser): ServiceResult<Unit> {
        authService.logout()
        userService.logOutUser(user)

        return ServiceResult.Value(Unit)
    }
}