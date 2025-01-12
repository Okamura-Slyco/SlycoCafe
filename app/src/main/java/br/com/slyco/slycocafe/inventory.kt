package br.com.slyco.slycocafe

class inventory {
    private lateinit var myItems:MutableList<ITEM>
    private val classTag = this::class.simpleName.toString()
    private var dispenserFlavors: Int = 0
    private var dispenserCapacity: Int = 0

    val myLog = log(classTag)

    constructor(items: List<inventoryStockDC>, dispenserCapacity:Int, dispenserFlavors: Int){
        this.dispenserFlavors = dispenserFlavors
        this.dispenserCapacity = dispenserCapacity
        reset(items)
    }

    fun reset(items:List<inventoryStockDC>){

        for (id in 0..dispenserFlavors-1){
            myLog.log("${id} ${items[id]}")
            val myItem = ITEM(
                NESPRESSO_FLAVORS.from(items[id].item.id),
                dispenserCapacity,
                items[id].item.recommendedPrice.toFloat()/100,
                items[id].item.coffeeSize,
                items[id].item.intensity)

            if (id == 0) myItems = mutableListOf(myItem)
            else myItems.add(myItem)
        }
        myLog.log("RESET")

    }
    fun initQty(dbItems: List<inventoryStockDC>){
        for (index in 0.. dispenserFlavors-1){
            myItems[index].qty = dbItems[index].quantity
        }
    }
    fun getFlavor (index:Int):NESPRESSO_FLAVORS? {
        return this.myItems[index]?.flavor
    }

    fun getQty(flavor: NESPRESSO_FLAVORS) : Int? {
        var myItem = myItems.find{ it?.flavor == flavor }

        return myItem?.qty
    }

    fun setQty(flavor: NESPRESSO_FLAVORS,qty:Int) {
        var myItem = myItems.find{ it?.flavor == flavor }

        myItem!!.qty = qty

    }

    fun setPrice(flavor: NESPRESSO_FLAVORS,price:Float) {
        var myItem = myItems.find{ it?.flavor == flavor }

        myItem!!.price = price
    }

    fun getPrice(flavor: NESPRESSO_FLAVORS): Float {
        var myItem = myItems.find{ it?.flavor == flavor }

        return myItem!!.price!!
    }

    fun getIntensity(flavor: NESPRESSO_FLAVORS): Int? {
        var myItem = myItems.find{ it?.flavor == flavor }

        if (myItem != null) {
            return myItem.intensity
        }
        return 0
    }

    fun getSize(flavor: NESPRESSO_FLAVORS): Int? {
        var myItem = myItems.find{ it?.flavor == flavor }

        if (myItem != null) {
            return myItem.size
        }
        return 0
    }
    fun getFlavorsQty():Int{
        return myItems.size
    }
    fun getItem(index:Int): ITEM? {
        if (index >= myItems.size) return null
        var ret = myItems[index]
        return ret
    }
}




