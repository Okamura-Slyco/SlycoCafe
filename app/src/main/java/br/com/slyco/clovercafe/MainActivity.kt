package br.com.slyco.clovercafe

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

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

class inventary {
    private var itens = arrayOfNulls<item>(6)

    constructor() {
        this.itens[0] = item(NESPRESSO_FLAVORS.RISTRETTO,50,2.7)
        this.itens[1] = item(NESPRESSO_FLAVORS.BRAZIL_ORGANIC,50,3.0)
        this.itens[2] = item(NESPRESSO_FLAVORS.LEGGERO,50,2.7)
        this.itens[3] = item(NESPRESSO_FLAVORS.DESCAFFEINADO,50,2.7)
        this.itens[4] = item(NESPRESSO_FLAVORS.INDIA,50,2.7)
        this.itens[5] = item(NESPRESSO_FLAVORS.CAFFE_VANILIO,50,2.7)
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
        this.itens[0] = cartItem(NESPRESSO_FLAVORS.RISTRETTO,50,2.7)
        this.itens[1] = cartItem(NESPRESSO_FLAVORS.BRAZIL_ORGANIC,50,3.0)
        this.itens[2] = cartItem(NESPRESSO_FLAVORS.LEGGERO,50,2.7)
        this.itens[3] = cartItem(NESPRESSO_FLAVORS.DESCAFFEINADO,50,2.7)
        this.itens[4] = cartItem(NESPRESSO_FLAVORS.INDIA,50,2.7)
        this.itens[5] = cartItem(NESPRESSO_FLAVORS.CAFFE_VANILIO,50,2.7)
    }

    fun calculateTotal() {
        this.total = 0.0
        for(item in itens)
        {
            this.total += item!!.getQty() * item.getCost()

            Log.i ("total", "${total}")
        }
    }
    fun cleanCart(){

    }
    fun addItemToCart(item:NESPRESSO_FLAVORS,qty: Int){
        var myItem = itens.find{ it?.getFlavor() == item }

        Log.i("teste","${item} ${qty}")
    }
    fun getCartItem(){

    }
    fun clearCart(){
        for (item in itens)
        {

        }
    }
    fun returnTotal():Double{

        return total
    }
}

class MainActivity : AppCompatActivity() {
    var inventary : inventary = inventary()

    var shoppingCart : shoppingCart = shoppingCart();

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
// set on-click listener for ImageView
//        image_view.setOnClickListener {
// your code here
//            capsulas++
//            val text_view = findViewById(R.id.textViewTotal) as TextView
//            text_view.setText(capsulas.toString())

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

        }
    val listener= View.OnClickListener { view ->
        when (view.getId()) {
            R.id.floatingActionButtonItem1Plus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.RISTRETTO,1)

            }
            R.id.floatingActionButtonItem2Plus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.BRAZIL_ORGANIC,1)
            }
            R.id.floatingActionButtonItem3Plus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.LEGGERO,1)
            }
            R.id.floatingActionButtonItem4Plus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.DESCAFFEINADO,1)
            }
            R.id.floatingActionButtonItem5Plus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.INDIA,1)
            }
            R.id.floatingActionButtonItem6Plus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.CAFFE_VANILIO,1)
            }

            R.id.floatingActionButtonItem1Minus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.RISTRETTO,-1)
            }
            R.id.floatingActionButtonItem2Minus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.BRAZIL_ORGANIC,-1)
            }
            R.id.floatingActionButtonItem3Minus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.LEGGERO,-1)
            }
            R.id.floatingActionButtonItem4Minus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.DESCAFFEINADO,-1)
            }
            R.id.floatingActionButtonItem5Minus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.INDIA,-1)
            }
            R.id.floatingActionButtonItem6Minus -> {
                // Do some work here
                shoppingCart.addItemToCart(NESPRESSO_FLAVORS.CAFFE_VANILIO,-1)
            }
        }
        shoppingCart.calculateTotal()
        shoppingCart.returnTotal()
    }
}