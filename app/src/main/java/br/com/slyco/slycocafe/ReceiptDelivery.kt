package br.com.slyco.slycocafe.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import br.com.slyco.slycocafe.R
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import android.widget.EditText
import android.text.InputType
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.NumberParseException

import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import br.com.slyco.slycocafe.postReceipt
import br.com.slyco.slycocafe.postReceiptDC
import br.com.slyco.slycocafe.printing.DevicePrinterFactory
import com.airbnb.lottie.LottieAnimationView


class ReceiptDelivery {
    private lateinit var context: Context
    private lateinit var rootView: View
    private lateinit var brand: String
    private lateinit var model: String
    private var hasPrinter: Boolean = false
    private lateinit var receiptBitmap: Bitmap
    private lateinit var locationId: String
    private lateinit var imageId: String
    private var dialog: AlertDialog? = null

    var onDismissCallback: (() -> Unit)? = null

    var onDismiss: (() -> Unit)? = null

    private var b_isShowing = false

    fun isShowing(): Boolean {
        return this.b_isShowing
    }

    private fun printReceipt() {
        if (!hasPrinter) {
            Log.e("Receipt", "❌ No printer available.")
            return
        }
        Log.i("Receipt", "🖨 Printing from $brand $model")
        // TODO: Implement actual printing logic

        val printer = DevicePrinterFactory.getPrinter()

        printer.print(context, receiptBitmap ) {} // `this` = Activity or context
    }

    private fun cleanPhoneNumber(input: String): String {
        return input.replace(Regex("[^\\d+]"), "")
            .replace(Regex("(?<!^)\\+"), "") // remove any extra '+' not at start
    }

    constructor(
        context: Context,
        rootView: View,
        brand: String,
        model: String,
        hasPrinter: Boolean,
        receiptBitmap: Bitmap,
        locationId: String,
        imageId: String
    )  {
        this.context = context
        this.rootView = rootView
        this.brand = brand
        this.model = model
        this.hasPrinter = hasPrinter
        this.receiptBitmap = receiptBitmap
        this.locationId = locationId
        this.imageId = imageId

        val receiptImage = rootView.findViewById<ImageView>(R.id.receiptImageView)
        receiptImage.setImageBitmap(this.receiptBitmap)

        var button = rootView.findViewById<LinearLayout>(R.id.buttonWhatsApp)
        button.setOnClickListener{
            this.b_isShowing = true
            promptPhoneNumber("WhatsApp") { number ->
                val cleaned = cleanPhoneNumber(number)
                val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_send, null)
                val messageText = dialogView.findViewById<TextView>(R.id.dialogMessage)

                messageText.text = "Enviar para:\n$number?"

                this.dialog = AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setPositiveButton("Enviar", null)
                    .setNegativeButton("Cancelar", null)
                    .create()

                this.dialog?.setOnDismissListener {
                    onDismissCallback?.invoke()
                }

                val dlg = this.dialog
                dlg?.setOnShowListener {
                    val buttonPositive = dlg.getButton(AlertDialog.BUTTON_POSITIVE)
                    buttonPositive.setOnClickListener {
                        // Build the request
                        val receiptPayload = postReceiptDC(
                            method = "whatsapp_template",
                            target = cleaned,
                            content = null,
                            contentSid = null,
                            variables = null,
                            filename = "$imageId.png"
                        )

                        postReceipt(locationId, receiptPayload)

                        // Disable buttons while animating
                        dlg.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        dlg.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = false
                        showSendAnimation()

                    }
                }
                dlg?.show()
            }
        }

        button = rootView.findViewById<LinearLayout>(R.id.buttonPrint)
        if (hasPrinter) {
            button.setOnClickListener {
                Log.d ("ReceiptDelivery" , "printButton.setOnClickListener")
                printReceipt()
                button.alpha = 0.5f // makes it look disabled
                button.isEnabled = false
            }
        } else {
            button.visibility = LinearLayout.GONE
        }

//        val closeDialogButton = view.findViewById<ImageView>(R.id.closeDialogButton)
//        closeDialogButton.setOnClickListener{
//            dialog.run { dismiss() }
//        }
//
        this.dialog?.setOnDismissListener {
            onDismiss?.invoke()
        }
//
//        dialog?.show()
    }

//    fun dismiss() {
//        dialog?.dismiss()
//    }
private fun showSendAnimation() {
    val rootView = dialog?.window?.decorView
    val lottieView = rootView?.findViewById<LottieAnimationView>(R.id.sendLottie)

    lottieView?.setAnimation(R.raw.send)
    lottieView?.visibility = View.VISIBLE
    lottieView?.playAnimation()

    lottieView?.addAnimatorListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator) {}
        override fun onAnimationEnd(animation: Animator) {
            Handler(Looper.getMainLooper()).postDelayed({
                b_isShowing = false
                dialog?.dismiss()
            }, 1000)
        }

        override fun onAnimationCancel(animation: Animator) {}

        override fun onAnimationRepeat(animation: Animator) {}
    })
}



    private fun promptPhoneNumber(label: String, onConfirm: (String) -> Unit) {
        var input = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Enter phone number"
            gravity = Gravity.CENTER
            isFocusableInTouchMode = true
            requestFocus()

            // 👇 Make it visually smaller
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            minHeight = 0
            setPadding(8, 8, 8, 8)
        }
        input.background = null

        val maxDigits = 11
        var currentDigits = ""

        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val raw = s.toString().replace(Regex("[^\\d+]"), "")
                val digitsOnly =
                    raw.removePrefix("55").removePrefix("+55") // 👈 remove typed country code

                if (digitsOnly.length > maxDigits) {
                    Toast.makeText(
                        context,
                        "Quantidade máxima de dígitos atingida ($maxDigits)",
                        Toast.LENGTH_SHORT
                    ).show()
                    input.removeTextChangedListener(this)
                    input.setText(formatPhoneNumberLib(currentDigits))
                    input.setSelection(input.text.length)
                    input.addTextChangedListener(this)
                    return
                }

                if (digitsOnly == currentDigits) return

                currentDigits = digitsOnly
                val formatted = formatPhoneNumberLib(digitsOnly)
                input.removeTextChangedListener(this)
                input.setText(formatted)
                input.setSelection(formatted.length)
                input.addTextChangedListener(this)
            }


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val dialog = AlertDialog.Builder(context)
            .setTitle("Digite o número $label ")
            .setView(input)
            .setPositiveButton("Enviar") { _, _ ->
                val formatted = input.text.toString()
                onConfirm(formatted)
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            input.post {
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        dialog.show()
    }

    fun formatPhoneNumberLib(number: String, region: String = "BR"): String {
        val phoneUtil = PhoneNumberUtil.getInstance()

        return try {
            val phoneNumber = phoneUtil.parse(number, region)
            phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
        } catch (e: NumberParseException) {
            number // fallback if parsing fails
        }
    }


}
