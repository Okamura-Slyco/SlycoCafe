package br.com.slyco.slycocafe

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Looper
import android.os.Looper.*
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import android.os.Handler
import android.widget.ProgressBar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


object AppConstants {
    const val DISPENSERS_QTY = 6
    const val MAX_DISPENSER_CAPACITY = 50
    const val ON_STOCK_ALPHA = 255
    const val OUT_OF_STOCK_ALPHA = 25
    const val ON_STOCK_ALPHA_FLOAT = 1.0f
    const val OUT_OF_STOCK_ALPHA_FLOAT = OUT_OF_STOCK_ALPHA.toFloat()/ON_STOCK_ALPHA.toFloat()
    const val DISPENSER_PID = 60000
    const val DISPENSER_VID = 4292
    const val ACTION_USB_PERMISSION = "com.android.pinpad.USB_PERMISSION"
    const val INACTIVITY_TIMEOUT = 30000L // 30s (em milissegundos)
}

enum class NESPRESSO_FLAVORS (val value:Int){
    NONE (0),

    RISTRETTO (R.drawable.ristretto),
    RISTRETTO_INTENSO(R.drawable.intenso),

    LEGGERO (R.drawable.leggero),
    FORTE (R.drawable.forte),
    FINEZZO (R.drawable.finezzo),
    INTENSO (R.drawable.intenso),
    DESCAFFEINADO (R.drawable.descafeinado),

    BRAZIL_ORGANIC (R.drawable.brazilorganic),
    INDIA (R.drawable.india),
    GUATEMALA (R.drawable.guatemala),

    CAFFE_NOCCIOLA (R.drawable.caffenocciola),
    CAFFE_CARAMELLO (R.drawable.caffecaramello),
    CAFFE_VANILIO (R.drawable.caffevanilio),
    BIANCO_INTENSO (R.drawable.intenso),
    BIANCO_DELICATO (R.drawable.biancodelicato);

    companion object {
        infix fun from(value: Int): NESPRESSO_FLAVORS? = NESPRESSO_FLAVORS.values().firstOrNull { it.value == value }
    }
}

data class item(
    var flavor: NESPRESSO_FLAVORS? = NESPRESSO_FLAVORS.NONE,
    var qty: Int?,
    var price: Float?,
    var flavorIndex: Int?
)

class inventory {
    private var itens = arrayOfNulls<item>(6)

    private lateinit var dbRef : DatabaseReference
    private var loaded = false

