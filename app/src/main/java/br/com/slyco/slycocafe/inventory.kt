package br.com.slyco.slycocafe

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Response


class inventory {
    private lateinit var myItems:MutableList<ITEM>
    private val classTag = this::class.simpleName.toString()
    private var dispenserFlavors: Int = 0
    private var dispenserCapacity: Int = 0
    private lateinit var myId: String
    private lateinit var myInventory: List<inventoryStockDC>
    val myLog = log(classTag)

    constructor(android_id: String, items: List<inventoryStockDC>?, dispenserCapacity:Int, dispenserFlavors: Int){
        this.dispenserFlavors = dispenserFlavors
        this.dispenserCapacity = dispenserCapacity
        myId = android_id

        if (items != null) {
            if (items.isEmpty()){
                initMyInventoryDefault()
                putInventory(myId,myInventory)
            }
            else myInventory = items
        }
        reset(myInventory)

    }

    fun getInventory():List<inventoryStockDC>{
        return myInventory
    }

    fun reset(items:List<inventoryStockDC>){

        if (items.size > 0) {

            for (id in 0..items.size - 1) {
                myLog.log("${id} ${items[id]}")
                val myItem = ITEM(
                    NESPRESSOFLAVORS.from(NESPRESSOFLAVORSHASH.getValue(items[id].item.id)),
                    items[id].quantity,
                    items[id].price.toFloat() / 100,
                    items[id].item.coffeeSize,
                    items[id].item.intensity
                )

                if (id == 0) myItems = mutableListOf(myItem)
                else myItems.add(myItem)
            }
            myLog.log("RESET")
        }
        else {
            initMyInventoryDefault()
            putInventory(myId,myInventory)
        }

    }
    fun initQty(dbItems: List<inventoryStockDC>){
        for (index in 0.. dispenserFlavors-1){
            myItems[index].qty = dbItems[index].quantity
        }
    }
    fun getFlavor (index:Int):NESPRESSOFLAVORS? {
        return this.myItems[index]?.flavor
    }

    fun getQty(flavor: NESPRESSOFLAVORS) : Int? {
        var myItem = myItems.find{ it?.flavor == flavor }

        return myItem?.qty
    }

    fun setQty(flavor: NESPRESSOFLAVORS,qty:Int) {
        var myItem = myItems.find{ it?.flavor == flavor }

        myItem!!.qty = qty

    }

    fun setPrice(flavor: NESPRESSOFLAVORS,price:Float) {
        var myItem = myItems.find{ it?.flavor == flavor }

        myItem!!.price = price
    }

    fun getPrice(flavor: NESPRESSOFLAVORS): Float {
        var myItem = myItems.find{ it?.flavor == flavor }

        return myItem!!.price!!
    }

    fun getIntensity(flavor: NESPRESSOFLAVORS): Int? {
        var myItem = myItems.find{ it?.flavor == flavor }

        if (myItem != null) {
            return myItem.intensity
        }
        return 0
    }

    fun getSize(flavor: NESPRESSOFLAVORS): Int? {
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

    fun putInventory(id:String,items: List<inventoryStockDC>){

//        for (i in 0.. dispenserFlavors-1 ) {
//            items[i].quantity = this.getQty(NESPRESSOFLAVORS.from(NESPRESSOFLAVORSHASH.getValue(items[i].item.id)))!!
//        }

        try {
            runBlocking {
                val result = async(Dispatchers.IO) {
                    val call = apiService.putInventory(id,items)
                    try {
                        var callReturn: Response<inventoryStockDC> = call.execute()
                        myLog.log("PUT " + callReturn.body()!!.toString())
                    }
                    catch (e: Exception){
                        myLog.log("${e.printStackTrace().toString()}")
                    }
                }.await()
            }
        } catch(e: Exception){
            myLog.log("${e.printStackTrace().toString()}")

        }

    }

    fun patchtInventoryQty(id:String,items: List<inventoryStockDC>){
        var myData = mutableListOf<patchInventoryQtyElementDC>()


        for (i in 0.. dispenserFlavors-1 ) {
            var myItem = patchInventoryQtyElementDC(
                keys = patchInventoryQtyKeysDC(
                    id = id,
                    dispenser_number = items[i].dispenserNumber
                ),
                values = patchInventoryQtyValuesDC(
                    item = null,
                    price = null,
                    qty = this.getQty(NESPRESSOFLAVORS.from(NESPRESSOFLAVORSHASH.getValue(items[i].item.id)))!!
                )
            )
            myData.add(myItem)
        }

        try {
            runBlocking {
                val result = async(Dispatchers.IO) {

                    val call = apiService.patchInventoryQty(id,myData)
                    try {
                        var callReturn: Response<patchInventoryQtyElementDC> = call.execute()
                        myLog.log("PATCH " + callReturn.body()!!.toString())
                    }
                    catch (e: Exception){
                        myLog.log("${e.printStackTrace().toString()}")
                    }
                }.await()
            }
        } catch(e: Exception){
            myLog.log("${e.printStackTrace().toString()}")
        }
    }

    fun fetchInventory() {
        val call = apiService.fetchInventory(myId)

        try {
            var callReturn: Response<List<inventoryStockDC>> = call.execute()
            if (callReturn.body() != null) {
                myInventory = callReturn.body()!!
            }
            else initMyInventoryDefault()
        }
        catch (e: Exception){
            myLog.log("${e.printStackTrace().toString()}")
        }

        if (!::myInventory.isInitialized)
        {
            initMyInventoryDefault()
            try {
                runBlocking {
                    val result = async(Dispatchers.IO) {
                        putInventory(myId,myInventory)
                    }.await()
                }
            } catch(e: Exception){
                myLog.log("${e.printStackTrace().toString()}")

            }
        }
        //return myLocation
    }

    fun initMyInventoryDefault()
    {
        myInventory = listOf(
            inventoryStockDC(
                item = ItemDC(
                    id = 2131230994,
                    name = "Ristretto",
                    coffeeSize = 1,
                    intensity = 9,
                    recommendedPrice = 600
                ),
                dispenserNumber = 1,
                price = 600,
                quantity = 50
            ),
            inventoryStockDC(
                item = ItemDC(
                    id = 2131230853,
                    name = "Brazil Organic",
                    coffeeSize = 3,
                    intensity = 4,
                    recommendedPrice = 600
                ),
                dispenserNumber = 2,
                price = 600,
                quantity = 50
            ),
            inventoryStockDC(
                item = ItemDC(
                    id = 2131230915,
                    name = "Leggero",
                    coffeeSize = 2,
                    intensity = 6,
                    recommendedPrice = 600
                ),
                dispenserNumber = 3,
                price = 600,
                quantity = 50
            ),
            inventoryStockDC(
                item = ItemDC(
                    id = 2131230887,
                    name = "Guatemala",
                    coffeeSize = 3,
                    intensity = 6,
                    recommendedPrice = 600
                ),
                dispenserNumber = 4,
                price = 600,
                quantity = 50
            ),
            inventoryStockDC(
                item = ItemDC(
                    id = 2131230865,
                    name = "Caffe Vanilio",
                    coffeeSize = 2,
                    intensity = 6,
                    recommendedPrice = 600
                ),
                dispenserNumber = 5,
                price = 600,
                quantity = 50
            ),
            inventoryStockDC(
                item = ItemDC(
                    id = 2131230876,
                    name = "Descaffeinado",
                    coffeeSize = 2,
                    intensity = 7,
                    recommendedPrice = 600
                ),
                dispenserNumber = 6,
                price = 600,
                quantity = 50
            )
        )
    }
}



