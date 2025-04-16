package br.com.slyco.slycocafe

import android.util.Log
import com.google.gson.annotations.SerializedName
import com.hoho.android.usbserial.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path

interface SlycoWalletApiService {

    @GET("location/{id}")
    fun fetchLocation(@Path("id") inventoryId: String): Call<locationDC>

    @PUT("location/{id}")
    fun putLocation(@Path("id") inventoryId: String,@Body putLocation: locationDC): Call<locationDC>

    @GET("inventory/{id}")
    fun fetchInventory(@Path("id") inventoryId: String): Call<List<inventoryStockDC>>

    @PUT("inventory/{id}")
    fun putInventory(@Path("id") inventoryId: String, @Body putInventory: List<inventoryStockDC>): Call<inventoryStockDC>

    @PATCH("inventory/{id}")
    fun patchInventoryQty(@Path("id") inventoryId: String, @Body patchInventory: List<patchInventoryQtyElementDC>): Call<patchInventoryQtyElementDC>

    @PUT("sale")
    fun putSale(@Body putInventory: saleResponseDC): Call<saleResponseDC>

}

//class HeaderInterceptor : Interceptor {
//    override fun intercept(chain: Interceptor.Chain): Response {
//        val request = chain.request().newBuilder()
//            .addHeader("authorization-token", br.com.slyco.slycocafe.BuildConfig.SLYCO_API_SECRET)
//            .addHeader("environment", br.com.slyco.slycocafe.BuildConfig.SLYCO_API_ENVIRONMENT)
//            .build()
//        return chain.proceed(request)
//    }
//}

class SlycoWalletHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("authorization-token", br.com.slyco.slycocafe.BuildConfig.SLYCO_API_SECRET)
            .addHeader("environment", br.com.slyco.slycocafe.BuildConfig.SLYCO_API_ENVIRONMENT)
            .build()

        val response = chain.proceed(request)

        // Log response body
        if (BuildConfig.DEBUG) {
            response.peekBody(Long.MAX_VALUE).use { peekBody ->
                Log.d("HTTP INTERCEPTOR ","Response Body: ${peekBody.string()}")
            }
        }

        return response
    }
}

val slycoWalletClient = OkHttpClient.Builder()
    .addInterceptor(HeaderInterceptor())
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    })
    .build()


val slycoWalletretrofit: Retrofit by lazy {
    Retrofit.Builder()
        .baseUrl(br.com.slyco.slycocafe.BuildConfig.SLYCO_API_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

val slycoWalletapiService: ApiService by lazy {
    retrofit.create(ApiService::class.java)
}