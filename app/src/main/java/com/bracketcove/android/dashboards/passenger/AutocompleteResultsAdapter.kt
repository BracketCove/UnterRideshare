package com.bracketcove.android.dashboards.passenger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bracketcove.android.databinding.ListItemSearchLocationBinding


class AutocompleteResultsAdapter : ListAdapter<AutoCompleteModel, AutocompleteResultsAdapter.AutoCompleteViewHolder>(
    object: DiffUtil.ItemCallback<AutoCompleteModel>() {
        override fun areItemsTheSame(oldItem: AutoCompleteModel, newItem: AutoCompleteModel): Boolean {
            return oldItem.prediction.placeId == newItem.prediction.placeId
        }

        override fun areContentsTheSame(oldItem: AutoCompleteModel, newItem: AutoCompleteModel): Boolean {
            return true //no need
        }
    }
) {

    var handleItemClick: ((AutoCompleteModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoCompleteViewHolder {
        return AutoCompleteViewHolder(
            ListItemSearchLocationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AutoCompleteViewHolder, position: Int) {
        getItem(position).apply {
            holder.location.text = address
            holder.layout.setOnClickListener { handleItemClick?.invoke(this) }
        }
    }

    inner class AutoCompleteViewHolder constructor(binding: ListItemSearchLocationBinding) :
        RecyclerView.ViewHolder(binding.root) {
            val location: TextView = binding.place
            val layout: View = binding.itemLayout
    }
}

