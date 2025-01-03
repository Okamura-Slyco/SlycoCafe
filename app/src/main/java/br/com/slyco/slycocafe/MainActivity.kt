
package br.com.slyco.slycocafe

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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

enum class INTEGRATION_APP(val value: Int) {
    MSITEF (0),
    SITEF_SALES_APP (1),
    NONE (-1)
}

enum class NESPRESSO_FLAVORS (val value:Int){
    NONE (0),

    RISTRETTO (R.drawable.ristretto_trn),
    RISTRETTO_INTENSO(R.drawable.ristretto_intenso_trn),

    LEGGERO (R.drawable.leggero_trn),
    FORTE (R.drawable.forte_trn),
    FINEZZO (R.drawable.finezzo_trn),
    INTENSO (R.drawable.intenso_trn),
    DESCAFFEINADO (R.drawable.descafeinado_trn),

    BRAZIL_ORGANIC (R.drawable.brasil_organic_trn),
    INDONESIA (R.drawable.indonesia_trn),
    INDIA (R.drawable.india_trn),
    GUATEMALA (R.drawable.guatemala_trn),

    CAFFE_NOCCIOLA (R.drawable.caffe_nocciola_trn),
    CAFFE_CARAMELLO (R.drawable.caffe_caramelo_trn),
    CAFFE_VANILIO (R.drawable.caffe_vanilio_trn),
    BIANCO_INTENSO (R.drawable.intenso_trn),
    BIANCO_DELICATO (R.drawable.bianco_delicato_trn);

    companion object {
        infix fun from(value: Int): NESPRESSO_FLAVORS? = NESPRESSO_FLAVORS.values().firstOrNull { it.value == value }
    }
}

data class ITEM(
    var flavor: NESPRESSO_FLAVORS = NESPRESSO_FLAVORS.NONE,
    var qty: Int?,
    var price: Float?,
    var size: Int?,
    var intensity: Int?

)

class inventory {
    private var myItems = arrayOfNulls<ITEM>(AppConstants.NESPRESSO_FLAVORS_QTY)

    constructor (){
        reset()
    }

    fun reset(){
        // TODO: Get parameterization from cloud database
        myItems = arrayOf(
            ITEM(NESPRESSO_FLAVORS.RISTRETTO,AppConstants.MAX_DISPENSER_CAPACITY,3.0f,1,9),
            ITEM(NESPRESSO_FLAVORS.RISTRETTO_INTENSO,AppConstants.MAX_DISPENSER_CAPACITY,3.0f,1,12),

            ITEM(NESPRESSO_FLAVORS.LEGGERO,AppConstants.MAX_DISPENSER_CAPACITY,3.0f,2,6),
            ITEM(NESPRESSO_FLAVORS.FORTE,AppConstants.MAX_DISPENSER_CAPACITY,3.0f,2,7),
            ITEM(NESPRESSO_FLAVORS.FINEZZO,AppConstants.MAX_DISPENSER_CAPACITY,3.0f,3,5),
            ITEM(NESPRESSO_FLAVORS.INTENSO,AppConstants.MAX_DISPENSER_CAPACITY,3.0f,3,8),
            ITEM(NESPRESSO_FLAVORS.DESCAFFEINADO,AppConstants.MAX_DISPENSER_CAPACITY,3.0f,2,7),

            ITEM(NESPRESSO_FLAVORS.BRAZIL_ORGANIC,AppConstants.MAX_DISPENSER_CAPACITY,3.0f,3,4),
            ITEM(NESPRESSO_FLAVORS.INDONESIA,AppConstants.MAX_DISPENSER_CAPACITY,3.0f,3,9),
            ITEM(NESPRESSO_FLAVORS.INDIA,AppConstants.MAX_DISPENSER_CAPACITY,3.0f,3,10),
            ITEM(NESPRESSO_FLAVORS.GUATEMALA,AppConstants.MAX_DISPENSER_CAPACITY,3.0f,3,6),

            ITEM(NESPRESSO_FLAVORS.CAFFE_NOCCIOLA,AppConstants.MAX_DISPENSER_CAPACITY,3.0f,2,6),
            ITEM(NESPRESSO_FLAVORS.CAFFE_CARAMELLO,AppConstants.MAX_DISPENSER_CAPACITY,3.0f,2,5),
            ITEM(NESPRESSO_FLAVORS.CAFFE_VANILIO,AppConstants.MAX_DISPENSER_CAPACITY,3.0f,2,6),
            ITEM(NESPRESSO_FLAVORS.BIANCO_INTENSO,AppConstants.MAX_DISPENSER_CAPACITY,3.0f,2,6),
            ITEM(NESPRESSO_FLAVORS.BIANCO_DELICATO,AppConstants.MAX_DISPENSER_CAPACITY,3.0f,2,4)
    )
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
}



class shoppingCart {
    private var itens = arrayOfNulls<ITEM>(AppConstants.DISPENSERS_QTY)
    private var total = 0.0

    private val customDateFormat: String
        get() = SimpleDateFormat("yyMMdd", Locale.ROOT).format(Date())
    private val customTimeFormat: String
        get() = SimpleDateFormat("HHmmss",Locale.ROOT).format(Date())

    val displayFlavors = arrayOf(
        NESPRESSO_FLAVORS.RISTRETTO,
        NESPRESSO_FLAVORS.BRAZIL_ORGANIC,
        NESPRESSO_FLAVORS.LEGGERO,
        NESPRESSO_FLAVORS.GUATEMALA,
        NESPRESSO_FLAVORS.CAFFE_VANILIO,
        NESPRESSO_FLAVORS.DESCAFFEINADO)

