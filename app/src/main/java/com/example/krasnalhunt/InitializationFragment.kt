package com.example.krasnalhunt

import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.krasnalhunt.model.AppDatabase
import com.example.krasnalhunt.model.DwarfItem
import com.google.android.gms.maps.model.LatLng


class InitializationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_initialization, container, false)
    }

    private fun String.toLatLng() = this.split(Regex("\\D+")).let { tokens ->
        val latitude = tokens[0].toInt() + (tokens[1].toInt() * (1.0 / 60.0)) + (tokens[2].toInt() * (1.0 / 3600.0))
        val longitude = tokens[3].toInt() + (tokens[4].toInt() * (1.0 / 60.0)) + (tokens[5].toInt() * (1.0 / 3600.0))

        LatLng(latitude, longitude)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val listener = activity as OnDoneListener

        AsyncTask.execute {
            val textFile = context!!.resources.openRawResource(R.raw.dwarfs)
            textFile.reader().useLines { lines ->
                lines.map {
                    it.split("\t").let { tokens ->
                        val coordinates = tokens[0].toLatLng()
                        val name = tokens[1]
                        val address = tokens[2]
                        val author = tokens[3]
                        val location = tokens[4]
                        val fileName = tokens[5]

                        DwarfItem(name, address, coordinates, location, author, fileName)
                    }
                }.let {
                    AppDatabase.instance?.dwarfItemDao()?.insertItems(it.toList())
                }
            }

            Thread.sleep(3000) //TODO: remove
            if (activity?.isFinishing == false)
                listener.onDone()
        }
    }

    interface OnDoneListener {
        fun onDone()
    }

}