    constructor() {
        dbRef = FirebaseDatabase.getInstance().getReference("slyco-cafe")
    this.reset()
    this.loadDb()
    }
    fun loadDb(){

//        dbRef.addValueEventListener(object: ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    //val myItems:List<item> = snapshot.children.map { dataSnapshot -> dataSnapshot.getValue(item::class.java)!!.copy() }
//                    var iter = 0
//                    for(itemSnap in snapshot.children) {
//                        var myItem = itemSnap.getValue(item::class.java)
//                        Log.d("a", "b")
//                        val index = itemSnap.key!!.toInt()
//                        if (index in 0..AppConstants.DISPENSERS_QTY - 1) {
//                            itens[index] = item(
//                                NESPRESSO_FLAVORS.from(1)!!,
//                                30,
//                                1.3f,
//                                NESPRESSO_FLAVORS.RISTRETTO.value
//                            )
//
//                        //}
//                    }
//                }
//                loaded = true
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//        })

        dbRef.child("slyco-cafe").child("0").get().addOnSuccessListener {
          Log.i("firebase", "Got value ${it.value}")
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
//        dbRef.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if(snapshot.exists()) {
//                    var iter = 0
//                    for(itemSnap in snapshot.children){
//                        itens[iter++] = itemSnap.getValue(item::class.java)
//                    }
//                }
//                Log.d("Inventory LoadDB","loaded =)")
//                loaded = true
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//        })

    }

    fun isLoaded():Boolean{
        return this.loaded
    }

    fun reset(){
        this.itens[0] = item(NESPRESSO_FLAVORS.RISTRETTO,AppConstants.MAX_DISPENSER_CAPACITY,2.75f,NESPRESSO_FLAVORS.RISTRETTO.value)
        this.itens[1] = item(NESPRESSO_FLAVORS.BRAZIL_ORGANIC,AppConstants.MAX_DISPENSER_CAPACITY,3.00f,NESPRESSO_FLAVORS.BRAZIL_ORGANIC.value)
        this.itens[2] = item(NESPRESSO_FLAVORS.LEGGERO,AppConstants.MAX_DISPENSER_CAPACITY,2.75f,NESPRESSO_FLAVORS.LEGGERO.value)
        this.itens[3] = item(NESPRESSO_FLAVORS.GUATEMALA,AppConstants.MAX_DISPENSER_CAPACITY,3.00f,NESPRESSO_FLAVORS.GUATEMALA.value)
        this.itens[4] = item(NESPRESSO_FLAVORS.CAFFE_VANILIO,AppConstants.MAX_DISPENSER_CAPACITY,3.00f,NESPRESSO_FLAVORS.CAFFE_VANILIO.value)
        this.itens[5] = item(NESPRESSO_FLAVORS.DESCAFFEINADO,AppConstants.MAX_DISPENSER_CAPACITY,2.75f,NESPRESSO_FLAVORS.DESCAFFEINADO.value)

        var i:Int = 0
        for (i in 0..AppConstants.DISPENSERS_QTY-1) {
            dbRef.child(i.toString()).setValue(this.itens[i])
                .addOnFailureListener{ err->
                    Log.e("inventory reset", "Error adding ${this.itens[i]?.flavor.toString()}: ${err.message}")
                }
        }

    }

    fun getFlavorIndex (index:Int): Int?{
        return this.itens[index]?.flavorIndex
    }
    fun getFlavor (index:Int):NESPRESSO_FLAVORS? {
        return this.itens[index]?.flavor
    }
    fun getQty(flavor: NESPRESSO_FLAVORS) : Int? {
        var myItem = itens.find{ it?.flavor == flavor }

        return myItem?.qty
    }

    fun setQty(flavor: NESPRESSO_FLAVORS,qty:Int) {
        var myItem = itens.find{ it?.flavor == flavor }

        myItem!!.qty = qty

        dbRef.child(myItem.flavor.toString()).setValue(myItem)
            .addOnFailureListener{ err->
                Log.e("inventory reset", "Error adding ${flavor.toString()}: ${err.message}")
            }

    }

    fun setPrice(flavor: NESPRESSO_FLAVORS,price:Float) {
        var myItem = itens.find{ it?.flavor == flavor }

        myItem!!.price = price
        dbRef.child(myItem?.flavor.toString()).setValue(myItem)
            .addOnFailureListener{ err->
                Log.e("inventory reset", "Error adding ${flavor.toString()}: ${err.message}")
            }
    }

    fun getPrice(flavor: NESPRESSO_FLAVORS): Float {
        var myItem = itens.find{ it?.flavor == flavor }

        return myItem!!.price!!
    }
}



class shoppingCart {
    private var itens = arrayOfNulls<item>(6)
    private var total = 0.0

    private val customDateFormat: String
        get() = SimpleDateFormat("yyMMdd", Locale.ROOT).format(Date())
    private val customTimeFormat: String
        get() = SimpleDateFormat("HHmmss",Locale.ROOT).format(Date())

    constructor(inventory: inventory) {
        this.itens[0] = item(NESPRESSO_FLAVORS.RISTRETTO, 0, inventory.getPrice(NESPRESSO_FLAVORS.RISTRETTO),0)
        this.itens[1] = item(NESPRESSO_FLAVORS.BRAZIL_ORGANIC, 0, inventory.getPrice(NESPRESSO_FLAVORS.BRAZIL_ORGANIC),1)
        this.itens[2] = item(NESPRESSO_FLAVORS.LEGGERO, 0, inventory.getPrice(NESPRESSO_FLAVORS.LEGGERO),2)
        this.itens[3] = item(NESPRESSO_FLAVORS.GUATEMALA, 0, inventory.getPrice(NESPRESSO_FLAVORS.GUATEMALA),3)
        this.itens[4] = item(NESPRESSO_FLAVORS.CAFFE_VANILIO, 0, inventory.getPrice(NESPRESSO_FLAVORS.CAFFE_VANILIO),4)
        this.itens[5] = item(NESPRESSO_FLAVORS.DESCAFFEINADO, 0, inventory.getPrice(NESPRESSO_FLAVORS.DESCAFFEINADO),5)
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

    fun getCartItemQuantity(flavor: NESPRESSO_FLAVORS): Int {
        var myItem = itens.find { it?.flavor == flavor }
        return myItem!!.qty!!
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

}



fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}


class MainActivity<Bitmap> : AppCompatActivity() {
    var inventory : inventory = inventory()

