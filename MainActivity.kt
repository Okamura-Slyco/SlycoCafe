package br.com.slyco.slycocafe

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.util.NoSuchPropertyException
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

object AppConstants {
    const val MAX_DISPENSER_CAPACITY = 50
    const val ON_STOCK_ALPHA = 255
    const val OUT_OF_STOCK_ALPHA = 25
    const val ON_STOCK_ALPHA_FLOAT = 1.0f
    const val OUT_OF_STOCK_ALPHA_FLOAT = OUT_OF_STOCK_ALPHA.toFloat() / ON_STOCK_ALPHA.toFloat()
    const val DISPENSER_PID = 51459
    const val DISPENSER_VID = 5971
    const val ACTION_USB_PERMISSION = "com.android.pinpad.USB_PERMISSION"
}

enum class NESPRESSO_FLAVORS(val index: Int) {
    NONE(0),

    RISTRETTO(1), RISTRETTO_INTENSO(2),

    LEGGERO(101), FORTE(102), FINEZZO(103), INTENSO(104), DESCAFFEINADO(105),

    BRAZIL_ORGANIC(201), INDIA(202), GUATEMALA(203),

    CAFFE_NOCCIOLA(301), CAFFE_CARAMELLO(302), CAFFE_VANILIO(303), BIANCO_INTENSO(304), BIANCO_DELICATO(
        305
    )
}

open class Item(type: NESPRESSO_FLAVORS, private var qty: Int, private var price: Double) {
    private var flavor: NESPRESSO_FLAVORS = type

    fun setQty(qty: Int) {
        this.qty = qty
    }

    fun getQty(): Int {
        return this.qty
    }

    fun getFlavor(): NESPRESSO_FLAVORS {
        return this.flavor
    }

    fun getPrice(): Double {
        return this.price
    }
}

class Inventory {
    private var itens = arrayOfNulls<Item>(6)

    fun reset() {
        this.itens[0] = Item(NESPRESSO_FLAVORS.RISTRETTO, AppConstants.MAX_DISPENSER_CAPACITY, 2.5)
        this.itens[1] =
            Item(NESPRESSO_FLAVORS.BRAZIL_ORGANIC, AppConstants.MAX_DISPENSER_CAPACITY, 2.75)
        this.itens[2] = Item(NESPRESSO_FLAVORS.LEGGERO, AppConstants.MAX_DISPENSER_CAPACITY, 2.5)
        this.itens[3] =
            Item(NESPRESSO_FLAVORS.DESCAFFEINADO, AppConstants.MAX_DISPENSER_CAPACITY, 2.5)
        this.itens[4] = Item(NESPRESSO_FLAVORS.INDIA, AppConstants.MAX_DISPENSER_CAPACITY, 2.75)
        this.itens[5] =
            Item(NESPRESSO_FLAVORS.CAFFE_VANILIO, AppConstants.MAX_DISPENSER_CAPACITY, 2.75)
    }

    fun getQty(flavor: NESPRESSO_FLAVORS): Int {
        val myItem = itens.find { it?.getFlavor() == flavor }
        return myItem!!.getQty()
    }

    fun setQty(flavor: NESPRESSO_FLAVORS, qty: Int) {
        val myItem = itens.find { it?.getFlavor() == flavor }
        myItem!!.setQty(qty)
    }

    fun getPrice(flavor: NESPRESSO_FLAVORS): Double {
        val myItem = itens.find { it?.getFlavor() == flavor }
        return myItem!!.getPrice()
    }
}

class CartItem(flavor: NESPRESSO_FLAVORS, itemQty: Int, itemValue: Double) : Item(
    flavor, itemQty, itemValue
)

class ShoppingCart(inventory: Inventory) {
    private var itens = arrayOfNulls<CartItem>(6)
    private var total = 0.0

