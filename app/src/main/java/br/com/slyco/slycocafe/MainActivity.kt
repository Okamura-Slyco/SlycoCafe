
package br.com.slyco.slycocafe

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
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
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.sql.Timestamp
import java.text.SimpleDateFormat
import kotlin.math.roundToInt
import java.util.*
import kotlin.experimental.xor

fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}


class MainActivity<Bitmap> : AppCompatActivity(),OnItemClickListener {
    private lateinit var myInventory : inventory

    private lateinit var paymentInterfaceFieldNames: PAYMENT_INTERFACE_FIELDS_NAMES
    private var paymentParameters :PAYMENT_INTERFACE_FIELDS_NAMES = PAYMENT_INTERFACE_FIELDS_NAMES()
    private var paymentReturnFields: PAYMENT_INTERFACE_RESPONSE_FIELDS = PAYMENT_INTERFACE_RESPONSE_FIELDS()

    private lateinit var myLocation:location

    private val myLog = log("MAIN ACTIVITY")

    private lateinit var shoppingCart : shoppingCart
    private var easterEgg = 0
    private var easterEgg1 = 0
    private var easterEgg2 = 0
    private var easterEgg3 = 0
    private lateinit var android_id:String

    private lateinit var watchDog: Handler

    private val tapTimestamps = mutableListOf<Long>()
    private val tapThreshold = 10
    private val tapWindowMs = 2000L

    private var viewLayout = R.layout.activity_main_smart_terminal
    private var purchaseSummaryLayout = R.layout.dialog_purchase_summary_portrait

    private lateinit var dialogElements : List<ITEM_VIEW_COMPONENTS>

