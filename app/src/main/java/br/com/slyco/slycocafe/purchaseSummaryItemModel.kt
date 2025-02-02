package br.com.slyco.slycocafe



class purchaseSummaryItemModel {
    constructor(flavor: NESPRESSOFLAVORS, quantity:Int) {
        this.flavor = flavor
        this.quantity = quantity
    }

    private var quantity:Int = 0
    private lateinit var flavor:NESPRESSOFLAVORS


    fun getFlavor(): NESPRESSOFLAVORS {
        return flavor
    }

    fun setFlavor(flavor: NESPRESSOFLAVORS) {
        this.flavor = flavor
    }
    fun getQuantity(): Int {
        return quantity
    }

    fun setQuantity(quantity: Int) {
        this.quantity = quantity
    }
}