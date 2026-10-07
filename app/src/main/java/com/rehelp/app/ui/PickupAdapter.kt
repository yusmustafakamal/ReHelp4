package com.rehelp.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rehelp.app.data.PickupDetail
import com.rehelp.app.databinding.ItemPickupBinding

// Used by the volunteer Tasks tab now, and by History later
class PickupAdapter(
    private var items: List<PickupDetail>,
    private val onClick: (PickupDetail) -> Unit
) : RecyclerView.Adapter<PickupAdapter.VH>() {

    class VH(val b: ItemPickupBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemPickupBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val d = items[position]
        holder.b.tvName.text = d.food.name
        holder.b.tvWhen.text = "Pickup ${d.pickup.pickupDate} at ${d.pickup.pickupTime}"
        holder.b.tvMeta.text = "${d.food.area} \u00B7 Qty ${d.food.quantity} \u00B7 For ${d.recipient?.name ?: "-"}"
        holder.b.tvStatus.text = d.pickup.status
        holder.b.tvStatus.setTextColor(StatusColors.of(d.pickup.status))
        FoodImages.load(holder.b.ivFood, d.food.imageName)
        holder.b.root.setOnClickListener { onClick(d) }
    }

    fun update(newItems: List<PickupDetail>) {
        items = newItems
        notifyDataSetChanged()
    }
}
