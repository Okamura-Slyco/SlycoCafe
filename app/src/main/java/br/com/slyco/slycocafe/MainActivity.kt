package br.com.slyco.slycocafe

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Icon
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
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.w3c.dom.Text

object AppConstants {
    const val MAX_DISPENSER_CAPACITY = 50
}

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
    private var price = 0.0

    constructor(type: NESPRESSO_FLAVORS, qty: Int, price: Double) {
        this.flavor = type
        this.qty = qty
        this.price = price
    }

    fun setQty(qty: Int)
    {
        this.qty = qty
    }
    fun setFlavor(flavor:NESPRESSO_FLAVORS){
        this.flavor = flavor
    }
    fun setPrice(price:Double){
        this.price = price
    }
    fun getQty():Int {
        return this.qty
    }
    fun getFlavor():NESPRESSO_FLAVORS {
        return this.flavor
    }
    fun getPrice():Double {
        return this.price
    }
}

class inventory {
    private var itens = arrayOfNulls<item>(6)

    constructor() {
        this.reset()
    }

    fun reset(){
        this.itens[0] = item(NESPRESSO_FLAVORS.RISTRETTO,AppConstants.MAX_DISPENSER_CAPACITY,1.7)
        this.itens[1] = item(NESPRESSO_FLAVORS.BRAZIL_ORGANIC,AppConstants.MAX_DISPENSER_CAPACITY,2.0)
        this.itens[2] = item(NESPRESSO_FLAVORS.LEGGERO,AppConstants.MAX_DISPENSER_CAPACITY,3.7)
        this.itens[3] = item(NESPRESSO_FLAVORS.DESCAFFEINADO,AppConstants.MAX_DISPENSER_CAPACITY,4.7)
        this.itens[4] = item(NESPRESSO_FLAVORS.INDIA,AppConstants.MAX_DISPENSER_CAPACITY,5.7)
        this.itens[5] = item(NESPRESSO_FLAVORS.CAFFE_VANILIO,AppConstants.MAX_DISPENSER_CAPACITY,6.7)
    }
    fun getQty(flavor: NESPRESSO_FLAVORS) : Int{
        var myItem = itens.find{ it?.getFlavor() == flavor }

        return myItem!!.getQty()
    }

    fun setQty(flavor: NESPRESSO_FLAVORS,qty:Int) {
        var myItem = itens.find{ it?.getFlavor() == flavor }

        myItem!!.setQty(qty)
    }

    fun setPrice(flavor: NESPRESSO_FLAVORS,price:Double) {
        var myItem = itens.find{ it?.getFlavor() == flavor }

        myItem!!.setPrice(price)
    }
    fun getPrice(flavor: NESPRESSO_FLAVORS):Double {
        var myItem = itens.find{ it?.getFlavor() == flavor }

        return myItem!!.getPrice()
    }
}

class cartItem :item {

    constructor(flavor: NESPRESSO_FLAVORS, itemQty: Int, itemValue: Double) : super(flavor, itemQty, itemValue) {

    }
}

class shoppingCart {
    private var itens = arrayOfNulls<cartItem>(6)
    private var total = 0.0

    constructor(inventory: inventory) {
        this.itens[0] = cartItem(NESPRESSO_FLAVORS.RISTRETTO, 0, inventory.getPrice(NESPRESSO_FLAVORS.RISTRETTO))
        this.itens[1] = cartItem(NESPRESSO_FLAVORS.BRAZIL_ORGANIC, 0, inventory.getPrice(NESPRESSO_FLAVORS.BRAZIL_ORGANIC))
        this.itens[2] = cartItem(NESPRESSO_FLAVORS.LEGGERO, 0, inventory.getPrice(NESPRESSO_FLAVORS.LEGGERO))
        this.itens[3] = cartItem(NESPRESSO_FLAVORS.DESCAFFEINADO, 0, inventory.getPrice(NESPRESSO_FLAVORS.DESCAFFEINADO))
        this.itens[4] = cartItem(NESPRESSO_FLAVORS.INDIA, 0, inventory.getPrice(NESPRESSO_FLAVORS.INDIA))
        this.itens[5] = cartItem(NESPRESSO_FLAVORS.CAFFE_VANILIO, 0, inventory.getPrice(NESPRESSO_FLAVORS.CAFFE_VANILIO))
    }

    fun calculateTotal() {
        this.total = 0.0
        for (item in itens) {
            this.total += item!!.getQty() * item.getPrice()
        }
        Log.i("total", "${total}")
    }

