package com.bracketcove.android.dashboards.driver

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bracketcove.android.BuildConfig
import com.bracketcove.android.R
import com.bracketcove.android.UnterApp
import com.bracketcove.android.databinding.FragmentDriverDashboardBinding
import com.bracketcove.android.uicommon.LOCATION_REQUEST_INTERVAL
import com.bracketcove.android.uicommon.handleToast
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.maps.DirectionsApi
import com.google.maps.android.PolyUtil
import com.google.maps.model.TravelMode
import com.zhuinden.simplestackextensions.fragmentsktx.lookup
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class DriverDashboardFragment : Fragment(R.layout.fragment_driver_dashboard), OnMapReadyCallback {
    private val viewModel by lazy { lookup<DriverDashboardViewModel>() }

    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null
    private var locationRequest: LocationRequest? = null
    private lateinit var locationClient: FusedLocationProviderClient

    lateinit var binding: FragmentDriverDashboardBinding
    //UNCOMMENT THIS CODE ONCE YOU HAVE COMPLETED view_map_layout.xml
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding = FragmentDriverDashboardBinding.bind(view)
//        Places.initialize(requireActivity().application, BuildConfig.MAPS_API_KEY)
//        locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
//
//        mapView = binding.mapLayout.mapView
//        mapView?.onCreate(savedInstanceState)
//
//        requestPermission()
//
//        lifecycleScope.launch {
//            viewModel.uiState
//                //Only emit states when lifecycle of the fragment is started
//                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
//                .distinctUntilChanged()
//                .collect { uiState ->
//                    updateUi(uiState)
//                }
//        }
//
//        viewModel.toastHandler = {
//            handleToast(it)
//        }
//
//        //This button is reused in most states so we add the listener here
//        binding.mapLayout.cancelButton.setOnClickListener { viewModel.cancelRide() }
//        binding.chatButton.setOnClickListener {
//            viewModel.openChat()
//        }
//
//        binding.toolbar.profileIcon.setOnClickListener { viewModel.goToProfile() }
//    }
//
//    private fun updateUi(uiState: DriverDashboardUiState) {
//        if (uiState != DriverDashboardUiState.SearchingForPassengers) binding.toolbar.profileIcon.visibility = View.GONE
//        else binding.toolbar.profileIcon.visibility = View.VISIBLE
//
//        when (uiState) {
//            is DriverDashboardUiState.Arrived -> updateMessageButton(uiState.totalMessages)
//            is DriverDashboardUiState.EnRoute -> updateMessageButton(uiState.totalMessages)
//            is DriverDashboardUiState.PassengerPickUp -> updateMessageButton(uiState.totalMessages)
//            else -> Unit
//        }
//
//        when (uiState) {
//            DriverDashboardUiState.Error -> viewModel.handleError()
//            DriverDashboardUiState.Loading -> {
//                binding.loadingView.loadingLayout.visibility = View.VISIBLE
//            }
//            is DriverDashboardUiState.SearchingForPassengers -> searchingForPassenger()
//            is DriverDashboardUiState.PassengerPickUp -> passengerPickUp(uiState)
//            is DriverDashboardUiState.EnRoute -> enRoute(uiState)
//            is DriverDashboardUiState.Arrived -> arrived(uiState)
//        }
//
//        updateMap(uiState)
//    }
//
//    private fun updateMessageButton(messageCount: Int) {
//        binding.chatButton.text = if (messageCount == 0) getString(R.string.contact_passenger)
//        else getString(R.string.you_have_messages)
//    }
//
//    private fun arrived(uiState: DriverDashboardUiState.Arrived) {
//        binding.apply {
//            rideLayout.visibility = View.VISIBLE
//            loadingView.loadingLayout.visibility = View.GONE
//            searchingLayout.visibility = View.GONE
//
//            //unbind recyclerview from adapter
//            passengerList.adapter = null
//
//            mapLayout.subtitle.text = getString(R.string.destination)
//            mapLayout.address.text = uiState.destinationAddress
//
//            advanceLayout.advanceRideStateLayout.visibility = View.GONE
//
//            returnLayout.rideCompleteLayout.visibility = View.VISIBLE
//            returnLayout.advanceButton.setOnClickListener {
//                viewModel.completeRide()
//            }
//
//            username.text = uiState.passengerName
//            Glide.with(requireContext())
//                .load(
//                    if (uiState.passengerAvatar != "") uiState.passengerAvatar
//                    else R.drawable.baseline_account_circle_24
//                )
//                .fitCenter()
//                .placeholder(
//                    CircularProgressDrawable(requireContext()).apply {
//                        setColorSchemeColors(
//                            ContextCompat.getColor(requireContext(), R.color.color_light_grey)
//                        )
//
//                        strokeWidth = 2f
//                        centerRadius = 48f
//                        start()
//                    }
//                )
//                .into(binding.avatar)
//        }
//    }
//
//    private fun enRoute(uiState: DriverDashboardUiState.EnRoute) {
//        binding.apply {
//            rideLayout.visibility = View.VISIBLE
//            loadingView.loadingLayout.visibility = View.GONE
//            searchingLayout.visibility = View.GONE
//
//            //unbind recyclerview from adapter
//            passengerList.adapter = null
//
//            mapLayout.subtitle.text = getString(R.string.destination)
//            mapLayout.address.text = uiState.destinationAddress
//
//            advanceLayout.advanceRideStateLayout.visibility = View.VISIBLE
//
//            advanceLayout.advanceButton.setImageResource(R.drawable.ic_arrival)
//            advanceLayout.advanceButton.setOnLongClickListener() {
//                viewModel.advanceRide()
//                true
//            }
//
//            advanceLayout.title.text = getString(R.string.arrived_at_destination)
//            returnLayout.rideCompleteLayout.visibility = View.GONE
//
//            username.text = uiState.passengerName
//            Glide.with(requireContext())
//                .load(
//                    if (uiState.passengerAvatar != "") uiState.passengerAvatar
//                    else R.drawable.baseline_account_circle_24
//                )
//                .fitCenter()
//                .placeholder(
//                    CircularProgressDrawable(requireContext()).apply {
//                        setColorSchemeColors(
//                            ContextCompat.getColor(requireContext(), R.color.color_light_grey)
//                        )
//
//                        strokeWidth = 2f
//                        centerRadius = 48f
//                        start()
//                    }
//                )
//                .into(binding.avatar)
//        }
//    }
//
//    private fun passengerPickUp(uiState: DriverDashboardUiState.PassengerPickUp) {
//        binding.apply {
//            rideLayout.visibility = View.VISIBLE
//            loadingView.loadingLayout.visibility = View.GONE
//            searchingLayout.visibility = View.GONE
//
//            //unbind recyclerview from adapter
//            passengerList.adapter = null
//
//            //Note: we update map subtitles in the updateMap function
//
//            advanceLayout.advanceRideStateLayout.visibility = View.VISIBLE
//            advanceLayout.advanceButton.setImageResource(R.drawable.ic_pick_up_passenger)
//            advanceLayout.advanceButton.setOnLongClickListener() {
//                viewModel.advanceRide()
//                true
//            }
//
//            advanceLayout.title.text = getString(R.string.pick_up_passenger)
//            returnLayout.rideCompleteLayout.visibility = View.GONE
//
//            username.text = uiState.passengerName
//            Glide.with(requireContext())
//                .load(
//                    if (uiState.passengerAvatar != "") uiState.passengerAvatar
//                    else R.drawable.baseline_account_circle_24
//                )
//                .fitCenter()
//                .placeholder(
//                    CircularProgressDrawable(requireContext()).apply {
//                        setColorSchemeColors(
//                            ContextCompat.getColor(requireContext(), R.color.color_light_grey)
//                        )
//
//                        strokeWidth = 2f
//                        centerRadius = 48f
//                        start()
//                    }
//                )
//                .into(binding.avatar)
//
//        }
//    }
//
//    /**
//     * - Map is visible
//     * - Search layout is visible
//     */
//    private fun searchingForPassenger() {
//        binding.apply {
//            rideLayout.visibility = View.GONE
//            loadingView.loadingLayout.visibility = View.GONE
//            searchingLayout.visibility = View.VISIBLE
//
//
//            if (passengerList.adapter == null) {
//                passengerList.adapter = PassengerListAdapter().apply {
//                    getDistance = ::requestDistanceBetweenPointsInKm
//
//                    handleItemClick = {
//                        viewModel.handlePassengerItemClick(it)
//                    }
//                }
//            }
//
//            //somewhat worried this could attach multiple observers
//            lifecycleScope.launch {
//                viewModel.locationAwarePassengerList
//                    //Only emit states when lifecycle of the fragment is started
//                    .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
//                    .distinctUntilChanged()
//                    .collect { models ->
//                        if (models.isNullOrEmpty()) {
//                            passengerList.visibility = View.GONE
//                            passengersLoadingLayout.visibility = View.VISIBLE
//                            checkAgainBT.setOnClickListener { viewModel.queryRidesAgain() }
//
//                        } else {
//                            passengerList.visibility = View.VISIBLE
//                            passengersLoadingLayout.visibility = View.GONE
//                            checkAgainBT.setOnClickListener(null)
//
//                            (passengerList.adapter as PassengerListAdapter)
//                                .submitList(models)
//                        }
//                    }
//            }
//        }
//    }
//
//
//
//    private fun updateMap(
//        uiState: DriverDashboardUiState
//    ) {
//        if (googleMap != null) {
//            googleMap!!.clear()
//            when (uiState) {
//                is DriverDashboardUiState.SearchingForPassengers -> Unit
//                is DriverDashboardUiState.PassengerPickUp -> {
//                    lifecycleScope.launch {
//                        val dirResult =
//                            DirectionsApi.newRequest((requireActivity().application as UnterApp).geoContext)
//                                .mode(TravelMode.DRIVING)
//                                .units(com.google.maps.model.Unit.METRIC)
//                                //Change this appropriately
//                                .region("ca")
//                                .origin(
//                                    com.google.maps.model.LatLng(
//                                        uiState.driverLat,
//                                        uiState.driverLon
//                                    )
//                                )
//                                .destination(
//                                    com.google.maps.model.LatLng(
//                                        uiState.passengerLat,
//                                        uiState.passengerLon
//                                    ).toString()
//                                )
//                                .await()
//
//                        if (dirResult.routes?.first() != null &&
//                            dirResult.routes.isNotEmpty() &&
//                            dirResult.routes.first().legs.isNotEmpty()
//                        ) {
//                            dirResult.routes.first().let { route ->
//                                googleMap!!.addPolyline(
//                                    PolylineOptions().apply {
//                                        clickable(false)
//                                        addAll(
//                                            PolyUtil.decode(
//                                                route.overviewPolyline.encodedPath
//                                            )
//                                        )
//                                        color(
//                                            ContextCompat.getColor(
//                                                requireContext(),
//                                                R.color.color_primary
//                                            )
//                                        )
//                                    }
//                                )
//
//                                route.legs.first().let { leg ->
//                                    binding.mapLayout.subtitle.text = getString(R.string.passenger_location)
//                                    binding.mapLayout.address.text = leg.endAddress ?: getString(R.string.unable_to_retrieve_address)
//
//                                    binding.tripDistance.text = buildString {
//                                        append(getString(R.string.passenger_is))
//                                        append(leg.distance.humanReadable)
//                                        append(getString(R.string.away))
//                                    }
//                                }
//                            }
//                        } else {
//                            Toast.makeText(
//                                requireContext(),
//                                R.string.unable_to_get_map_directions,
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//
//                        googleMap!!.addMarker(
//                            MarkerOptions().apply {
//                                position(LatLng(uiState.passengerLat, uiState.passengerLon))
//                            }
//                        )
//                        googleMap!!.addMarker(
//                            MarkerOptions().apply {
//                                position(LatLng(uiState.driverLat, uiState.driverLon))
//                                val markerBitmap = requireContext().getDrawable(R.drawable.ic_car_marker)?.toBitmap()
//                                markerBitmap?.let {
//                                    icon(BitmapDescriptorFactory.fromBitmap(it))
//                                }
//                            }
//                        )
//
//
//                        googleMap!!.moveCamera(
//                            CameraUpdateFactory.newLatLngZoom(
//                                LatLng(uiState.driverLat, uiState.driverLon),
//                                13f
//                            )
//                        )
//                    }
//
//
//                }
//                is DriverDashboardUiState.EnRoute -> {
//                    lifecycleScope.launch {
//                        val dirResult =
//                            DirectionsApi.newRequest((requireActivity().application as UnterApp).geoContext)
//                                .mode(TravelMode.DRIVING)
//                                .units(com.google.maps.model.Unit.METRIC)
//                                //Change this appropriately
//                                .region("ca")
//                                .origin(
//                                    com.google.maps.model.LatLng(
//                                        uiState.driverLat,
//                                        uiState.driverLon
//                                    )
//                                )
//                                .destination(
//                                    com.google.maps.model.LatLng(
//                                        uiState.destinationLat,
//                                        uiState.destinationLon
//                                    ).toString()
//                                )
//                                .await()
//
//                        if (dirResult.routes?.first() != null &&
//                            dirResult.routes.isNotEmpty() &&
//                            dirResult.routes.first().legs.isNotEmpty()
//                        ) {
//                            dirResult.routes.first().let { route ->
//                                dirResult.routes.first().let { route ->
//                                    googleMap!!.addPolyline(
//                                        PolylineOptions().apply {
//                                            clickable(false)
//                                            addAll(
//                                                PolyUtil.decode(
//                                                    route.overviewPolyline.encodedPath
//                                                )
//                                            )
//                                            color(
//                                                ContextCompat.getColor(
//                                                    requireContext(),
//                                                    R.color.color_primary
//                                                )
//                                            )
//                                        }
//                                    )
//
//                                    route.legs.first().let { leg ->
//                                        binding.tripDistance.text = buildString {
//                                            append(getString(R.string.destination_is))
//                                            append(leg.distance.humanReadable)
//                                            append(getString(R.string.away))
//                                        }
//                                    }
//                                }
//                            }
//                        } else {
//                            Toast.makeText(
//                                requireContext(),
//                                R.string.unable_to_get_map_directions,
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//
//                        googleMap!!.addMarker(
//                            MarkerOptions().apply {
//                                position(LatLng(uiState.driverLat, uiState.driverLon))
//                                val markerBitmap = requireContext().getDrawable(R.drawable.ic_car_marker)?.toBitmap()
//                                markerBitmap?.let {
//                                    icon(BitmapDescriptorFactory.fromBitmap(it))
//                                }
//                            }
//                        )
//
//                        googleMap!!.addMarker(
//                            MarkerOptions().apply {
//                                position(LatLng(uiState.destinationLat, uiState.destinationLon))
//                            }
//                        )
//
//                        googleMap!!.moveCamera(
//                            CameraUpdateFactory.newLatLngZoom(
//                                LatLng(uiState.driverLat, uiState.driverLon),
//                                14f
//                            )
//                        )
//                    }
//
//                }
//                is DriverDashboardUiState.Arrived -> {
//                    googleMap!!.addMarker(
//                        MarkerOptions().apply {
//                            position(LatLng(uiState.destinationLat, uiState.destinationLon))
//                        }
//                    )
//
//                    googleMap!!.moveCamera(
//                        CameraUpdateFactory.newLatLngZoom(
//                            LatLng(uiState.destinationLat, uiState.destinationLon),
//                            14f
//                        )
//                    )
//                }
//                //do nothing
//                else -> Unit
//            }
//        }
//
//    }

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

            googleMap.setMinZoomPreference(11f)
            viewModel.mapIsReady()
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
                    setMinUpdateDistanceMeters(10f)
                }.build()

                //determine if device settings are configured properly
                val locationSettingsRequest = LocationSettingsRequest.Builder().apply {
                    addLocationRequest(locationRequest!!)
                }.build()

                LocationServices.getSettingsClient(requireContext())
                    .checkLocationSettings(locationSettingsRequest).addOnCompleteListener { task ->
                        if (task.isSuccessful) startRequestingLocationUpdates(locationRequest!!)
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
    private fun startRequestingLocationUpdates(locationRequest: LocationRequest) {
        locationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    if (result.lastLocation != null) {
                        //UNCOMMENT THIS AFTER IMPLEMENTING DriverDashboardViewModel
//                        viewModel.updateDriverLocation(
//                            com.google.maps.model.LatLng(
//                                result.lastLocation!!.latitude,
//                                result.lastLocation!!.longitude
//                            )
//                        )
                    } else {
                        Toast.makeText(
                            requireContext(),
                            R.string.unable_to_retrieve_coordinates_user,
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.handleError()
                    }
                }
            },
            Looper.myLooper()
        )
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
        if (isGranted) requestLocation()
        else {
            Toast.makeText(
                requireContext(),
                R.string.permissions_required_to_use_this_app,
                Toast.LENGTH_LONG
            ).show()
            viewModel.handleError()
        }
    }

    fun requestDistanceBetweenPointsInKm(
        originLat: Double?,
        originLon: Double?,
        destinationLat: Double?,
        destinationLon: Double?
    ): String {
        if (
            originLat == null || originLon == null
            || destinationLat == null || destinationLon == null
        ) {
            return getString(R.string.unable_to_calculate_distance)
        } else {
            val dirResult =
                DirectionsApi.newRequest((requireActivity().application as UnterApp).geoContext)
                    .mode(TravelMode.DRIVING)
                    .units(com.google.maps.model.Unit.METRIC)
                    //Change this appropriately
                    .region("ca")
                    .origin(
                        com.google.maps.model.LatLng(
                            originLat,
                            originLon
                        )
                    )
                    .destination(
                        com.google.maps.model.LatLng(
                            destinationLat,
                            destinationLon
                        ).toString()
                    )
                    .await()

            if (dirResult.routes?.first() != null &&
                dirResult.routes.isNotEmpty() &&
                dirResult.routes.first().legs.isNotEmpty()
            ) {
                dirResult.routes.first().let { route ->
                    route.legs.first().let { leg ->
                        val distance = buildString {
                            append(leg.distance.humanReadable)
                        }

                        return distance
                    }
                }
            } else {
                return getString(R.string.unable_to_calculate_distance)
            }
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
        if (binding.passengerList.adapter != null) {
            //Safeguard to help avoid issues
            (binding.passengerList.adapter as PassengerListAdapter).getDistance = null
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }
}