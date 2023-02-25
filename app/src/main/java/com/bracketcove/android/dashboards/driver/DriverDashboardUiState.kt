package com.bracketcove.android.dashboards.driver

sealed interface DriverDashboardUiState {
    object SearchingForPassengers: DriverDashboardUiState
    data class PassengerPickUp(
        val passengerLat: Double,
        val passengerLon: Double,
        val driverLat: Double,
        val driverLon: Double,
        val destinationLat: Double,
        val destinationLon: Double,
        val destinationAddress: String,
        val passengerName: String,
        val passengerAvatar: String,
        val totalMessages: Int
    ): DriverDashboardUiState
    data class EnRoute(
        val driverLat: Double,
        val driverLon: Double,
        val destinationLat: Double,
        val destinationLon: Double,
        val destinationAddress: String,
        val passengerName: String,
        val passengerAvatar: String,
        val totalMessages: Int
    ): DriverDashboardUiState

    data class Arrived(
        val driverLat: Double,
        val driverLon: Double,
        val destinationLat: Double,
        val destinationLon: Double,
        val destinationAddress: String,
        val passengerName: String,
        val passengerAvatar: String,
        val totalMessages: Int
    ): DriverDashboardUiState

    //Signals something unexpected has happened
    object Error: DriverDashboardUiState
    object Loading: DriverDashboardUiState
}