    init {
        this.itens[0] = CartItem(
            NESPRESSO_FLAVORS.RISTRETTO, 0, inventory.getPrice(NESPRESSO_FLAVORS.RISTRETTO)
        )
        this.itens[1] = CartItem(
            NESPRESSO_FLAVORS.BRAZIL_ORGANIC,
            0,
            inventory.getPrice(NESPRESSO_FLAVORS.BRAZIL_ORGANIC)
        )
        this.itens[2] =
            CartItem(NESPRESSO_FLAVORS.LEGGERO, 0, inventory.getPrice(NESPRESSO_FLAVORS.LEGGERO))
        this.itens[3] = CartItem(
            NESPRESSO_FLAVORS.DESCAFFEINADO, 0, inventory.getPrice(NESPRESSO_FLAVORS.DESCAFFEINADO)
        )
        this.itens[4] =
            CartItem(NESPRESSO_FLAVORS.INDIA, 0, inventory.getPrice(NESPRESSO_FLAVORS.INDIA))
        this.itens[5] = CartItem(
            NESPRESSO_FLAVORS.CAFFE_VANILIO, 0, inventory.getPrice(NESPRESSO_FLAVORS.CAFFE_VANILIO)
        )
    }

    private fun calculateTotal() {
        this.total = 0.0
        for (item in itens) {
            this.total += item!!.getQty() * item.getPrice()
        }
        Log.i("total", "$total")
    }

    fun addItemToCart(item: NESPRESSO_FLAVORS, qty: Int, inventory: Inventory): Int {
        val myItem = itens.find { it?.getFlavor() == item }
        val myQty = myItem!!.getQty() + qty
        if (myQty >= 0) {
            if (myQty <= inventory.getQty(item)) {
                myItem.setQty(myQty)
            } else
                return -1
        } else
            return -2
        this.calculateTotal()
        Log.i("teste", "$item $qty")
        return 0
    }

    fun getCartItemQuantity(flavor: NESPRESSO_FLAVORS): Int {
        val myItem = itens.find { it?.getFlavor() == flavor }
        return myItem!!.getQty()
    }

