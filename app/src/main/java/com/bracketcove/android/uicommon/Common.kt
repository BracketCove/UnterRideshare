package com.bracketcove.android.uicommon

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import com.bracketcove.android.R
import com.bracketcove.android.style.typography
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/*
These functions are from:
https://github.com/Zhuinden/flow-combinetuple-kt/blob/master/src/main/java/com/zhuinden/flowcombinetuplekt/FlowCombineTuple.kt
 */

fun <T1, T2> combineTuple(f1: Flow<T1>, f2: Flow<T2>): Flow<Pair<T1, T2>> = combine(f1, f2) { t1, t2 -> Pair(t1, t2) }

fun <T1, T2, T3> combineTuple(f1: Flow<T1>, f2: Flow<T2>, f3: Flow<T3>): Flow<Triple<T1, T2, T3>> = combine(f1, f2, f3) { t1, t2, t3 -> Triple<T1, T2, T3>(t1, t2, t3) }

//How frequently do we want to request the location in milliseconds (10s here)
internal const val LOCATION_REQUEST_INTERVAL = 10000L

fun hideKeyboard(view: View, context: Context) {
    val inputMethodManager : InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken,0)
}

fun Fragment.handleToast(code: ToastMessages) {
    val message = when (code) {
        ToastMessages.GENERIC_ERROR -> getString(R.string.generic_error)
        ToastMessages.SERVICE_ERROR -> getString(R.string.service_error)
        ToastMessages.PERMISSION_ERROR -> getString(R.string.permissions_required_to_use_this_app)
        ToastMessages.INVALID_CREDENTIALS -> getString(R.string.invalid_credentails)
        ToastMessages.ACCOUNT_EXISTS -> getString(R.string.an_account_already_exists)
        ToastMessages.UPDATE_SUCCESSFUL -> getString(R.string.update_successful)
        ToastMessages.UNABLE_TO_RETRIEVE_COORDINATES -> getString(R.string.unable_to_retrieve_coordinates_address)
        ToastMessages.UNABLE_TO_RETRIEVE_USER_COORDINATES -> getString(R.string.unable_to_retrieve_coordinates_user)
    }
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

@Composable
fun UnterHeader(
    modifier: Modifier = Modifier,
    subtitleText: String = "Sign up for free"
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.unter),
            style = typography.h1
        )
        Text(
            text = subtitleText,
            style = typography.subtitle2
        )
    }
}
