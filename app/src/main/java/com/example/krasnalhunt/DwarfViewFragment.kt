package com.example.krasnalhunt

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.krasnalhunt.model.DwarfItem
import kotlinx.android.synthetic.main.fragment_dwarf_view.*
import kotlinx.android.synthetic.main.fragment_dwarf_view.view.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [DwarfViewFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 *
 */
class DwarfViewFragment(private val dwarfItem: DwarfItem) : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

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
        view.caughtImage.visibility = if (dwarfItem.caught) View.VISIBLE else View.INVISIBLE
        view.imageView.setImageResource(
            resources.getIdentifier(
                dwarfItem.fileName.dropLast(4),
                "drawable",
                context!!.packageName
            )
        )
        view.showOnTheMapButton.setOnClickListener { onShowOnTheMapButtonPressed() }
        return view
    }

    private fun onShowOnTheMapButtonPressed() {
        listener?.onFragmentInteraction()
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

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction()
    }

}
