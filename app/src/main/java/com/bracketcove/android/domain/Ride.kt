package com.bracketcove.android.domain

data class Ride(
    val rideId: String = "",
    val status: String = RideStatus.SEARCHING_FOR_DRIVER.value,
    val destinationLatitude: Double = 0.0,
    val destinationLongitude: Double = 0.0,
    val destinationAddress: String = "",
    val passengerId: String = "",
    val passengerLatitude: Double = 0.0,
    val passengerLongitude: Double = 0.0,
    val passengerName: String = "",
    val passengerAvatarUrl: String = "",
    val driverId: String? = null,
    val driverLatitude: Double? = null,
    val driverLongitude: Double? = null,
    val driverName: String? = null,
    val driverAvatarUrl: String? = null,
    val createdAt: String = "",
    val updatedAT: String = "",
    val totalMessages: Int = 0
) {
    companion object {
        fun getDefaultRide(): Ride = Ride()
    }
}
