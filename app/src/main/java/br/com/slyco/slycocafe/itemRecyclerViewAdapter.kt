package br.com.slyco.slycocafe

import android.icu.text.ListFormatter.Width
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

interface OnItemClickListener {
    fun setPlusOnClickListener(listId: Int, position: Int)
    fun setPlusLongOnClickListener(listId: Int, position: Int)
    fun setMinusOnClickListener(listId: Int, position: Int)
    fun setMinusLongOnClickListener(listId: Int, position: Int)
}

private fun ConstraintLayout.scaleLayout(scaleFactor: Float) {
    val constraintSet = ConstraintSet()

    constraintSet.clone(this as ConstraintLayout) // Clones the constraints from an existing layout

    for (i in 0 until childCount) {
        val view = getChildAt(i)
        val id = view.id

        if (id == View.NO_ID) continue

        // Scale width and height
        if (view.layoutParams.width > 0) {
            constraintSet.constrainWidth(id, (view.layoutParams.width * scaleFactor).toInt())
        }
        if (view.layoutParams.height > 0) {
            constraintSet.constrainHeight(id, (view.layoutParams.height * scaleFactor).toInt())
        }

        // Scale different button types
        when (view) {
            is com.google.android.material.button.MaterialButton, is Button -> {
                // Scale icon size for MaterialButton
                if (view is MaterialButton){


                    view.iconSize = (view.iconSize * scaleFactor).toInt()

                    // Scale icon padding
                    view.iconPadding = (view.iconPadding * scaleFactor).toInt()

                    // Scale text size
                    view.setTextSize(TypedValue.COMPLEX_UNIT_PX, view.textSize * scaleFactor)
                }

                // Scale button's compound drawables (icons)
                if (view is Button) {
                    val drawables = view.compoundDrawables
                    drawables.filterNotNull().forEach { drawable ->
                        val width = (drawable.bounds.width() * scaleFactor).toInt()
                        val height = (drawable.bounds.height() * scaleFactor).toInt()
                        drawable.setBounds(0, 0, width, height)
                    }
                    view.setCompoundDrawables(
                        drawables[0], // left
                        drawables[1], // top
                        drawables[2], // right
                        drawables[3]  // bottom
                    )

                    // Scale drawable padding
                    view.compoundDrawablePadding =
                        (view.compoundDrawablePadding * scaleFactor).toInt()

                    // Scale text if button has text
                    view.setTextSize(TypedValue.COMPLEX_UNIT_PX, view.textSize * scaleFactor)
                }

            }

            is ImageButton -> {
                view.drawable?.let { drawable ->
                    val width = (drawable.intrinsicWidth * scaleFactor).toInt()
                    val height = (drawable.intrinsicHeight * scaleFactor).toInt()
                    drawable.setBounds(0, 0, width, height)
                    view.setImageDrawable(drawable)
                }
            }


        }

        // Scale padding
        view.setPadding(
            (view.paddingLeft * scaleFactor).toInt(),
            (view.paddingTop * scaleFactor).toInt(),
            (view.paddingRight * scaleFactor).toInt(),
            (view.paddingBottom * scaleFactor).toInt()
        )

        // Scale margins
        val params = view.layoutParams as? ConstraintLayout.LayoutParams
        params?.let {
            constraintSet.setMargin(id, ConstraintSet.START,
                (params.leftMargin * scaleFactor).toInt())
            constraintSet.setMargin(id, ConstraintSet.END,
                (params.rightMargin * scaleFactor).toInt())
            constraintSet.setMargin(id, ConstraintSet.TOP,
                (params.topMargin * scaleFactor).toInt())
            constraintSet.setMargin(id, ConstraintSet.BOTTOM,
                (params.bottomMargin * scaleFactor).toInt())
        }
    }

    constraintSet.applyTo(this)
}

