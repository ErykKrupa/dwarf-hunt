package com.example.krasnalhunt

import android.graphics.drawable.ClipDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.krasnalhunt.model.DwarfItem
import com.example.krasnalhunt.model.DwarfViewModel
import com.example.krasnalhunt.model.MainViewModel


class DwarfItemListFragment : Fragment(), MyDwarfItemRecyclerViewAdapter.OnListFragmentInteractionListener {
    private val dwarfViewModel: DwarfViewModel by activityViewModels()

    override fun onListFragmentInteraction(item: Pair<DwarfItem, Float>) {
        Log.d("TAG", "$item clicked")

        dwarfViewModel.dwarfItem = item.first
        dwarfViewModel.distance = item.second.toInt()
        val dwarfViewFragment = DwarfViewFragment()
        requireActivity().supportFragmentManager.commit {
            addToBackStack(null)
            replace(R.id.content, dwarfViewFragment, "dwarfView")
        }
    }

    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var mAdapter: MyDwarfItemRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dwarfitem_list, container, false)

        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                mAdapter = MyDwarfItemRecyclerViewAdapter(this@DwarfItemListFragment)
                adapter = mAdapter
                // TODO: decide whether setHasFixedSize(true) is correct
            }
            val decoration = DividerItemDecoration(context, ClipDrawable.HORIZONTAL)
            view.addItemDecoration(decoration)
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        mainViewModel.dwarfsWithDistance.observe(this, Observer { newList ->
            mAdapter.updateData(newList.sortedBy { it.second })
        })

    }

    companion object {
        @JvmStatic
        fun newInstance() =
            DwarfItemListFragment()
    }
}
