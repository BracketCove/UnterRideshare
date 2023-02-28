package com.bracketcove.android.dashboards.passenger

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.location.LocationManagerCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bracketcove.android.BuildConfig
import com.bracketcove.android.R
import com.bracketcove.android.UnterApp
import com.bracketcove.android.dashboards.driver.DriverDashboardUiState
import com.bracketcove.android.databinding.FragmentPassengerDashboardBinding
import com.bracketcove.android.uicommon.LOCATION_REQUEST_INTERVAL
import com.bracketcove.android.uicommon.handleToast
import com.bracketcove.android.uicommon.hideKeyboard
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.maps.DirectionsApi
import com.google.maps.android.PolyUtil
import com.google.maps.model.TravelMode
import com.google.maps.model.Unit
import com.zhuinden.simplestackextensions.fragmentsktx.lookup
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class PassengerDashboardFragment : Fragment(R.layout.fragment_passenger_dashboard),
    OnMapReadyCallback {

    private val viewModel by lazy { lookup<PassengerDashboardViewModel>() }


    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null
    private var locationRequest: LocationRequest? = null
    private lateinit var locationClient: FusedLocationProviderClient

    lateinit var binding: FragmentPassengerDashboardBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentPassengerDashboardBinding.bind(view)
        Places.initialize(requireActivity().application, BuildConfig.MAPS_API_KEY)
        locationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        mapView = binding.mapLayout.mapView
        mapView?.onCreate(savedInstanceState)

        requestPermission()

        lifecycleScope.launch {
            viewModel.uiState
                //Only emit states when lifecycle of the fragment is started
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { uiState ->
                    updateUi(uiState)
                }
        }

        viewModel.toastHandler = {
            handleToast(it)
        }

        //This button is reused in most states so we add the listener here
        binding.mapLayout.cancelButton.setOnClickListener { viewModel.cancelRide() }
        binding.chatButton.setOnClickListener {
            viewModel.openChat()
        }
        binding.toolbar.profileIcon.setOnClickListener { viewModel.goToProfile() }
    }

    private fun updateUi(uiState: PassengerDashboardUiState) {
        //only make profile accessible when not in a ride
        if (uiState != PassengerDashboardUiState.RideInactive) binding.toolbar.profileIcon.visibility =
            View.GONE
        else binding.toolbar.profileIcon.visibility = View.VISIBLE

        when (uiState) {
            is PassengerDashboardUiState.Arrived -> updateMessageButton(uiState.totalMessages)
            is PassengerDashboardUiState.EnRoute -> updateMessageButton(uiState.totalMessages)
            is PassengerDashboardUiState.PassengerPickUp -> updateMessageButton(uiState.totalMessages)
            else -> Unit
        }

        when (uiState) {
            PassengerDashboardUiState.Error -> viewModel.handleError()
            PassengerDashboardUiState.Loading -> {
                binding.loadingView.loadingLayout.visibility = View.VISIBLE
            }
            is PassengerDashboardUiState.RideInactive -> rideInactiveState()
            is PassengerDashboardUiState.SearchingForDriver -> searchingForDriverState(uiState)
            is PassengerDashboardUiState.PassengerPickUp -> passengerPickUp(uiState)
            is PassengerDashboardUiState.EnRoute -> enRoute(uiState)
            is PassengerDashboardUiState.Arrived -> arrived(uiState)
        }

        updateMap(uiState)
    }

    private fun updateMessageButton(messageCount: Int) {
        binding.chatButton.text = if (messageCount == 0) getString(R.string.contact_driver)
        else getString(R.string.you_have_messages)
//        else buildString {
//            append(messageCount)
//            append(R.string.new_messages)
//        }
    }

    private fun arrived(uiState: PassengerDashboardUiState.Arrived) {
        binding.apply {
            rideLayout.visibility = View.VISIBLE
            loadingView.loadingLayout.visibility = View.GONE
            searchingLayout.visibility = View.GONE
            rideComplete.rideCompleteLayout.visibility = View.VISIBLE

            searchingForDriver.searchingForDriverLayout.visibility = View.GONE
            //unbind recyclerview from adapter
            autocompleteResults.adapter = null

            mapLayout.subtitle.text = getString(R.string.destination)
            mapLayout.address.text = uiState.destinationAddress

            rideComplete.rideCompleteLayout.visibility = View.VISIBLE
            rideComplete.advanceButton.visibility = View.GONE
            driverName.text = uiState.driverName
            Glide.with(requireContext())
                .load(
                    if (uiState.driverAvatar != "") uiState.driverAvatar
                    else R.drawable.baseline_account_circle_24
                )
                .fitCenter()
                .placeholder(
                    CircularProgressDrawable(requireContext()).apply {
                        setColorSchemeColors(
                            ContextCompat.getColor(requireContext(), R.color.color_light_grey)
                        )

                        strokeWidth = 2f
                        centerRadius = 48f
                        start()
                    }
                )
                .into(binding.avatar)

            driverInfoLayout.visibility = View.VISIBLE
        }
    }

    private fun enRoute(uiState: PassengerDashboardUiState.EnRoute) {
        binding.apply {
            rideLayout.visibility = View.VISIBLE
            loadingView.loadingLayout.visibility = View.GONE
            searchingLayout.visibility = View.GONE

            searchingForDriver.searchingForDriverLayout.visibility = View.GONE
            //unbind recyclerview from adapter
            autocompleteResults.adapter = null

            mapLayout.subtitle.text = getString(R.string.destination)
            mapLayout.address.text = uiState.destinationAddress

            driverName.text = uiState.driverName
            Glide.with(requireContext())
                .load(
                    if (uiState.driverAvatar != "") uiState.driverAvatar
                    else R.drawable.baseline_account_circle_24
                )
                .fitCenter()
                .placeholder(
                    CircularProgressDrawable(requireContext()).apply {
                        setColorSchemeColors(
                            ContextCompat.getColor(requireContext(), R.color.color_light_grey)
                        )

                        strokeWidth = 2f
                        centerRadius = 48f
                        start()
                    }
                )
                .into(binding.avatar)

            driverInfoLayout.visibility = View.VISIBLE
            rideComplete.rideCompleteLayout.visibility = View.GONE

        }
    }

    private fun passengerPickUp(uiState: PassengerDashboardUiState.PassengerPickUp) {
        binding.apply {
            rideLayout.visibility = View.VISIBLE
            loadingView.loadingLayout.visibility = View.GONE
            searchingLayout.visibility = View.GONE

            searchingForDriver.searchingForDriverLayout.visibility = View.GONE
            //unbind recyclerview from adapter
            autocompleteResults.adapter = null

            mapLayout.subtitle.text = getString(R.string.destination)
            mapLayout.address.text = uiState.destinationAddress

            driverName.text = uiState.driverName
            Glide.with(requireContext())
                .load(
                    if (uiState.driverAvatar != "") uiState.driverAvatar
                    else R.drawable.baseline_account_circle_24
                )
                .fitCenter()
                .placeholder(
                    CircularProgressDrawable(requireContext()).apply {
                        setColorSchemeColors(
                            ContextCompat.getColor(requireContext(), R.color.color_light_grey)
                        )

                        strokeWidth = 2f
                        centerRadius = 48f
                        start()
                    }
                )
                .into(binding.avatar)

            driverInfoLayout.visibility = View.VISIBLE
            rideComplete.rideCompleteLayout.visibility = View.GONE
        }
    }

    private fun searchingForDriverState(uiState: PassengerDashboardUiState.SearchingForDriver) {
        binding.apply {
            rideLayout.visibility = View.VISIBLE
            loadingView.loadingLayout.visibility = View.GONE
            searchingLayout.visibility = View.GONE

            driverInfoLayout.visibility = View.GONE
            rideComplete.rideCompleteLayout.visibility = View.GONE

            searchingForDriver.searchingForDriverLayout.visibility = View.VISIBLE
            //unbind recyclerview from adapter
            autocompleteResults.adapter = null

            mapLayout.subtitle.text = getString(R.string.destination)
            mapLayout.address.text = uiState.destinationAddress
        }
    }

    /**
     * - Map is visible
     * - Search layout is visible
     */
    private fun rideInactiveState() {
        binding.apply {
            rideLayout.visibility = View.GONE
            loadingView.loadingLayout.visibility = View.GONE
            searchingLayout.visibility = View.VISIBLE


            if (autocompleteResults.adapter == null) {
                autocompleteResults.adapter = AutocompleteResultsAdapter().apply {
                    handleItemClick = {
                        hideKeyboard(binding.searchEditText, requireContext())
                        viewModel.handleSearchItemClick(it)
                        this.submitList(emptyList())
                    }
                }
            }

            //somewhat worried this could attach multiple observers
            lifecycleScope.launch {
                viewModel.autoCompleteList
                    //Only emit states when lifecycle of the fragment is started
                    .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                    .distinctUntilChanged()
                    .collect { models ->
                        (autocompleteResults.adapter as AutocompleteResultsAdapter)
                            .submitList(models)
                    }
            }

            searchEditText.doOnTextChanged { text, start, before, count ->
                if (text.isNullOrBlank() || text.length < 3) Unit
                else {
                    viewModel.requestAutocompleteResults(text.toString())
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                requireContext(),
                R.string.permissions_required_to_use_this_app,
                Toast.LENGTH_LONG
            ).show()
            viewModel.handleError()
        } else {
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.setAllGesturesEnabled(true)

            googleMap.setMinZoomPreference(12f)
            viewModel.mapIsReady()
        }
    }


    private fun updateMap(
        uiState: PassengerDashboardUiState
    ) {
        if (googleMap != null) {

            googleMap!!.clear()
            when (uiState) {
                is PassengerDashboardUiState.SearchingForDriver -> {
                    googleMap!!.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(uiState.passengerLat, uiState.passengerLon),
                            14f
                        )
                    )
                }
                is PassengerDashboardUiState.PassengerPickUp -> {
                    lifecycleScope.launch {
                        val dirResult =
                            DirectionsApi.newRequest((requireActivity().application as UnterApp).geoContext)
                                .mode(TravelMode.DRIVING)
                                .units(Unit.METRIC)
                                //Change this appropriately
                                .region("ca")
                                .origin(
                                    com.google.maps.model.LatLng(
                                        uiState.passengerLat,
                                        uiState.passengerLon
                                    )
                                )
                                .destination(
                                    com.google.maps.model.LatLng(
                                        uiState.driverLat,
                                        uiState.driverLon
                                    ).toString()
                                )
                                .await()

                        if (dirResult.routes?.first() != null &&
                            dirResult.routes.isNotEmpty() &&
                            dirResult.routes.first().legs.isNotEmpty()
                        ) {
                            dirResult.routes.first().let { route ->
                                googleMap!!.addPolyline(
                                    PolylineOptions().apply {
                                        clickable(false)
                                        addAll(
                                            PolyUtil.decode(
                                                route.overviewPolyline.encodedPath
                                            )
                                        )
                                        color(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.color_primary
                                            )
                                        )
                                    }
                                )

                                route.legs.first().let { leg ->
                                    binding.distance.text = buildString {
                                        append(getString(R.string.driver_is))
                                        append(leg.distance.humanReadable)
                                        append(getString(R.string.away))
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                R.string.unable_to_get_map_directions,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        googleMap!!.addMarker(
                            MarkerOptions().apply {
                                position(LatLng(uiState.passengerLat, uiState.passengerLon))
                            }
                        )
                        googleMap!!.addMarker(
                            MarkerOptions().apply {
                                position(LatLng(uiState.driverLat, uiState.driverLon))
                                val markerBitmap = requireContext().getDrawable(R.drawable.ic_car_marker)?.toBitmap()
                                markerBitmap?.let {
                                    icon(BitmapDescriptorFactory.fromBitmap(it))
                                }
                            }
                        )


                        googleMap!!.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(uiState.driverLat, uiState.driverLon),
                                14f
                            )
                        )
                    }
                }
                is PassengerDashboardUiState.EnRoute -> {
                    lifecycleScope.launch {
                        val dirResult =
                            DirectionsApi.newRequest((requireActivity().application as UnterApp).geoContext)
                                .mode(TravelMode.DRIVING)
                                .units(Unit.METRIC)
                                //Change this appropriately
                                .region("ca")
                                .origin(
                                    com.google.maps.model.LatLng(
                                        uiState.driverLat,
                                        uiState.driverLon
                                    )
                                )
                                .destination(
                                    com.google.maps.model.LatLng(
                                        uiState.destinationLat,
                                        uiState.destinationLon
                                    ).toString()
                                )
                                .await()

                        if (dirResult.routes?.first() != null &&
                            dirResult.routes.isNotEmpty() &&
                            dirResult.routes.first().legs.isNotEmpty()
                        ) {
                            dirResult.routes.first().let { route ->
                                googleMap!!.addPolyline(
                                    PolylineOptions().apply {
                                        clickable(false)
                                        addAll(
                                            PolyUtil.decode(
                                                route.overviewPolyline.encodedPath
                                            )
                                        )
                                        color(
                                            ContextCompat.getColor(
                                                requireContext(),
                                                R.color.color_primary
                                            )
                                        )
                                    }
                                )

                                route.legs.first().let { leg ->
                                    binding.distance.text = buildString {
                                        append(getString(R.string.destination_is))
                                        append(leg.distance.humanReadable)
                                        append(getString(R.string.away))
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                R.string.unable_to_get_map_directions,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        googleMap!!.addMarker(
                            MarkerOptions().apply {
                                position(LatLng(uiState.driverLat, uiState.driverLon))
                                val markerBitmap = requireContext().getDrawable(R.drawable.ic_car_marker)?.toBitmap()
                                markerBitmap?.let {
                                    icon(BitmapDescriptorFactory.fromBitmap(it))
                                }
                            }
                        )
                        googleMap!!.addMarker(
                            MarkerOptions().apply {
                                position(LatLng(uiState.destinationLat, uiState.destinationLon))
                            }
                        )

                        googleMap!!.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(uiState.driverLat, uiState.driverLon),
                                14f
                            )
                        )
                    }
                }

                is PassengerDashboardUiState.Arrived -> {
                    googleMap!!.addMarker(
                        MarkerOptions().apply {
                            position(LatLng(uiState.destinationLat, uiState.destinationLon))
                        }
                    )

                    googleMap!!.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(uiState.destinationLat, uiState.destinationLon),
                            14f
                        )
                    )
                }

                //do nothing
                else -> Unit
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        //This function is a great introduction to programming with the Android SDK ;)
        val locationManager = (requireActivity()
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager)

        if (LocationManagerCompat.isLocationEnabled(locationManager)) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission()
            } else {
                // Create the location request to start receiving updates
                locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    LOCATION_REQUEST_INTERVAL
                ).apply {
                    //only update if user moved more than 10 meters
                    setMinUpdateDistanceMeters(0f)
                }.build()

                //determine if device settings are configured properly
                val locationSettingsRequest = LocationSettingsRequest.Builder().apply {
                    addLocationRequest(locationRequest!!)
                }.build()

                LocationServices.getSettingsClient(requireContext())
                    .checkLocationSettings(locationSettingsRequest).addOnCompleteListener { task ->
                        if (task.isSuccessful) startRequestingLocationUpdates()
                        else {
                            Toast.makeText(
                                requireContext(),
                                R.string.system_settings_are_preventing_location_updates,
                                Toast.LENGTH_LONG
                            ).show()
                            viewModel.handleError()
                        }
                    }
            }
        } else {
            Toast.makeText(
                requireContext(),
                R.string.location_must_be_enabled,
                Toast.LENGTH_LONG
            ).show()
            viewModel.handleError()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRequestingLocationUpdates() {
        locationClient
            .lastLocation
            .addOnCompleteListener { locationRequest ->
                if (locationRequest.isSuccessful && locationRequest.result != null) {
                    val location = locationRequest.result

                    val lat = location.latitude
                    val lon = location.longitude

                    viewModel.updatePassengerLocation(com.google.maps.model.LatLng(lat, lon))


                } else {

                    Log.d("PLACES", locationRequest.exception.toString())

                    Toast.makeText(
                        requireContext(),
                        R.string.unable_to_retrieve_coordinates_user,
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.handleError()
                }
            }
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //permission not granted
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            //begin map initialization
            mapView?.getMapAsync(this)

            //get user location
            requestLocation()
        }
    }

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted){
            mapView?.getMapAsync(this)
            requestLocation()
        }
        else {
            Toast.makeText(
                requireContext(),
                R.string.permissions_required_to_use_this_app,
                Toast.LENGTH_LONG
            ).show()
            viewModel.handleError()
        }
    }

    //So yeah, if you don't add this crap here, the MapView will be basically useless.
    //Apparently this happenings when working with a MapView that starts out View.INVISIBLE or smth?
    override fun onResume() {
        mapView?.onResume()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }
}