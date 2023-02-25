package com.bracketcove.android.authentication.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bracketcove.android.ServiceResult
import com.bracketcove.android.navigation.SplashKey
import com.bracketcove.android.uicommon.ToastMessages
import com.bracketcove.android.services.SignUpResult
import com.bracketcove.android.usecase.SignUpUser
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.ScopedServices
import com.zhuinden.simplestack.StateChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class SignUpViewModel(
    private val backstack: Backstack,
    private val signUp: SignUpUser,
) : ScopedServices.Activated, CoroutineScope {
    internal var toastHandler: ((ToastMessages) -> Unit)? = null

    var email by mutableStateOf("")
        private set

    fun updateEmail(input: String) {
        email = input
    }

    var name by mutableStateOf("")
        private set

    fun updateName(input: String) {
        name = input
    }

    var password by mutableStateOf("")
        private set

    fun updatePassword(input: String) {
        password = input
    }


    fun handleSignUp() = launch(Dispatchers.Main) {
        val signupAttempt = signUp.signUpUser(email, password, name)
        when (signupAttempt) {
            is ServiceResult.Failure -> toastHandler?.invoke(ToastMessages.SERVICE_ERROR)
            is ServiceResult.Value -> {
                when (signupAttempt.value) {
                    is SignUpResult.Success -> {
                        backstack.setHistory(
                            History.of(SplashKey()),
                            //Direction of navigation which is used for animation
                            StateChange.REPLACE
                        )
                    }
                    SignUpResult.InvalidCredentials -> toastHandler?.invoke(ToastMessages.INVALID_CREDENTIALS)
                    SignUpResult.AlreadySignedUp -> toastHandler?.invoke(ToastMessages.ACCOUNT_EXISTS)
                }
            }
        }
    }

    fun handleBackPress() {
        backstack.goBack()
    }
    override fun onServiceActive() = Unit

    override fun onServiceInactive() {
        canceller.cancel()
        toastHandler = null
    }

    private val canceller = Job()

    override val coroutineContext: CoroutineContext
        get() = canceller + Dispatchers.Main
}