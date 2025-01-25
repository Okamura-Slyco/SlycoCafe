package br.com.slyco.slycocafe

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class locationDC (
    var id: String,
    var name:String,
    var address:String,
    var zip:String,
    var country:String,
    @SerializedName("dispenser_model")
    var dispenserModel:dispenserModelDC,

    @SerializedName("payment_device")
    var paymentDevice: deviceDC,
    var pos :deviceDC,
    var merchant:merchantDC,
    @SerializedName("demo_mode")
    var demoMode: Boolean,
    var items: List<inventoryStockDC>
)




public class location {
    private lateinit var myLocation: locationDC
    private var myLoc: String
    val mylog = log("LOCATION CLASS")

//    suspend fun initializeDataAsync() {
//        val deferred: Deferred<locationDC> = GlobalScope.async {
//            fetchLocation()
//        }
//        myLocation = deferred.await()
//    }

    fun getLocation() :locationDC {
        if (!::myLocation.isInitialized) {
            initMyLocationDefault()
        }
            return myLocation
    }

    constructor(myLoc:String) {
        this.myLoc = myLoc

        fetchLocation()
    }


    private fun initMyLocationDefault(){
        myLocation = locationDC(
            id = myLoc,
            name = myLoc,
            address = myLoc,
            zip = myLoc,
            country = myLoc,
            dispenserModel = dispenserModelDC(
                id = 1,
                modelName = myLoc,
                flavors = 6,
                capacityPerFlavor = 50
            ),
            paymentDevice = deviceDC(
                id = 1,
                brand = myLoc,
                model = myLoc,
                name = myLoc,
                paymentDevice = true,
                posDevice = true
            ),

            pos = deviceDC(
                id = 1,
                brand = myLoc,
                model = myLoc,
                name = myLoc,
                paymentDevice = true,
                posDevice = true
            ),
            merchant = merchantDC(
                id = 1,
                environment = "DEVELOPMENT",
                paymentGateway = "SITEF",
                paymentGatewayMid = "00000048",
                taxId = "55.833.084/0001-36"
            ),
            items = listOf(
                inventoryStockDC(
                    item = ItemDC(
                        id = 2131230994,
                        name = myLoc,
                        coffeeSize = 1,
                        intensity = 12,
                        recommendedPrice = 600
                    ),
                    dispenserNumber = 1,
                    price = 300,
                    quantity = 50
                ),
                inventoryStockDC(
                    item = ItemDC(
                        id = 2131230853,
                        name = myLoc,
                        coffeeSize = 1,
                        intensity = 12,
                        recommendedPrice = 600
                    ),
                    dispenserNumber = 2,
                    price = 300,
                    quantity = 50
                ),
                inventoryStockDC(
                    item = ItemDC(
                        id = 2131230915,
                        name = myLoc,
                        coffeeSize = 1,
                        intensity = 12,
                        recommendedPrice = 600
                    ),
                    dispenserNumber = 3,
                    price = 300,
                    quantity = 50
                ),
                inventoryStockDC(
                    item = ItemDC(
                        id = 2131230887,
                        name = myLoc,
                        coffeeSize = 1,
                        intensity = 12,
                        recommendedPrice = 600
                    ),
                    dispenserNumber = 4,
                    price = 300,
                    quantity = 50
                ),
                inventoryStockDC(
                    item = ItemDC(
                        id = 2131230865,
                        name = myLoc,
                        coffeeSize = 1,
                        intensity = 12,
                        recommendedPrice = 600
                    ),
                    dispenserNumber = 5,
                    price = 300,
                    quantity = 50
                ),
                inventoryStockDC(
                    item = ItemDC(
                        id = 2131230876,
                        name = myLoc,
                        coffeeSize = 1,
                        intensity = 12,
                        recommendedPrice = 600
                    ),
                    dispenserNumber = 6,
                    price = 300,
                    quantity = 50
                ),

                ),

            demoMode = false
        )
        val call = apiService.putLocation(myLoc,myLocation)
        try {
            var callReturn: Response<locationDC> = call.execute()
            mylog.log("PUT " + callReturn.body()!!.toString())
        }
        catch (e: Exception){
            mylog.log("${e.printStackTrace().toString()}")
        }
    }

    fun fetchLocation() {
        try {
            runBlocking {
                val result = async(Dispatchers.IO) {
                    val call = apiService.fetchLocation(myLoc)

                    try {
                        var callReturn: Response<locationDC> = call.execute()
                        if ((callReturn.body() != null) && (callReturn.code()) == 200) {
                            myLocation = callReturn.body()!!
                        } else if (callReturn.code() == 403) {
                            var callReturn: Response<locationDC> = call.execute()
                            if ((callReturn.body() != null) && (callReturn.code()) == 200) {
                                myLocation = callReturn.body()!!
                            } else {
                                initMyLocationDefault()
                            }
                        } else {
                            initMyLocationDefault()
                        }
                    } catch (e: Exception) {
                        mylog.log("${e.printStackTrace().toString()}")
                    }

                    if (!::myLocation.isInitialized) {
                        initMyLocationDefault()
                    }
                    //return myLocation
                }.await()
            }
        }
         catch(e: Exception){
            mylog.log("${e.printStackTrace().toString()}")

        }
}
}