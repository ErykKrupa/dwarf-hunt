package com.example.krasnalhunt

import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.krasnalhunt.model.DwarfItem
import com.example.krasnalhunt.model.MainViewModel
import com.google.android.gms.maps.model.LatLng


class InitializationFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_initialization, container, false)
    }

    private fun String.toLatLng() = this.split(Regex("[^\\d.]+")).let { tokens ->
        val latitude = tokens[0].toFloat() + (tokens[1].toFloat() * (1.0 / 60.0)) + (tokens[2].toFloat() * (1.0 / 3600.0))
        val longitude = tokens[3].toFloat() + (tokens[4].toFloat() * (1.0 / 60.0)) + (tokens[5].toFloat() * (1.0 / 3600.0))

        LatLng(latitude, longitude)
    }

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val listener = activity as OnDoneListener

        AsyncTask.execute {
            val textFile = context!!.resources.openRawResource(R.raw.dwarfs)
            textFile.reader().useLines { lines ->
                val dwarfs = lines.mapIndexed { index, it ->
                    it.split("\t").let { tokens ->
                        val coordinates = tokens[0].toLatLng()
                        val name = tokens[1]
                        val address = tokens[2]
                        val author = tokens[3]
                        val location = tokens[4]
                        val fileName = tokens[5]

                        DwarfItem(name, address, coordinates, location, author, fileName, id = index + 1)
                    }
                }
                mainViewModel.database.dwarfItemDao().insertItems(dwarfs.toList())
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
