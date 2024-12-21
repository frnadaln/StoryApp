package com.dicoding.storyapp.view.maps

import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityMapsBinding
import com.dicoding.storyapp.view.ViewModelFactory
import com.dicoding.storyapp.di.Result
import com.dicoding.storyapp.utils.vectorToBitmap

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val viewModel by viewModels<MapsViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {isGranted : Boolean ->
        if(isGranted) {
            getMyLocation()
        }
    }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            Log.e("MapsActivity", "Location permission not granted")
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupData()

        supportActionBar?.title = getString(R.string.story_location)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        getMyLocation()
        setMapStyle()
    }

    private fun setupData() {
        viewModel.getStoriesWithLocation().observe(this) { result ->
            when(result) {
                is Result.Loading -> {

                }
                is Result.Error -> {
                    Log.d("error Map marker", result.error)
                }
                is Result.Success -> {
                    val data = result.data.listStory
                    val boundsBuilder = LatLngBounds.Builder()
                    Log.d("MapsActivity", "Data Result: ${result.data}")
                    Log.d("MapsActivity", "Data Size: ${result.data.listStory.size}")
                    data.forEach { res ->
                        val latLng = LatLng(res.lat!!, res.lon!!)
                        mMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(res.name)
                                .snippet(res.description)
                                .icon(vectorToBitmap(R.drawable.ic_story_marker, Color.parseColor("#3DDC84"), this@MapsActivity))
                        )
                        boundsBuilder.include(latLng)
                        Log.d("MapsActivity", "Story: ${res.name}, Lat: ${res.lat}, Lon: ${res.lon}")
                    }
                    val latestLocation = data.first()
                    val setInitialMarker = LatLng(latestLocation.lat!!, latestLocation.lon!!)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(setInitialMarker, 12f))

                    Log.d("Data Map", data.toString())
                }
            }
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.map_style))
            if(!success) {
                Log.e("Parse error", "Style Parsing failed")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("Error Parse map Style", "Can't find style. Error : $e")
        }
    }
}
