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
    var items: List<inventoryStockDC>
)


public class location {
    private lateinit var myLocation: locationDC
    private var myLoc: String
    val mylog = log("LOCATION CLASS")

    suspend fun initializeDataAsync() {
        val deferred: Deferred<locationDC> = GlobalScope.async {
            fetchLocation()
        }
        myLocation = deferred.await()
    }

    fun getLocation() :locationDC {
        return myLocation
    }

    constructor(myLoc:String){
        this.myLoc = myLoc
        runBlocking {
            val result = async(Dispatchers.IO) {
                fetchLocation()
            }.await()
        }
    }

    fun fetchLocation() :locationDC{
        val call = apiService.getLocation(myLoc)
        var ret: locationDC? = null
        try {
            var callReturn: Response<locationDC> = call.execute()
            myLocation = callReturn.body()!!
        }
        catch (e: Exception){
            mylog.log("${e.printStackTrace().toString()}")
        }

        return myLocation
    }
}