package com.bracketcove.android.services

import com.bracketcove.android.ServiceResult
import com.bracketcove.android.domain.UnterUser

interface AuthorizationService {
    /**
     * @return uid if sign up is successful
     */
    suspend fun signUp(email: String, password: String): ServiceResult<SignUpResult>
    suspend fun login(email: String, password: String): ServiceResult<LogInResult>

    suspend fun logout(): ServiceResult<Unit>

    /**
     * @return true if a user session is active, else null
     */
    suspend fun getSession(): ServiceResult<UnterUser?>

}

sealed interface SignUpResult {
    data class Success(val uid: String) : SignUpResult
    object AlreadySignedUp : SignUpResult
    object InvalidCredentials : SignUpResult
}

sealed interface LogInResult {
    data class Success(val user: UnterUser) : LogInResult
    object InvalidCredentials : LogInResult
}