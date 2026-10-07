package com.rehelp.app.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rehelp.app.data.Food
import com.rehelp.app.databinding.ItemFoodBinding

// Used by My listings now, and by Browse later (pass onClick to open details)
class FoodAdapter(
    private var items: List<Food>,
    private val onClick: ((Food) -> Unit)? = null
) : RecyclerView.Adapter<FoodAdapter.VH>() {

    class VH(val b: ItemFoodBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val f = items[position]
        holder.b.tvName.text = f.name
        holder.b.tvMeta.text = "Qty ${f.quantity} \u00B7 ${f.category} \u00B7 ${f.area}"
        holder.b.tvExpiry.text = "Expires ${f.expiryTime}"
        holder.b.tvStatus.text = f.status
        holder.b.tvStatus.setTextColor(
            Color.parseColor(
                when (f.status) {
                    "Available" -> "#2E7D32"
                    "Reserved" -> "#B26A00"
                    else -> "#5F6368"
                }
            )
        )
        FoodImages.load(holder.b.ivFood, f.imageName)
        holder.b.root.setOnClickListener { onClick?.invoke(f) }
    }

    fun update(newItems: List<Food>) {
        items = newItems
        notifyDataSetChanged()
    }
}
