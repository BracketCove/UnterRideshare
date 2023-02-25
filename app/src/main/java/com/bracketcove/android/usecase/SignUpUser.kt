package com.bracketcove.android.usecase

import com.bracketcove.android.ServiceResult
import com.bracketcove.android.domain.UnterUser
import com.bracketcove.android.services.AuthorizationService
import com.bracketcove.android.services.SignUpResult
import com.bracketcove.android.services.UserService

class SignUpUser(
    val authService: AuthorizationService,
    val userService: UserService
) {

    suspend fun signUpUser(email: String, password: String, username: String): ServiceResult<SignUpResult> {
        val authAttempt = authService.signUp(email, password)

        return if (authAttempt is ServiceResult.Value) {
            when (authAttempt.value) {
                is SignUpResult.Success -> updateUserDetails(
                    username,
                    authAttempt.value.uid
                )
                else -> authAttempt
            }
        } else authAttempt
    }

    private suspend fun updateUserDetails(
        username: String,
        uid: String
    ): ServiceResult<SignUpResult> {
        return userService.initializeNewUser(
            UnterUser(
                userId = uid,
                username = username
            )
        ).let { updateResult ->
            when (updateResult) {
                is ServiceResult.Failure -> ServiceResult.Failure(updateResult.exception)
                is ServiceResult.Value -> ServiceResult.Value(SignUpResult.Success(uid))
            }
        }
    }
}