    constructor(inventory: inventory) {

        for (i in 0..<AppConstants.DISPENSERS_QTY)
            this.itens[i] = ITEM(
                displayFlavors[i],
                0,
                inventory.getPrice(displayFlavors[i]),
                inventory.getSize(displayFlavors[i]),
                inventory.getIntensity(displayFlavors[i]))
    }

    fun calculateTotal() {
        this.total = 0.0
        for (item in itens) {
            this.total += item!!.qty!! * item.price!!
        }

        Log.i("total", "${total}")
    }

    fun addItemToCart(item: NESPRESSO_FLAVORS, qty: Int, inventory: inventory) :Int{
        var myItem = itens.find { it?.flavor == item }
        var myQty = myItem!!.qty!! + qty
        if (myQty >= 0) {
            if (myQty <= inventory.getQty(item)!!) {
                myItem!!.qty = myQty
            }
            else{
                return -1
            }
        }
        else{
            return -2
        }
        this.calculateTotal()
        Log.i("teste", "${item} ${qty}")
        return 0
    }

    fun getCartItemQuantity(flavor: NESPRESSO_FLAVORS,index:Int = 0): Int {
        if (flavor != NESPRESSO_FLAVORS.NONE) {
            var myItem = itens.find { it?.flavor == flavor }
            return myItem!!.qty!!
        }
        return itens[index]!!.qty!!
    }

    fun getFlavor (id: Int): NESPRESSO_FLAVORS {
        return itens[id]?.flavor ?: NESPRESSO_FLAVORS.NONE
    }

    fun clearCart() {
        for (item in itens) {
            item!!.qty = 0
        }
        this.calculateTotal()
    }

    fun returnTotal(): Double {

        return total
    }
    fun returnSubTotal(flavor: NESPRESSO_FLAVORS): Double {
        var myItem = itens.find { it?.flavor == flavor }
        if ((myItem!!.qty != null) && (myItem!!.price != null))
            return (myItem!!.qty!!.toDouble() * myItem!!.price!!.toDouble())
        return 0.00
    }

}



fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}


class MainActivity<Bitmap> : AppCompatActivity() {
    var inventory : inventory = inventory()

    lateinit var paymentInterfaceFieldNames: PAYMENT_INTERFACE_FIELDS_NAMES
    var paymentParameters :PAYMENT_INTERFACE_FIELDS_NAMES = PAYMENT_INTERFACE_FIELDS_NAMES()

    lateinit var shoppingCart : shoppingCart
    var easterEgg = 0
    var easterEgg1 = 0
    var easterEgg2 = 0
    var easterEgg3 = 0
    var demoMode = false

    var integrationApp:INTEGRATION_APP = INTEGRATION_APP.NONE

    private lateinit var watchDog: Handler

    private var serial: String? = null

