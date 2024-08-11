package br.com.slyco.slycocafe

import android.app.Activity
import android.os.Bundle
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.w3c.dom.Text

enum class NESPRESSO_FLAVORS (val index:Int){
    NONE (0),

    RISTRETTO (1),
    RISTRETTO_INTENSO(2),

    LEGGERO (101),
    FORTE (102),
    FINEZZO (103),
    INTENSO (104),
    DESCAFFEINADO (105),

    BRAZIL_ORGANIC (201),
    INDIA (202),
    GUATEMALA (203),

    CAFFE_NOCCIOLA (301),
    CAFFE_CARAMELLO (302),
    CAFFE_VANILIO (303),
    BIANCO_INTENSO (304),
    BIANCO_DELICATO (305)
}

open class item {
    private var flavor : NESPRESSO_FLAVORS = NESPRESSO_FLAVORS.NONE
    private var qty = 0
    private var cost = 0.0

    constructor(type: NESPRESSO_FLAVORS, qty: Int, cost: Double) {
        this.flavor = type
        this.qty = qty
        this.cost = cost
    }

    fun setQty(qty: Int)
    {
        this.qty = qty
    }
    fun setFlavor(flavor:NESPRESSO_FLAVORS){
        this.flavor = flavor
    }
    fun setCost(cost:Double){
        this.cost = cost
    }
    fun getQty():Int {
        return this.qty
    }
    fun getFlavor():NESPRESSO_FLAVORS {
        return this.flavor
    }
    fun getCost():Double {
        return this.cost
    }
}

class inventory {
    private var itens = arrayOfNulls<item>(6)

    constructor() {
        this.itens[0] = item(NESPRESSO_FLAVORS.RISTRETTO,50,2.7)
        this.itens[1] = item(NESPRESSO_FLAVORS.BRAZIL_ORGANIC,50,3.0)
        this.itens[2] = item(NESPRESSO_FLAVORS.LEGGERO,50,2.7)
        this.itens[3] = item(NESPRESSO_FLAVORS.DESCAFFEINADO,50,2.7)
        this.itens[4] = item(NESPRESSO_FLAVORS.INDIA,50,2.7)
        this.itens[5] = item(NESPRESSO_FLAVORS.CAFFE_VANILIO,50,2.7)
    }

    fun getQty(flavor: NESPRESSO_FLAVORS) : Int{
        var myItem = itens.find{ it?.getFlavor() == flavor }

        return myItem!!.getQty()
    }

    fun setQty(flavor: NESPRESSO_FLAVORS,qty:Int) {
        var myItem = itens.find{ it?.getFlavor() == flavor }

        myItem!!.setQty(qty)
    }

    fun setCost(flavor: NESPRESSO_FLAVORS,cost:Double) {
        var myItem = itens.find{ it?.getFlavor() == flavor }

        myItem!!.setCost(cost)
    }
}

class cartItem :item {

    constructor(flavor: NESPRESSO_FLAVORS, itemQty: Int, itemValue: Double) : super(flavor, itemQty, itemValue) {

    }
}

class shoppingCart {
    private var itens = arrayOfNulls<cartItem>(6)
    private var total = 0.0

    constructor() {
        this.itens[0] = cartItem(NESPRESSO_FLAVORS.RISTRETTO, 0, 2.7)
        this.itens[1] = cartItem(NESPRESSO_FLAVORS.BRAZIL_ORGANIC, 0, 3.0)
        this.itens[2] = cartItem(NESPRESSO_FLAVORS.LEGGERO, 0, 2.7)
        this.itens[3] = cartItem(NESPRESSO_FLAVORS.DESCAFFEINADO, 0, 2.7)
        this.itens[4] = cartItem(NESPRESSO_FLAVORS.INDIA, 0, 2.7)
        this.itens[5] = cartItem(NESPRESSO_FLAVORS.CAFFE_VANILIO, 0, 2.7)
    }

    fun calculateTotal() {
        this.total = 0.0
        for (item in itens) {
            this.total += item!!.getQty() * item.getCost()
        }
        Log.i("total", "${total}")
    }

    fun addItemToCart(item: NESPRESSO_FLAVORS, qty: Int, inventory: inventory) {
        var myItem = itens.find { it?.getFlavor() == item }
        var myQty = myItem!!.getQty() + qty
        if (myQty >= 0) {
            if (myQty <= inventory.getQty(item)) {
                myItem!!.setQty(myQty)
            }
        }

        Log.i("teste", "${item} ${qty}")
    }

