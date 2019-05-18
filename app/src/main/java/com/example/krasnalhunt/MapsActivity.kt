package com.example.krasnalhunt

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.Observer
import com.example.krasnalhunt.model.AppDatabase
import com.google.android.gms.maps.model.CameraPosition

const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, InitializationFragment.OnDoneListener {

    override fun onDone() {
        runOnUiThread {
            getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit {
                putBoolean(PREF_FIRST_LAUNCH, false)
            }

            loadMap()
        }
    }

    private lateinit var mMap: GoogleMap
    private var mLocationPermissionGranted = false
    private var mLastKnownLocation: Location? = null

    private fun launchInitialization() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content, InitializationFragment())
            .commit()
    }

    private fun loadMap() {
        getLocationPermission()

        val mapFragment = SupportMapFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content, mapFragment, "map")
            .commit()
        mapFragment.getMapAsync(this)
    }

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        database = AppDatabase.createInstance(applicationContext)

        val firstLaunch = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)
            .getBoolean(PREF_FIRST_LAUNCH, true)

        if (firstLaunch)
            launchInitialization()
        else
            loadMap()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        database.dwarfItemDao().findItems().observe(this, Observer { dwarfs ->
            Log.d("TAG", dwarfs.toString())
            for (dwarf in dwarfs) {
                mMap.addMarker(MarkerOptions().position(dwarf.coordinates).title(dwarf.name))
            }
            val pos = CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(LatLng(51.109286, 17.032307), 16.0f))
            mMap.moveCamera(pos)
        })
    }

    private fun getLocationPermission() {
        /*
        * Request location permission, so that we can get the location of the
        * device. The result of the permission request is handled by a callback,
        * onRequestPermissionsResult.
        */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askForPermissions()
        } else {
            mLocationPermissionGranted = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                } else
                    showPermissionDialog({
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            finish()
                        } else
                            askForPermissions()
                    })
            }
        }
        updateLocationUI()
    }

    private fun updateLocationUI() {
        try {
            if (mLocationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                mLastKnownLocation = null
                //getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }

    private fun showPermissionDialog(handler: () -> Unit, message: String? = null) {
        AlertDialog.Builder(this).setTitle(R.string.location_permission_alert_title)
            .setMessage(
                when {
                    message != null -> message
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) -> getString(R.string.location_permission_alert_message)
                    else -> getString(R.string.location_permission_alert_message_on_dont_show_again)
                }
            )
            .setPositiveButton(getString(R.string.positive_button_text)) { _, _ -> handler() }
            .setCancelable(false)
            .create().show()
    }

    private fun askForPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        )
    }

    companion object {
        const val PREF_FIRST_LAUNCH = "first-launch"
        const val SHARED_PREFERENCES = "shared-preferences"
    }

}
