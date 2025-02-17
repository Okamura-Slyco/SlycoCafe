package br.com.slyco.slycocafe

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import retrofit2.Response

class putSale {

    val myLog = log(javaClass.simpleName)

    constructor(mySaleId: String, mySaleResponseData:saleResponseDC){
        try {
            runBlocking {
                val result = async(Dispatchers.IO) {
                    intPutSale(mySaleId, mySaleResponseData)
                }.await()
            }
        } catch(e: Exception){
            myLog.log("${e.printStackTrace().toString()}")

        }

    }
    fun intPutSale(mySaleId: String, mySaleResponseData:saleResponseDC){

        val call = apiService.putSale(mySaleResponseData)
        try {
            myLog.log("mySaleResponseData: ${mySaleResponseData.toString()}")
            var callReturn: Response<saleResponseDC> = call.execute()
            myLog.log("PUT " + callReturn.body()?.toString())
        }
        catch (e: Exception){
            myLog.log("${e.printStackTrace().toString()}")
        }
    }
}