    fun getCartItemQuantity(flavor: NESPRESSO_FLAVORS): Int {
        var myItem = itens.find { it?.getFlavor() == flavor }
        return myItem!!.getQty()
    }

    fun clearCart() {
        for (item in itens) {
            item!!.setQty(0)
        }
    }

    fun returnTotal(): Double {

        return total
    }

    fun checkout(inventory: inventory) :Int {
//            val i = Intent("br.com.softwareexpress.sitef.msitef.ACTIVITY_CLISITEF")
//            i.putExtra("empresaSitef", "00000001")
//            i.putExtra("enderecoSitef", "127.0.0.1;127.0.0.1:20036")
//            i.putExtra("operador", "0001")
//            i.putExtra("data", "20140312")
//            i.putExtra("hora", "150000")
//            i.putExtra("numeroCupom", "1")
//            i.putExtra("numParcelas", "3")
//            i.putExtra("modalidade", "0")
//            i.putExtra("valor", "9000")
//            i.putExtra("CNPJ_CPF", "12345678912345")
//            i.putExtra("timeoutColeta", "30")
//            i.putExtra("acessibilidadeVisual", "0")
//            i.putExtra("comExterna", "1")
//            startActivity(i)
        Log.i("Call", "m-SiTef")
        for (item in itens) {
            if (item!!.getQty() <= inventory.getQty(item!!.getFlavor())) {
                inventory!!.setQty(
                    item!!.getFlavor(),
                    inventory!!.getQty(item!!.getFlavor()) - item!!.getQty()
                )
            }
        }
        return 0
    }
}
fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}
class MainActivity : AppCompatActivity() {
    var inventory : inventory = inventory()

    var shoppingCart : shoppingCart = shoppingCart();
    var easterEgg = 0
    var easterEgg1 = 0
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
        var capsulas = 0;
        var button = findViewById<FloatingActionButton>(R.id.floatingActionButtonItem1Plus)
        button.setOnClickListener(listener)

        button = findViewById<FloatingActionButton>(R.id.floatingActionButtonItem2Plus)
        button.setOnClickListener(listener)

        button = findViewById<FloatingActionButton>(R.id.floatingActionButtonItem3Plus)
        button.setOnClickListener(listener)

        button = findViewById<FloatingActionButton>(R.id.floatingActionButtonItem4Plus)
        button.setOnClickListener(listener)

        button = findViewById<FloatingActionButton>(R.id.floatingActionButtonItem5Plus)
        button.setOnClickListener(listener)

        button = findViewById<FloatingActionButton>(R.id.floatingActionButtonItem6Plus)
        button.setOnClickListener(listener)

        button = findViewById<FloatingActionButton>(R.id.floatingActionButtonItem1Minus)
        button.setOnClickListener(listener)

        button = findViewById<FloatingActionButton>(R.id.floatingActionButtonItem2Minus)
        button.setOnClickListener(listener)

        button = findViewById<FloatingActionButton>(R.id.floatingActionButtonItem3Minus)
        button.setOnClickListener(listener)

        button = findViewById<FloatingActionButton>(R.id.floatingActionButtonItem4Minus)
        button.setOnClickListener(listener)

        button = findViewById<FloatingActionButton>(R.id.floatingActionButtonItem5Minus)
        button.setOnClickListener(listener)

        button = findViewById<FloatingActionButton>(R.id.floatingActionButtonItem6Minus)
        button.setOnClickListener(listener)

        var button1 = findViewById<Button>(R.id.buttonEmpty)
        button1.setOnClickListener(listener)
        button1 = findViewById<Button>(R.id.buttonCheckout)
        button1.setOnClickListener(listener)

        var text1 = findViewById<TextView>(R.id.textViewTotalFix)
        text1.setOnClickListener(listener)

