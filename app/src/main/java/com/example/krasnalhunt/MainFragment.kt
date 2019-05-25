package com.example.krasnalhunt

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import com.example.krasnalhunt.model.MainViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_main.*


class MainFragment : Fragment(), OnMapReadyCallback {

    private var listener: OnFragmentInteractionListener? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private val mainViewModel: MainViewModel by activityViewModels()

    private var currentCircle: Circle? = null

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mainViewModel.map = googleMap

        mainViewModel.items.observe(this, Observer { dwarfs ->
            Log.d("TAG", dwarfs.toString())
            mainViewModel.map.clear()
            currentCircle = null
            for (dwarf in dwarfs) {
                mainViewModel.map.addMarker(MarkerOptions().position(dwarf.coordinates).title(dwarf.name)).apply {
                    tag = dwarf
                }
            }
        })

        mainViewModel.map.setOnMarkerClickListener {
            currentCircle?.remove()
            currentCircle = mainViewModel.map.addCircle(
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

        mainViewModel.map.setOnMapClickListener {
            currentCircle?.remove()
            currentCircle = null
        }

        mainViewModel.locationPermissionGranted.observe(this, Observer {
            mainViewModel.map.run {
                isMyLocationEnabled = it
                uiSettings.isMyLocationButtonEnabled = it
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(BOTTOM_SHEET_BEHAVIOR_STATE, bottomSheetBehavior.state)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    private fun initViews() {
        childFragmentManager.commit {
            childFragmentManager.findFragmentByTag("map")?.let { mapFragment ->
                (mapFragment as SupportMapFragment).getMapAsync(this@MainFragment)
            } ?: run {
                val mapFragment = SupportMapFragment.newInstance(
                    GoogleMapOptions()
                        .camera(CameraPosition.fromLatLngZoom(LatLng(51.109286, 17.032307), 16.0f))
                        .maxZoomPreference(19.0f))

                replace(R.id.content_map, mapFragment, "map")
                mapFragment.getMapAsync(this@MainFragment)
            }

            replace(R.id.content_list, DwarfItemListFragment.newInstance(), "list")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        val lp = content_list.layoutParams
        if (lp is CoordinatorLayout.LayoutParams) {
            val behavior = lp.behavior
            if (behavior is BottomSheetBehavior<*>) {
                bottomSheetBehavior = behavior
            }
        }

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) = Unit

            override fun onStateChanged(p0: View, p1: Int) {
                when (p1) {
                    BottomSheetBehavior.STATE_HIDDEN ->
                        fab.setImageResource(R.drawable.ic_view_list_black_24dp)
                    else ->
                        fab.setImageResource(R.drawable.ic_map_black_24dp)
                }
            }
        })

        if (savedInstanceState?.containsKey(BOTTOM_SHEET_BEHAVIOR_STATE) == true) {
            bottomSheetBehavior.state = savedInstanceState.getInt(BOTTOM_SHEET_BEHAVIOR_STATE)
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        fab.setOnClickListener {
            bottomSheetBehavior.state = if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                BottomSheetBehavior.STATE_HIDDEN
            } else {
                BottomSheetBehavior.STATE_EXPANDED
            }
        }

        fab.show()
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
//            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            MainFragment()

        private const val BOTTOM_SHEET_BEHAVIOR_STATE = "bottom-sheet-behavior-state"
    }
}