    fun addItemToCart(item: NESPRESSO_FLAVORS, qty: Int, inventory: inventory) :Int{
        var myItem = itens.find { it?.getFlavor() == item }
        var myQty = myItem!!.getQty() + qty
        if (myQty >= 0) {
            if (myQty <= inventory.getQty(item)) {
                myItem!!.setQty(myQty)
            }
            else{
                return -1
            }
        }
        else{
            return -2
        }

        Log.i("teste", "${item} ${qty}")
        return 0
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

//        val i = Intent("br.com.softwareexpress.sitef.msitef.ACTIVITY_CLISITEF")
//        i.putExtra("empresaSitef", "00000001")
//        i.putExtra("enderecoSitef", "127.0.0.1;127.0.0.1:20036")
//        i.putExtra("operador", "0001")
//        i.putExtra("data", "20140312")
//        i.putExtra("hora", "150000")
//        i.putExtra("numeroCupom", "1")
//        i.putExtra("numParcelas", "3")
//        i.putExtra("modalidade", "0")
//        i.putExtra("valor", "9000")
//        i.putExtra("CNPJ_CPF", "12345678912345")
//        i.putExtra("timeoutColeta", "30")
//        i.putExtra("acessibilidadeVisual", "0")
//        i.putExtra("comExterna", "1")
//        startPagamentoIntent.launch(i)
//
//        ActivityResultLauncher<Intent> startPagamentoIntent = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(), result -> {
//            if (result.getResultCode() == RESULT_OK) {
//                Intent data = result.getData();
//                log.d("msitef", "CODRESP: " + data.getExtras().getString("CODRESP"));
//                log.d("msitef", "VIA_ESTABELECIMENTO: " + data.getExtras().getString("VIA_ESTABELECIMENTO"));
//                log.d("msitef", "VIA_CLIENTE: " + data.getExtras().getString("VIA_CLIENTE"));
//            }
//        });

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

    var shoppingCart : shoppingCart = shoppingCart(inventory);
    var easterEgg = 0
    var easterEgg1 = 0
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

        var button1 = findViewById<Button>(R.id.buttonEmpty)
        button1.setOnClickListener(listener)
        button1 = findViewById<Button>(R.id.buttonCheckout)
        button1.setOnClickListener(listener)

        var text1 = findViewById<TextView>(R.id.textViewTotalFix)
        text1.setOnClickListener(listener)

        updateView(0)

        }
    val listener= View.OnClickListener { view ->
        var res:Int = 0
        when (view.getId()) {
            R.id.floatingActionButtonItem1Plus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.RISTRETTO,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 0) easterEgg1 = 1
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem2Plus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.BRAZIL_ORGANIC,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 1) easterEgg1 = 2
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem3Plus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.LEGGERO,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 2) easterEgg1 = 3
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem4Plus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.DESCAFFEINADO,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 3) easterEgg1 = 4
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem5Plus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.INDIA,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 4) easterEgg1 = 5
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem6Plus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.CAFFE_VANILIO,1, inventory)
                easterEgg = 0
                if (easterEgg1 == 5) easterEgg1 = 6
                else easterEgg1 = 0
            }

            R.id.floatingActionButtonItem1Minus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.RISTRETTO,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 6) easterEgg1 = 7
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem2Minus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.BRAZIL_ORGANIC,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 7) easterEgg1 = 8
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem3Minus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.LEGGERO,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 8) easterEgg1 = 9
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem4Minus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.DESCAFFEINADO,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 9) easterEgg1 = 10
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem5Minus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.INDIA,-1, inventory)
                easterEgg = 0
                if (easterEgg1 == 10) easterEgg1 = 11
                else easterEgg1 = 0
            }
            R.id.floatingActionButtonItem6Minus -> {
                // Do some work here
                res = shoppingCart.addItemToCart(NESPRESSO_FLAVORS.CAFFE_VANILIO,-1, inventory)
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

        updateView(res)
    }

    fun updateView(res:Int)
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

        if (res == -1)
        {
            toast("Não foi possível adicionar mais itens ao carrinho.")
        }
        else if (res == -2){
            toast("Não foi possível remover o item do carrinho.")
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
        textView1.setText(String.format("R$%.2f",inventory.getPrice(NESPRESSO_FLAVORS.DESCAFFEINADO)))
        textView1 = findViewById<TextView>(R.id.textViewPrice5)
        textView1.setText(String.format("R$%.2f",inventory.getPrice(NESPRESSO_FLAVORS.INDIA)))
        textView1 = findViewById<TextView>(R.id.textViewPrice6)
        textView1.setText(String.format("R$%.2f",inventory.getPrice(NESPRESSO_FLAVORS.CAFFE_VANILIO)))
    }
}