class ShoppingCartAdapter(
    private val items: MutableList<shoppingCartItemModel>,
    private val listener: OnItemClickListener,
    private var listId:Int,
    private var elementsInView: Int = 3,
    private var screeenHeight:Int,
    private var screeenWidth:Int,
    private var displayOrientation:Int = LinearLayoutManager.HORIZONTAL
) : RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder>() {

    private var recyclerViewWidth:Int = 400
    private var currentScale: Float = 1.0f
    private var currentScaleHeigth: Float = 1.0f
    private var currentScaleWidth: Float = 1.0f
    private var maxHeight: Int = 0
    private var maxWidth: Int = 0



    fun setRecyclerViewWidth(width: Int) {
        recyclerViewWidth = width
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shoppingCartConstraintLayout: ConstraintLayout = itemView.findViewById(R.id.shoppingCartConstraintLayout)
        val flavorImage: ImageView = itemView.findViewById(R.id.imageViewCapsula)
        val attributesButton: MaterialButton = itemView.findViewById(R.id.textViewAttributes)
        val quantityEditText: EditText = itemView.findViewById(R.id.editTextNumberItem)

        val plusButton: MaterialButton = itemView.findViewById(R.id.floatingActionButtonItemPlus)
        val minusButton: MaterialButton= itemView.findViewById(R.id.floatingActionButtonItemMinus)

        val qtyField: EditText = itemView.findViewById(R.id.editTextNumberItem)

        val priceButton: MaterialButton= itemView.findViewById(R.id.textViewPrice)

        init {
            // Set click listener for the entire item
            flavorImage.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.setPlusOnClickListener(listId,position)
                }
            }

            flavorImage.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.setPlusLongOnClickListener(listId,position)
                }
                true
            }

            plusButton.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.setPlusOnClickListener(listId,position)
                }
            }

            plusButton.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.setPlusLongOnClickListener(listId,position)
                }
                true
            }

            minusButton.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.setMinusOnClickListener(listId,position)
                }
            }

            minusButton.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.setMinusLongOnClickListener(listId,position)
                }
                true
            }

        }

        fun bind(item: shoppingCartItemModel) {
            // Bind your data here

            // Apply scaling

        }
    }
    // Method to update entire item

    fun updateItem(position: Int, newItem: shoppingCartItemModel) {
        if (position in items.indices) {
            if (newItem.getQuantity() >= 0) items[position].setQuantity(newItem.getQuantity())
            if (newItem.getSize() > 0) items[position].setSize(newItem.getSize())
            if (newItem.getFlavor() != NESPRESSOFLAVORS.NONE) items[position].setFlavor(newItem.getFlavor())
            if (newItem.getIntensity() > 0) items[position].setIntensity(newItem.getIntensity())
            if (newItem.getPrice() > 0f) items[position].setPrice(newItem.getPrice())
            items[position].setEnabledItem(newItem.getEnabledItem())
            items[position].setEnabledMinusButton(newItem.getEnabledMinusButton())
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shopping_cart_item, parent, false)

        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

// Get the measured width

        val holder = ViewHolder(view)

        var dimensionToResize = 0

        var viewWidth = view.measuredWidth
        var viewHeigth = view.measuredHeight

        var maxViewWidth = 0.0f
        var maxViewHeight = 0.0f

        if (this.displayOrientation == LinearLayoutManager.VERTICAL){
            maxViewHeight = (screeenHeight - 200).toFloat() / (elementsInView).toFloat()
            maxViewWidth = (screeenWidth).toFloat() / 2.0f
            viewHeigth = maxViewHeight.toInt()

            Log.d ("Recycler","Vertical - maxViewHeight: ${maxViewHeight}   maxViewWidth: ${maxViewWidth}")
        }
        else
        {
            maxViewHeight = (screeenHeight-120).toFloat() / 2.0f
            maxViewWidth = (screeenWidth).toFloat() / (elementsInView).toFloat()
            viewWidth = maxViewWidth.toInt()
            Log.d ("Recycler","Horizontal - maxViewHeight: ${maxViewHeight}   maxViewWidth: ${maxViewWidth}")
        }

        currentScaleHeigth = maxViewHeight / (viewHeigth).toFloat()  // considering 200 bottom margin
        currentScaleWidth = maxViewWidth/ (viewWidth).toFloat()  // considering 200 bottom margin

        currentScale = minOf(currentScaleWidth,currentScaleHeigth)

        with(holder) {
            shoppingCartConstraintLayout.scaleLayout(currentScale)
            shoppingCartConstraintLayout.layoutParams.height = viewHeigth
            shoppingCartConstraintLayout.layoutParams.width = viewWidth
//            // Scale buttons
//            minusButton.apply {
//                val originalIconSize = minusButton.iconSize
//                iconSize = (originalIconSize * currentScale).toInt()
//            }
//
//            plusButton.apply {
//                val originalIconSize = plusButton.iconSize
//                iconSize = (originalIconSize * currentScale).toInt()
//            }
//
//            // Scale image if needed
//            flavorImage.apply {
//                scaleX = currentScale
//                scaleY = currentScale
//            }
//
//            priceButton.apply{
//
//            }
//
//            qtyField.apply {
//
//            }

        }
        return holder
    }

    fun updateCoffeIcon(id:Int): Int{
        when (id){
            1 -> return R.drawable.coffeeicon_s
            2 -> return R.drawable.coffeeicon_m
            3 -> return R.drawable.coffeeicon_l
        }

        return 0
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
        holder.attributesButton.text = item.getIntensity().toString()
        holder.attributesButton.setIconResource(updateCoffeIcon(item.getSize()))
        holder.priceButton.text = "R$ ${String.format("%.2f",item.getPrice())}"
        if (qty != null)
            holder.qtyField.setText(qty.toString())

        if (item.getEnabledItem()){
            setAlphaForAllChildren(
                layout = holder.shoppingCartConstraintLayout,
                alpha = AppConstants.ON_STOCK_ALPHA_FLOAT
            )
        }
        else
        {
            setAlphaForAllChildren(
                layout = holder.shoppingCartConstraintLayout,
                alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            )
        }

        if (item.getEnabledMinusButton()){
            holder.minusButton.alpha = AppConstants.ON_STOCK_ALPHA_FLOAT
        }
        else
        {
            holder.minusButton.alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
        }

        holder.bind(items[position])
    }

    fun updateScale(newScale: Float) {
        currentScale = newScale
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}