    private var dialogElements = arrayOfNulls<ITEM_VIEW_COMPONENTS>(AppConstants.DISPENSERS_QTY)

//    private var TAG = "GetEmployeeExample"
//    private var mEmployeeConnector: EmployeeConnector? = null
//    private var account: Account? = null
    fun hideActionBar(){
        val actionBar: androidx.appcompat.app.ActionBar? = supportActionBar
        if (actionBar != null) actionBar.hide()
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    
    }


    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        hideActionBar()
        super.onActivityReenter(resultCode, data)
    }
    override fun onResume() {
        hideActionBar()

        super.onResume()
    }

    override fun onStart() {
        hideActionBar()
        super.onStart()
    }
    override fun onRestart(){
        hideActionBar()
        super.onRestart()
    }

    fun initDialogElements() {
        dialogElements[0] = ITEM_VIEW_COMPONENTS(
            R.id.g1,
            R.id.i1g1,
            R.id.imageViewCapsula1,
            R.id.floatingActionButtonItem1Plus,
            R.id.floatingActionButtonItem1Minus,
            R.id.editTextNumberItem1,
            R.id.textViewAttributes1,
            R.id.textViewPrice1,
            R.id.frameLayout1,
            R.id.imageViewDialog1,
            R.id.qtyTextView1)

        dialogElements[1] = ITEM_VIEW_COMPONENTS(
            R.id.g1,
            R.id.i2g1,
            R.id.imageViewCapsula2,
            R.id.floatingActionButtonItem2Plus,
            R.id.floatingActionButtonItem2Minus,
            R.id.editTextNumberItem2,
            R.id.textViewAttributes2,
            R.id.textViewPrice2,
            R.id.frameLayout2,
            R.id.imageViewDialog2,
            R.id.qtyTextView2)

        dialogElements[2] = ITEM_VIEW_COMPONENTS(
            R.id.g1,
            R.id.i3g1,
            R.id.imageViewCapsula3,
            R.id.floatingActionButtonItem3Plus,
            R.id.floatingActionButtonItem3Minus,
            R.id.editTextNumberItem3,
            R.id.textViewAttributes3,
            R.id.textViewPrice3,
            R.id.frameLayout3,
            R.id.imageViewDialog3,
            R.id.qtyTextView3)

        dialogElements[3] = ITEM_VIEW_COMPONENTS(
            R.id.g2,
            R.id.i1g2,
            R.id.imageViewCapsula4,
            R.id.floatingActionButtonItem4Plus,
            R.id.floatingActionButtonItem4Minus,
            R.id.editTextNumberItem4,
            R.id.textViewAttributes4,
            R.id.textViewPrice4,
            R.id.frameLayout4,
            R.id.imageViewDialog4,
            R.id.qtyTextView4)

        dialogElements[4] = ITEM_VIEW_COMPONENTS(
            R.id.g2,
            R.id.i1g2,
            R.id.imageViewCapsula5,
            R.id.floatingActionButtonItem5Plus,
            R.id.floatingActionButtonItem5Minus,
            R.id.editTextNumberItem5,
            R.id.textViewAttributes5,
            R.id.textViewPrice5,
            R.id.frameLayout5,
            R.id.imageViewDialog5,
            R.id.qtyTextView5)

        dialogElements[5] = ITEM_VIEW_COMPONENTS(
            R.id.g2,
            R.id.i3g2,
            R.id.imageViewCapsula6,
            R.id.floatingActionButtonItem6Plus,
            R.id.floatingActionButtonItem6Minus,
            R.id.editTextNumberItem6,
            R.id.textViewAttributes6,
            R.id.textViewPrice6,
            R.id.frameLayout6,
            R.id.imageViewDialog6,
            R.id.qtyTextView6)

    }

    fun activate_msitef(){


        var activationIntent = Intent ("br.com.bin.ACTION_SITEF_ACTIVATION")
        if (isCallable(Intent(activationIntent))){
            activationIntent.putExtra("ACTIVATION_MODE",1)
            startActivityForResult(activationIntent,99)
            Log.d ("Smart Fiserv", "m-SiTef Activation")
            toast("m-SiTef Activation")
        }
        else {
            Log.d ("Smart Fiserv", "m-SiTef Activation not found")

            toast("m-SiTef Activation not found")

        }
    }

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {

        shoppingCart = shoppingCart(inventory)
        hideActionBar()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d ("DeviceInfo","Name: ${DeviceInfoModule.deviceName}  Brand: ${DeviceInfoModule.deviceBrand}    Model: ${DeviceInfoModule.deviceModel}")

        var viewLayout = R.layout.activity_main

        if ((DeviceInfoModule.deviceBrand == "Clover") && (DeviceInfoModule.deviceModel == "C305")) {
            Log.d ("Dettected Device","Clover Mini")
            viewLayout = R.layout.activity_main
        }
        else if (((DeviceInfoModule.deviceBrand == "ingenico") && (DeviceInfoModule.deviceModel == "DX8000") )||
            ((DeviceInfoModule.deviceBrand == "SUNMI") && (DeviceInfoModule.deviceModel == "P2-A11"))) {

            Log.d ("Dettected Device","Smart Terminal")
            viewLayout = R.layout.activity_main_smart_terminal
        }
        else if ((DeviceInfoModule.deviceBrand == "Gertec") && (DeviceInfoModule.deviceModel == "SK-210") ) {
            viewLayout = R.layout.activity_main_smart_terminal
            paymentParameters.pinpadTypeStr="ANDROID_USB"
        }

        if (isCallable(Intent("com.fiserv.sitef.action.TRANSACTION"))){
            Log.d("TRANSACTION","SiTef Sales App")
            paymentInterfaceFieldNames = PAYMENT_INTERFACE_FIELDS_NAMES(
                INTEGRATION_APP.SITEF_SALES_APP,
                "com.fiserv.sitef.action.TRANSACTION",
                "",
                "",
                "",
                "functionId",
                "",
                "transactionAmount",
                "functionAdditionalParameters",
                "tenderOperator",
                "invoiceDate",
                "invoiceTime",
                "invoiceNumber",
                "transactionInstallments",
                "",
                "enabledTransactions",
                "",
                "",
                "",
                "isvTaxId",
                "",
                "",
                "",
                "",
                "subAcquirerParameters",
                "autoFields",
                "",
                "",
                "",
                ""
            )
        }
        else if (isCallable(Intent("br.com.softwareexpress.sitef.msitef.ACTIVITY_CLISITEF"))
        ){
            paymentInterfaceFieldNames = PAYMENT_INTERFACE_FIELDS_NAMES(
                INTEGRATION_APP.MSITEF,
                "br.com.softwareexpress.sitef.msitef.ACTIVITY_CLISITEF",
                "empresaSitef",
                "enderecoSitef",
                "terminalSitef",
                "modalidade",
                "CNPJ_CPF",
                "valor",
                "restricoes",
                "operador",
                "Data",
                "Hora",
                "numeroCupom",
                "numParcelas",
                "Otp",
                "transacoesHabilitadas",
                "pinpadMac",
                "comExterna",
                "isDoubleValidation",
                "cnpj_automacao",
                "cnpj_facilitador",
                "timeoutColeta",
                "acessibilidadeVisual",
                "tipoPinpad",
                "dadosSubAdqui",
                "tipoCampos",
                "habilitaColetaTaxaEmbarqueIATA",
                "habilitaColetaValorEntradaIATA",
                "clsit",
                "tokenRegistroTls"
            )
            Log.d("TRANSACTION","m-SiTef")
        }
        else {
            Log.d("TRANSACTION","none")
        }


        setContentView(viewLayout)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        initDialogElements()

        updatePriceTags()


        for (i in 0..< AppConstants.DISPENSERS_QTY){
            var image = findViewById<ImageView>(dialogElements[i]!!.shoppingCartImage)
            image.setImageResource(shoppingCart.getFlavor(i)!!.value)
            image.setOnClickListener(listener)

            var button = findViewById<MaterialButton>(dialogElements[i]!!.shoppingCartPlusButton)
            button.setOnClickListener(listener)

            button = findViewById<MaterialButton>(dialogElements[i]!!.shoppingCartMinusButton)
            button.setOnClickListener(listener)

            button = findViewById<MaterialButton>(dialogElements[i]!!.shoppingCartItemPrice)
            button.setOnClickListener(listener)

        }

        var productImage = findViewById<ImageView>(R.id.helpButton)
        productImage.setOnClickListener(listener)

        var button1 = findViewById<Button>(R.id.buttonEmpty)
        button1.setOnClickListener(listener)
        button1 = findViewById<Button>(R.id.buttonCheckout)
        button1.setOnClickListener(listener)

        var text1 = findViewById<TextView>(R.id.textViewTotalFix)
        text1.setOnClickListener(listener)

        watchDog = Handler(Looper.getMainLooper())

        resetWatchDog()

        updateView(0)

    }

    private val watchDogCallback = Runnable {
        val intent: Intent = Intent(this, ScreenSaver::class.java)
        if (shoppingCart.returnTotal() == 0.0) intent.putExtra("activateContinueButton", 0)
        else intent.putExtra("activateContinueButton", 1)
        startActivityForResult(intent, 3)
    }

    private fun disableWatchdog() {
        watchDog.removeCallbacks(watchDogCallback)
    }

    private fun resetWatchDog(n:Int = 1) {
        disableWatchdog()
        watchDog.postDelayed(watchDogCallback, n* AppConstants.INACTIVITY_TIMEOUT)
    }

    private fun setAlphaForAllChildren(layout: ConstraintLayout, alpha: Float) {
        for (i in 0 until layout.childCount) {
            val child = layout.getChildAt(i)
            child.alpha = alpha
        }
    }

    fun callPaymentApp(transactionParameters:PAYMENT_INTERFACE_FIELDS_NAMES) {
        // Handle positive button click

        hideActionBar()


        if (this.demoMode == false) {
            try {
                PAYMENT_INTERFACE_FIELDS_NAMES(intentActionStr = paymentInterfaceFieldNames.intentActionStr)
                val intent: Intent = Intent(paymentInterfaceFieldNames.intentActionStr)

                Log.d ("Action","Intent ${paymentInterfaceFieldNames.intentActionStr}")

                if ((paymentInterfaceFieldNames.sitefMIDStr != "") && (transactionParameters.sitefMIDStr != "")) { intent.putExtra(paymentInterfaceFieldNames.sitefMIDStr, transactionParameters.sitefMIDStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.sitefMIDStr}:${transactionParameters.sitefMIDStr}")}

                if ((paymentInterfaceFieldNames.endpointStr != "") && (transactionParameters.endpointStr != "")) { intent.putExtra(paymentInterfaceFieldNames.endpointStr, transactionParameters.endpointStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.endpointStr}:${transactionParameters.endpointStr}")}

                if ((paymentInterfaceFieldNames.terminalIdStr != "") && (transactionParameters.terminalIdStr != "")) { intent.putExtra(paymentInterfaceFieldNames.terminalIdStr, transactionParameters.terminalIdStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.terminalIdStr}:${transactionParameters.terminalIdStr}")}

                if ((paymentInterfaceFieldNames.functionIdStr != "") && (transactionParameters.functionIdStr != "")) { intent.putExtra(paymentInterfaceFieldNames.functionIdStr, transactionParameters.functionIdStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.functionIdStr}:${transactionParameters.functionIdStr}")}

                if ((paymentInterfaceFieldNames.merchant_TIDStr != "") && (transactionParameters.merchant_TIDStr != "")) { intent.putExtra(paymentInterfaceFieldNames.merchant_TIDStr, transactionParameters.merchant_TIDStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.merchant_TIDStr}:${transactionParameters.merchant_TIDStr}")}

                if ((paymentInterfaceFieldNames.amountStr != "") && (transactionParameters.amountStr != "")) { intent.putExtra(paymentInterfaceFieldNames.amountStr, transactionParameters.amountStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.amountStr}:${transactionParameters.amountStr}")}

                if ((paymentInterfaceFieldNames.restrictionStr != "") && (transactionParameters.restrictionStr != "")) { intent.putExtra(paymentInterfaceFieldNames.restrictionStr, transactionParameters.restrictionStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.restrictionStr}:${transactionParameters.restrictionStr}")}

                if ((paymentInterfaceFieldNames.operatorIdStr != "") && (transactionParameters.operatorIdStr != "")) { intent.putExtra(paymentInterfaceFieldNames.operatorIdStr, transactionParameters.operatorIdStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.operatorIdStr}:${transactionParameters.operatorIdStr}")}

                if ((paymentInterfaceFieldNames.dateStr != "") && (transactionParameters.dateStr != "")) { intent.putExtra(paymentInterfaceFieldNames.dateStr, transactionParameters.dateStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.dateStr}:${transactionParameters.dateStr}")}

                if ((paymentInterfaceFieldNames.hourStr != "") && (transactionParameters.hourStr != "")) { intent.putExtra(paymentInterfaceFieldNames.hourStr, transactionParameters.hourStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.hourStr}:${transactionParameters.hourStr}")}

                if ((paymentInterfaceFieldNames.invoiceNumberStr != "") && (transactionParameters.invoiceNumberStr != "")) { intent.putExtra(paymentInterfaceFieldNames.invoiceNumberStr, transactionParameters.invoiceNumberStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.invoiceNumberStr}:${transactionParameters.invoiceNumberStr}")}

                if ((paymentInterfaceFieldNames.installmentsStr != "") && (transactionParameters.installmentsStr != "")) { intent.putExtra(paymentInterfaceFieldNames.installmentsStr, transactionParameters.installmentsStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.installmentsStr}:${transactionParameters.installmentsStr}")}

                if ((paymentInterfaceFieldNames.otpStr != "") && (transactionParameters.otpStr != "")) { intent.putExtra(paymentInterfaceFieldNames.otpStr, transactionParameters.otpStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.otpStr}:${transactionParameters.otpStr}")}

                if ((paymentInterfaceFieldNames.enabledTransactionsStr != "") && (transactionParameters.enabledTransactionsStr != "")) { intent.putExtra(paymentInterfaceFieldNames.enabledTransactionsStr, transactionParameters.enabledTransactionsStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.enabledTransactionsStr}:${transactionParameters.enabledTransactionsStr}")}

                if ((paymentInterfaceFieldNames.pinpadMACStr != "") && (transactionParameters.pinpadMACStr != "")) { intent.putExtra(paymentInterfaceFieldNames.pinpadMACStr, transactionParameters.pinpadMACStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.pinpadMACStr}:${transactionParameters.pinpadMACStr}")}

                if ((paymentInterfaceFieldNames.comProtocolString != "") && (transactionParameters.comProtocolString != "")) { intent.putExtra(paymentInterfaceFieldNames.comProtocolString, transactionParameters.comProtocolString); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.comProtocolString}:${transactionParameters.comProtocolString}")}

                if ((paymentInterfaceFieldNames.doubleValidationStr != "") && (transactionParameters.doubleValidationStr != "")) { intent.putExtra(paymentInterfaceFieldNames.doubleValidationStr, transactionParameters.doubleValidationStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.doubleValidationStr}:${transactionParameters.doubleValidationStr}")}

                if ((paymentInterfaceFieldNames.isv_TIDStr != "") && (transactionParameters.isv_TIDStr != "")) { intent.putExtra(paymentInterfaceFieldNames.isv_TIDStr, transactionParameters.isv_TIDStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.isv_TIDStr}:${transactionParameters.isv_TIDStr}")}

                if ((paymentInterfaceFieldNames.van_IDStr != "") && (transactionParameters.van_IDStr != "")) { intent.putExtra(paymentInterfaceFieldNames.van_IDStr, transactionParameters.van_IDStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.van_IDStr}:${transactionParameters.van_IDStr}")}

                if ((paymentInterfaceFieldNames.inputTimeoutStr != "") && (transactionParameters.inputTimeoutStr != "")) { intent.putExtra(paymentInterfaceFieldNames.inputTimeoutStr, transactionParameters.inputTimeoutStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.inputTimeoutStr}:${transactionParameters.inputTimeoutStr}")}

                if ((paymentInterfaceFieldNames.acessibilityStr != "") && (transactionParameters.acessibilityStr != "")) { intent.putExtra(paymentInterfaceFieldNames.acessibilityStr, transactionParameters.acessibilityStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.acessibilityStr}:${transactionParameters.acessibilityStr}")}

                if ((paymentInterfaceFieldNames.pinpadTypeStr != "") && (transactionParameters.pinpadTypeStr != "")) { intent.putExtra(paymentInterfaceFieldNames.pinpadTypeStr, transactionParameters.pinpadTypeStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.pinpadTypeStr}:${transactionParameters.pinpadTypeStr}")}

                if ((paymentInterfaceFieldNames.softDescriptorStr != "") && (transactionParameters.softDescriptorStr != "")) { intent.putExtra(paymentInterfaceFieldNames.softDescriptorStr, transactionParameters.softDescriptorStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.softDescriptorStr}:${transactionParameters.softDescriptorStr}")}

                if ((paymentInterfaceFieldNames.fieldsStr != "") && (transactionParameters.fieldsStr != "")) { intent.putExtra(paymentInterfaceFieldNames.fieldsStr, transactionParameters.fieldsStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.fieldsStr}:${transactionParameters.fieldsStr}")}

                if ((paymentInterfaceFieldNames.IATA_inputStr != "") && (transactionParameters.IATA_inputStr != "")) { intent.putExtra(paymentInterfaceFieldNames.IATA_inputStr, transactionParameters.IATA_inputStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.IATA_inputStr}:${transactionParameters.IATA_inputStr}")}

                if ((paymentInterfaceFieldNames.IATA_installmentInputStr != "") && (transactionParameters.IATA_installmentInputStr != "")) { intent.putExtra(paymentInterfaceFieldNames.IATA_installmentInputStr, transactionParameters.IATA_installmentInputStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.IATA_installmentInputStr}:${transactionParameters.IATA_installmentInputStr}")}

                if ((paymentInterfaceFieldNames.clsitStr != "") && (transactionParameters.clsitStr != "")) { intent.putExtra(paymentInterfaceFieldNames.clsitStr, transactionParameters.clsitStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.clsitStr}:${transactionParameters.clsitStr}")}

                disableWatchdog()

                startActivityForResult(intent, 1)

                toast("Call SiTef Sales App")
            }
            catch (e:Exception) {
                toast("Call m-SiTef")
            }
        } else {
            releaseCoffee()
            shoppingCart.clearCart()
            updateView(0)
            Log.d("DemoMode", "shoppingCart.clearCart()")
        }
    }

    fun addItemToDialog(index: Int, dialogView: View) {
        val flavor = shoppingCart.getFlavor(index)
        val quantity = flavor?.let { shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.NONE,index) }
        if (quantity != null) {
            var image = dialogElements[index]?.let { dialogView?.findViewById<ImageView>(it.dialogImage) }
            image?.setImageResource(flavor.value)
            var text = dialogElements[index]?.let { dialogView?.findViewById<TextView>(it.dialogQty) }
            text?.text = shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.NONE,index).toString()
            if (quantity >= 1) {
                setAlphaForAllChildren(dialogView.findViewById<ConstraintLayout>(dialogElements[index]!!.dialogInnerLayout),AppConstants.ON_STOCK_ALPHA_FLOAT)
            }
            else {
                setAlphaForAllChildren(dialogView.findViewById<ConstraintLayout>(dialogElements[index]!!.dialogInnerLayout),AppConstants.OUT_OF_STOCK_ALPHA_FLOAT)
            }
        }
        var totalText = dialogView.findViewById<TextView>(R.id.totalAmountTextView)
        totalText.text = String.format("%.2f",shoppingCart.returnTotal())

    }

    private fun isCallable(intent: Intent): Boolean {
        val list = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        return list.size > 0
    }

    val listener= View.OnClickListener { view ->
        var res:Int = 0
        var bUpdateView = true
        resetWatchDog()
        hideActionBar()
        when (view.getId()) {

            dialogElements[0]?.shoppingCartPlusButton, dialogElements[0]?.shoppingCartImage, dialogElements[0]?.shoppingCartItemPrice, dialogElements[0]?.shoppingCartItemInfo -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(0),1, inventory)
                easterEgg = 0
                if (easterEgg1 == 0) easterEgg1 = 1
                else easterEgg1 = 0

                if (easterEgg2 == 0) easterEgg2 = 1
                else easterEgg2 = 0
                easterEgg3 = 0
            }
            dialogElements[1]?.shoppingCartPlusButton, dialogElements[1]?.shoppingCartImage, dialogElements[1]?.shoppingCartItemPrice, dialogElements[1]?.shoppingCartItemInfo -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(1),1, inventory)
                easterEgg = 0
                if (easterEgg1 == 1) easterEgg1 = 2
                else easterEgg1 = 0

                easterEgg2 = 0
                easterEgg3 = 0
            }
            dialogElements[2]?.shoppingCartPlusButton, dialogElements[2]?.shoppingCartImage, dialogElements[2]?.shoppingCartItemPrice, dialogElements[2]?.shoppingCartItemInfo -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(2),1, inventory)
                easterEgg = 0
                if (easterEgg1 == 2) easterEgg1 = 3
                else easterEgg1 = 0

                if (easterEgg2 == 1) easterEgg2 = 2
                else easterEgg2 = 0
                easterEgg3 = 0
            }
            dialogElements[3]?.shoppingCartPlusButton, dialogElements[3]?.shoppingCartImage, dialogElements[3]?.shoppingCartItemPrice, dialogElements[3]?.shoppingCartItemInfo -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(3),1, inventory)
                easterEgg = 0
                if (easterEgg1 == 3) easterEgg1 = 4
                else easterEgg1 = 0

                easterEgg2 = 0
                easterEgg3 = 0
            }
            dialogElements[4]?.shoppingCartPlusButton, dialogElements[4]?.shoppingCartImage, dialogElements[4]?.shoppingCartItemPrice, dialogElements[4]?.shoppingCartItemInfo -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(4),1, inventory)
                easterEgg = 0
                if (easterEgg1 == 4) easterEgg1 = 5
                else easterEgg1 = 0

                if (easterEgg2 == 2) easterEgg2 = 3
                else easterEgg2 = 0
                easterEgg3 = 0

            }

            dialogElements[5]?.shoppingCartPlusButton, dialogElements[5]?.shoppingCartImage, dialogElements[5]?.shoppingCartItemPrice, dialogElements[5]?.shoppingCartItemInfo -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(5),1, inventory)
                easterEgg = 0
                if (easterEgg1 == 5) easterEgg1 = 6
                else easterEgg1 = 0

                easterEgg2 = 0
                easterEgg3 = 0
            }

            dialogElements[0]?.shoppingCartMinusButton -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(0),-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 6) easterEgg1 = 7
                else easterEgg1 = 0
                easterEgg2 = 0
                easterEgg3 = 1
            }
            dialogElements[1]?.shoppingCartMinusButton -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(1),-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 7) easterEgg1 = 8
                else easterEgg1 = 0
                easterEgg2 = 0
                if (easterEgg3 == 2) easterEgg3 = 3
            }
            dialogElements[2]?.shoppingCartMinusButton -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(2),-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 8) easterEgg1 = 9
                else easterEgg1 = 0
                easterEgg2 = 0
                if (easterEgg3 == 4) easterEgg3 = 5
            }
            dialogElements[3]?.shoppingCartMinusButton -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(3),-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 9) easterEgg1 = 10
                else easterEgg1 = 0
                easterEgg2 = 0
                easterEgg3 = 0
            }
            dialogElements[4]?.shoppingCartMinusButton -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(4),-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 10) easterEgg1 = 11
                else easterEgg1 = 0
                easterEgg2 = 0
                easterEgg3 = 0
            }
            dialogElements[5]?.shoppingCartMinusButton -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(5),-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 11) easterEgg1 = 12
                else easterEgg1 = 0
                easterEgg2 = 0
                easterEgg3 = 0
            }
            R.id.textViewTotalFix -> {
                easterEgg++

                if (easterEgg == 20) {
                    easterEgg = 0

                    for (i in 0..<AppConstants.DISPENSERS_QTY){
                        var textView = findViewById<EditText>(dialogElements[i]!!.shoppingCartQty )
                        inventory.setQty(shoppingCart.getFlavor(i),Integer.valueOf(textView.text.toString()))
                    }

                    Log.i("INVENTORY","SET")
                    toast("Inventory SET")
                }
                easterEgg2 = 0
                easterEgg3 = 0
            }
            R.id.buttonEmpty -> {
                shoppingCart.clearCart()
                easterEgg = 0
                if (easterEgg1 == 12)
                {
                    inventory.reset()
                    for (i in 0..<AppConstants.DISPENSERS_QTY) {
                        var textView = findViewById<EditText>(dialogElements[i]!!.shoppingCartQty)
                        textView.setText("0")
                    }

                    updatePriceTags()



                    Log.i("INVENTORY","RESET")
                }

                if (easterEgg2 == 3) {
                    sendDmp()
                    toast("Send DMP")
                }

                easterEgg2 = 0
                easterEgg1 = 0

                if (easterEgg3 == 5){

                    activate_msitef()
                }
                else if (easterEgg3%2 == 1) easterEgg3++
                else easterEgg3 = 0
                toast("Inventory Reset")
            }
            R.id.buttonCheckout -> {
                if (shoppingCart.returnTotal() > 0.0) {
                    var textMessage = "\n"

                    // Função para adicionar item ao texto

                    val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_purchase_summary, null)
                    for(i in 0..5) addItemToDialog(i,dialogView)
                    disableWatchdog()

                    val dialogBuilder = AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setTitle("COMPRAR")

                    // Show the dialog
                    val customDialog = dialogBuilder.create()

                    val totalStr = (shoppingCart.returnTotal() * 100).toInt().toString()

                    val timestamp = Timestamp(System.currentTimeMillis())

                    val sdf = SimpleDateFormat("yyyyMMddHHmmss")

                    paymentParameters.amountStr = totalStr
                    paymentParameters.invoiceNumberStr = sdf.format(timestamp)
                    paymentParameters.merchant_TIDStr = AppConstants.merchantTaxId
                    paymentParameters.isv_TIDStr = AppConstants.isvTaxId
                    paymentParameters.sitefMIDStr = "00000000"
                    paymentParameters.endpointStr = "3.19.30.51:4096"
                    paymentParameters.comProtocolString = "0"
                    paymentParameters.operatorIdStr = "1"
                    paymentParameters.tlsToken = "8977316332439824"

                    var myButton = dialogView.findViewById<ImageView>(R.id.botaoPix)
                    myButton.setOnClickListener{
                        paymentParameters.functionIdStr = "122"
                        callPaymentApp(paymentParameters)
                        customDialog.dismiss()
                    }

                    myButton = dialogView.findViewById<ImageView>(R.id.botaoCredito)
                    myButton.setOnClickListener{
                        paymentParameters.functionIdStr = "3"
                        paymentParameters.enabledTransactionsStr = "26"
                        paymentParameters.restrictionStr = "{TransacoesHabilitadas=26}"
                        paymentParameters.installmentsStr = "1"
                        callPaymentApp(paymentParameters)
                        customDialog.dismiss()
                    }

                    myButton = dialogView.findViewById<ImageView>(R.id.botaoDebito)
                    myButton.setOnClickListener{
                        paymentParameters.functionIdStr = "2"
                        callPaymentApp(paymentParameters)
                        customDialog.dismiss()
                    }

                    myButton = dialogView.findViewById<ImageView>(R.id.botaoVoucher)
                    myButton.setOnClickListener{
                        paymentParameters.functionIdStr = "2"
                        callPaymentApp(paymentParameters)
                        customDialog.dismiss()
                    }
                    customDialog.show()
                    hideActionBar()

                } else {
                    toast("Adicione itens ao carrinho.")
                }

                easterEgg = 0
                easterEgg1 = 0
                easterEgg2 = 0
                easterEgg3 = 0
            }

            R.id.helpButton -> {

                easterEgg = 0
                easterEgg1 = 0
                easterEgg2 = 0
                easterEgg3 = 0

                val intent: Intent = Intent(this, helperDialog::class.java)

                resetWatchDog(10)
                startActivityForResult(intent, 10)

            }

        }
        Log.i("easterEgg1","${easterEgg1}")

        if (bUpdateView == true) updateView(res)
    }



    fun intentCallback(){

    }

    fun sendDmp(){
        callPaymentApp(PAYMENT_INTERFACE_FIELDS_NAMES(functionIdStr = "121", amountStr = "0", invoiceNumberStr = "", merchant_TIDStr = AppConstants.merchantTaxId, isv_TIDStr = AppConstants.isvTaxId))

    }

    fun updateView(res:Int)
    {
        if (res == 0) {
            for (i in 0..<AppConstants.DISPENSERS_QTY){
                var textView = findViewById<EditText>(dialogElements[i]!!.shoppingCartQty)
                textView.setText(
                    shoppingCart!!.getCartItemQuantity(NESPRESSO_FLAVORS.NONE,i).toString()
                )
                var flavor : NESPRESSO_FLAVORS? = shoppingCart.getFlavor(i)

                if ((flavor?.let { inventory.getQty(it) }!! - shoppingCart.getCartItemQuantity(flavor)) <=0 ) {
                findViewById<ImageView>(dialogElements[i]!!.shoppingCartImage).imageAlpha = AppConstants.OUT_OF_STOCK_ALPHA
                findViewById<Button>(dialogElements[i]!!.shoppingCartPlusButton).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            }
            else {
                findViewById<ImageView>(dialogElements[i]!!.shoppingCartImage).imageAlpha = AppConstants.ON_STOCK_ALPHA
                findViewById<Button>(dialogElements[i]!!.shoppingCartPlusButton).alpha = AppConstants.ON_STOCK_ALPHA_FLOAT
            }
            if (shoppingCart.getCartItemQuantity(flavor) <=0) findViewById<Button>(dialogElements[i]!!.shoppingCartMinusButton).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            else findViewById<Button>(dialogElements[i]!!.shoppingCartMinusButton).alpha = AppConstants.ON_STOCK_ALPHA_FLOAT

            }

            var textView1 = findViewById<TextView>(R.id.textViewTotal)
            textView1.setText(String.format("%.2f", shoppingCart.returnTotal()))

        }
        else if (res == -1)
        {
            toast("Não foi possível adicionar mais itens ao carrinho.")
        }
        else if (res == -2){
            toast("Não foi possível remover o item do carrinho.")
        }
        else if (res == -3){
            toast("Selecione Checkout para tentar novamente.")
        }

    }

    fun updateCoffeIcon(flavor: NESPRESSO_FLAVORS, id:Int){
        val materialButton: MaterialButton = findViewById(id)
        materialButton.setText(String.format("%d",inventory.getIntensity(flavor)))
        when (inventory.getSize(flavor)){
            1 -> materialButton.setIconResource(R.drawable.coffeeicon_s)
            2 -> materialButton.setIconResource(R.drawable.coffeeicon_m)
            3 -> materialButton.setIconResource(R.drawable.coffeeicon_l)
        }
    }

    @SuppressLint("CutPasteId")
    fun updatePriceTags(){

        for (i in 0..<AppConstants.DISPENSERS_QTY){
            val myFlavor = shoppingCart.getFlavor(i)
            var textView2 = findViewById<Button>(dialogElements[i]!!.shoppingCartItemPrice)
            textView2.setText(String.format("R$%.2f",inventory.getPrice(myFlavor)))
            updateCoffeIcon(myFlavor,dialogElements[i]!!.shoppingCartItemInfo)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        hideActionBar()
        super.onActivityResult(requestCode, resultCode, data)

        resetWatchDog()
        Log.d ("onActivityResult requestCode",requestCode.toString())

        if (requestCode == 1) {
            try {
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@", this.javaClass.getName() + " | "
                            + object : Any() {}.javaClass.getEnclosingMethod().name + " | "
                            + "RequestCode: " + requestCode
                )

                Log.d("@@PRE_PAYMENT_SAMPLE@@", "requestCode: " + requestCode);
                Log.d("@@PRE_PAYMENT_SAMPLE@@", "resultCode: " + resultCode);

                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "responseCode: " + data!!.getStringExtra("responseCode")
                )
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "transactionType: " + data!!.getStringExtra("transactionType")
                )
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "installmentType: " + data!!.getStringExtra("installmentType")
                )
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "cashbackAmount: " + data!!.getStringExtra("cashbackAmount")
                )
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "acquirerId: " + data!!.getStringExtra("acquirerId")
                )
                Log.d("@@PRE_PAYMENT_SAMPLE@@", "cardBrand: " + data!!.getStringExtra("cardBrand"))
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "sitefTransactionId: " + data!!.getStringExtra("sitefTransactionId")
                )
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "hostTrasactionId: " + data!!.getStringExtra("hostTrasactionId")
                )
                Log.d("@@PRE_PAYMENT_SAMPLE@@", "authCode: " + data!!.getStringExtra("authCode"))
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "transactionInstallments: " + data!!.getStringExtra("transactionInstallments")
                )
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "merchantReceipt: " + data!!.getStringExtra("merchantReceipt")
                )
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "customerReceipt: " + data!!.getStringExtra("customerReceipt")
                )
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "returnedFields: " + data!!.getStringExtra("returnedFields")
                )

                var cupom: String? = data!!.getStringExtra("merchantReceipt")

                if (cupom != null) {
                    releaseCoffee()

                    shoppingCart.clearCart()

                    updateView(0)
                }
            }
            catch (e: Exception)  {
                Log.e ("Exception Sales App",e.toString())
                resetWatchDog()
                shoppingCart.clearCart()
                updateView(0)
            }
        }
        else if (requestCode == 3) {
            try {
                val retVal = data!!.getStringExtra("action")
                when (retVal) {
                    "New" -> {
                        shoppingCart.clearCart()

                        updateView(0)
                        Log.d("ret ScreenSaver", "New")
                    }

                    "Continue" -> {

                        Log.d("ret ScreenSaver", "Continue")
                    }
                }
            }
            catch (e: Exception)  {
                Log.e ("ScreenSaver",e.toString())
            }
        }
        else if (requestCode == 99){
            if (resultCode == RESULT_OK){
                if (data == null){
                    Log.i ("Smart Fiserv","REQUEST_ACTIVATE_TEF null data")
                }
                else {
                    Log.d ("Smart Fiserv","WIFI_ADDRESS: "+ data!!.getStringExtra("WIFI_ADDRESS"))
                    Log.d ("Smart Fiserv","GPRS_ADDRESS: "+ data!!.getStringExtra("GPRS_ADDRESS"))
                    Log.d ("Smart Fiserv","SIM_CARD_TYPE: "+ data!!.getStringExtra("SIM_CARD_TYPE"))
                    Log.d ("Smart Fiserv","PRIVATE_SIM_CARD_MANUFACTURER: "+ data!!.getStringExtra("PRIVATE_SIM_CARD_MANUFACTURER"))
                    Log.d ("Smart Fiserv","BUSINESS_ID: "+ data!!.getStringExtra("BUSINESS_ID"))
                    Log.d ("Smart Fiserv","TERMINAL_ID: "+ data!!.getStringExtra("TERMINAL_ID"))
                    Log.d ("Smart Fiserv","SUPERVISOR_CODE: "+ data!!.getStringExtra("SUPERVISOR_CODE"))
                    Log.d ("Smart Fiserv","ACQUIRER_CNPJ: "+ data!!.getStringExtra("ACQUIRER_CNPJ"))
                }
            }
        }
    }
    fun releaseCoffee (){

        val intent: Intent = Intent(this, DispenserProgress::class.java)

        for (i in 0..<AppConstants.DISPENSERS_QTY){
            val myFlavor = shoppingCart.getFlavor(i)
            this.inventory.setQty(
                myFlavor,
                inventory.getQty(myFlavor)!! - shoppingCart.getCartItemQuantity(
                    myFlavor
                )
            )
            intent.putExtra(
                GlobalVariables.dispenserElements[i].id+AppConstants.dispenserIdSufix,
                shoppingCart.getCartItemQuantity(myFlavor)
            )
            intent.putExtra(
                GlobalVariables.dispenserElements[i].id+AppConstants.dispenserFlavorSufix,
                myFlavor.value
            )

        }
        resetWatchDog(10)
        startActivityForResult(intent, 2)
    }

}