    lateinit var shoppingCart : shoppingCart
    var easterEgg = 0
    var easterEgg1 = 0
    var easterEgg2 = 0

    private lateinit var watchDog: Handler

    private var serial: String? = null

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {

        shoppingCart = shoppingCart(inventory)
        val actionBar: androidx.appcompat.app.ActionBar? = supportActionBar
        if (actionBar != null) actionBar.hide()
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        updatePriceTags()

        var image = findViewById<ImageView>(R.id.imageViewCapsula1)
        inventory.getFlavorIndex(0)?.let { image.setImageResource(it) }
        image = findViewById<ImageView>(R.id.imageViewCapsula2)
        inventory.getFlavorIndex(1)?.let { image.setImageResource(it) }
        image = findViewById<ImageView>(R.id.imageViewCapsula3)
        inventory.getFlavorIndex(2)?.let { image.setImageResource(it) }
        image = findViewById<ImageView>(R.id.imageViewCapsula4)
        inventory.getFlavorIndex(3)?.let { image.setImageResource(it) }
        image = findViewById<ImageView>(R.id.imageViewCapsula5)
        inventory.getFlavorIndex(4)?.let { image.setImageResource(it) }
        image = findViewById<ImageView>(R.id.imageViewCapsula6)
        inventory.getFlavorIndex(5)?.let { image.setImageResource(it) }

        var button = findViewById<MaterialButton>(R.id.floatingActionButtonItem1Plus)
        button.setOnClickListener(listener)

        button = findViewById<MaterialButton>(R.id.floatingActionButtonItem2Plus)
        button.setOnClickListener(listener)

        button = findViewById<MaterialButton>(R.id.floatingActionButtonItem3Plus)
        button.setOnClickListener(listener)

        button = findViewById<MaterialButton>(R.id.floatingActionButtonItem4Plus)
        button.setOnClickListener(listener)

        button = findViewById<MaterialButton>(R.id.floatingActionButtonItem5Plus)
        button.setOnClickListener(listener)

        button = findViewById<MaterialButton>(R.id.floatingActionButtonItem6Plus)
        button.setOnClickListener(listener)

        button = findViewById<MaterialButton>(R.id.floatingActionButtonItem1Minus)
        button.setOnClickListener(listener)

        button = findViewById<MaterialButton>(R.id.floatingActionButtonItem2Minus)
        button.setOnClickListener(listener)

        button = findViewById<MaterialButton>(R.id.floatingActionButtonItem3Minus)
        button.setOnClickListener(listener)

        button = findViewById<MaterialButton>(R.id.floatingActionButtonItem4Minus)
        button.setOnClickListener(listener)

        button = findViewById<MaterialButton>(R.id.floatingActionButtonItem5Minus)
        button.setOnClickListener(listener)

        button = findViewById<MaterialButton>(R.id.floatingActionButtonItem6Minus)
        button.setOnClickListener(listener)

        var productImage = findViewById<ImageView>(R.id.imageViewCapsula1)
        productImage.setOnClickListener(listener)

        productImage = findViewById<ImageView>(R.id.imageViewCapsula2)
        productImage.setOnClickListener(listener)

        productImage = findViewById<ImageView>(R.id.imageViewCapsula3)
        productImage.setOnClickListener(listener)

        productImage = findViewById<ImageView>(R.id.imageViewCapsula4)
        productImage.setOnClickListener(listener)

        productImage = findViewById<ImageView>(R.id.imageViewCapsula5)
        productImage.setOnClickListener(listener)

        productImage = findViewById<ImageView>(R.id.imageViewCapsula6)
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

    private fun resetWatchDog(n:Int = 1) {
        watchDog.removeCallbacks(watchDogCallback)
        watchDog.postDelayed(watchDogCallback, n* AppConstants.INACTIVITY_TIMEOUT)
    }

    val listener= View.OnClickListener { view ->
        var res:Int = 0
        var bUpdateView = true
        resetWatchDog()
        when (view.getId()) {
            R.id.floatingActionButtonItem1Plus, R.id.imageViewCapsula1 -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.RISTRETTO,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 0) easterEgg1 = 1
                else easterEgg1 = 0

                if (easterEgg2 == 0) easterEgg2 = 1
                else easterEgg2 = 0
            }
            R.id.floatingActionButtonItem2Plus,R.id.imageViewCapsula2 -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.BRAZIL_ORGANIC,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 1) easterEgg1 = 2
                else easterEgg1 = 0
                easterEgg2 = 0
            }
            R.id.floatingActionButtonItem3Plus, R.id.imageViewCapsula3 -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.LEGGERO,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 2) easterEgg1 = 3
                else easterEgg1 = 0

