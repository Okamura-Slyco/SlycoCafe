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
import retrofit2.http.POST
import retrofit2.http.Path

data class MAIN_VIEW_ATTRIBUTES (
    @SerializedName("plus_minus_height")
    var plusMinusHeigth: Int,
    @SerializedName("tag_alpha")
    var tagAlpha: Float,
    @SerializedName("margin_top")
    var marginTop: Int,
    @SerializedName("qty_ems")
    var qtyEms: Int,
    @SerializedName("img_size")
    var imgSize: Int,
    @SerializedName("plus_minus_width")
    var plusMinusWidth: Int,
    @SerializedName("recycler_margin_top")
    var recyclerMarginTop: Int,
    @SerializedName("plus_minus_margin")
    var plusMinusMargin: Int,
    @SerializedName("qty_margin_top")
    var qtyMarginTop: Int,
    @SerializedName("tag_icon_size")
    var tagIconSize: Int,
    @SerializedName("tag_width")
    var tagWidth: Int,
    @SerializedName("minus_tint")
    var minusTint: String,
    @SerializedName("tag_text_size")
    var tagTextSize: Int,
    @SerializedName("plus_tint")
    var plusTint: String,
    @SerializedName("plus_minus_icon_size")
    var plusMinusIconSize: Int,
    @SerializedName("tag_height")
    var tagHeight: Int,
    @SerializedName("qty_text_size")
    var qtyTextSize: Int,
    @SerializedName("layout_height")
    var layoutHeight: Int,
    @SerializedName("layout_width")
    var layoutWidth: Int,
)



data class ITEM_VIEW_COMPONENTS (
    var firstLevelLayout:Int,
    var secondLevelLayout:Int,
    var shoppingCartImage:Int,
    var shoppingCartPlusButton:Int,
    var shoppingCartMinusButton:Int,
    var shoppingCartQty: Int,
    var shoppingCartItemInfo: Int,
    var shoppingCartItemPrice: Int,
    var dialogInnerLayout: Int,
    var dialogImage: Int,
    var dialogQty: Int
)

data class PAYMENT_INTERFACE_FIELDS_NAMES (
    var integrationApp:INTEGRATION_APP = INTEGRATION_APP.NONE,
    var intentActionStr:String = "",
    var sitefMIDStr:String = "",
    var endpointStr:String = "",
    var terminalIdStr:String = "",
    var functionIdStr:String = "",
    var merchant_TIDStr:String = "",
    var amountStr:String = "",
    var restrictionStr:String = "",
    var operatorIdStr:String = "",
    var dateStr:String = "",
    var hourStr:String = "",
    var invoiceNumberStr:String = "",
    var installmentsStr:String = "",
    var otpStr:String = "",
    var enabledTransactionsStr:String = "",
    var pinpadMACStr:String = "",
    var comProtocolString:String = "",
    var doubleValidationStr:String = "",
    var isv_TIDStr:String = "",
    var van_IDStr:String = "",
    var inputTimeoutStr:String = "",
    var acessibilityStr:String = "",
    var pinpadTypeStr:String = "",
    var softDescriptorStr:String = "",
    var fieldsStr:String = "",
    var IATA_inputStr:String = "",
    var IATA_installmentInputStr:String = "",
    var clsitStr:String = "",
    var tlsToken:String = ""
)

data class PAYMENT_INTERFACE_RESPONSE_FIELDS(
    var responseCode: String = "responseCode",
    var transactionType: String = "transactionType",
    var installmentType: String = "installmentType",
    var cashbackAmount: String = "cashbackAmount",
    var acquirerId: String = "acquirerId",
    var cardBrand: String = "cardBrand",
    var sitefTransactionId: String = "sitefTransactionId",
    var hostTrasactionId: String = "hostTrasactionId",
    var authCode: String = "authCode",
    var transactionInstallments: String = "transactionInstallments",
    var merchantReceipt: String = "merchantReceipt",
    var customerReceipt: String = "customerReceipt",
    var returnedFields: String = "returnedFields"
)

enum class INTEGRATION_APP(val value: Int) {
    MSITEF (0),
    SITEF_SALES_APP (1),
    NONE (-1)
}



enum class NESPRESSOFLAVORS (val value:Int){
    NONE (0),

    RISTRETTO (R.drawable.ristretto_trn),
    RISTRETTOINTENSO(R.drawable.ristretto_intenso_trn),

    LEGGERO (R.drawable.leggero_trn),
    FORTE (R.drawable.forte_trn),
    FINEZZO (R.drawable.finezzo_trn),
    INTENSO (R.drawable.intenso_trn),
    DESCAFFEINADO (R.drawable.descafeinado_trn),

