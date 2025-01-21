
package br.com.slyco.slycocafe

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
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
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.google.ar.core.Config
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import android.graphics.Bitmap


fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}


class MainActivity<Bitmap> : AppCompatActivity() {
    private lateinit var myInventory : inventory

    private lateinit var paymentInterfaceFieldNames: PAYMENT_INTERFACE_FIELDS_NAMES
    private var paymentParameters :PAYMENT_INTERFACE_FIELDS_NAMES = PAYMENT_INTERFACE_FIELDS_NAMES()

    private lateinit var myLocation:location

    private val myLog = log("MAIN ACTIVITY")

    private lateinit var shoppingCart : shoppingCart
    private var easterEgg = 0
    private var easterEgg1 = 0
    private var easterEgg2 = 0
    private var easterEgg3 = 0
    private lateinit var android_id:String

    private lateinit var watchDog: Handler


    private var viewLayout = R.layout.activity_main_smart_terminal
    private var purchaseSummaryLayout = R.layout.dialog_purchase_summary_portrait

    private lateinit var dialogElements : List<ITEM_VIEW_COMPONENTS>

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
        dialogElements = listOf(
            ITEM_VIEW_COMPONENTS(
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
                R.id.qtyTextView1),
            ITEM_VIEW_COMPONENTS(
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
                R.id.qtyTextView2),
            ITEM_VIEW_COMPONENTS(
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
                R.id.qtyTextView3),
            ITEM_VIEW_COMPONENTS(
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
                R.id.qtyTextView4),
            ITEM_VIEW_COMPONENTS(
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
                R.id.qtyTextView5),

            ITEM_VIEW_COMPONENTS(
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
        )
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
    fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }


    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {

        hideActionBar()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Using constructor
        val hashMap: HashMap<String, Int> = HashMap()

// Using hashMapOf function


        android_id = getAndroidId(this).toUpperCase().chunked(4).joinToString("-")

        val cryptoManager = CryptoManager()

        val cryptoKey =cryptoManager.getKey()

        myLog.log( "Key: ${cryptoKey.toString()}")

        val encryptOutput = cryptoManager.encrypt(android_id.encodeToByteArray())

        myLog.log("IV size ${encryptOutput.ivSize.toString()}")
        myLog.log("IV ${encryptOutput.iv.decodeToString()}")
        myLog.log("Enc data size ${encryptOutput.encryptedDataSize.toString()}")
        myLog.log("Enc data ${encryptOutput.encryptedData.decodeToString()}")

        myLog.log("Dec data ${cryptoManager.decrypt(encryptOutput).decodeToString()}")

        myLog.log("API Secret: ${BuildConfig.SLYCO_API_SECRET}")


        myLocation = location(android_id)

        myInventory = inventory(android_id,myLocation.getLocation().items,myLocation.getLocation().dispenserModel.capacityPerFlavor,myLocation.getLocation().dispenserModel.flavors)
        shoppingCart = shoppingCart(myInventory.getInventory())

        Log.d ("DeviceInfo","Name: ${DeviceInfoModule.deviceName}  Brand: ${DeviceInfoModule.deviceBrand}    Model: ${DeviceInfoModule.deviceModel}   DeviceID ${android_id}")

        if ((DeviceInfoModule.deviceBrand.toUpperCase() == "CLOVER") && (DeviceInfoModule.deviceModel.toUpperCase() == "C305")) {
            Log.d ("Dettected Device","Clover Mini")
            viewLayout = R.layout.activity_main
            purchaseSummaryLayout = R.layout.dialog_purchase_summary
        }
        else if ((DeviceInfoModule.deviceBrand.toUpperCase() == "CLOVER") && ((DeviceInfoModule.deviceModel.toUpperCase() == "C405")||(DeviceInfoModule.deviceModel.toUpperCase() == "C406"))) {
            Log.d ("Dettected Device","Clover Flex")
            viewLayout = R.layout.activity_main_smart_terminal
            purchaseSummaryLayout = R.layout.dialog_purchase_summary_portrait
        }
        else if (((DeviceInfoModule.deviceBrand.toUpperCase() == "INGENICO") && (DeviceInfoModule.deviceModel.toUpperCase() == "DX8000") )||
            ((DeviceInfoModule.deviceBrand == "SUNMI") && (DeviceInfoModule.deviceModel == "P2-A11"))) {

            Log.d ("Dettected Device","Smart Terminal")
            viewLayout = R.layout.activity_main_smart_terminal
            purchaseSummaryLayout = R.layout.dialog_purchase_summary_portrait
        }
        else if ((DeviceInfoModule.deviceBrand.toUpperCase() == "GERTEC") && (DeviceInfoModule.deviceModel.toUpperCase() == "SK-210") ) {
            Log.d ("Dettected Device","Gertec SK210")
            viewLayout = R.layout.activity_main_smart_terminal
            purchaseSummaryLayout = R.layout.dialog_purchase_summary_portrait
            paymentParameters.pinpadTypeStr="ANDROID_USB"
            paymentParameters.sitefMIDStr="00000048"
            paymentParameters.endpointStr="tls-prod.fiservapp.com"
            paymentParameters.comProtocolString="4"
            paymentParameters.merchant_TIDStr="55833084000136"
            paymentParameters.isv_TIDStr = "55833084000136"
        }
        else {
            Log.d ("Dettected Device","Unknown")
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


        for (i in 0..< myLocation.getLocation().dispenserModel.flavors){
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
        intent.putExtra("locationName", myLocation.getLocation().name)
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


        if (this.myLocation.getLocation().demoMode == false) {
            try {
                PAYMENT_INTERFACE_FIELDS_NAMES(intentActionStr = paymentInterfaceFieldNames.intentActionStr)
                val intent: Intent = Intent(paymentInterfaceFieldNames.intentActionStr)

                Log.d ("Action","Intent ${paymentInterfaceFieldNames.intentActionStr}")

                if ((paymentInterfaceFieldNames.sitefMIDStr != "") && (transactionParameters.sitefMIDStr != "")) { intent.putExtra(paymentInterfaceFieldNames.sitefMIDStr, transactionParameters.sitefMIDStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.sitefMIDStr}:${transactionParameters.sitefMIDStr}")}

                if ((paymentInterfaceFieldNames.endpointStr != "") && (transactionParameters.endpointStr != "")) { intent.putExtra(paymentInterfaceFieldNames.endpointStr, transactionParameters.endpointStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.endpointStr}:${transactionParameters.endpointStr}")}

                if ((paymentInterfaceFieldNames.terminalIdStr != "") && (transactionParameters.terminalIdStr != "")) { intent.putExtra(paymentInterfaceFieldNames.terminalIdStr, transactionParameters.terminalIdStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.terminalIdStr}:${transactionParameters.terminalIdStr}")}

                if ((paymentInterfaceFieldNames.functionIdStr != "") && (transactionParameters.functionIdStr != "")) { intent.putExtra(paymentInterfaceFieldNames.functionIdStr, transactionParameters.functionIdStr); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.functionIdStr}:${transactionParameters.functionIdStr}")}

                if ((paymentInterfaceFieldNames.merchant_TIDStr != "") && (transactionParameters.merchant_TIDStr != "")) { intent.putExtra(paymentInterfaceFieldNames.merchant_TIDStr, transactionParameters.merchant_TIDStr.replace(".", "").replace("/", "").replace("-", "")); Log.d ("PaymentIntentParam","${paymentInterfaceFieldNames.merchant_TIDStr}:${transactionParameters.merchant_TIDStr.replace(".", "").replace("/", "").replace("-", "")}")}

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
            val timestamp = Timestamp(System.currentTimeMillis())

            val sdf = SimpleDateFormat("yyyyMMddHHmmss")

            var mySaleResponseData = saleResponseDC(
                locationId = android_id,
                transactionType = "demo mode",
                installmentType = "",
                cashbackAmount = "",
                acquirerId = "",
                cardBrand = "",
                sitefTransactionId = "",
                hostTrasactionId = "",
                authCode = "",
                transactionInstallments = "",
                pan = "", // pan
                goodThru = "", // good thru
                cardType = "", // card_type
                cardReadStatus = "", // card read status
                paymentSourceTaxID = "", // payment source tax id
                invoiceBrandID = "", // brand id for invoice
                invoiceNumber = "", // invoice number
                authorizerResponseCode = "", // authorizer response code
                authorizationCode = "", // authorization code
                transactionTimestamp = sdf.format(timestamp).toLong(), //transaction timestamp
                authorizationNetworkID = "", // authorization network id
                merchantID = "", //merchant id,
                sitefIf = "", //if sitef,
                cardBrandID = "", // sitef cardbrand id
                invoiceAuthorizationCode = "", // invoice authorization code
                saleItems = ""
            )
            finishTransaction(mySaleResponseData)
            Log.d("DemoMode", "shoppingCart.clearCart()")
        }
    }

    fun addItemToDialog(index: Int, dialogView: View) {
        val flavor = shoppingCart.getFlavor(index)
        val quantity = flavor?.let { shoppingCart.getCartItemQuantity(NESPRESSOFLAVORS.NONE,index) }
        if (quantity != null) {
            var image = dialogElements[index]?.let { dialogView?.findViewById<ImageView>(it.dialogImage) }
            image?.setImageResource(flavor.value)
            var text = dialogElements[index]?.let { dialogView?.findViewById<TextView>(it.dialogQty) }
            text?.text = shoppingCart.getCartItemQuantity(NESPRESSOFLAVORS.NONE,index).toString()
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

    private fun finishTransaction(mySaleResponse: saleResponseDC){
        releaseCoffee()

        myInventory.patchtInventoryQty(myLocation.getLocation().id,myLocation.getLocation().items)

        var saleItems = ""

        for (i in 0..myLocation.getLocation().dispenserModel.flavors-1){
            var qty = shoppingCart.getCartItemQuantity(NESPRESSOFLAVORS.NONE,i)
            if (qty > 0){
                saleItems += "${INVENTORYHASH.get(shoppingCart.getFlavor(i).value)}:${i+1}:${qty};"
            }
        }
        mySaleResponse.saleItems = saleItems
        putSale (android_id,mySaleResponse)
        shoppingCart.clearCart()

        updateView(0)
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
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(0),1, myInventory)
                easterEgg = 0
                if (easterEgg1 == 0) easterEgg1 = 1
                else easterEgg1 = 0

                if (easterEgg2 == 0) easterEgg2 = 1
                else easterEgg2 = 0
                easterEgg3 = 0
            }
            dialogElements[1]?.shoppingCartPlusButton, dialogElements[1]?.shoppingCartImage, dialogElements[1]?.shoppingCartItemPrice, dialogElements[1]?.shoppingCartItemInfo -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(1),1, myInventory)
                easterEgg = 0
                if (easterEgg1 == 1) easterEgg1 = 2
                else easterEgg1 = 0

                easterEgg2 = 0
                easterEgg3 = 0
            }
            dialogElements[2]?.shoppingCartPlusButton, dialogElements[2]?.shoppingCartImage, dialogElements[2]?.shoppingCartItemPrice, dialogElements[2]?.shoppingCartItemInfo -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(2),1, myInventory)
                easterEgg = 0
                if (easterEgg1 == 2) easterEgg1 = 3
                else easterEgg1 = 0

                if (easterEgg2 == 1) easterEgg2 = 2
                else easterEgg2 = 0
                easterEgg3 = 0
            }
            dialogElements[3]?.shoppingCartPlusButton, dialogElements[3]?.shoppingCartImage, dialogElements[3]?.shoppingCartItemPrice, dialogElements[3]?.shoppingCartItemInfo -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(3),1, myInventory)
                easterEgg = 0
                if (easterEgg1 == 3) easterEgg1 = 4
                else easterEgg1 = 0

                easterEgg2 = 0
                easterEgg3 = 0
            }
            dialogElements[4]?.shoppingCartPlusButton, dialogElements[4]?.shoppingCartImage, dialogElements[4]?.shoppingCartItemPrice, dialogElements[4]?.shoppingCartItemInfo -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(4),1, myInventory)
                easterEgg = 0
                if (easterEgg1 == 4) easterEgg1 = 5
                else easterEgg1 = 0

