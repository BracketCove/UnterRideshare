package com.bracketcove.android.dashboards.driver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bracketcove.android.R
import com.bracketcove.android.databinding.ListItemPassengerBinding
import com.bracketcove.android.domain.Ride
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.maps.model.LatLng

class PassengerListAdapter :
    ListAdapter<Pair<Ride, LatLng>, PassengerListAdapter.PassengerViewHolder>(
        object : DiffUtil.ItemCallback<Pair<Ride, LatLng>>() {
            override fun areItemsTheSame(
                oldItem: Pair<Ride, LatLng>,
                newItem: Pair<Ride, LatLng>
            ): Boolean {
                return oldItem.first.rideId == newItem.first.rideId
            }

            override fun areContentsTheSame(
                oldItem: Pair<Ride, LatLng>,
                newItem: Pair<Ride, LatLng>
            ): Boolean {
                return oldItem.first.rideId == newItem.first.rideId
            }
        }
    ) {

    var handleItemClick: ((Ride) -> Unit)? = null
    var getDistance: ((Double?, Double?, Double?, Double?) -> String)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PassengerViewHolder {
        return PassengerViewHolder(
            ListItemPassengerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PassengerViewHolder, position: Int) {
        getItem(position).apply {
            val ride = first
            val driverLatLng = second

            holder.username.text = ride.passengerName

            if (ride.passengerAvatarUrl == "") {
                Glide.with(holder.itemView.context)
                    .load(R.drawable.baseline_account_circle_24)
                    .fitCenter()
                    .placeholder(
                        CircularProgressDrawable(holder.itemView.context).apply {
                            setColorSchemeColors(
                                ContextCompat.getColor(
                                    holder.itemView.context,
                                    R.color.color_light_grey
                                )
                            )

                            strokeWidth = 2f
                            centerRadius = 48f
                            start()
                        }
                    )
                    .into(holder.avatar)
            } else {
                Glide.with(holder.itemView.context)
                    .load(ride.passengerAvatarUrl)
                    .fitCenter()
                    .placeholder(
                        CircularProgressDrawable(holder.itemView.context).apply {
                            setColorSchemeColors(
                                ContextCompat.getColor(
                                    holder.itemView.context,
                                    R.color.color_light_grey
                                )
                            )

                            strokeWidth = 2f
                            centerRadius = 48f
                            start()
                        }
                    )
                    .into(holder.avatar)
            }

            //This is definitely a patch work solution; ideally these values would be
            //calculated server side!
            if (getDistance != null) {
                holder.passengerDistance.text = buildString {
                    append(holder.itemView.context.getString(R.string.passenger_is))
                    append(
                        getDistance!!.invoke(
                            driverLatLng.lat,
                            driverLatLng.lng,
                            ride.passengerLatitude,
                            ride.passengerLongitude
                        )
                    )
                    append(holder.itemView.context.getString(R.string.away))
                }

                holder.tripDistance.text = buildString {
                    append(holder.itemView.context.getString(R.string.destination_is))
                    append(
                        getDistance!!.invoke(
                            ride.passengerLatitude,
                            ride.passengerLongitude,
                            ride.destinationLatitude,
                            ride.destinationLongitude
                        )
                    )
                    append(holder.itemView.context.getString(R.string.from_passenger))
                }
            }

            holder.layout.setOnClickListener { handleItemClick?.invoke(ride) }
        }
    }

    inner class PassengerViewHolder constructor(binding: ListItemPassengerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val username: TextView = binding.username
        val passengerDistance: TextView = binding.passengerDistance
        val tripDistance: TextView = binding.tripDistance
        val avatar: ShapeableImageView = binding.avatar
        val layout: View = binding.listItemLayout
    }
}