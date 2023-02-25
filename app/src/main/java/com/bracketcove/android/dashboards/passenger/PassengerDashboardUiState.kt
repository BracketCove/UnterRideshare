package com.bracketcove.android.dashboards.passenger

sealed interface PassengerDashboardUiState {
    object RideInactive: PassengerDashboardUiState
    data class SearchingForDriver(
        val passengerLat: Double,
        val passengerLon: Double,
        val destinationAddress: String
    ): PassengerDashboardUiState
    data class PassengerPickUp(
        val passengerLat: Double,
        val passengerLon: Double,
        val driverLat: Double,
        val driverLon: Double,
        val destinationLat: Double,
        val destinationLon: Double,
        val destinationAddress: String,
        val driverName: String,
        val driverAvatar: String,
        val totalMessages: Int
    ): PassengerDashboardUiState
    data class EnRoute(
        val passengerLat: Double,
        val passengerLon: Double,
        val destinationLat: Double,
        val destinationLon: Double,
        val driverLat: Double,
        val driverLon: Double,
        val destinationAddress: String,
        val driverName: String,
        val driverAvatar: String,
        val totalMessages: Int
    ): PassengerDashboardUiState

    data class Arrived(
        val passengerLat: Double,
        val passengerLon: Double,
        val destinationLat: Double,
        val destinationLon: Double,
        val destinationAddress: String,
        val driverName: String,
        val driverAvatar: String,
        val totalMessages: Int
    ): PassengerDashboardUiState

    //Signals something unexpected has happened
    object Error: PassengerDashboardUiState
    object Loading: PassengerDashboardUiState
}