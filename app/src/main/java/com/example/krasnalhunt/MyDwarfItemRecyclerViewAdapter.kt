package com.example.krasnalhunt

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.krasnalhunt.model.DwarfItem
import kotlinx.android.synthetic.main.fragment_dwarfitem.view.*


class MyDwarfItemRecyclerViewAdapter(
    private val mListener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<MyDwarfItemRecyclerViewAdapter.ViewHolder>() {

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: DwarfItem?)
    }

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as DwarfItem
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_dwarfitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = ald.currentList[position]
        holder.mIdView.text = item.id.toString()
        holder.mContentView.text = "${item.name} - ${item.location}"
        holder.mView.background = if (item.caught) ColorDrawable(Color.GREEN) else ColorDrawable(Color.RED)

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    private val ald = AsyncListDiffer<DwarfItem>(this, object : DiffUtil.ItemCallback<DwarfItem>() {
        override fun areContentsTheSame(oldItem: DwarfItem, newItem: DwarfItem): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: DwarfItem, newItem: DwarfItem): Boolean {
            return oldItem.id == newItem.id
        }

    })

    fun updateData(newItems: List<DwarfItem>) {
        ald.submitList(newItems)
    }

    override fun getItemCount(): Int = ald.currentList.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.item_number
        val mContentView: TextView = mView.content

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