                if (easterEgg2 == 1) easterEgg2 = 2
                else easterEgg2 = 0
            }
            R.id.floatingActionButtonItem4Plus, R.id.imageViewCapsula4 -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.GUATEMALA,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 3) easterEgg1 = 4
                else easterEgg1 = 0
                easterEgg2 = 0
            }
            R.id.floatingActionButtonItem5Plus, R.id.imageViewCapsula5 -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.CAFFE_VANILIO,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 4) easterEgg1 = 5
                else easterEgg1 = 0

                if (easterEgg2 == 2) easterEgg2 = 3
                else easterEgg2 = 0
            }
            R.id.floatingActionButtonItem6Plus, R.id.imageViewCapsula6 -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.DESCAFFEINADO,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 5) easterEgg1 = 6
                else easterEgg1 = 0
                easterEgg2 = 0
            }

            R.id.floatingActionButtonItem1Minus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.RISTRETTO,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 6) easterEgg1 = 7
                else easterEgg1 = 0
                easterEgg2 = 0
            }
            R.id.floatingActionButtonItem2Minus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.BRAZIL_ORGANIC,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 7) easterEgg1 = 8
                else easterEgg1 = 0
                easterEgg2 = 0
            }
            R.id.floatingActionButtonItem3Minus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.LEGGERO,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 8) easterEgg1 = 9
                else easterEgg1 = 0
                easterEgg2 = 0
            }
            R.id.floatingActionButtonItem4Minus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.GUATEMALA,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 9) easterEgg1 = 10
                else easterEgg1 = 0
                easterEgg2 = 0
            }
            R.id.floatingActionButtonItem5Minus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.CAFFE_VANILIO,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 10) easterEgg1 = 11
                else easterEgg1 = 0
                easterEgg2 = 0
            }
            R.id.floatingActionButtonItem6Minus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.DESCAFFEINADO,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 11) easterEgg1 = 12
                else easterEgg1 = 0
                easterEgg2 = 0
            }
            R.id.textViewTotalFix -> {
                easterEgg++

                if (easterEgg == 20) {
                    easterEgg = 0
                    var textView = findViewById<EditText>(R.id.editTextNumberItem1)
                    inventory.setQty(NESPRESSO_FLAVORS.RISTRETTO,Integer.valueOf(textView.text.toString()))
                    textView = findViewById<EditText>(R.id.editTextNumberItem2)
                    inventory.setQty(NESPRESSO_FLAVORS.BRAZIL_ORGANIC,Integer.valueOf(textView.text.toString()))
                    textView = findViewById<EditText>(R.id.editTextNumberItem3)
                    inventory.setQty(NESPRESSO_FLAVORS.LEGGERO,Integer.valueOf(textView.text.toString()))
                    textView = findViewById<EditText>(R.id.editTextNumberItem4)
                    inventory.setQty(NESPRESSO_FLAVORS.GUATEMALA,Integer.valueOf(textView.text.toString()))
                    textView = findViewById<EditText>(R.id.editTextNumberItem5)
                    inventory.setQty(NESPRESSO_FLAVORS.CAFFE_VANILIO,Integer.valueOf(textView.text.toString()))
                    textView = findViewById<EditText>(R.id.editTextNumberItem6)
                    inventory.setQty(NESPRESSO_FLAVORS.DESCAFFEINADO,Integer.valueOf(textView.text.toString()))
                    Log.i("INVENTORY","SET")
                    toast("Inventory SET")
                }
                easterEgg2 = 0
            }
            R.id.buttonEmpty -> {
                shoppingCart.clearCart()
                easterEgg = 0
                if (easterEgg1 == 12)
                {
                    inventory.reset()
                    var textView = findViewById<EditText>(R.id.editTextNumberItem1)
                    textView.setText("0")
                    textView = findViewById<EditText>(R.id.editTextNumberItem2)
                    textView.setText("0")
                    textView = findViewById<EditText>(R.id.editTextNumberItem3)
                    textView.setText("0")
                    textView = findViewById<EditText>(R.id.editTextNumberItem4)
                    textView.setText("0")
                    textView = findViewById<EditText>(R.id.editTextNumberItem5)
                    textView.setText("0")
                    textView = findViewById<EditText>(R.id.editTextNumberItem6)
                    textView.setText("0")

                    updatePriceTags()



                    Log.i("INVENTORY","RESET")
                }

                if (easterEgg2 == 3) {
                    sendDmp()
                    toast("Send DMP")
                }

                easterEgg2 = 0
                easterEgg1 = 0
                toast("Inventory Reset")
            }
            R.id.buttonCheckout -> {
                if (shoppingCart.returnTotal() > 0.0) {
                    var textMessage = "\n"

                    // Função para adicionar item ao texto
                    fun addItemToTextMessage(flavor: NESPRESSO_FLAVORS) {
                        val quantity = shoppingCart.getCartItemQuantity(flavor)
                        if (quantity >= 1) {
                            val price = inventory.getPrice(flavor)!!.toFloat()
                            val total = price * quantity
                            textMessage += "\n${flavor.name.replace("_", " ")} - ${quantity}x R$${String.format("%.2f", price)} = R$${String.format("%.2f", total)}\n"
                        }
                    }

                    addItemToTextMessage(NESPRESSO_FLAVORS.RISTRETTO)
                    addItemToTextMessage(NESPRESSO_FLAVORS.BRAZIL_ORGANIC)
                    addItemToTextMessage(NESPRESSO_FLAVORS.LEGGERO)
                    addItemToTextMessage(NESPRESSO_FLAVORS.GUATEMALA)
                    addItemToTextMessage(NESPRESSO_FLAVORS.CAFFE_VANILIO)
                    addItemToTextMessage(NESPRESSO_FLAVORS.DESCAFFEINADO)

                    textMessage += "\n\nValor total da compra: R$ ${String.format("%.2f", shoppingCart.returnTotal())}"

                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Resumo da Compra")
                    builder.setMessage(textMessage)

                    builder.setPositiveButton("Pagar") { dialog, _ ->
                        val totalStr = (shoppingCart.returnTotal() * 100).toInt().toString()

                        val timestamp = Timestamp(System.currentTimeMillis())

                        val sdf = SimpleDateFormat("yyyyMMddHHmmss")

                        val intent: Intent = Intent("com.fiserv.sitef.action.TRANSACTION")
                        intent.putExtra("merchantTaxId", "55833084000136")
                        intent.putExtra("isvTaxId", "55833084000136")
                        intent.putExtra("functionId", "0")
                        intent.putExtra("transactionAmount", totalStr)
                        intent.putExtra("invoiceNumber",sdf.format(timestamp) )

                        resetWatchDog(10)

                        Log.d("INVOICENUMBER",sdf.format(timestamp))
                        startActivityForResult(intent, 1)

                        toast("Call SiTef Sales App")
                        bUpdateView = false
                    }

                    // Botão de Cancelar
                    builder.setNegativeButton("Voltar") { dialog, _ ->
                        //toast("Compra cancelada.")
                        dialog.dismiss()
                    }

                    // Mostrar o AlertDialog
                    val dialog = builder.create()
                    dialog.show()
                    val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    positiveButton.setTextColor(ContextCompat.getColor(this, R.color.green))
                    positiveButton.setTypeface(null, Typeface.BOLD)

                    val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    negativeButton.setTextColor(ContextCompat.getColor(this, R.color.red))
                    negativeButton.setTypeface(null, Typeface.BOLD)
                } else {
                    toast("Adicione itens ao carrinho.")
                }

                easterEgg = 0
                easterEgg1 = 0
                easterEgg2 = 0
            }
        }
        Log.i("easterEgg1","${easterEgg1}")

        if (bUpdateView == true) updateView(res)
    }

    fun intentCallback(){

    }

    fun sendDmp(){

        val intent: Intent = Intent("com.fiserv.sitef.action.TRANSACTION")
        intent.putExtra("merchantTaxId", "55833084000136")
        intent.putExtra("isvTaxId", "55833084000136")
        intent.putExtra("functionId", "121")
        intent.putExtra("transactionAmount", "0")
        resetWatchDog(10)
        startActivityForResult(intent, 1)

    }

    fun updateView(res:Int)
    {
        if (res == 0) {
            var textView = findViewById<EditText>(R.id.editTextNumberItem1)
            textView.setText(
                shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.RISTRETTO).toString()
            )
            textView = findViewById<EditText>(R.id.editTextNumberItem2)
            textView.setText(
                shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.BRAZIL_ORGANIC).toString()
            )
            textView = findViewById<EditText>(R.id.editTextNumberItem3)
            textView.setText(shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.LEGGERO).toString())
            textView = findViewById<EditText>(R.id.editTextNumberItem4)
            textView.setText(
                shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.GUATEMALA).toString()
            )
            textView = findViewById<EditText>(R.id.editTextNumberItem5)
            textView.setText(shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.CAFFE_VANILIO).toString())
            textView = findViewById<EditText>(R.id.editTextNumberItem6)
            textView.setText(
                shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.DESCAFFEINADO).toString()
            )

            var textView1 = findViewById<TextView>(R.id.textViewTotal)
            textView1.setText(String.format("%.2f", shoppingCart.returnTotal()))


            if ((inventory.getQty(NESPRESSO_FLAVORS.RISTRETTO)!! - shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.RISTRETTO)) <=0 ) {
                findViewById<ImageView>(R.id.imageViewCapsula1).imageAlpha = AppConstants.OUT_OF_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem1Plus).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            }
            else {
                findViewById<ImageView>(R.id.imageViewCapsula1).imageAlpha = AppConstants.ON_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem1Plus).alpha = AppConstants.ON_STOCK_ALPHA_FLOAT
            }
            if (shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.RISTRETTO) <=0) findViewById<Button>(R.id.floatingActionButtonItem1Minus).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            else findViewById<Button>(R.id.floatingActionButtonItem1Minus).alpha = AppConstants.ON_STOCK_ALPHA_FLOAT

            if ((inventory.getQty(NESPRESSO_FLAVORS.BRAZIL_ORGANIC)!! - shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.BRAZIL_ORGANIC)) <=0 ) {
                findViewById<ImageView>(R.id.imageViewCapsula2).imageAlpha = AppConstants.OUT_OF_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem2Plus).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            }
            else {
                findViewById<ImageView>(R.id.imageViewCapsula2).imageAlpha = AppConstants.ON_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem2Plus).alpha = AppConstants.ON_STOCK_ALPHA_FLOAT
            }
            if (shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.BRAZIL_ORGANIC) <=0) findViewById<Button>(R.id.floatingActionButtonItem2Minus).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            else findViewById<Button>(R.id.floatingActionButtonItem2Minus).alpha = AppConstants.ON_STOCK_ALPHA_FLOAT

            if ((inventory.getQty(NESPRESSO_FLAVORS.LEGGERO)!! - shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.LEGGERO)) <=0 ) {
                findViewById<ImageView>(R.id.imageViewCapsula3).imageAlpha = AppConstants.OUT_OF_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem3Plus).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            }
            else {
                findViewById<ImageView>(R.id.imageViewCapsula3).imageAlpha = AppConstants.ON_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem3Plus).alpha = AppConstants.ON_STOCK_ALPHA_FLOAT
            }
            if (shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.LEGGERO) <=0) findViewById<Button>(R.id.floatingActionButtonItem3Minus).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            else findViewById<Button>(R.id.floatingActionButtonItem3Minus).alpha = AppConstants.ON_STOCK_ALPHA_FLOAT

            if ((inventory.getQty(NESPRESSO_FLAVORS.GUATEMALA)!! - shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.GUATEMALA)) <=0 ) {
                findViewById<ImageView>(R.id.imageViewCapsula4).imageAlpha = AppConstants.OUT_OF_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem4Plus).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            }
            else {
                findViewById<ImageView>(R.id.imageViewCapsula4).imageAlpha = AppConstants.ON_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem4Plus).alpha = AppConstants.ON_STOCK_ALPHA_FLOAT
            }
            if (shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.GUATEMALA) <=0) findViewById<Button>(R.id.floatingActionButtonItem4Minus).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            else findViewById<Button>(R.id.floatingActionButtonItem4Minus).alpha = AppConstants.ON_STOCK_ALPHA_FLOAT

            if ((inventory.getQty(NESPRESSO_FLAVORS.CAFFE_VANILIO)!! - shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.CAFFE_VANILIO)) <=0 ) {
                findViewById<ImageView>(R.id.imageViewCapsula5).imageAlpha = AppConstants.OUT_OF_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem5Plus).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            }
            else {
                findViewById<ImageView>(R.id.imageViewCapsula5).imageAlpha = AppConstants.ON_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem5Plus).alpha = AppConstants.ON_STOCK_ALPHA_FLOAT
            }
            if (shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.CAFFE_VANILIO) <=0) findViewById<Button>(R.id.floatingActionButtonItem5Minus).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            else findViewById<Button>(R.id.floatingActionButtonItem5Minus).alpha = AppConstants.ON_STOCK_ALPHA_FLOAT

            if ((inventory.getQty(NESPRESSO_FLAVORS.DESCAFFEINADO)!! - shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.DESCAFFEINADO)) <=0 ) {
                findViewById<ImageView>(R.id.imageViewCapsula6).imageAlpha = AppConstants.OUT_OF_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem6Plus).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            }
            else {
                findViewById<ImageView>(R.id.imageViewCapsula6).imageAlpha = AppConstants.ON_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem6Plus).alpha = AppConstants.ON_STOCK_ALPHA_FLOAT
            }
            if (shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.DESCAFFEINADO) <=0) findViewById<Button>(R.id.floatingActionButtonItem6Minus).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            else findViewById<Button>(R.id.floatingActionButtonItem6Minus).alpha = AppConstants.ON_STOCK_ALPHA_FLOAT
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

    fun updatePriceTags(){

        var textView1 = findViewById<TextView>(R.id.textViewPrice1)
        textView1.setText(String.format("R$%.2f",inventory.getPrice(NESPRESSO_FLAVORS.RISTRETTO)))
        textView1 = findViewById<TextView>(R.id.textViewPrice2)
        textView1.setText(String.format("R$%.2f",inventory.getPrice(NESPRESSO_FLAVORS.BRAZIL_ORGANIC)))
        textView1 = findViewById<TextView>(R.id.textViewPrice3)
        textView1.setText(String.format("R$%.2f",inventory.getPrice(NESPRESSO_FLAVORS.LEGGERO)))
        textView1 = findViewById<TextView>(R.id.textViewPrice4)
        textView1.setText(String.format("R$%.2f",inventory.getPrice(NESPRESSO_FLAVORS.GUATEMALA)))
        textView1 = findViewById<TextView>(R.id.textViewPrice5)
        textView1.setText(String.format("R$%.2f",inventory.getPrice(NESPRESSO_FLAVORS.CAFFE_VANILIO)))
        textView1 = findViewById<TextView>(R.id.textViewPrice6)
        textView1.setText(String.format("R$%.2f",inventory.getPrice(NESPRESSO_FLAVORS.DESCAFFEINADO)))
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
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
            Log.d("@@PRE_PAYMENT_SAMPLE@@", "acquirerId: " + data!!.getStringExtra("acquirerId"))
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
                this.inventory.setQty(
                    NESPRESSO_FLAVORS.RISTRETTO,
                    inventory.getQty(NESPRESSO_FLAVORS.RISTRETTO)!! - shoppingCart.getCartItemQuantity(
                        NESPRESSO_FLAVORS.RISTRETTO
                    )
                )
                this.inventory.setQty(
                    NESPRESSO_FLAVORS.BRAZIL_ORGANIC,
                    inventory.getQty(NESPRESSO_FLAVORS.BRAZIL_ORGANIC)!! - shoppingCart.getCartItemQuantity(
                        NESPRESSO_FLAVORS.BRAZIL_ORGANIC
                    )
                )
                this.inventory.setQty(
                    NESPRESSO_FLAVORS.LEGGERO,
                    inventory.getQty(NESPRESSO_FLAVORS.LEGGERO)!! - shoppingCart.getCartItemQuantity(
                        NESPRESSO_FLAVORS.LEGGERO
                    )
                )
                this.inventory.setQty(
                    NESPRESSO_FLAVORS.GUATEMALA,
                    inventory.getQty(NESPRESSO_FLAVORS.GUATEMALA)!! - shoppingCart.getCartItemQuantity(
                        NESPRESSO_FLAVORS.GUATEMALA
                    )
                )
                this.inventory.setQty(
                    NESPRESSO_FLAVORS.CAFFE_VANILIO,
                    inventory.getQty(NESPRESSO_FLAVORS.CAFFE_VANILIO)!! - shoppingCart.getCartItemQuantity(
                        NESPRESSO_FLAVORS.CAFFE_VANILIO
                    )
                )
                this.inventory.setQty(
                    NESPRESSO_FLAVORS.DESCAFFEINADO,
                    inventory.getQty(NESPRESSO_FLAVORS.DESCAFFEINADO)!! - shoppingCart.getCartItemQuantity(
                        NESPRESSO_FLAVORS.DESCAFFEINADO
                    )
                )


                val dispenserBufferString =
                    "A".repeat(shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.RISTRETTO)) +
                            "B".repeat(shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.BRAZIL_ORGANIC)) +
                            "C".repeat(shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.LEGGERO)) +
                            "D".repeat(shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.GUATEMALA)) +
                            "E".repeat(shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.CAFFE_VANILIO)) +
                            "F".repeat(shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.DESCAFFEINADO)) + "\n"

                val intent: Intent = Intent(this, DispenserProgress::class.java)
                intent.putExtra(
                    "A_itemQty",
                    shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.RISTRETTO)
                )
                intent.putExtra(
                    "B_itemQty",
                    shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.BRAZIL_ORGANIC)
                )
                intent.putExtra(
                    "C_itemQty",
                    shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.LEGGERO)
                )
                intent.putExtra(
                    "D_itemQty",
                    shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.GUATEMALA)
                )
                intent.putExtra(
                    "E_itemQty",
                    shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.CAFFE_VANILIO)
                )
                intent.putExtra(
                    "F_itemQty",
                    shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.DESCAFFEINADO)
                )
                resetWatchDog(10)
                startActivityForResult(intent, 2)

                shoppingCart.clearCart()

                updateView(0)
            }
        }
        else if (requestCode == 3)
        {
            val retVal = data!!.getStringExtra("action")
            when (retVal){
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
        resetWatchDog()

        //printTextAsImage("", cupom, "", applicationContext, account)

        //val intent: Intent = Intent(this, MainActivity::class.java)
        //startActivityForResult(intent, 1)
    }
}
