package com.bracketcove.android.usecase

import com.bracketcove.android.ServiceResult
import com.bracketcove.android.domain.UnterUser
import com.bracketcove.android.services.AuthorizationService
import com.bracketcove.android.services.UserService

class GetUser(
    val authService: AuthorizationService,
    val userService: UserService
) {

    suspend fun getUser(): ServiceResult<UnterUser?> {
        val getSession = authService.getSession()
        return when (getSession) {
            is ServiceResult.Failure -> getSession
            is ServiceResult.Value -> {
                if (getSession.value == null) getSession
                else getUserDetails(getSession.value.userId)
            }
        }
    }

    private suspend fun getUserDetails(uid: String): ServiceResult<UnterUser?> {
        return userService.getUserById(uid).let { getDetailsResult ->
            when (getDetailsResult) {
                    is ServiceResult.Failure -> ServiceResult.Failure(getDetailsResult.exception)
                is ServiceResult.Value -> ServiceResult.Value(getDetailsResult.value)
            }
        }
    }
}