package br.com.slyco.slycocafe

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.zxing.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import com.clover.sdk.util.CustomerMode
import com.clover.sdk.v3.scanner.BarcodeResult
import com.clover.sdk.v3.scanner.BarcodeScanner
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okio.ByteString.Companion.decodeBase64

data class QrCodeData(
    var uid:String?="",
    var tid:String?="",
    var v:Int?=0,
    var cts:String?=""
)


class SlycoWallet : AppCompatActivity() {

    private var mBarcodeScanner: BarcodeScanner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        enableEdgeToEdge()
        setContentView(R.layout.slyco_wallet_purchase)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        var thisIntent = Intent()

        thisIntent?.putExtra("hostTrasactionId", "TODO")
        thisIntent?.putExtra("authCode", "TODO")
        thisIntent?.putExtra("pan", "TODO")
        thisIntent?.putExtra("idMethod", "TODO")
        thisIntent?.putExtra("transactionTimestamp", "123456789")

        mBarcodeScanner = BarcodeScanner(this)
        mBarcodeScanner!!.startScan()

        registerBarcodeScanner()


        setResult(Activity.RESULT_OK, thisIntent)
        //finish()
    }

    private val barcodeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val barcodeResult = BarcodeResult(intent)
            if (barcodeResult.isBarcodeAction) {
                lifecycleScope.launch {
                    getBarcode(barcodeResult.barcode)
                }
            } else {

            }
        }
    }
    private fun registerBarcodeScanner() {
        registerReceiver(barcodeReceiver, IntentFilter("com.clover.BarcodeBroadcast"))
    }

    private fun unregisterBarcodeScanner() {
        unregisterReceiver(barcodeReceiver)
    }

    private fun showPinDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.pin_entry_dialog, null)
        val pinEditText = dialogLayout.findViewById<EditText>(R.id.pinEditText)

        // Create dialog instance first
        val dialog = builder.setView(dialogLayout)
            .setTitle("Enter PIN")
            .setPositiveButton("OK", null) // Set to null to override default behavior
            .setNegativeButton("Cancel") { dialog, _ ->
                // Hide keyboard before dismissing
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(pinEditText.windowToken, 0)
                dialog.cancel()
            }
            .create()

        // Add TextWatcher after dialog creation
        pinEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 4) {
                    // Automatically validate when PIN is complete
                    validateAndSubmit(pinEditText, dialog)
                }
            }
        })

        // Set window attributes for keyboard
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        // Handle keyboard done action
        pinEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateAndSubmit(pinEditText, dialog)
                true
            } else {
                false
            }
        }

        dialog.setOnShowListener {
            // Show keyboard using multiple approaches
            pinEditText.requestFocus()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(pinEditText, InputMethodManager.SHOW_IMPLICIT)

            // Backup approach with delay
            pinEditText.postDelayed({
                if (!imm.isActive(pinEditText)) {
                    imm.showSoftInput(pinEditText, InputMethodManager.SHOW_FORCED)
                }
            }, 100)

            // Set up positive button click listener
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                validateAndSubmit(pinEditText, dialog)
            }
        }

        // Make sure EditText is focusable
        pinEditText.isFocusableInTouchMode = true
        pinEditText.isFocusable = true

        dialog.show()
    }
    // Helper function to validate and submit PIN
    private fun validateAndSubmit(pinEditText: EditText, dialog: AlertDialog) {
        val enteredPin = pinEditText.text.toString()
        if (validatePin(enteredPin)) {
            // Hide keyboard before dismissing
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(pinEditText.windowToken, 0)
            dialog.dismiss()

            // submit to server
            onCorrectPin()
        } else {
            pinEditText.error = "Incorrect PIN"
            pinEditText.setText("")
            pinEditText.requestFocus() // Keep focus for retry
        }
    }
    private fun validatePin(pin: String): Boolean {
        return pin.length == AppConstants.PIN_LENGTH
    }

    private fun onCorrectPin() {

        setResult(Activity.RESULT_OK, intent)
        finish()
    }
    private fun getBarcode(barcode: String) {
        try {
            Log.d("BarCode", "${barcode}")

            val decodedBytes = android.util.Base64.decode(barcode, android.util.Base64.DEFAULT)
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            Log.d("decodedString", decodedString)

            val gson = Gson()

            // Parse JSON to object
            var person = gson.fromJson(decodedString, QrCodeData::class.java)
            var text1 = findViewById<TextView>(R.id.txtContent)
            unregisterBarcodeScanner()
            showPinDialog()
            //finish()

        } catch (e: Exception) {
            mBarcodeScanner!!.startScan()
            Log.e("JSON Parse Error", e.message ?: "Unknown error")


        }
    }
}
