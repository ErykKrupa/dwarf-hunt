package com.example.krasnalhunt


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.example.krasnalhunt.model.DwarfViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.MarkerOptions


class SingleDwarfMapFragment : Fragment(), OnMapReadyCallback {

    private val dwarfViewModel: DwarfViewModel by activityViewModels()
    private var currentCircle: Circle? = null

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        currentCircle = null
        googleMap.addMarker(
            MarkerOptions()
                .position(dwarfViewModel.dwarfItem!!.coordinates)
                .title(dwarfViewModel.dwarfItem!!.name)
        )

        googleMap.setOnMarkerClickListener {
            currentCircle?.remove()
            currentCircle = googleMap.addCircle(
                CircleOptions()
                    .center(it.position)
                    .radius(30.0)
                    .strokeWidth(3.0f)
                    .strokeColor(Color.GREEN)
                    .fillColor(Color.argb(128, 255, 0, 0))
                    .clickable(false)
            )
            googleMap.uiSettings.isMapToolbarEnabled = true
            it.showInfoWindow()
            false
        }

        googleMap.setOnMapClickListener {
            currentCircle?.remove()
            currentCircle = null
        }

        googleMap.run {
            isMyLocationEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isZoomControlsEnabled = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_single_dwarf_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
    }

    private fun initializeView() {
        childFragmentManager.findFragmentByTag("single_dwarf_map")?.let { mapFragment ->
            (mapFragment as SupportMapFragment).getMapAsync(this@SingleDwarfMapFragment)
        } ?: run {
            val mapFragment = SupportMapFragment.newInstance(
                GoogleMapOptions()
                    .camera(CameraPosition.fromLatLngZoom(dwarfViewModel.dwarfItem!!.coordinates, 17.5f))
                    .maxZoomPreference(19.0f)
            )
            childFragmentManager.commit {
                replace(R.id.single_dwarf_map_content, mapFragment, "single_dwarf_map")
                mapFragment.getMapAsync(this@SingleDwarfMapFragment)
            }
        }
    }

}