    BRAZILORGANIC (R.drawable.brasil_organic_trn),
    INDONESIA (R.drawable.indonesia_trn),
    INDIA (R.drawable.india_trn),
    GUATEMALA (R.drawable.guatemala_trn),

    CAFFENOCCIOLA (R.drawable.caffe_nocciola_trn),
    CAFFECARAMELLO (R.drawable.caffe_caramelo_trn),
    CAFFEVANILIO (R.drawable.caffe_vanilio_trn),
    BIANCOINTENSO (R.drawable.bianco_intenso_trn),
    BIANCODELICATO (R.drawable.bianco_delicato_trn);

    companion object {
        infix fun from(value: Int): NESPRESSOFLAVORS = NESPRESSOFLAVORS.values().first() { it.value == value }
    }
}

val NESPRESSOFLAVORSHASH = mapOf(
    2131230994 to NESPRESSOFLAVORS.RISTRETTO.value,
    2131230993 to NESPRESSOFLAVORS.RISTRETTOINTENSO.value,
    2131230915 to NESPRESSOFLAVORS.LEGGERO.value,
    2131230885 to NESPRESSOFLAVORS.FORTE.value,
    2131230883 to NESPRESSOFLAVORS.FINEZZO.value,
    2131230913 to NESPRESSOFLAVORS.INTENSO.value,
    2131230876 to NESPRESSOFLAVORS.DESCAFFEINADO.value,
    2131230853 to NESPRESSOFLAVORS.BRAZILORGANIC.value,
    2131230911 to NESPRESSOFLAVORS.INDONESIA.value,
    2131230910 to NESPRESSOFLAVORS.INDIA.value,
    2131230887 to NESPRESSOFLAVORS.GUATEMALA.value,
    2131230864 to NESPRESSOFLAVORS.CAFFENOCCIOLA.value,
    2131230863 to NESPRESSOFLAVORS.CAFFECARAMELLO.value,
    2131230865 to NESPRESSOFLAVORS.CAFFEVANILIO.value,
    2131230849 to NESPRESSOFLAVORS.BIANCOINTENSO.value,
    2131230848 to NESPRESSOFLAVORS.BIANCODELICATO.value)

val INVENTORYHASH = mapOf(

    NESPRESSOFLAVORS.RISTRETTO.value to 2131230994,
    NESPRESSOFLAVORS.RISTRETTOINTENSO.value to 2131230993,
    NESPRESSOFLAVORS.LEGGERO.value to 2131230915,
    NESPRESSOFLAVORS.FORTE.value to 2131230885,
    NESPRESSOFLAVORS.FINEZZO.value to 2131230883,
    NESPRESSOFLAVORS.INTENSO.value to 2131230913,
    NESPRESSOFLAVORS.DESCAFFEINADO.value to 2131230876,
    NESPRESSOFLAVORS.BRAZILORGANIC.value to 2131230853,
    NESPRESSOFLAVORS.INDONESIA.value to 2131230911,
    NESPRESSOFLAVORS.INDIA.value to 2131230910,
    NESPRESSOFLAVORS.GUATEMALA.value to 2131230887,
    NESPRESSOFLAVORS.CAFFENOCCIOLA.value to 2131230864,
    NESPRESSOFLAVORS.CAFFECARAMELLO.value to 2131230863,
    NESPRESSOFLAVORS.CAFFEVANILIO.value to 2131230865,
    NESPRESSOFLAVORS.BIANCOINTENSO.value to 2131230849,
    NESPRESSOFLAVORS.BIANCODELICATO.value to 2131230848)


data class dispenserModelDC (
    var id:Int,
    @SerializedName("model_name")
    var modelName:String,
    @SerializedName("flavors_qty")
    var flavors:Int,
    @SerializedName("capacity_per_flavor")
    var capacityPerFlavor:Int
)
data class deviceDC (
    var id:Int,
    var brand:String,
    var model:String,
    var name:String,
    @SerializedName("payment_device")
    var paymentDevice:Boolean,
    @SerializedName("pos_device")
    var posDevice:Boolean,
    @SerializedName("main_view_attributes")
    var mainViewAttributes: MAIN_VIEW_ATTRIBUTES,
    @SerializedName("screen_format")
    var screenFormat: String
)