    private lateinit var helpDialog: helperDialog

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
    /**
     * Define o app de início, quando o terminal for ligado é este app que será chamado
     */
    private fun defineHomeApp() {
        try {
            val packageName = BuildConfig.APPLICATION_ID
            val appLabel = resources.getString(R.string.app_name)
            val intent = Intent().apply {
                component = ComponentName("br.com.bin", "br.com.bin.service.DefineHomeAppService")
                action = "br.com.bin.service.DefineHomeAppService.action.DEFINE_HOME_APP"
                putExtra("br.com.bin.service.DefineHomeAppService.extra.PACKAGE_NAME", packageName)
                putExtra("br.com.bin.service.DefineHomeAppService.extra.APP_LABEL", appLabel)
            }

            // Check if the service is callable
            if (isServiceCallable(intent)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            } else {
                Log.w("MainActivity", "DefineHomeAppService is not available")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error defining home app", e)
        }
    }

    /**
     * Checks if the service is callable
     */
    private fun isServiceCallable(intent: Intent): Boolean {
        return try {
            packageManager.queryIntentServices(intent, 0).isNotEmpty()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error checking service availability", e)
            false
        }
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
            mainViewAttributes = myLocation.getLocation().pos.mainViewAttributes
        )
        recyclerView1.adapter = adapter1

        adapter2 = ShoppingCartAdapter(
            displayList[1], this, 1,
            elementsInView = displayList[1].size,
            screeenHeight = screeenHeight,
            screeenWidth = screenWidth,
            displayOrientation = displayOrientation,
            mainViewAttributes = myLocation.getLocation().pos.mainViewAttributes
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
        easterEgg3 = 0
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
        updateView(0,-1,listId,position)
        //updateItem(listId,position,-1)
        resetWatchDog()
        hideActionBar()
        easterEgg3++
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

        // Calculate how long to display the splash screen
        val startTime = System.currentTimeMillis()

        // Apply splash theme before super.onCreate()
        setTheme(R.style.Theme_SlycoCafé)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Do initialization work
        initializeApp()

        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                // ✅ Ready to render
                content.viewTreeObserver.removeOnPreDrawListener(this)

                // Notify SplashActivity via callback, flag, or event
                sendBroadcast(Intent("br.com.slyco.slycocafe.MAIN_READY"))

                return true
            }
        })


        // Ensure minimum display time if needed
        val endTime = System.currentTimeMillis()
        val displayTime = endTime - startTime
        val minDisplayTime = 3000 // 1 second minimum

        if (displayTime < minDisplayTime) {
            Handler(Looper.getMainLooper()).postDelayed({
                // Any post-splash operations
            }, minDisplayTime - displayTime)
        }

    }

    private val watchDogCallback = Runnable {
        val intent: Intent = Intent(this, ScreenSaver::class.java)
        if (shoppingCart.returnTotal() == 0.0) intent.putExtra("activateContinueButton", 0)
        else intent.putExtra("activateContinueButton", 1)
        intent.putExtra("locationName", myLocation.getLocation().name)
        intent.putExtra("merchantNumber",myLocation.getLocation().merchant.id.toString())
        startActivityForResult(intent, 3)
    }

    private fun disableWatchdog() {
        Log.d ("DisableWatchdog", "")
        watchDog.removeCallbacks(watchDogCallback)
    }

    private fun resetWatchDog(n:Int = 1) {
        Log.d ("resetWatchDog", "")
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
                //PAYMENT_INTERFACE_FIELDS_NAMES(intentActionStr = paymentInterfaceFieldNames.intentActionStr)
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

                resetWatchDog(100)
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
                saleItems = "",
                merchantReceipt = "",
                customerReceipt = "",
                receiptImage = "",
            )
            finishTransaction(mySaleResponseData)
            Log.d("DemoMode", "shoppingCart.clearCart()")
        }
    }


    private fun finishTransaction(mySaleResponse: saleResponseDC,customerReceipt: String=""){
        val (date, time) = parseTransactionTimestamp(mySaleResponse.transactionTimestamp.toString())

        val myReceipt = Receipt(this,spacing = 8)

        val headerBitmap = myReceipt.generateMerchantReceiptHeaderBitmap(
            cnpj = myLocation.getLocation().merchant.taxId,
            merchantName = "Slyco Cafè - #${myLocation.getLocation().merchant.id}",
            locationName = myLocation.getLocation().name,
            date = date,
            time = time,
            locationId = android_id
        )
        val bodyBitmap = myReceipt.generateReceiptBodyBitmap(shoppingCart.itens,0,0,paymentParameters.amountStr.toInt())

        val customerReceiptBitmap = myReceipt.generateCustomerReceiptBitmap(customerReceipt)

        val footerBitmap = myReceipt.generateFooterBitmap("Recibo sem valor fiscal.\nBom café!")

        val urlKey = generateAccessToken(android_id,mySaleResponse.transactionTimestamp.toString())

        val qrCodeBitmap = myReceipt.generateQrCodeBitmap("https://www.slyco.com.br/receipts/${android_id}/${mySaleResponse.transactionTimestamp.toString()}?key=${urlKey}")

        val postQrCodeBitmap = qrCodeBitmap?.let {
            myReceipt.generatePostQrCodeBitmap("Seu recibo digital?\nEscaneie este QR Code!", fontSize = 16f)
        }

        val fullBitmap = myReceipt.generateFullReceiptBitmap(
            context = this,
            logoResId = R.drawable.slyco_icon,
            headerBitmap = headerBitmap,
            bodyBitmap = bodyBitmap,
            footerBitmap = footerBitmap,
            customerReceiptBitmap = customerReceiptBitmap, // nullable
            qrCodeBitmap = qrCodeBitmap,                // nullable
            postQrCodeBitmap = postQrCodeBitmap         // nullable
        )
        ReceiptHolder.bitmap = fullBitmap
        ReceiptHolder.timestamp = mySaleResponse.transactionTimestamp.toString()
        ReceiptHolder.qrCodeBitmap = qrCodeBitmap

        myInventory.patchtInventoryQty(myLocation.getLocation().id,myLocation.getLocation().items)

        var saleItems = ""

        for (i in 0..myLocation.getLocation().dispenserModel.flavors-1){
            var qty = shoppingCart.getCartItemQuantity(i)
            if (qty > 0){
                saleItems += "${INVENTORYHASH.get(shoppingCart.getFlavor(i).value)}:${i+1}:${qty}:${(shoppingCart.getPrice(i)*100.0f).roundToInt()};"
            }
        }
        Log.d ("Sale Items" , saleItems)
        mySaleResponse.saleItems = saleItems

        val byteArrayOutputStream = ByteArrayOutputStream()
        fullBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)

        val base64Image = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)

        mySaleResponse.receiptImage = base64Image
        postSale (android_id,mySaleResponse)

        releaseCoffee()

        shoppingCart.clearCart()

        updateView(0)
    }

    fun generateAccessToken(path: String, timestamp: String): String {
        val base = "$path+$timestamp+SlycoReceipt"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(base.toByteArray(Charsets.UTF_8))

        val half = hash.size / 2
        val xor = ByteArray(half) { i ->
            (hash[i].toInt() xor hash[i + half].toInt()).toByte()
        }

        return xor.joinToString("") { "%02x".format(it) } // 128-bit token (32 hex chars)
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

                    val dialogBuilder = AlertDialog.Builder(this, R.style.NoActionBarDialog)
                        .setView(dialogView)
                        .setTitle("COMPRAR")

                    // Show the dialog
                    val customDialog = dialogBuilder.create()

                    val totalStr = (shoppingCart.returnTotal() * 100).roundToInt().toString()


                    val timestamp = Timestamp(System.currentTimeMillis())

                    val sdf = SimpleDateFormat("yyyyMMddHHmmss")

                    paymentParameters.amountStr = totalStr
                    paymentParameters.invoiceNumberStr = sdf.format(timestamp)
                    paymentParameters.merchant_TIDStr = myLocation.getLocation().merchant.taxId
                    paymentParameters.endpointStr = myLocation.getLocation().merchant.paymentEndpoint
                    paymentParameters.sitefMIDStr = myLocation.getLocation().merchant.paymentGatewayMid
                    paymentParameters.isv_TIDStr = AppConstants.isvTaxId
                    paymentParameters.comProtocolString = "4"
                    paymentParameters.operatorIdStr = "1"
                    if ((myLocation.getLocation().merchant.tlsFiservToken != null) && (myLocation.getLocation().merchant.tlsFiservToken != "")) {
                        paymentParameters.tlsToken = myLocation.getLocation().merchant.tlsFiservToken
                    }

                    var myButton: ImageView
                    var myText: TextView

                    myButton = dialogView.findViewById<ImageView>(R.id.botaoPix)
                    if (myLocation.getLocation().merchant.enabledPaymetMethods.digitalWallet == true) {
                        myButton.setOnClickListener {
                            paymentParameters.functionIdStr = "122"
                            callPaymentApp(paymentParameters)
                            customDialog.dismiss()
                        }
                        myButton.alpha = AppConstants.imageAlphaEnabled
                        myButton = dialogView.findViewById<ImageView>(R.id.imagePix)
                        myButton.alpha = AppConstants.imageAlphaEnabled

                    }
                    else {
                        myButton.alpha = AppConstants.imageAlphaDisabled
                        myButton = dialogView.findViewById<ImageView>(R.id.imagePix)
                        myButton.alpha = AppConstants.imageAlphaDisabled
                    }

                    myButton = dialogView.findViewById<ImageView>(R.id.botaoCredito)
                    myText = dialogView.findViewById<TextView>(R.id.textBotaoCredito)
                    if (myLocation.getLocation().merchant.enabledPaymetMethods.credit == true) {
                        myButton = dialogView.findViewById<ImageView>(R.id.botaoCredito)
                        myButton.setOnClickListener {
                            paymentParameters.functionIdStr = "0"
                            paymentParameters.enabledTransactionsStr = "26"
                            paymentParameters.restrictionStr = "{TransacoesHabilitadas=26}"
                            paymentParameters.installmentsStr = "1"
                            callPaymentApp(paymentParameters)
                            customDialog.dismiss()
                        }
                        myButton.alpha = AppConstants.imageAlphaEnabled
                        myText.alpha = AppConstants.imageAlphaEnabled
                    }
                    else {
                        myButton.alpha = AppConstants.imageAlphaDisabled
                        myText.alpha = AppConstants.imageAlphaDisabled
                    }

                    myButton = dialogView.findViewById<ImageView>(R.id.botaoDebito)
                    myText = dialogView.findViewById(R.id.textBotaoDebito)
                    if (myLocation.getLocation().merchant.enabledPaymetMethods.debit == true) {
                        myButton.setOnClickListener {
                            paymentParameters.functionIdStr = "2"
                            paymentParameters.restrictionStr = "{TransacoesHabilitadas=16}"
                            callPaymentApp(paymentParameters)
                            customDialog.dismiss()
                        }
                        myButton.alpha = AppConstants.imageAlphaEnabled
                        myText.alpha = AppConstants.imageAlphaEnabled
                    }
                    else {
                        myButton.alpha = AppConstants.imageAlphaDisabled
                        myText.alpha = AppConstants.imageAlphaDisabled
                    }

                    myButton = dialogView.findViewById<ImageView>(R.id.botaoVoucher)
                    myText = dialogView.findViewById(R.id.textBotaoVoucher)
                    if (myLocation.getLocation().merchant.enabledPaymetMethods.voucher == true) {
                        myButton.setOnClickListener {
                            paymentParameters.functionIdStr = "2"
                            paymentParameters.restrictionStr = "{TransacoesHabilitadas=16}"
                            callPaymentApp(paymentParameters)
                            customDialog.dismiss()
                        }
                        myButton.alpha = AppConstants.imageAlphaEnabled
                        myText.alpha = AppConstants.imageAlphaEnabled
                    }
                    else {
                        myButton.alpha = AppConstants.imageAlphaDisabled
                        myText.alpha = AppConstants.imageAlphaDisabled
                    }

                    myButton = dialogView.findViewById<ImageView>(R.id.botaoGrandeSlycoWallet)
                    myText = dialogView.findViewById(R.id.textBotaoSlycoWallet)
                    if (myLocation.getLocation().merchant.enabledPaymetMethods.slycoWallet == true) {
                        myButton = dialogView.findViewById<ImageView>(R.id.botaoGrandeSlycoWallet)
                        myButton.setOnClickListener {
                            toast("SlycoWallet.")
                            customDialog.dismiss()
                        }
                        myButton.alpha = AppConstants.imageAlphaEnabled
                        myText.alpha = AppConstants.imageAlphaEnabled
                    }
                    else {
                        myButton.alpha = AppConstants.imageAlphaDisabled
                        myButton.alpha = AppConstants.imageAlphaDisabled
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

                resetWatchDog(10)

                helpDialog = helperDialog(this)
                helpDialog.show(myLocation.getLocation().name, android_id,  cancellable = true)
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
                    "responseCode: " + data!!.getStringExtra(paymentReturnFields.responseCode)
                )
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "transactionType: " + data!!.getStringExtra(paymentReturnFields.transactionType)
                )
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "installmentType: " + data!!.getStringExtra(paymentReturnFields.installmentType)
                )
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "cashbackAmount: " + data!!.getStringExtra(paymentReturnFields.cashbackAmount)
                )
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "acquirerId: " + data!!.getStringExtra(paymentReturnFields.acquirerId)
                )
                Log.d("@@PRE_PAYMENT_SAMPLE@@", "cardBrand: " + data!!.getStringExtra(paymentReturnFields.cardBrand))
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "sitefTransactionId: " + data!!.getStringExtra(paymentReturnFields.sitefTransactionId)
                )
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "hostTrasactionId: " + data!!.getStringExtra(paymentReturnFields.hostTrasactionId)
                )
                Log.d("@@PRE_PAYMENT_SAMPLE@@", "authCode: " + data!!.getStringExtra(paymentReturnFields.authCode))
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "transactionInstallments: " + data!!.getStringExtra(paymentReturnFields.transactionInstallments)
                )
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "merchantReceipt: " + data!!.getStringExtra(paymentReturnFields.merchantReceipt)
                )
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "customerReceipt: " + data!!.getStringExtra(paymentReturnFields.customerReceipt)
                )
                var returnedFields = data!!.getStringExtra(paymentReturnFields.returnedFields)
                Log.d(
                    "@@PRE_PAYMENT_SAMPLE@@",
                    "returnedFields: $returnedFields"
                )

                val gson = Gson()
                val mapType = object : TypeToken<Map<String, Any>>() {}.type
                val genericMap: Map<String, Any> = gson.fromJson(returnedFields, mapType)
                println(genericMap)

                var merchantReceipt: String = data?.getStringExtra(paymentReturnFields.merchantReceipt)?:""
                var customerReceipt: String = data?.getStringExtra(paymentReturnFields.customerReceipt)?:""

                val mySaleResponseData = saleResponseDC(
                    locationId = android_id,
                    transactionType = data?.getStringExtra(paymentReturnFields.transactionType) as? String ?: "",
                    installmentType = data?.getStringExtra(paymentReturnFields.installmentType) as? String ?: "",
                    cashbackAmount = data?.getStringExtra(paymentReturnFields.cashbackAmount) as? String ?: "",
                    acquirerId = data?.getStringExtra(paymentReturnFields.acquirerId) as? String ?: "",
                    cardBrand = data?.getStringExtra(paymentReturnFields.cardBrand) as? String ?: "",
                    sitefTransactionId = data?.getStringExtra(paymentReturnFields.sitefTransactionId) as? String ?: "",
                    hostTrasactionId = data?.getStringExtra(paymentReturnFields.hostTrasactionId) as? String ?: "",
                    authCode = data?.getStringExtra(paymentReturnFields.authCode) as? String ?: "",
                    transactionInstallments = data?.getStringExtra(paymentReturnFields.transactionInstallments) as? String ?: "",
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
                    saleItems = "",
                    merchantReceipt = merchantReceipt,
                    customerReceipt = customerReceipt,
                    receiptImage = ""
                )

                myLog.log(mySaleResponseData.toString())



                if ((merchantReceipt != null)||
                    (customerReceipt != null)){

                    finishTransaction(mySaleResponseData,customerReceipt)
                }
            }
            catch (e: Exception)  {
                Log.e ("Exception Sales App",e.toString())
                resetWatchDog()
                //shoppingCart.clearCart()
                updateView(0)
            }
        }
        else if (requestCode == 2){
            ReceiptHolder.bitmap = null
        }
        else if (requestCode == 3) {
            try {
                resetWatchDog()
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
        disableWatchdog()
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
            intent.putExtra(
                GlobalVariables.dispenserElements[i].id+AppConstants.dispenserFlavorNameSufix,
                shoppingCart.getCartItemName(i)
            )
            intent.putExtra ( AppConstants.deviceBrandFieldName, myLocation.getLocation().pos.brand)
            intent.putExtra ( AppConstants.deviceModelFieldName, myLocation.getLocation().pos.model)
            intent.putExtra ( AppConstants.deviceHasPrinterFieldName, myLocation.getLocation().pos.hasPrinter)
        }

        intent.putExtra(
            AppConstants.locationNameFieldName, myLocation.getLocation().name
        )

        intent.putExtra(
            AppConstants.locationCodeFieldName, android_id
        )
        resetWatchDog(10)
        startActivityForResult(intent, 2)
    }

    fun setupView(){

        when (myLocation.getLocation().pos.screenFormat) {
            "portrait" ->{
                this.displayOrientation = LinearLayoutManager.VERTICAL
                viewLayout = R.layout.activity_main_smart_terminal
                purchaseSummaryLayout = R.layout.dialog_purchase_summary_portrait
            }
            else ->{
                this.displayOrientation = LinearLayoutManager.HORIZONTAL
                viewLayout = R.layout.activity_main
                purchaseSummaryLayout = R.layout.dialog_purchase_summary
            }
        }

    }

    fun setupPaymentApp(){
        when  (myLocation.getLocation().merchant.paymentApp) {

            "SiTef Sales App" -> {
                Log.d("TRANSACTION", "SiTef Sales App")
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
                Log.d("TRANSACTION", "SiTef Sales App")
            }

            "m-SiTef" -> {
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

                paymentReturnFields = PAYMENT_INTERFACE_RESPONSE_FIELDS(
                    responseCode = "CODRESP",
                    transactionType = "CODTRANS",
                    installmentType = "TIPO_PARC",
                    cashbackAmount = "VLTROCO",
                    acquirerId = "REDE_AUT",
                    cardBrand = "BANDEIRA",
                    sitefTransactionId = "NSU_SITEF",
                    hostTrasactionId = "NSU_HOST",
                    authCode = "COD_AUTORIZACAO",
                    transactionInstallments = "NUM_PARC",
                    merchantReceipt = "VIA_ESTABELECIMENTO",
                    customerReceipt = "VIA_CLIENTE",
                    returnedFields = "TIPO_CAMPOS"
                )
                Log.d("TRANSACTION", "m-SiTef")
            }

            else -> {
                Log.d("TRANSACTION", "none")
            }
        }

    }
    @SuppressLint("MissingInflatedId")
    fun initializeApp(){

        android_id = getAndroidId(this).toUpperCase().chunked(4).joinToString("-")

        defineHomeApp()

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screeenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels


        myLocation = location(android_id,DeviceInfoModule.deviceBrand, DeviceInfoModule.deviceModel)

        Log.d ("DeviceInfo","Name: ${DeviceInfoModule.deviceName}  Brand: ${DeviceInfoModule.deviceBrand}    Model: ${DeviceInfoModule.deviceModel}   DeviceID ${android_id}")

        setupView()

        setupPaymentApp()

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

        val hotspot = findViewById<View>(R.id.tapHotspot)
        hotspot.setOnClickListener {
            val now = System.currentTimeMillis()
            tapTimestamps.add(now)

            // Keep only taps within time window
            tapTimestamps.removeAll { now - it > tapWindowMs }

            if (tapTimestamps.size >= tapThreshold) {
                tapTimestamps.clear()
                restartMainActivity()
            }
        }
    }
    private fun restartMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

}