    fun clearCart() {
        for (item in itens) {
            item!!.setQty(0)
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

class MainActivity : AppCompatActivity() {
    private var inventory: Inventory = Inventory()

    private var shoppingCart: ShoppingCart = ShoppingCart(this.inventory)
    private var easterEgg = 0
    private var easterEgg1 = 0
    private var dispenserUsbDevice: UsbDevice? = null
    private var dispenserUsbConnection: UsbDeviceConnection? = null

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        updatePriceTags()
        var button = findViewById<MaterialButton>(R.id.floatingActionButtonItem1Plus)
        button.setOnClickListener(listener)

        button = findViewById(R.id.floatingActionButtonItem2Plus)
        button.setOnClickListener(listener)

        button = findViewById(R.id.floatingActionButtonItem3Plus)
        button.setOnClickListener(listener)

        button = findViewById(R.id.floatingActionButtonItem4Plus)
        button.setOnClickListener(listener)

        button = findViewById(R.id.floatingActionButtonItem5Plus)
        button.setOnClickListener(listener)

        button = findViewById(R.id.floatingActionButtonItem6Plus)
        button.setOnClickListener(listener)

        button = findViewById(R.id.floatingActionButtonItem1Minus)
        button.setOnClickListener(listener)

        button = findViewById(R.id.floatingActionButtonItem2Minus)
        button.setOnClickListener(listener)

        button = findViewById(R.id.floatingActionButtonItem3Minus)
        button.setOnClickListener(listener)

        button = findViewById(R.id.floatingActionButtonItem4Minus)
        button.setOnClickListener(listener)

        button = findViewById(R.id.floatingActionButtonItem5Minus)
        button.setOnClickListener(listener)

        button = findViewById(R.id.floatingActionButtonItem6Minus)
        button.setOnClickListener(listener)

        var productImage = findViewById<ImageView>(R.id.imageViewCapsula1)
        productImage.setOnClickListener(listener)

        productImage = findViewById(R.id.imageViewCapsula2)
        productImage.setOnClickListener(listener)

        productImage = findViewById(R.id.imageViewCapsula3)
        productImage.setOnClickListener(listener)

        productImage = findViewById(R.id.imageViewCapsula4)
        productImage.setOnClickListener(listener)

        productImage = findViewById(R.id.imageViewCapsula5)
        productImage.setOnClickListener(listener)

        productImage = findViewById(R.id.imageViewCapsula6)
        productImage.setOnClickListener(listener)

        var button1 = findViewById<Button>(R.id.buttonEmpty)
        button1.setOnClickListener(listener)
        button1 = findViewById(R.id.buttonCheckout)
        button1.setOnClickListener(listener)
        val text1 = findViewById<TextView>(R.id.textViewTotalFix)
        text1.setOnClickListener(listener)
        updateView(0)
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList
        var iterator = 0

        for (device in deviceList.values) {
            Log.i("Slyco-USB", "$iterator - Device Name: ${device.deviceName}")
            Log.i("Slyco-USB", "$iterator - Product Name: ${device.productName.toString()}")
            Log.i("Slyco-USB", "$iterator - Manufacturer Name: ${device.manufacturerName.toString()}")
            Log.i("Slyco-USB", "$iterator - Product Id: ${device.productId}")
            Log.i("Slyco-USB", "$iterator - Vendor Id: ${device.vendorId}")
            if ((device.productId == AppConstants.DISPENSER_PID) && (device.vendorId == AppConstants.DISPENSER_VID)) {
                Log.i("Slyco-USB", "$iterator - Match!!!")
                dispenserUsbDevice = device
                break
            }
            iterator++
        }
        if (dispenserUsbDevice != null) {
            if (!usbManager.hasPermission(dispenserUsbDevice)) {
                Log.i("Slyco-USB", "Setando permissoes do dispenser")
            } else
                Log.i("Slyco-USB", "Permissoes OK!")
            dispenserUsbConnection = usbManager.openDevice(dispenserUsbDevice)
            if (dispenserUsbConnection == null) {
                throw NoSuchPropertyException("Slyco-USB - nao conseguiu abrir conexao com dispositivo")
            } else {
                Log.i("Slyco-USB", "Conexao OK!")
                val counter = dispenserUsbDevice?.interfaceCount
                Log.i("Slyco-USB", "Interfaces: $counter")
                if (counter == 0) throw NoSuchPropertyException("Slyco-USB - Sem interfaces")

            }
        } else
            Log.w("Slyco-USB", "Sem dispenser USB identificado. Trabalhando no modo nao integrado.")
    }

    private val listener = View.OnClickListener { view ->
        var res: Int = 0
        var bUpdateView = true
        when (view.id) {
            R.id.floatingActionButtonItem1Plus, R.id.imageViewCapsula1 -> {
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.RISTRETTO, 1, inventory)
                easterEgg = 0
                easterEgg1 = if (easterEgg1 == 0) 1
                else 0
            }

            R.id.floatingActionButtonItem2Plus, R.id.imageViewCapsula2 -> {
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.BRAZIL_ORGANIC, 1, inventory)
                easterEgg = 0
                easterEgg1 = if (easterEgg1 == 1) 2
                else 0
            }

            R.id.floatingActionButtonItem3Plus, R.id.imageViewCapsula3 -> {
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.LEGGERO, 1, inventory)
                easterEgg = 0
                easterEgg1 = if (easterEgg1 == 2) 3
                else 0
            }

            R.id.floatingActionButtonItem4Plus, R.id.imageViewCapsula4 -> {
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.DESCAFFEINADO, 1, inventory)
                easterEgg = 0
                easterEgg1 = if (easterEgg1 == 3) 4
                else 0
            }

