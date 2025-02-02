package br.com.slyco.slycocafe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class purchaseSummaryRecyclerViewAdapter(
    private val items: MutableList<purchaseSummaryItemModel>
) : RecyclerView.Adapter<purchaseSummaryRecyclerViewAdapter.ViewHolder>() {



    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flavorImage: ImageView = itemView.findViewById(R.id.imageViewDialog)
        val qtyField: TextView = itemView.findViewById(R.id.qtyTextView)
        val purchaseSummaryItem: ConstraintLayout= itemView.findViewById(R.id.purchaseSummaryItem)
    }
    // Method to update entire item

    fun updateItem(position: Int, newItem: shoppingCartItemModel) {
        if (position in items.indices) {
            if (newItem.getQuantity() >= 0) {
                items[position].setQuantity(newItem.getQuantity())
            }
            else {
            }
            if (newItem.getFlavor() != NESPRESSOFLAVORS.NONE) items[position].setFlavor(newItem.getFlavor())
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dialog_purchase_summary_item, parent, false)
        return ViewHolder(view)
    }

    private fun setAlphaForAllChildren(layout: ConstraintLayout, alpha: Float) {
        for (i in 0 until layout.childCount) {
            val child = layout.getChildAt(i)
            child.alpha = alpha
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val qty = item.getQuantity()

        holder.flavorImage.setImageResource(item.getFlavor().value)
        if (qty != null)
            holder.qtyField.setText(qty.toString())

        if (qty>0){
            setAlphaForAllChildren(
                layout = holder.purchaseSummaryItem,
                alpha = AppConstants.ON_STOCK_ALPHA_FLOAT
            )
        }
        else {
            setAlphaForAllChildren(
                layout = holder.purchaseSummaryItem,
                alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            )
        }
    }

    override fun getItemCount() = items.size
}