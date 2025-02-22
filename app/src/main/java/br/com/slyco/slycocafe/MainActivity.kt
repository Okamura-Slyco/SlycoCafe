
package br.com.slyco.slycocafe

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.sql.Timestamp
import java.text.SimpleDateFormat


fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}


class MainActivity<Bitmap> : AppCompatActivity(),OnItemClickListener {
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

    private lateinit var loadingDialog: LoadingDialog

    private lateinit var watchDog: Handler


    private var viewLayout = R.layout.activity_main_smart_terminal
    private var purchaseSummaryLayout = R.layout.dialog_purchase_summary_portrait

    private lateinit var dialogElements : List<ITEM_VIEW_COMPONENTS>


    private lateinit var recyclerView1: RecyclerView
    private lateinit var recyclerView2: RecyclerView
    private lateinit var adapter1: ShoppingCartAdapter
    private lateinit var adapter2: ShoppingCartAdapter

    private lateinit var summaryRecyclerView1: RecyclerView
    private lateinit var summaryRecyclerView2: RecyclerView
    private lateinit var summaryAdapter1: purchaseSummaryRecyclerViewAdapter
    private lateinit var summaryAdapter2: purchaseSummaryRecyclerViewAdapter

    private var displayList: MutableList<MutableList<shoppingCartItemModel>> = mutableListOf()
    private lateinit var summaryDisplayList: MutableList<MutableList<purchaseSummaryItemModel>>

    private var displayOrientation:Int = LinearLayoutManager.HORIZONTAL

