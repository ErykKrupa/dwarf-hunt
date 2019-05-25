package com.example.krasnalhunt

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.krasnalhunt.model.AppDatabase
import com.example.krasnalhunt.model.DwarfItem
import com.example.krasnalhunt.model.MainViewModel
import com.example.krasnalhunt.model.Player
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

class MapsActivity : AppCompatActivity(), InitializationFragment.OnDoneListener, DwarfViewFragment.OnFragmentInteractionListener {

    override fun onDone() {
        runOnUiThread {
            getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit {
                putBoolean(PREF_FIRST_LAUNCH, false)
            }

            postInit()
        }
    }

    private fun postInit() {
        if (mainViewModel.user != null) {
            loadMainFragment()
            getLocationPermission()
            loadFirestoreData()
        } else {
            login()
        }
    }

    private fun loadMainFragment() {
        if (supportFragmentManager.findFragmentByTag("main") == null) {
            supportFragmentManager.commit {
                replace(R.id.content, MainFragment.newInstance(), "main")
            }
        }
    }

    private fun loadFirestoreData() {
        mainViewModel.observeFirestore(this)
    }

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    var player = Player(Location("Player location"))

    private fun launchInitialization() {
        supportFragmentManager.commit {
            replace(R.id.content, InitializationFragment())
        }
    }

    private val mainViewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    MainViewModel(applicationContext) as T
                } else {
                    throw IllegalArgumentException("ViewModel Not Found")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

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
                mainViewModel.removeFirestoreListener()
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
            else -> super.onOptionsItemSelected(item)
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
            mainViewModel.locationPermissionGranted.value = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mainViewModel.locationPermissionGranted.value = true
                } else {
                    mainViewModel.locationPermissionGranted.value = false
                    showPermissionDialog({
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            finish()
                        } else
                            askForPermissions()
                    })
                }
            }
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

    override fun onFragmentInteraction(item: DwarfItem?) {
        if (item == null)
            return
        mainViewModel.updateCaught(item)
    }

    companion object {
        const val PREF_FIRST_LAUNCH = "first-launch"
        const val SHARED_PREFERENCES = "shared-preferences"
        private const val RC_SIGN_IN = 1
    }

    private fun getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            if (mainViewModel.locationPermissionGranted.value == true) {
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