                if (easterEgg2 == 2) easterEgg2 = 3
                else easterEgg2 = 0
                easterEgg3 = 0

            }

            dialogElements[5]?.shoppingCartPlusButton, dialogElements[5]?.shoppingCartImage, dialogElements[5]?.shoppingCartItemPrice, dialogElements[5]?.shoppingCartItemInfo -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(5),1, myInventory)
                easterEgg = 0
                if (easterEgg1 == 5) easterEgg1 = 6
                else easterEgg1 = 0

                easterEgg2 = 0
                easterEgg3 = 0
            }

            dialogElements[0]?.shoppingCartMinusButton -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(0),-1, myInventory)
                easterEgg = 0
                if (easterEgg1 == 6) easterEgg1 = 7
                else easterEgg1 = 0
                easterEgg2 = 0
                easterEgg3 = 1
            }
            dialogElements[1]?.shoppingCartMinusButton -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(1),-1, myInventory)
                easterEgg = 0
                if (easterEgg1 == 7) easterEgg1 = 8
                else easterEgg1 = 0
                easterEgg2 = 0
                if (easterEgg3 == 2) easterEgg3 = 3
            }
            dialogElements[2]?.shoppingCartMinusButton -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(2),-1, myInventory)
                easterEgg = 0
                if (easterEgg1 == 8) easterEgg1 = 9
                else easterEgg1 = 0
                easterEgg2 = 0
                if (easterEgg3 == 4) easterEgg3 = 5
            }
            dialogElements[3]?.shoppingCartMinusButton -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(3),-1, myInventory)
                easterEgg = 0
                if (easterEgg1 == 9) easterEgg1 = 10
                else easterEgg1 = 0
                easterEgg2 = 0
                easterEgg3 = 0
            }
            dialogElements[4]?.shoppingCartMinusButton -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(4),-1, myInventory)
                easterEgg = 0
                if (easterEgg1 == 10) easterEgg1 = 11
                else easterEgg1 = 0
                easterEgg2 = 0
                easterEgg3 = 0
            }
            dialogElements[5]?.shoppingCartMinusButton -> {
                // Do some work here
                res = shoppingCart.addItemToCart(shoppingCart.getFlavor(5),-1, myInventory)
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

                    for (i in 0..<myLocation.getLocation().dispenserModel.flavors){
                        var textView = findViewById<EditText>(dialogElements[i]!!.shoppingCartQty )
                        myInventory.setQty(shoppingCart.getFlavor(i),Integer.valueOf(textView.text.toString()))
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
                    myLocation.getLocation()?.let { myInventory.reset(it.items) }
                    for (i in 0..<myLocation.getLocation().dispenserModel.flavors) {
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

                    val dialogView = LayoutInflater.from(this).inflate(purchaseSummaryLayout, null)
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
                    paymentParameters.merchant_TIDStr = myLocation.getLocation().merchant.tax_id
                    paymentParameters.isv_TIDStr = AppConstants.isvTaxId
                    paymentParameters.operatorIdStr = "1"

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

        val test = PAYMENT_INTERFACE_FIELDS_NAMES(
            pinpadTypeStr="ANDROID_USB",
            sitefMIDStr="00000048",
            endpointStr="tls-prod.fiservapp.com",
            comProtocolString="4",
            functionIdStr = "121",
            amountStr = "0",
            invoiceNumberStr = "",
            merchant_TIDStr = myLocation.getLocation().merchant.tax_id.replace(".", "").replace("/", "").replace("-", ""),
            isv_TIDStr = AppConstants.isvTaxId
        )
        callPaymentApp(test)

    }

    fun updateView(res:Int)
    {
        if (res == 0) {
            for (i in 0..<myLocation.getLocation().dispenserModel.flavors){
                var textView = findViewById<EditText>(dialogElements[i]!!.shoppingCartQty)
                textView.setText(
                    shoppingCart!!.getCartItemQuantity(NESPRESSOFLAVORS.NONE,i).toString()
                )
                var flavor : NESPRESSOFLAVORS? = shoppingCart.getFlavor(i)

                if ((flavor?.let { myInventory.getQty(it) }!! - shoppingCart.getCartItemQuantity(flavor)) <=0 ) {
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

    fun updateCoffeIcon(flavor: NESPRESSOFLAVORS, id:Int){
        val materialButton: MaterialButton = findViewById(id)
        materialButton.setText(String.format("%d",myInventory.getIntensity(flavor)))
        when (myInventory.getSize(flavor)){
            1 -> materialButton.setIconResource(R.drawable.coffeeicon_s)
            2 -> materialButton.setIconResource(R.drawable.coffeeicon_m)
            3 -> materialButton.setIconResource(R.drawable.coffeeicon_l)
        }
    }

    @SuppressLint("CutPasteId")
    fun updatePriceTags(){

        for (i in 0..<myLocation.getLocation().dispenserModel.flavors){
            val myFlavor = shoppingCart.getFlavor(i)
            var textView2 = findViewById<Button>(dialogElements[i]!!.shoppingCartItemPrice)
            textView2.setText(String.format("R$%.2f",myInventory.getPrice(myFlavor)))
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
                var returnedFields = data!!.getStringExtra("returnedFields")
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "returnedFields: $returnedFields"
                )

                val gson = Gson()
                val mapType = object : TypeToken<Map<String, Any>>() {}.type
                val genericMap: Map<String, Any> = gson.fromJson(returnedFields, mapType)
                println(genericMap)
                var mydata = genericMap["2021"]

                myLog.log("genericMap: ${(mydata as ArrayList<String>).get(0)}")


                var mySaleResponseData = saleResponseDC(
                    locationId = android_id,
                    transactionType = (data?.getStringExtra("transactionType") as? String ?: ""),
                    installmentType = data?.getStringExtra("installmentType") as? String ?: "",
                    cashbackAmount = data?.getStringExtra("cashbackAmount") as? String ?: "",
                    acquirerId = data?.getStringExtra("acquirerId") as? String ?: "",
                    cardBrand = data?.getStringExtra("cardBrand") as? String ?: "",
                    sitefTransactionId = data?.getStringExtra("sitefTransactionId") as? String ?: "",
                    hostTrasactionId = data?.getStringExtra("hostTrasactionId") as? String ?: "",
                    authCode = data?.getStringExtra("authCode") as? String ?: "",
                    transactionInstallments = data?.getStringExtra("transactionInstallments") as? String ?: "",
                    pan = (genericMap["2021"] as ArrayList<String>).get(0) as? String ?: "", // pan
                    goodThru = (genericMap["1002"] as ArrayList<String>)?.get(0) as? String ?: "", // good thru
                    cardType = (genericMap["2090"] as ArrayList<String>)?.get(0) as? String ?: "", // card_type
                    cardReadStatus = (genericMap["2091"] as ArrayList<String>)?.get(0) as? String ?: "", // card read status
                    paymentSourceTaxID = (genericMap["950"] as? ArrayList<String>)?.getOrNull(0) ?: "", // payment source tax id
                    invoiceBrandID = (genericMap["951"] as? ArrayList<String>)?.getOrNull(0) ?: "", // brand id for invoice
                    invoiceNumber = (genericMap["953"] as? ArrayList<String>)?.getOrNull(0) ?: "", // invoice number
                    authorizerResponseCode = (genericMap["2010"]  as? ArrayList<String>)?.getOrNull(0) ?: "", // authorizer response code
                    authorizationCode = (genericMap["135"]  as? ArrayList<String>)?.getOrNull(0) ?: "", // authorization code
                    transactionTimestamp = (genericMap["105"] as? ArrayList<String>)?.getOrNull(0) as? Long ?: 0, //transaction timestamp
                    authorizationNetworkID = (genericMap["158"] as? ArrayList<String>)?.getOrNull(0) ?: "", // authorization network id
                    merchantID = (genericMap["157"] as? ArrayList<String>)?.getOrNull(0) ?: "", //merchant id,
                    sitefIf = (genericMap["131"] as? ArrayList<String>)?.getOrNull(0) ?: "", //if sitef,
                    cardBrandID = (genericMap["132"] as? ArrayList<String>)?.getOrNull(0) ?: "", // sitef cardbrand id
                    invoiceAuthorizationCode = (genericMap["952"] as? ArrayList<String>)?.getOrNull(0) ?: "", // invoice authorization code
                    saleItems = ""
                )



                myLog.log(mySaleResponseData.toString())

                var cupom: String? = data?.getStringExtra("merchantReceipt")

                if (cupom != null) {
                    finishTransaction(mySaleResponseData)
                }
            }
            catch (e: Exception)  {
                Log.e ("Exception Sales App",e.toString())
                resetWatchDog()
                //shoppingCart.clearCart()
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
        intent.putExtra(
            "dispensersQty",
            myLocation.getLocation().dispenserModel.flavors
        )



        for (i in 0..<myLocation.getLocation().dispenserModel.flavors){
            val myFlavor = shoppingCart.getFlavor(i)
            this.myInventory.setQty(
                myFlavor,
                myInventory.getQty(myFlavor)!! - shoppingCart.getCartItemQuantity(
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
