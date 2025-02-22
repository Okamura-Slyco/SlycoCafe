package br.com.slyco.slycocafe

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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




class location(private var myLoc: String, private var myBrand:String="", private var myModel:String="") {
    private lateinit var myLocation: locationDC
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

    init {
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
                brand = myBrand,
                model = myModel,
                name = "${myBrand} ${myModel}",
                paymentDevice = true,
                posDevice = true
            ),

            pos = deviceDC(
                id = 1,
                brand = myBrand,
                model = myModel,
                name = "${myBrand} ${myModel}",
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
                    price = 600,
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
                    price = 600,
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
                    price = 600,
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
                    price = 600,
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
                    price = 600,
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
                    price = 600,
                    quantity = 50
                ),

                ),

            demoMode = false
        )
        val call = apiService.putLocation(myLoc,myLocation)
        try {
            val callReturn: Response<locationDC> = call.execute()
            mylog.log("PUT " + callReturn.body()!!.toString())
        }
        catch (e: Exception){
            mylog.log(e.printStackTrace().toString())
        }
    }

    fun fetchLocation(maxRetries: Int = 20, initialDelayMs: Long = 1000) {

        try {
            runBlocking {
                val result = async(Dispatchers.IO) {
                    var currentRetry = 0
                    var currentDelay = initialDelayMs

                    while (currentRetry <= maxRetries) {
                        try {
                            mylog.log("fetchLocation: try $currentRetry ($currentDelay)")
                            val call = apiService.fetchLocation(myLoc)
                            val callReturn: Response<locationDC> = call.execute()

                            when (callReturn.code()) {
                                200 -> {
                                    if (callReturn.body() != null) {
                                        myLocation = callReturn.body()!!
                                        return@async // Success, exit the retry loop
                                    }
                                }
                                403 -> {
                                    // Handle 403 case with one immediate retry
                                    val retryCall = apiService.fetchLocation(myLoc)
                                    val retryReturn: Response<locationDC> = retryCall.execute()
                                    if (retryReturn.code() == 200 && retryReturn.body() != null) {
                                        myLocation = retryReturn.body()!!
                                        return@async // Success, exit the retry loop
                                    }
                                }
                            }

                            // If we reach here, the call wasn't successful
                            if ((currentRetry < maxRetries) && (callReturn.code() != 404)) {
                                delay(currentDelay)
                                currentDelay *= 2 // Exponential backoff
                                currentRetry++
                            } else {
                                // Max retries reached, use default
                                initMyLocationDefault()
                                return@async
                            }

                        } catch (e: Exception) {
                            mylog.log("Attempt ${currentRetry + 1} failed: ${e.message}")
                            if (currentRetry >= maxRetries) {
                                initMyLocationDefault()
                                return@async
                            }
                            delay(currentDelay)
                            currentDelay *= 2 // Exponential backoff
                            currentRetry++
                        }
                    }

                    // Ensure myLocation is initialized
                    if (!::myLocation.isInitialized) {
                        initMyLocationDefault()
                    }
                }.await()
            }
        } catch (e: Exception) {
            mylog.log("${e.printStackTrace()}")
            initMyLocationDefault()
        }
    }
}