package com.example.krasnalhunt

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.krasnalhunt.model.DwarfItem
import com.example.krasnalhunt.model.DwarfViewModel
import kotlinx.android.synthetic.main.fragment_dwarf_view.view.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [DwarfViewFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 *
 */
class DwarfViewFragment : Fragment() {
    lateinit var dwarfItem: DwarfItem
    private var listener: OnFragmentInteractionListener? = null
    private var catchable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dwarfItem = ViewModelProviders.of(requireActivity()).get(DwarfViewModel::class.java).dwarfItem!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dwarf_view, container, false)
        view.nameTextView.text = dwarfItem.name
        view.authorTextView.text = dwarfItem.author
        view.locationTextView.text = dwarfItem.location
        view.addressTextView.text = dwarfItem.address
        if (dwarfItem.caught) setGraphicToCaught()
        view.imageView.setImageResource(
            resources.getIdentifier(
                dwarfItem.fileName.dropLast(4),
                "drawable",
                context!!.packageName
            )
        )
        view.showOnTheMapButton.setOnClickListener { onShowOnTheMapButtonPressed() }
        view.caughtImageBackground.setOnClickListener { catchButtonClicked(dwarfItem) }
        view.caughtButton.setOnClickListener { catchButtonClicked(dwarfItem) }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCatchable()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun catchButtonClicked(dwarfItem: DwarfItem?) {
        if (catchable) {
            Log.i("DwarfView", "catching dwarf")
            catchDwarf(dwarfItem)
            setGraphicToCaught()
            catchable = false
        }
    }

    private fun setGraphicToCaught() {
        if (catchable) {
            view?.apply {
                caughtButton.setImageResource(R.drawable.ic_check_icon)
                caughtButton.setOnClickListener { }
                caughtImageBackground.background = resources.getDrawable(R.color.greenBackground, null)
                caughtImageBackground.setOnClickListener { }
            }
        }
    }

    private fun onShowOnTheMapButtonPressed() {
        //TODO: implement showing dwarf on the map
    }

    private fun catchDwarf(item: DwarfItem?) {
        listener?.onFragmentInteraction(item)
    }

    fun setCatchable() {
        if (!dwarfItem.caught) {
            catchable = true
            view?.let {
                it.caughtButton.setImageResource(R.drawable.ic_hand_yellow)
                it.caughtImageBackground.background = resources.getDrawable(R.color.yellowBackground, null)
            } ?: Log.i("DwarfView", "setting catchable failed due to view being null")
        }
    }


    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(item: DwarfItem?)
    }

}