        updateView()

        }
    val listener= View.OnClickListener { view ->
        when (view.getId()) {
            R.id.floatingActionButtonItem1Plus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.RISTRETTO,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 0) easterEgg1 = 1
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem2Plus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.BRAZIL_ORGANIC,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 1) easterEgg1 = 2
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem3Plus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.LEGGERO,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 2) easterEgg1 = 3
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem4Plus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.DESCAFFEINADO,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 3) easterEgg1 = 4
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem5Plus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.INDIA,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 4) easterEgg1 = 5
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem6Plus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.CAFFE_VANILIO,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 5) easterEgg1 = 6
                else easterEgg1 = 0
            }

            R.id.floatingActionButtonItem1Minus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.RISTRETTO,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 6) easterEgg1 = 7
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem2Minus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.BRAZIL_ORGANIC,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 7) easterEgg1 = 8
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem3Minus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.LEGGERO,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 8) easterEgg1 = 9
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem4Minus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.DESCAFFEINADO,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 9) easterEgg1 = 10
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem5Minus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.INDIA,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 10) easterEgg1 = 11
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem6Minus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.CAFFE_VANILIO,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 11) easterEgg1 = 12
                else easterEgg1 = 0

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
                    inventory.setQty(NESPRESSO_FLAVORS.DESCAFFEINADO,Integer.valueOf(textView.text.toString()))
                    textView = findViewById<EditText>(R.id.editTextNumberItem5)
                    inventory.setQty(NESPRESSO_FLAVORS.INDIA,Integer.valueOf(textView.text.toString()))
                    textView = findViewById<EditText>(R.id.editTextNumberItem6)
                    inventory.setQty(NESPRESSO_FLAVORS.CAFFE_VANILIO,Integer.valueOf(textView.text.toString()))
                    Log.i("INVENTORY","SET")
                    toast("Inventory SET",Toast.LENGTH_LONG)
                }
            }
            R.id.buttonEmpty -> {
                shoppingCart.clearCart()
                easterEgg = 0
                if (easterEgg1 == 12)
                {
                    var textView = findViewById<EditText>(R.id.editTextNumberItem1)
                    inventory.setQty(NESPRESSO_FLAVORS.RISTRETTO,50)
                    textView = findViewById<EditText>(R.id.editTextNumberItem2)
                    inventory.setQty(NESPRESSO_FLAVORS.BRAZIL_ORGANIC,50)
                    textView = findViewById<EditText>(R.id.editTextNumberItem3)
                    inventory.setQty(NESPRESSO_FLAVORS.LEGGERO,50)
                    textView = findViewById<EditText>(R.id.editTextNumberItem4)
                    inventory.setQty(NESPRESSO_FLAVORS.DESCAFFEINADO,50)
                    textView = findViewById<EditText>(R.id.editTextNumberItem5)
                    inventory.setQty(NESPRESSO_FLAVORS.INDIA,50)
                    textView = findViewById<EditText>(R.id.editTextNumberItem6)
                    inventory.setQty(NESPRESSO_FLAVORS.CAFFE_VANILIO,50)
                    Log.i("INVENTORY","RESET")
                }
                easterEgg1 = 0
                toast("Inventory Reset",Toast.LENGTH_LONG)
            }
            R.id.buttonCheckout -> {
                if (shoppingCart.checkout(inventory) == 0) {
                    shoppingCart.clearCart()
                }

                easterEgg = 0
                easterEgg1 = 0

                toast("Call SiTef Sales App",Toast.LENGTH_LONG)

            }
        }
        shoppingCart.calculateTotal()
        Log.i("easterEgg1","${easterEgg1}")

        updateView()
    }
    fun updateView()
    {
        var textView = findViewById<EditText>(R.id.editTextNumberItem1)
        textView.setText(shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.RISTRETTO).toString())
        textView = findViewById<EditText>(R.id.editTextNumberItem2)
        textView.setText(shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.BRAZIL_ORGANIC).toString())
        textView = findViewById<EditText>(R.id.editTextNumberItem3)
        textView.setText(shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.LEGGERO).toString())
        textView = findViewById<EditText>(R.id.editTextNumberItem4)
        textView.setText(shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.DESCAFFEINADO).toString())
        textView = findViewById<EditText>(R.id.editTextNumberItem5)
        textView.setText(shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.INDIA).toString())
        textView = findViewById<EditText>(R.id.editTextNumberItem6)
        textView.setText(shoppingCart.getCartItemQuantity(NESPRESSO_FLAVORS.CAFFE_VANILIO).toString())

        var textView1 = findViewById<TextView>(R.id.textViewTotal)
        textView1.setText(String.format("%.2f",shoppingCart.returnTotal()))


    }
}