data class merchantDC (
    var id:Int,
    var environment:String,
    @SerializedName("payment_gateway")
    var paymentGateway:String,
    @SerializedName("payment_gateway_mid")
    var paymentGatewayMid:String,
    @SerializedName("tax_id")
    var taxId:String,
    @SerializedName("payment_endpoint")
    var paymentEndpoint:String,
    @SerializedName("payment_app")
    var paymentApp:String,
    @SerializedName("tls_fiserv_token")
    var tlsFiservToken: String
)



data class ITEM(
    var flavor: NESPRESSOFLAVORS = NESPRESSOFLAVORS.NONE,
    var name: String,
    var qty: Int,
    var price: Float?,
    var size: Int?,
    var intensity: Int?

)

//@Serializable
data class saleResponseDC(

    @SerializedName("location_id")
    var locationId :String,

    @SerializedName("transaction_type")
    var transactionType: String,

    @SerializedName("installment_type")
    var installmentType: String,

    @SerializedName("cashback_amount")
    var cashbackAmount: String,

    @SerializedName("acquirer_id")
    var acquirerId: String,

    @SerializedName("card_brand")
    var cardBrand: String,

    @SerializedName("sitef_transaction_id")
    var sitefTransactionId: String,

    @SerializedName("host_transaction_id")
    var hostTrasactionId: String,

    @SerializedName("authorizer_code")
    var authCode: String,

    @SerializedName("transaction_installments")
    var transactionInstallments: String,

    var pan: String,

    @SerializedName("good_thru")
    var goodThru: String,

    @SerializedName("card_type")
    var cardType:String,

    @SerializedName("card_read_status")
    var cardReadStatus: String,

    @SerializedName("payment_source_tax_id")
    var paymentSourceTaxID:String,

    @SerializedName("invoice_brand_id")
    var invoiceBrandID:String,

    @SerializedName("invoice_number")
    var invoiceNumber:String,

    @SerializedName("if_sitef")
    var sitefIf: String,

    @SerializedName("card_brand_id")
    var cardBrandID: String,

    @SerializedName("invoice_authorization_code")
    var invoiceAuthorizationCode: String,

    @SerializedName("authorizer_response_code")
    var authorizerResponseCode: String,

    @SerializedName("authorization_code")
    var authorizationCode: String,

    @SerializedName("transaction_timestamp")
    var transactionTimestamp: Long,

    @SerializedName("authorization_network_id")
    var authorizationNetworkID: String,

    @SerializedName("merchant_id")
    var merchantID: String,

    @SerializedName("sale_items")
    var saleItems: String
)

data class ItemDC(
    val id: Int,
    val name: String,
    @SerializedName("coffee_size")
    val coffeeSize: Int,
    val intensity: Int,
    @SerializedName("recommended_price")
    val recommendedPrice: Int
)

data class inventoryStockDC(
    val item: ItemDC,
    @SerializedName("dispenser_number")
    val dispenserNumber: Int,
    val price:Int,
    var quantity:Int
)


data class patchInventoryQtyElementDC(
    val keys: patchInventoryQtyKeysDC,
    val values:  patchInventoryQtyValuesDC
)
data class patchInventoryQtyValuesDC(
    val item: Long?,
    val price: Int?,
    val qty: Int?
)
data class patchInventoryQtyKeysDC (
    val id:String,
    val dispenser_number: Int
)


interface ApiService {

    @GET("location/{id}")
    fun fetchLocation(@Path("id") inventoryId: String): Call<locationDC>

    @POST("location/{id}")
    fun postLocation(@Path("id") inventoryId: String,@Body postLocation: locationDC): Call<locationDC>

    @GET("inventory/{id}")
    fun fetchInventory(@Path("id") inventoryId: String): Call<List<inventoryStockDC>>

    @POST("inventory/{id}")
    fun postInventory(@Path("id") inventoryId: String, @Body postInventory: List<inventoryStockDC>): Call<inventoryStockDC>

    @PATCH("inventory/{id}")
    fun patchInventoryQty(@Path("id") inventoryId: String, @Body patchInventory: List<patchInventoryQtyElementDC>): Call<patchInventoryQtyElementDC>

    @POST("sale/{id}")
    fun postSale(@Path("id") inventoryId: String,@Body postInventory: saleResponseDC): Call<saleResponseDC>

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

class HeaderInterceptor : Interceptor {
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

val client = OkHttpClient.Builder()
    .addInterceptor(HeaderInterceptor())
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    })
    .build()


val retrofit: Retrofit by lazy {
    Retrofit.Builder()
        .baseUrl(br.com.slyco.slycocafe.BuildConfig.SLYCO_API_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

val apiService: ApiService by lazy {
    retrofit.create(ApiService::class.java)
}