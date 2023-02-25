package com.bracketcove.android.dashboards.passenger

import com.google.android.libraries.places.api.model.AutocompletePrediction

data class AutoCompleteModel(
    val address: String,
    val prediction: AutocompletePrediction
)