            R.id.floatingActionButtonItem5Plus, R.id.imageViewCapsula5 -> {
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.INDIA, 1, inventory)
                easterEgg = 0
                easterEgg1 = if (easterEgg1 == 4) 5
                else 0
            }

            R.id.floatingActionButtonItem6Plus, R.id.imageViewCapsula6 -> {
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.CAFFE_VANILIO, 1, inventory)
                easterEgg = 0
                easterEgg1 = if (easterEgg1 == 5) 6
                else 0
            }

            R.id.floatingActionButtonItem1Minus -> {
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.RISTRETTO, -1, inventory)
                easterEgg = 0
                easterEgg1 = if (easterEgg1 == 6) 7
                else 0
            }

            R.id.floatingActionButtonItem2Minus -> {
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.BRAZIL_ORGANIC, -1, inventory)
                easterEgg = 0
                easterEgg1 = if (easterEgg1 == 7) 8
                else 0
            }

            R.id.floatingActionButtonItem3Minus -> {
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.LEGGERO, -1, inventory)
                easterEgg = 0
                easterEgg1 = if (easterEgg1 == 8) 9
                else 0
            }

            R.id.floatingActionButtonItem4Minus -> {
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.DESCAFFEINADO, -1, inventory)
                easterEgg = 0
                easterEgg1 = if (easterEgg1 == 9) 10
                else 0
            }

            R.id.floatingActionButtonItem5Minus -> {
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.INDIA, -1, inventory)
                easterEgg = 0
                easterEgg1 = if (easterEgg1 == 10) 11
                else 0
            }

            R.id.floatingActionButtonItem6Minus -> {
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.CAFFE_VANILIO, -1, inventory)
                easterEgg = 0
                easterEgg1 = if (easterEgg1 == 11) 12
                else 0
            }

            R.id.textViewTotalFix -> {
                easterEgg++
                if (easterEgg == 20) {
                    easterEgg = 0
                    var textView = findViewById<EditText>(R.id.editTextNumberItem1)
                    inventory.setQty(
                        NESPRESSO_FLAVORS.RISTRETTO,
                        Integer.valueOf(textView.text.toString())
                    )
                    textView = findViewById(R.id.editTextNumberItem2)
                    inventory.setQty(
                        NESPRESSO_FLAVORS.BRAZIL_ORGANIC,
                        Integer.valueOf(textView.text.toString())
                    )
                    textView = findViewById(R.id.editTextNumberItem3)
                    inventory.setQty(
                        NESPRESSO_FLAVORS.LEGGERO,
                        Integer.valueOf(textView.text.toString())
                    )
                    textView = findViewById(R.id.editTextNumberItem4)
                    inventory.setQty(
                        NESPRESSO_FLAVORS.DESCAFFEINADO,
                        Integer.valueOf(textView.text.toString())
                    )
                    textView = findViewById(R.id.editTextNumberItem5)
                    inventory.setQty(
                        NESPRESSO_FLAVORS.INDIA,
                        Integer.valueOf(textView.text.toString())
                    )
                    textView = findViewById(R.id.editTextNumberItem6)
                    inventory.setQty(
                        NESPRESSO_FLAVORS.CAFFE_VANILIO,
                        Integer.valueOf(textView.text.toString())
                    )
                    Log.i("INVENTORY", "SET")
                    toast("Inventory SET")
                }
            }

            R.id.buttonEmpty -> {
                shoppingCart.clearCart()
                easterEgg = 0
                if (easterEgg1 == 12) {
                    inventory.reset()
                    var textView = findViewById<EditText>(R.id.editTextNumberItem1)
                    textView.setText("0")
                    textView = findViewById(R.id.editTextNumberItem2)
                    textView.setText("0")
                    textView = findViewById(R.id.editTextNumberItem3)
                    textView.setText("0")
                    textView = findViewById(R.id.editTextNumberItem4)
                    textView.setText("0")
                    textView = findViewById(R.id.editTextNumberItem5)
                    textView.setText("0")
                    textView = findViewById(R.id.editTextNumberItem6)
                    textView.setText("0")

                    updatePriceTags()
                    Log.i("INVENTORY", "RESET")
                }
                easterEgg1 = 0
                toast("Inventory Reset")
            }

            R.id.buttonCheckout -> {
                if (shoppingCart.returnTotal() > 0.0) {
                    val totalStr = (shoppingCart.returnTotal() * 100).toInt().toString()
                    val intent = Intent("com.fiserv.sitef.action.TRANSACTION")
                    intent.putExtra("merchantSiTef", "DEVRELBR")
                    intent.putExtra("sitefIP", "https://tls-uat.fiservapp.com")
                    intent.putExtra("merchantTaxId", "04988631000111")
                    intent.putExtra("functionId", "0")
                    intent.putExtra("transactionAmount", totalStr)
                    intent.putExtra("transactionInstallments", "1")
                    intent.putExtra("enabledTransactions", "16")
                    startActivityForResult(intent, 1)

                    toast("Call SiTef Sales App")
                    bUpdateView = false
                } else
                    toast("Adicione itens ao carrinho.")
                easterEgg = 0
                easterEgg1 = 0
            }
        }
        Log.i("easterEgg1", "$easterEgg1")
        if (bUpdateView) updateView(res)
    }

    private fun updateView(res: Int) {
        if (res == 0) {
            var textView = findViewById<EditText>(R.id.editTextNumberItem1)
            textView.setText(
                shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.RISTRETTO).toString()
            )
            textView = findViewById(R.id.editTextNumberItem2)
            textView.setText(
                shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.BRAZIL_ORGANIC).toString()
            )
            textView = findViewById(R.id.editTextNumberItem3)
            textView.setText(shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.LEGGERO).toString())
            textView = findViewById(R.id.editTextNumberItem4)
            textView.setText(
                shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.DESCAFFEINADO).toString()
            )
            textView = findViewById(R.id.editTextNumberItem5)
            textView.setText(shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.INDIA).toString())
            textView = findViewById(R.id.editTextNumberItem6)
            textView.setText(
                shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.CAFFE_VANILIO).toString()
            )
            val textView1 = findViewById<TextView>(R.id.textViewTotal)
            textView1.text = String.format("%.2f", shoppingCart.returnTotal())
            if ((inventory.getQty(NESPRESSO_FLAVORS.RISTRETTO) - shoppingCart.getCartItemQuantity(
                    NESPRESSO_FLAVORS.RISTRETTO
                )) <= 0
            ) {
                findViewById<ImageView>(R.id.imageViewCapsula1).imageAlpha =
                    AppConstants.OUT_OF_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem1Plus).alpha =
                    AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            } else {
                findViewById<ImageView>(R.id.imageViewCapsula1).imageAlpha =
                    AppConstants.ON_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem1Plus).alpha =
                    AppConstants.ON_STOCK_ALPHA_FLOAT
            }
            if (shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.RISTRETTO) <= 0) findViewById<Button>(
                R.id.floatingActionButtonItem1Minus
            ).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            else findViewById<Button>(R.id.floatingActionButtonItem1Minus).alpha =
                AppConstants.ON_STOCK_ALPHA_FLOAT

            if ((inventory.getQty(NESPRESSO_FLAVORS.BRAZIL_ORGANIC) - shoppingCart.getCartItemQuantity(
                    NESPRESSO_FLAVORS.BRAZIL_ORGANIC
                )) <= 0
            ) {
                findViewById<ImageView>(R.id.imageViewCapsula2).imageAlpha =
                    AppConstants.OUT_OF_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem2Plus).alpha =
                    AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            } else {
                findViewById<ImageView>(R.id.imageViewCapsula2).imageAlpha =
                    AppConstants.ON_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem2Plus).alpha =
                    AppConstants.ON_STOCK_ALPHA_FLOAT
            }
            if (shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.BRAZIL_ORGANIC) <= 0) findViewById<Button>(
                R.id.floatingActionButtonItem2Minus
            ).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            else findViewById<Button>(R.id.floatingActionButtonItem2Minus).alpha =
                AppConstants.ON_STOCK_ALPHA_FLOAT

            if ((inventory.getQty(NESPRESSO_FLAVORS.LEGGERO) - shoppingCart.getCartItemQuantity(
                    NESPRESSO_FLAVORS.LEGGERO
                )) <= 0
            ) {
                findViewById<ImageView>(R.id.imageViewCapsula3).imageAlpha =
                    AppConstants.OUT_OF_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem3Plus).alpha =
                    AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            } else {
                findViewById<ImageView>(R.id.imageViewCapsula3).imageAlpha =
                    AppConstants.ON_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem3Plus).alpha =
                    AppConstants.ON_STOCK_ALPHA_FLOAT
            }
            if (shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.LEGGERO) <= 0) findViewById<Button>(
                R.id.floatingActionButtonItem3Minus
            ).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            else findViewById<Button>(R.id.floatingActionButtonItem3Minus).alpha =
                AppConstants.ON_STOCK_ALPHA_FLOAT

            if ((inventory.getQty(NESPRESSO_FLAVORS.DESCAFFEINADO) - shoppingCart.getCartItemQuantity(
                    NESPRESSO_FLAVORS.DESCAFFEINADO
                )) <= 0
            ) {
                findViewById<ImageView>(R.id.imageViewCapsula4).imageAlpha =
                    AppConstants.OUT_OF_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem4Plus).alpha =
                    AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            } else {
                findViewById<ImageView>(R.id.imageViewCapsula4).imageAlpha =
                    AppConstants.ON_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem4Plus).alpha =
                    AppConstants.ON_STOCK_ALPHA_FLOAT
            }
            if (shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.DESCAFFEINADO) <= 0) findViewById<Button>(
                R.id.floatingActionButtonItem4Minus
            ).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            else findViewById<Button>(R.id.floatingActionButtonItem4Minus).alpha =
                AppConstants.ON_STOCK_ALPHA_FLOAT

            if ((inventory.getQty(NESPRESSO_FLAVORS.INDIA) - shoppingCart.getCartItemQuantity(
                    NESPRESSO_FLAVORS.INDIA
                )) <= 0
            ) {
                findViewById<ImageView>(R.id.imageViewCapsula5).imageAlpha =
                    AppConstants.OUT_OF_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem5Plus).alpha =
                    AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            } else {
                findViewById<ImageView>(R.id.imageViewCapsula5).imageAlpha =
                    AppConstants.ON_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem5Plus).alpha =
                    AppConstants.ON_STOCK_ALPHA_FLOAT
            }
            if (shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.INDIA) <= 0) findViewById<Button>(
                R.id.floatingActionButtonItem5Minus
            ).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            else findViewById<Button>(R.id.floatingActionButtonItem5Minus).alpha =
                AppConstants.ON_STOCK_ALPHA_FLOAT

            if ((inventory.getQty(NESPRESSO_FLAVORS.CAFFE_VANILIO) - shoppingCart.getCartItemQuantity(
                    NESPRESSO_FLAVORS.CAFFE_VANILIO
                )) <= 0
            ) {
                findViewById<ImageView>(R.id.imageViewCapsula6).imageAlpha =
                    AppConstants.OUT_OF_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem6Plus).alpha =
                    AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            } else {
                findViewById<ImageView>(R.id.imageViewCapsula6).imageAlpha =
                    AppConstants.ON_STOCK_ALPHA
                findViewById<Button>(R.id.floatingActionButtonItem6Plus).alpha =
                    AppConstants.ON_STOCK_ALPHA_FLOAT
            }
            if (shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.CAFFE_VANILIO) <= 0) findViewById<Button>(
                R.id.floatingActionButtonItem6Minus
            ).alpha = AppConstants.OUT_OF_STOCK_ALPHA_FLOAT
            else findViewById<Button>(R.id.floatingActionButtonItem6Minus).alpha =
                AppConstants.ON_STOCK_ALPHA_FLOAT
        } else if (res == -1)
            toast("Não foi possível adicionar mais itens ao carrinho.")
        else if (res == -2)
            toast("Não foi possível remover o item do carrinho.")
        else if (res == -3)
            toast("Selecione Checkout para tentar novamente.")
    }

    private fun updatePriceTags() {
        var textView1 = findViewById<TextView>(R.id.textViewPrice1)
        textView1.text = String.format("R$%.2f", inventory.getPrice(NESPRESSO_FLAVORS.RISTRETTO))
        textView1 = findViewById(R.id.textViewPrice2)
        textView1.text = String.format("R$%.2f", inventory.getPrice(NESPRESSO_FLAVORS.BRAZIL_ORGANIC))
        textView1 = findViewById(R.id.textViewPrice3)
        textView1.text = String.format("R$%.2f", inventory.getPrice(NESPRESSO_FLAVORS.LEGGERO))
        textView1 = findViewById(R.id.textViewPrice4)
        textView1.text = String.format("R$%.2f", inventory.getPrice(NESPRESSO_FLAVORS.DESCAFFEINADO))
        textView1 = findViewById(R.id.textViewPrice5)
        textView1.text = String.format("R$%.2f", inventory.getPrice(NESPRESSO_FLAVORS.INDIA))
        textView1 = findViewById(R.id.textViewPrice6)
        textView1.text = String.format("R$%.2f", inventory.getPrice(NESPRESSO_FLAVORS.CAFFE_VANILIO))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val imgViewCheckList = findViewById<ImageView>(R.id.imgViewChecklist)
        imgViewCheckList.isInvisible = true

        Glide.with(this).asGif().load(R.drawable.checklist).into(imgViewCheckList);

        Log.d(
            "@@PRE_PAYMENT_SAMPLE@@",
            this.javaClass.name + " | " + (object :
                Any() {}.javaClass.enclosingMethod?.name) + " | " + "RequestCode: " + requestCode
        )

        Log.d("@@PRE_PAYMENT_SAMPLE@@", "requestCode: $requestCode")
        Log.d("@@PRE_PAYMENT_SAMPLE@@", "resultCode: $resultCode")

        Log.d("@@PRE_PAYMENT_SAMPLE@@", "responseCode: " + data!!.getStringExtra("responseCode"))
        Log.d(
            "@@PRE_PAYMENT_SAMPLE@@",
            "transactionType: " + data.getStringExtra("transactionType")
        )
        Log.d(
            "@@PRE_PAYMENT_SAMPLE@@",
            "installmentType: " + data.getStringExtra("installmentType")
        )
        Log.d("@@PRE_PAYMENT_SAMPLE@@", "cashbackAmount: " + data.getStringExtra("cashbackAmount"))
        Log.d("@@PRE_PAYMENT_SAMPLE@@", "acquirerId: " + data.getStringExtra("acquirerId"))
        Log.d("@@PRE_PAYMENT_SAMPLE@@", "cardBrand: " + data.getStringExtra("cardBrand"))
        Log.d(
            "@@PRE_PAYMENT_SAMPLE@@",
            "sitefTransactionId: " + data.getStringExtra("sitefTransactionId")
        )
        Log.d(
            "@@PRE_PAYMENT_SAMPLE@@",
            "hostTrasactionId: " + data.getStringExtra("hostTrasactionId")
        )
        Log.d("@@PRE_PAYMENT_SAMPLE@@", "authCode: " + data.getStringExtra("authCode"))
        Log.d(
            "@@PRE_PAYMENT_SAMPLE@@",
            "transactionInstallments: " + data.getStringExtra("transactionInstallments")
        )
        Log.d(
            "@@PRE_PAYMENT_SAMPLE@@",
            "merchantReceipt: " + data.getStringExtra("merchantReceipt")
        )
        Log.d(
            "@@PRE_PAYMENT_SAMPLE@@",
            "customerReceipt: " + data.getStringExtra("customerReceipt")
        )
        Log.d("@@PRE_PAYMENT_SAMPLE@@", "returnedFields: " + data.getStringExtra("returnedFields"))

        imgViewCheckList.isInvisible = false

        val cupom: String? = data.getStringExtra("merchantReceipt")

        if (cupom != null) {
            this.inventory.setQty(
                NESPRESSO_FLAVORS.RISTRETTO,
                inventory.getQty(NESPRESSO_FLAVORS.RISTRETTO) - shoppingCart.getCartItemQuantity(
                    NESPRESSO_FLAVORS.RISTRETTO
                )
            )
            this.inventory.setQty(
                NESPRESSO_FLAVORS.BRAZIL_ORGANIC,
                inventory.getQty(NESPRESSO_FLAVORS.BRAZIL_ORGANIC) - shoppingCart.getCartItemQuantity(
                    NESPRESSO_FLAVORS.BRAZIL_ORGANIC
                )
            )
            this.inventory.setQty(
                NESPRESSO_FLAVORS.LEGGERO,
                inventory.getQty(NESPRESSO_FLAVORS.LEGGERO) - shoppingCart.getCartItemQuantity(
                    NESPRESSO_FLAVORS.LEGGERO
                )
            )
            this.inventory.setQty(
                NESPRESSO_FLAVORS.DESCAFFEINADO,
                inventory.getQty(NESPRESSO_FLAVORS.DESCAFFEINADO) - shoppingCart.getCartItemQuantity(
                    NESPRESSO_FLAVORS.DESCAFFEINADO
                )
            )
            this.inventory.setQty(
                NESPRESSO_FLAVORS.INDIA,
                inventory.getQty(NESPRESSO_FLAVORS.INDIA) - shoppingCart.getCartItemQuantity(
                    NESPRESSO_FLAVORS.INDIA
                )
            )
            this.inventory.setQty(
                NESPRESSO_FLAVORS.CAFFE_VANILIO,
                inventory.getQty(NESPRESSO_FLAVORS.CAFFE_VANILIO) - shoppingCart.getCartItemQuantity(
                    NESPRESSO_FLAVORS.CAFFE_VANILIO
                )
            )
            shoppingCart.clearCart()
            updateView(0)
        }
    }
}