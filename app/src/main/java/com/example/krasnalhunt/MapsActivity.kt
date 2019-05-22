package com.example.krasnalhunt

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Observer
import com.example.krasnalhunt.model.AppDatabase
import com.example.krasnalhunt.model.Player
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges


const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, InitializationFragment.OnDoneListener {

    override fun onDone() {
        runOnUiThread {
            getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit {
                putBoolean(PREF_FIRST_LAUNCH, false)
            }

            postInit()
        }
    }

    private fun postInit() {
        if (user != null) {
            loadMap()
            loadFirestoreData()
        } else {
            login()
        }
    }

    private fun loadFirestoreData() {
        firestoreListener?.remove()
        AsyncTask.execute {
            database.dwarfItemDao().resetCaught()
            runOnUiThread {
                firestoreListener = firestore.collection("caught-dwarfs")
                    .document(user!!.uid)
                    .addSnapshotListener(MetadataChanges.EXCLUDE) { documentSnapshot, firebaseFirestoreException ->
                        documentSnapshot?.run {
                            AsyncTask.execute {
                                data?.forEach { (dwarfIdString, caught) ->
                                    val id = dwarfIdString.toInt()
                                    val dwarf = database.dwarfItemDao().findItem(id)
                                    dwarf.caught = caught as Boolean
                                    database.dwarfItemDao().updateItems(dwarf)
                                }
                            }
                        } ?: run {
                            Log.e("TAG", "Document snapshot unavailable", firebaseFirestoreException)
                        }
                    }
            }
        }
    }

    private lateinit var mMap: GoogleMap
    private var mLocationPermissionGranted = false
    private var mLastKnownLocation: Location? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    var player = Player(Location("Player location"))

    private fun launchInitialization() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content, InitializationFragment())
            .commit()
    }

    private fun loadMap() {
        getLocationPermission()

        supportFragmentManager.findFragmentByTag("map")?.let { mapFragment ->
            (mapFragment as SupportMapFragment).getMapAsync(this)
        } ?: run {
            val mapFragment = SupportMapFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, mapFragment, "map")
                .commit()
            mapFragment.getMapAsync(this)
        }

        Log.d("TAG", user?.run { displayName ?: "Anonymous" } ?: "what")
    }

    private lateinit var database: AppDatabase
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private lateinit var firestore: FirebaseFirestore
    private var firestoreListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        database = AppDatabase.createInstance(applicationContext)
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        firestore = FirebaseFirestore.getInstance()

        val firstLaunch = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)
            .getBoolean(PREF_FIRST_LAUNCH, true)

        if (firstLaunch)
            launchInitialization()
        else
            postInit()
    }

    private fun login() {
        val providers = listOf(
            AuthUI.IdpConfig.EmailBuilder().build()
            // TODO: AuthUI.IdpConfig.AnonymousBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
//                .enableAnonymousUsersAutoUpgrade()
                .build(),
            RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == RESULT_OK) {
                user = auth.currentUser
                postInit()
            } else {
                if (response == null) {
                    finish()
                    return
                }

                if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
                    finish()
                    return
                }

                Log.e("TAG", "Sign-in error: ", response.error)
                Toast.makeText(this, "Sign-in error: ${response.error?.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                firestoreListener?.remove()
                firestoreListener = null
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener {
                        login()
                    }
                true
            }
            R.id.action_reset -> {
                getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit {
                    putBoolean(PREF_FIRST_LAUNCH, true)
                }
                AsyncTask.execute {
                    AppDatabase.instance?.clearAllTables()
                    runOnUiThread {
                        finish()
                    }
                }
                true
            }
            R.id.action_list -> {
                if (supportFragmentManager.findFragmentByTag("list") == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.content, DwarfItemListFragment.newInstance(), "list")
                        .addToBackStack(null)
                        .commit()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private var currentCircle: Circle? = null

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        database.dwarfItemDao().findItems().observe(this, Observer { dwarfs ->
            Log.d("TAG", dwarfs.toString())
            mMap.clear()
            currentCircle = null
            for (dwarf in dwarfs) {
                mMap.addMarker(MarkerOptions().position(dwarf.coordinates).title(dwarf.name)).apply {
                    tag = dwarf
                }
            }
            val pos = CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(LatLng(51.109286, 17.032307), 16.0f)
            )
            mMap.moveCamera(pos)
        })

        mMap.setOnMarkerClickListener {
            currentCircle?.remove()
            currentCircle = mMap.addCircle(
                CircleOptions()
                    .center(it.position)
                    .radius(30.0)
                    .strokeWidth(3.0f)
                    .strokeColor(Color.GREEN)
                    .fillColor(Color.argb(128, 255, 0, 0))
                    .clickable(false)
            )
            it.showInfoWindow()
            true
        }

        mMap.setOnMapClickListener {
            currentCircle?.remove()
            currentCircle = null
        }

        if (mLocationPermissionGranted) {
            getDeviceLocation()
            mMap.isMyLocationEnabled = true
        }
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
        private const val RC_SIGN_IN = 1
    }

    private fun getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            if (mLocationPermissionGranted) {
                val location = mFusedLocationProviderClient!!.lastLocation
                location.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentLocation = task.result as Location
                        player.setPlayerLocation(currentLocation)
                    } else {
                        // TODO: not successful
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }


}
