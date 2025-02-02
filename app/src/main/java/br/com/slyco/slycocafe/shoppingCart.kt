package br.com.slyco.slycocafe

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class shoppingCart {
    private lateinit var itens :MutableList<ITEM>
    private var total = 0.0

    private val customDateFormat: String
        get() = SimpleDateFormat("yyMMdd", Locale.ROOT).format(Date())
    private val customTimeFormat: String
        get() = SimpleDateFormat("HHmmss", Locale.ROOT).format(Date())
    private var myLog  =  log("SHOPPING CART")

    constructor(items: List<inventoryStockDC>) {
        for (i in 0..<items.size) {
            var myItem = ITEM(
                NESPRESSOFLAVORS.from(NESPRESSOFLAVORSHASH.getValue(items[i].item.id)),
                0,
                items[i].price.toFloat()/100,
                items[i].item.coffeeSize,
                items[i].item.intensity)
            if (i == 0) this.itens = mutableListOf(myItem!!)
            else myItem?.let { this.itens.add(it) }
        }
        //clearCart()
    }

    fun calculateTotal() {
        this.total = 0.0
        for (item in itens) {
            this.total += item!!.qty!! * item.price!!
        }

        myLog.log( "${total}")
    }

    fun addItemToCart(itemId: Int, qty: Int, inventory: inventory) :Int{
        var myItem = itens[itemId]
        var myQty = myItem!!.qty!! + qty
        if (myQty >= 0) {
            if (myQty <= inventory.getQty(itemId)!!) {
                myItem!!.qty = myQty
            }
            else{
                return -1001
            }
        }
        else{
            return -1002
        }
        this.calculateTotal()
        myLog.log( "${itemId} ${qty}")
        return qty
    }

    fun getCartItemQuantity(index:Int = 0): Int {
        return itens[index]!!.qty!!
    }

    fun getFlavor (id: Int): NESPRESSOFLAVORS {
        return itens[id]?.flavor ?: NESPRESSOFLAVORS.NONE
    }

    fun clearCart() {
        for (item in itens) {
            item!!.qty = 0
        }
        this.calculateTotal()
    }

    fun returnTotal(): Double {

        return total
    }
    fun returnSubTotal(flavor: NESPRESSOFLAVORS): Double {
        var myItem = itens.find { it?.flavor == flavor }
        if ((myItem!!.qty != null) && (myItem!!.price != null))
            return (myItem!!.qty!!.toDouble() * myItem!!.price!!.toDouble())
        return 0.00
    }

}