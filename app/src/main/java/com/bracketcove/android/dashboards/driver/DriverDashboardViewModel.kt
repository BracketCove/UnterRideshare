package com.bracketcove.android.dashboards.driver

import android.util.Log
import com.bracketcove.android.ServiceResult
import com.bracketcove.android.google.GoogleService
import com.bracketcove.android.navigation.ChatKey
import com.bracketcove.android.navigation.LoginKey
import com.bracketcove.android.navigation.ProfileSettingsKey
import com.bracketcove.android.navigation.SplashKey
import com.bracketcove.android.uicommon.ToastMessages
import com.bracketcove.android.uicommon.combineTuple
import com.bracketcove.android.domain.Ride
import com.bracketcove.android.domain.RideStatus
import com.bracketcove.android.domain.UnterUser
import com.bracketcove.android.services.RideService
import com.bracketcove.android.usecase.GetUser
import com.google.maps.model.LatLng
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.ScopedServices
import com.zhuinden.simplestack.StateChange
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

class DriverDashboardViewModel(
    val backstack: Backstack,
    val getUser: GetUser,
    val rideService: RideService
) : ScopedServices.Activated, CoroutineScope {

    private val canceller = Job()

    override val coroutineContext: CoroutineContext
        get() = canceller + Dispatchers.Main

    internal var toastHandler: ((ToastMessages) -> Unit)? = null

    private val _driverModel = MutableStateFlow<UnterUser?>(null)
    private val _rideModel: Flow<ServiceResult<Ride?>> = rideService.rideFlow()
    private val _mapIsReady = MutableStateFlow(false)

    /*
    Different UI states:
    1. User may never be null
    2. Ride may be null (If User.status is INACTIVE, then no need to try to fetch a ride)
    3. Ride may be not null, and in varying states:
        - SEARCHING_FOR_DRIVER
        - PASSENGER_PICK_UP
        - EN_ROUTE
        - ARRIVED
     */
    val uiState = combineTuple(_driverModel, _rideModel, _mapIsReady).map { (driver, rideResult, isMapReady) ->
        if (rideResult is ServiceResult.Failure) return@map DriverDashboardUiState.Error
        val ride = (rideResult as ServiceResult.Value).value

        if (driver == null || !isMapReady) DriverDashboardUiState.Loading
        else {
            when {
                ride == null -> DriverDashboardUiState.SearchingForPassengers

                ride.status == RideStatus.PASSENGER_PICK_UP.value
                        && ride.driverLatitude != null
                        && ride.driverLongitude != null -> DriverDashboardUiState.PassengerPickUp(
                    passengerLat = ride.passengerLatitude,
                    passengerLon = ride.passengerLongitude,
                    driverLat = ride.driverLatitude,
                    driverLon = ride.driverLongitude,
                    destinationLat = ride.destinationLatitude,
                    destinationLon = ride.destinationLongitude,
                    destinationAddress = ride.destinationAddress,
                    passengerName = ride.passengerName,
                    passengerAvatar = ride.passengerAvatarUrl,
                    totalMessages = ride.totalMessages
                )

                ride.status == RideStatus.EN_ROUTE.value
                        && ride.driverLatitude != null
                        && ride.driverLongitude != null -> DriverDashboardUiState.EnRoute(
                    driverLat = ride.driverLatitude,
                    driverLon = ride.driverLongitude,
                    destinationLat = ride.destinationLatitude,
                    destinationLon = ride.destinationLongitude,
                    destinationAddress = ride.destinationAddress,
                    passengerName = ride.passengerName,
                    passengerAvatar = ride.passengerAvatarUrl,
                    totalMessages = ride.totalMessages
                )

                ride.status == RideStatus.ARRIVED.value
                        && ride.driverLatitude != null
                        && ride.driverLongitude != null -> DriverDashboardUiState.Arrived(
                    driverLat = ride.driverLatitude,
                    driverLon = ride.driverLongitude,
                    destinationLat = ride.destinationLatitude,
                    destinationLon = ride.destinationLongitude,
                    destinationAddress = ride.destinationAddress,
                    passengerName = ride.passengerName,
                    passengerAvatar = ride.passengerAvatarUrl,
                    totalMessages = ride.totalMessages
                )

                else -> {
                    Log.d("ELSE", "${driver}, ${ride}")
                    DriverDashboardUiState.Error
                }
            }
        }
    }

    //999 represents an impossible value, indicating we don't know the driver's location
    private val DEFAULT_LAT_OR_LON = 999.0
    private val _driverLocation = MutableStateFlow(LatLng(DEFAULT_LAT_OR_LON, DEFAULT_LAT_OR_LON))
    private var _passengerList = rideService.openRides()

    //I don't want a driver to be able to accept a ride unless we know their location first.
    val locationAwarePassengerList = combineTuple(_driverLocation, _passengerList).map {
        if (it.first.lat == DEFAULT_LAT_OR_LON
            || it.first.lng == DEFAULT_LAT_OR_LON
        ) emptyList<Pair<Ride, LatLng>>()
        else {
            if (it.second is ServiceResult.Failure) {
                handleError()
                emptyList<Pair<Ride, LatLng>>()
            } else {
                val result = it.second as ServiceResult.Value
                result.value.map { ride ->
                    Pair(ride, _driverLocation.value)
                }
            }
        }
    }

    fun mapIsReady() {
        _mapIsReady.value = true
    }

    fun getDriver() = launch(Dispatchers.Main) {
        val getUser = getUser.getUser()
        when (getUser) {
            is ServiceResult.Failure -> {
                toastHandler?.invoke(ToastMessages.GENERIC_ERROR)
                sendToLogin()
            }
            is ServiceResult.Value -> {
                if (getUser.value == null) sendToLogin()
                else {
                    getActiveRideIfItExists(getUser.value)
                }
            }
        }
    }

    /**
     * The Passenger model must always be the last model which is mutated from a null state. By
     * setting the other models first, we avoid the UI rapidly switching between different states
     * in a disorganized way.
     */
    private suspend fun getActiveRideIfItExists(user: UnterUser) {
        val result = rideService.getRideIfInProgress()

        when (result) {
            is ServiceResult.Failure -> {
                toastHandler?.invoke(ToastMessages.SERVICE_ERROR)
                sendToLogin()
            }
            is ServiceResult.Value -> {
                //if null, no active ride exists
                if (result.value == null) {
                    _driverModel.value = user
                    getPassengerList()
                } else observeRideModel(result.value, user)
            }
        }
    }

    private suspend fun observeRideModel(rideId: String, user: UnterUser) {
        //The result of this call is handled inside the flowable assigned to _rideModel
        rideService.observeRideById(rideId)
        _driverModel.value = user
    }

    private suspend fun getPassengerList() {
        rideService.observeOpenRides()
    }

    fun handlePassengerItemClick(clickedRide: Ride) = launch(Dispatchers.Main) {
        //We must only proceed if driver LatLng are real values!
        if (_driverLocation.value.lat != DEFAULT_LAT_OR_LON
            && _driverLocation.value.lng != DEFAULT_LAT_OR_LON
        ) {
            val result = rideService.connectDriverToRide(
                clickedRide.copy(
                    driverLatitude = _driverLocation.value.lat,
                    driverLongitude = _driverLocation.value.lng
                ), _driverModel.value!!
            )

            when (result) {
                is ServiceResult.Failure -> toastHandler?.invoke(ToastMessages.SERVICE_ERROR)
                is ServiceResult.Value -> {
                    _passengerList = emptyFlow()
                    rideService.observeRideById(result.value)
                }
            }
        } else {
            toastHandler?.invoke(ToastMessages.UNABLE_TO_RETRIEVE_USER_COORDINATES)
        }
    }

    fun goToProfile() {
        //normally we would use backStack.goTo(...), but we always want to reload the state
        //of the dashboard
        backstack.setHistory(
            History.of(ProfileSettingsKey()),
            StateChange.FORWARD
        )
    }

    fun updateDriverLocation(latLng: LatLng) = launch(Dispatchers.Main) {
        _driverLocation.value = latLng

        val currentRide = _rideModel.first()

        if (currentRide is ServiceResult.Value && currentRide.value != null) {
            val result = rideService.updateDriverLocation(
                currentRide.value!!,
                latLng.lat,
                latLng.lng
            )

            if (result is ServiceResult.Failure) {
                toastHandler?.invoke(ToastMessages.SERVICE_ERROR)
            }
        }
    }

    fun cancelRide() = launch(Dispatchers.Main) {
        val cancelRide = rideService.cancelRide()
        when (cancelRide) {
            is ServiceResult.Failure -> {
                toastHandler?.invoke(ToastMessages.GENERIC_ERROR)
                sendToSplash()
            }

            //State should automatically be handled by the flow
            is ServiceResult.Value -> Unit
        }
    }

    private fun sendToLogin() {
        backstack.setHistory(
            History.of(LoginKey()),
            StateChange.BACKWARD
        )
    }

    private fun sendToSplash() {
        backstack.setHistory(
            History.of(SplashKey()),
            StateChange.REPLACE
        )
    }


    override fun onServiceActive() {
        getDriver()
    }

    override fun onServiceInactive() {
        canceller.cancel()
    }

    fun handleError() {
        sendToLogin()
    }

    fun completeRide() = launch(Dispatchers.Main) {

        val ride = _rideModel.first()

        if (ride is ServiceResult.Value && ride.value != null) {
            val completeRide = rideService.completeRide(ride.value!!)
            when (completeRide) {
                is ServiceResult.Failure -> {
                    toastHandler?.invoke(ToastMessages.GENERIC_ERROR)
                    sendToSplash()
                }
                is ServiceResult.Value -> {
                    sendToSplash()
                }
            }
        }
    }

    fun advanceRide() = launch {
        val oldRideState = _rideModel.first()

        if (oldRideState is ServiceResult.Value && oldRideState.value != null) {
            val updateRide = rideService.advanceRide(
                oldRideState.value!!.rideId,
                advanceRideState(oldRideState.value!!.status)
            )

            when (updateRide) {
                is ServiceResult.Failure -> {
                    toastHandler?.invoke(ToastMessages.SERVICE_ERROR)
                }
                is ServiceResult.Value -> Unit
            }
        }

    }

    private fun advanceRideState(status: String): String {
        return when (status) {
            RideStatus.SEARCHING_FOR_DRIVER.value -> RideStatus.PASSENGER_PICK_UP.value
            RideStatus.PASSENGER_PICK_UP.value -> RideStatus.EN_ROUTE.value
            else -> RideStatus.ARRIVED.value
        }
    }

    fun openChat() = launch(Dispatchers.Main) {
        val currentRide = _rideModel.first()

        if (currentRide is ServiceResult.Value && currentRide.value != null) {
            backstack.setHistory(
                History.of(ChatKey(currentRide.value!!.rideId)),
                StateChange.FORWARD
            )
        }
    }

    fun queryRidesAgain() = launch(Dispatchers.Main) {
        getPassengerList()
    }
}