    private var screenWidth = 1280
    private var screeenHeight = 1024

//    private var TAG = "GetEmployeeExample"
//    private var mEmployeeConnector: EmployeeConnector? = null
//    private var account: Account? = null
    fun hideActionBar(){
        val actionBar: ActionBar? = supportActionBar
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

    @SuppressLint("WrongConstant")
    fun initDialogElements() {

        recyclerView1 = findViewById(R.id.itemsRecyclerView1)
        recyclerView1.layoutManager = LinearLayoutManager(this, displayOrientation, false)

        recyclerView2 = findViewById(R.id.itemsRecyclerView2)
        recyclerView2.layoutManager = LinearLayoutManager(this, displayOrientation, false)

        adapter1 = ShoppingCartAdapter(
            displayList[0], this, 0,
            elementsInView = displayList[0].size,
            screeenHeight = screeenHeight,
            screeenWidth = screenWidth,
            displayOrientation = displayOrientation,
        )
        recyclerView1.adapter = adapter1

        adapter2 = ShoppingCartAdapter(
            displayList[1], this, 1,
            elementsInView = displayList[1].size,
            screeenHeight = screeenHeight,
            screeenWidth = screenWidth,
            displayOrientation = displayOrientation
        )
        recyclerView2.adapter = adapter2

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

    override fun setPlusOnClickListener(listId:Int , position: Int) {
        val clickedItem = "${listId}: $position"
        updateView(0,1, listId,position)
        //updateItem(listId, position, 1)
        resetWatchDog()
        hideActionBar()
// Handle the click
        //Toast.makeText(this, "setPlusOnClickListener: $clickedItem", Toast.LENGTH_SHORT).show()
    }

    private fun updateItem(listId: Int, position: Int,qtyToAdd:Int) {

        val list =  displayList[listId]
        val item = list[position]
        val itemIndex = item.getIndex()

        val flavor: NESPRESSOFLAVORS = shoppingCart.getFlavor(itemIndex)
        val ret = shoppingCart.addItemToCart(itemIndex, qtyToAdd, myInventory)
        val qty = shoppingCart.getCartItemQuantity(itemIndex)
        val enabledItem: Boolean = myInventory.getItem(itemIndex)?.qty?.minus(shoppingCart.getCartItemQuantity(itemIndex))!! > 0

        if (ret > -1000) {
            updateItemView(
                listId, position,
                flavor = flavor,
                quantity = qty,
                enabledItem = enabledItem,
                enabledMinusButton = qty > 0,
                index=itemIndex
            )
        }
    }

    override fun setPlusLongOnClickListener(listId:Int , position: Int) {
        val clickedItem = "${listId}: $position"
        // Handle the click
        resetWatchDog()
        hideActionBar()
        //Toast.makeText(this, "setPlusLongOnClickListener : $clickedItem", Toast.LENGTH_SHORT).show()
    }

    override fun setMinusOnClickListener(listId:Int , position: Int) {
        val clickedItem = "${listId}: $position"
        updateView(0,-1)
        //updateItem(listId,position,-1)
        resetWatchDog()
        hideActionBar()
        //Toast.makeText(this, "setMinusOnClickListener: $clickedItem", Toast.LENGTH_SHORT).show()
    }
    override fun setMinusLongOnClickListener(listId:Int , position: Int) {
        val clickedItem = "${listId}: $position"
        // Handle the click
        resetWatchDog()
        hideActionBar()
        //Toast.makeText(this, "setMinusLongOnClickListener : $clickedItem", Toast.LENGTH_SHORT).show()
    }

    private fun setupArrayList(items:List<inventoryStockDC>,dispenserFlavors:Int){
        val myItemsPerRecycler:Int = myLocation.getLocation().dispenserModel.flavors/2
        for (i in 0..< items.count()){
            if (i >= dispenserFlavors) break
            val item:shoppingCartItemModel = shoppingCartItemModel(
                size = items[i].item.coffeeSize,
                intensity = items[i].item.intensity,
                price = items[i].price.toFloat()/100.0f,
                flavor = NESPRESSOFLAVORS.from(NESPRESSOFLAVORSHASH.getValue(items[i].item.id)),
                quantity = 0,
                enabledItem = items[i].quantity > 0,
                enabledMinusButton = false,
                index = i,

            )

            if (displayOrientation == LinearLayoutManager.HORIZONTAL) {
                if (i == 0){
                    displayList.add(mutableListOf())
                    displayList[0] = mutableListOf(item)
                }
                else if ((i > 0) && (i < myItemsPerRecycler))
                {
                    displayList[0].add(item)
                }
                else if (i == myLocation.getLocation().dispenserModel.flavors/2){
                    displayList.add(mutableListOf())
                    displayList[1] = mutableListOf(item)
                }
                else
                {
                    displayList[1].add(item)
                }
            }
            else {
                if (i == 0){
                    displayList.add(mutableListOf())
                    displayList[0] = mutableListOf(item)
                }
                else if (i == 1){
                    displayList.add(mutableListOf())
                    displayList[1] = mutableListOf(item)
                }
                else
                {
                    displayList[i%2].add(item)
                }

            }

        }
        Log.d("teste","123")
    }

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {

        hideActionBar()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        loadingDialog = LoadingDialog(this)
        // Show loading
        if (!isFinishing) {
            loadingDialog.show()
        }

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

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screeenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels


        myLocation = location(android_id,DeviceInfoModule.deviceBrand, DeviceInfoModule.deviceModel)

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
            this.displayOrientation = LinearLayoutManager.VERTICAL
        }
        else if (((DeviceInfoModule.deviceBrand.toUpperCase() == "INGENICO") && (DeviceInfoModule.deviceModel.toUpperCase() == "DX8000") )||
            ((DeviceInfoModule.deviceBrand.toUpperCase() == "SUNMI") && (DeviceInfoModule.deviceModel.toUpperCase() == "P2-A11"))||
            ((DeviceInfoModule.deviceBrand.toUpperCase() == "NEWPOS") && (DeviceInfoModule.deviceModel.toUpperCase() == "NEW9220"))||
            ((DeviceInfoModule.deviceBrand.toUpperCase() == "VERIFONE") && (DeviceInfoModule.deviceModel.toUpperCase() == "X990"))||
            ((DeviceInfoModule.deviceBrand.toUpperCase() == "INGENICO") && (DeviceInfoModule.deviceModel.toUpperCase() == "DX6000"))) {

            Log.d ("Dettected Device","Smart Terminal")
            viewLayout = R.layout.activity_main_smart_terminal
            purchaseSummaryLayout = R.layout.dialog_purchase_summary_portrait
            this.displayOrientation = LinearLayoutManager.VERTICAL
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

            this.displayOrientation = LinearLayoutManager.VERTICAL
        }
        else if (((DeviceInfoModule.deviceBrand.toUpperCase() == "GOOGLE") && (DeviceInfoModule.deviceModel.toUpperCase() == "ANDROID SDK BUILT FOR X86") )||
            ((DeviceInfoModule.deviceBrand.toUpperCase() == "CLOVER") && (DeviceInfoModule.deviceModel.toUpperCase() == "C506") )){
            Log.d ("Dettected Device","Clover Kiosk")
            viewLayout = R.layout.activity_main_large_screen_toten
            purchaseSummaryLayout = R.layout.dialog_purchase_summary_portrait
            this.displayOrientation = LinearLayoutManager.VERTICAL

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

        myInventory = inventory(android_id,myLocation.getLocation().items,myLocation.getLocation().dispenserModel.capacityPerFlavor,myLocation.getLocation().dispenserModel.flavors)
        setupArrayList(myLocation.getLocation().items,myLocation.getLocation().dispenserModel.flavors)
        shoppingCart = shoppingCart(myInventory.getInventory())

        setContentView(viewLayout)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        initDialogElements()

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
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
        //updateView(0)

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


    private fun finishTransaction(mySaleResponse: saleResponseDC){
        releaseCoffee()

        myInventory.patchtInventoryQty(myLocation.getLocation().id,myLocation.getLocation().items)

        var saleItems = ""

        for (i in 0..myLocation.getLocation().dispenserModel.flavors-1){
            var qty = shoppingCart.getCartItemQuantity(i)
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

            R.id.buttonEmpty -> {
                shoppingCart.clearCart()

                if (easterEgg2 == 3) {
                    sendDmp()
                    toast("Send DMP")
                }

                if (easterEgg3 == 5){

                    activate_msitef()
                }
                else if (easterEgg3%2 == 1) easterEgg3++
                else easterEgg3 = 0
                toast("Inventory Reset")
            }
            R.id.buttonCheckout -> {
                if (shoppingCart.returnTotal() > 0.0) {

                    val dialogView = LayoutInflater.from(this).inflate(purchaseSummaryLayout, null)

                    summaryRecyclerView1 = dialogView.findViewById(R.id.summaryRecyclerView1)
                    summaryRecyclerView1.layoutManager = LinearLayoutManager(this)

                    summaryRecyclerView2 = dialogView.findViewById(R.id.summaryRecyclerView2)
                    summaryRecyclerView2.layoutManager = LinearLayoutManager(this)

                    summaryDisplayList = mutableListOf()
                    summaryDisplayList.add(mutableListOf())
                    summaryDisplayList.add(mutableListOf())

                    for (i in 0..myLocation.getLocation().dispenserModel.flavors-1){
                        lateinit var listToAdd: MutableList<purchaseSummaryItemModel>
                        if (i%2 == 0)
                        {
                            listToAdd = summaryDisplayList[0]
                        }
                        else
                        {
                            listToAdd = summaryDisplayList[1]
                        }
                        listToAdd.add(purchaseSummaryItemModel(shoppingCart.getFlavor(i),shoppingCart.getCartItemQuantity(i)))
                    }

                    summaryAdapter1 = purchaseSummaryRecyclerViewAdapter(summaryDisplayList[0])
                    summaryRecyclerView1.adapter = summaryAdapter1

                    summaryAdapter2 = purchaseSummaryRecyclerViewAdapter(summaryDisplayList[1])
                    summaryRecyclerView2.adapter = summaryAdapter2

                    var totalText = dialogView.findViewById<TextView>(R.id.totalAmountTextView)
                    totalText.text = String.format("%.2f",shoppingCart.returnTotal())

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
                    paymentParameters.merchant_TIDStr = myLocation.getLocation().merchant.taxId
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
                        paymentParameters.restrictionStr = "{TransacoesHabilitadas=16}"
                        callPaymentApp(paymentParameters)
                        customDialog.dismiss()
                    }

                    myButton = dialogView.findViewById<ImageView>(R.id.botaoVoucher)
                    myButton.setOnClickListener{
                        paymentParameters.functionIdStr = "2"
                        paymentParameters.restrictionStr = "{TransacoesHabilitadas=16}"
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
            merchant_TIDStr = myLocation.getLocation().merchant.taxId.replace(".", "").replace("/", "").replace("-", ""),
            isv_TIDStr = AppConstants.isvTaxId
        )
        callPaymentApp(test)

    }
    fun updateItemView(listId:Int,position:Int,flavor:NESPRESSOFLAVORS,quantity:Int,enabledItem:Boolean,enabledMinusButton:Boolean,index:Int){

        var myShoppingCartItem = shoppingCartItemModel(
            size = -1,
            intensity = -1,
            price = -1.0f,
            flavor = flavor,
            quantity = quantity,
            enabledItem = enabledItem,
            enabledMinusButton = enabledMinusButton,
            index = index
        )

        if (listId == 0){
            adapter1.updateItem(position, myShoppingCartItem)
        }
        else {
            adapter2.updateItem(position,myShoppingCartItem)
        }

    }
    fun updateView(res:Int, qtyToAdd: Int=0, listId: Int=-1,position: Int = -1)
    {
        if (res == 0) {
            if ((listId <0) || (position <0)) {
                for (displayListColumn in displayList) {
                    for (displayItem in displayListColumn) {
                        updateItem(
                            displayList.indexOf(displayListColumn),
                            displayListColumn.indexOf(displayItem),
                            qtyToAdd
                        )
                    }

                }
            }
            else {
                updateItem(
                    listId,
                    position,
                    qtyToAdd
                )
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
                //var mydata = genericMap["2021"]

                //myLog.log("genericMap: ${(mydata as ArrayList<String>).get(0)}")


                val mySaleResponseData = saleResponseDC(
                    locationId = android_id,
                    transactionType = data?.getStringExtra("transactionType") as? String ?: "",
                    installmentType = data?.getStringExtra("installmentType") as? String ?: "",
                    cashbackAmount = data?.getStringExtra("cashbackAmount") as? String ?: "",
                    acquirerId = data?.getStringExtra("acquirerId") as? String ?: "",
                    cardBrand = data?.getStringExtra("cardBrand") as? String ?: "",
                    sitefTransactionId = data?.getStringExtra("sitefTransactionId") as? String ?: "",
                    hostTrasactionId = data?.getStringExtra("hostTrasactionId") as? String ?: "",
                    authCode = data?.getStringExtra("authCode") as? String ?: "",
                    transactionInstallments = data?.getStringExtra("transactionInstallments") as? String ?: "",
                    pan = (genericMap["2021"] as? ArrayList<String>)?.getOrNull(0) as? String ?: "", // pan
                    goodThru = (genericMap["1002"] as? ArrayList<String>)?.getOrNull(0) as? String ?: "", // good thru
                    cardType = (genericMap["2090"] as? ArrayList<String>)?.getOrNull(0) as? String ?: "", // card_type
                    cardReadStatus = (genericMap["2091"] as? ArrayList<String>)?.getOrNull(0) as? String ?: "", // card read status
                    paymentSourceTaxID = (genericMap["950"] as? ArrayList<String>)?.getOrNull(0) ?: "", // payment source tax id
                    invoiceBrandID = (genericMap["951"] as? ArrayList<String>)?.getOrNull(0) ?: "", // brand id for invoice
                    invoiceNumber = (genericMap["953"] as? ArrayList<String>)?.getOrNull(0) ?: "", // invoice number
                    authorizerResponseCode = (genericMap["2010"] as? ArrayList<String>)?.getOrNull(0) ?: "", // authorizer response code
                    authorizationCode = (genericMap["135"] as? ArrayList<String>)?.getOrNull(0) ?: "", // authorization code
                    transactionTimestamp = (genericMap["105"] as? ArrayList<String>)?.getOrNull(0)?.toLongOrNull() ?: 0L, //transaction timestamp
                    authorizationNetworkID = (genericMap["158"] as? ArrayList<String>)?.getOrNull(0) ?: "", // authorization network id
                    merchantID = (genericMap["157"] as? ArrayList<String>)?.getOrNull(0) ?: "", //merchant id
                    sitefIf = (genericMap["131"] as? ArrayList<String>)?.getOrNull(0) ?: "", //if sitef
                    cardBrandID = (genericMap["132"] as? ArrayList<String>)?.getOrNull(0) ?: "", // sitef cardbrand id
                    invoiceAuthorizationCode = (genericMap["952"] as? ArrayList<String>)?.getOrNull(0) ?: "", // invoice authorization code
                    saleItems = ""
                )




                myLog.log(mySaleResponseData.toString())

                var merchantReceipt: String? = data?.getStringExtra("merchantReceipt")
                var customerReceipt: String? = data?.getStringExtra("customerReceipt")

                if ((merchantReceipt != null)||
                    (customerReceipt != null)){
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
                i,
                myInventory.getQty(i)!! - shoppingCart.getCartItemQuantity(
                    i
                )
            )
            intent.putExtra(
                GlobalVariables.dispenserElements[i].id+AppConstants.dispenserIdSufix,
                shoppingCart.getCartItemQuantity(i)
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
