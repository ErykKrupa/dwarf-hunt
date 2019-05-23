package com.example.krasnalhunt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.krasnalhunt.model.DwarfItem
import com.squareup.picasso.Picasso
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
        holder.nameHolder.text = item.name
        holder.locationHolder.text = item.location
        val imgResId = holder.imageHolder.resources.getIdentifier(
            item.fileName.dropLast(4),
            "drawable",
            holder.imageHolder.context.packageName
        )
        if (imgResId == 0) {
            Picasso.get().load(R.drawable.ic_block_black_24dp).into(holder.imageHolder)
        } else {
            Picasso.get().load(imgResId).error(R.drawable.ic_block_black_24dp).fit().into(holder.imageHolder)
        }
        if (item.caught) {
            holder.caughtImageHolder.setImageResource(R.drawable.ic_check_icon)
            holder.caughtImageBackground.background =
                holder.caughtImageBackground.resources.getDrawable(R.color.greenBackground, null)
        } else {
            holder.caughtImageHolder.setImageResource(R.drawable.ic_check_icon_gray)
            holder.caughtImageBackground.background =
                holder.caughtImageBackground.resources.getDrawable(R.color.grayBackground, null)
        }
        holder.distanceHolder.text = "0"

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
        val nameHolder: TextView = mView.nameTextView
        val locationHolder: TextView = mView.locationTextView
        val imageHolder: ImageView = mView.dwarfImage
        val caughtImageHolder: ImageView = mView.caughtImage
        val caughtImageBackground: ImageView = mView.imageBackground
        val distanceHolder: TextView = mView.distanceTextView

        override fun toString(): String {
            return super.toString() + " '" + nameHolder.text + "'"
        }
    }
}
