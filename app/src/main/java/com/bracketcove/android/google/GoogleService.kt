package com.bracketcove.android.google

import android.content.Context
import android.util.Log
import com.bracketcove.android.ServiceResult
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.LatLng
import com.google.maps.model.TravelMode
import com.google.maps.model.Unit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GoogleService(
    context: Context,
    val geoApiContext: GeoApiContext
) {

    private val client: PlacesClient by lazy {
        Places.createClient(context)
    }
    private var token: AutocompleteSessionToken? = null

    suspend fun getPlaceCoordinates(placeId: String): ServiceResult<FetchPlaceResponse?> = withContext(Dispatchers.IO) {
        val placeFields: List<Place.Field> = listOf(
            Place.Field.LAT_LNG
        )
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        try {
            ServiceResult.Value(awaitResult(client.fetchPlace(request)))
        } catch (e: Exception) {
            ServiceResult.Failure(e)
        }
    }

    suspend fun getDistanceBetween(userLatLng: LatLng, comparedTo: LatLng): String = withContext(Dispatchers.IO) {
        Log.d("DIRECTIONS", userLatLng.toString())
        val dirResult =
            DirectionsApi.newRequest(geoApiContext)
                .mode(TravelMode.DRIVING)
                .units(Unit.METRIC)
                //Change this appropriately
                .region("ca")
                .origin(
                    userLatLng
                )
                .destination(
                    comparedTo
                )
                .await()

        if (dirResult.routes?.first() != null &&
            dirResult.routes.isNotEmpty() &&
            dirResult.routes.first().legs.isNotEmpty()
        ) {
            dirResult.routes.first().legs.first().distance.humanReadable
        } else{
            "Error"
        }
    }

    suspend fun getAutocompleteResults(query: String): ServiceResult<List<AutocompletePrediction>> =
        withContext(
            Dispatchers.IO
        ) {
            if (token == null) token = AutocompleteSessionToken.newInstance()

            val request = FindAutocompletePredictionsRequest.builder()
                    //Obviously change this according to distribution of the app
                .setCountry("CA")
                .setTypesFilter(listOf(PlaceTypes.ADDRESS))
                .setSessionToken(token)
                .setQuery(query)
                .build()

            try {
                val task = awaitResult(client.findAutocompletePredictions(request))
                ServiceResult.Value(task.autocompletePredictions)
            } catch (e: Exception) {
                ServiceResult.Failure(e)
            }
        }

    private suspend fun <T> awaitResult(task: Task<T>): T = suspendCoroutine { continuation ->
        task.addOnCompleteListener {
            if (task.isSuccessful) continuation.resume(task.result!!)
            else continuation.resumeWithException(task.exception!!)
        }
    }
}