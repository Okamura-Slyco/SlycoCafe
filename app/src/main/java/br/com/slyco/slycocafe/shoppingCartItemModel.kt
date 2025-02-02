package br.com.slyco.slycocafe

import androidx.recyclerview.widget.LinearLayoutManager


class shoppingCartItemModel {
    constructor(size: Int, intensity: Int, price: Float, flavor: NESPRESSOFLAVORS, quantity:Int,enabledItem:Boolean, enabledMinusButton: Boolean,index:Int) {
        this.size = size
        this.intensity = intensity
        this.price = price
        this.flavor = flavor
        this.quantity = quantity
        this.enabledItem = enabledItem
        this.enabledMinusButton = enabledMinusButton
        this.index = index
    }

    private var size:Int = 0
    private var intensity:Int = 0
    private var price:Float = 0.0f
    private var quantity:Int = 0
    private lateinit var flavor:NESPRESSOFLAVORS
    private var enabledItem: Boolean = false
    private var enabledMinusButton: Boolean = false
    private var index: Int= 0

    fun getSize(): Int {
        return size
    }

    fun setSize(size: Int) {
        this.size = size
    }

    fun getIntensity(): Int {
        return intensity
    }

    fun setIntensity(intensity: Int) {
        this.intensity = intensity
    }

    fun getPrice(): Float {
        return price
    }

    fun setPrice(price: Float) {
        this.price = price
    }

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

    fun getEnabledItem(): Boolean {
        return enabledItem
    }

    fun setEnabledItem(enabledItem: Boolean) {
        this.enabledItem = enabledItem
    }

    fun getEnabledMinusButton(): Boolean {
        return enabledMinusButton
    }

    fun setEnabledMinusButton(enabledMinusButton: Boolean) {
        this.enabledMinusButton = enabledMinusButton
    }

    fun getIndex(): Int {
        return index
    }

    fun setIndex(index: Int) {
        this.index = index
    }
}