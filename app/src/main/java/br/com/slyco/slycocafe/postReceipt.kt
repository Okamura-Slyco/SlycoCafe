package br.com.slyco.slycocafe

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import retrofit2.Response

class postReceipt {

    val myLog = log(javaClass.simpleName)

    constructor(mySaleId: String, receiptDC: postReceiptDC){
        try {
            runBlocking {
                val result = async(Dispatchers.IO) {
                    intPostReceipt(mySaleId, receiptDC)
                }.await()
            }
        } catch(e: Exception){
            myLog.log("${e.printStackTrace().toString()}")

        }

    }
    fun intPostReceipt(mySaleId: String, receiptDC:postReceiptDC){

        val call = apiService.postReceipt(mySaleId,receiptDC)
        try {
            myLog.log("receiptDC: ${receiptDC.toString()}")
            var callReturn: Response<postReceiptResponseDC> = call.execute()
            myLog.log("POST " + callReturn.body()?.toString())
        }
        catch (e: Exception){
            myLog.log("${e.printStackTrace().toString()}")
        }
    }

}