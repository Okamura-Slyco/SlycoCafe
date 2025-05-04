package br.com.slyco.slycocafe.utils

import br.com.slyco.slycocafe.R
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.media.Image
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
import br.com.slyco.slycocafe.postReceipt
import br.com.slyco.slycocafe.postReceiptDC
import br.com.slyco.slycocafe.printing.DevicePrinterFactory
import java.io.ByteArrayOutputStream
import android.util.Base64

class ReceiptDelivery(
    private val context: Context,
    private val rootView: View,
    private val brand: String,
    private val model: String,
    private val hasPrinter: Boolean,
    private val receiptBitmap: Bitmap,
    private val locationId: String,
    private val imageId: String
) {
    private var dialog: AlertDialog? = null
    var onDismiss: (() -> Unit)? = null


    fun isShowing(): Boolean {
        return dialog?.isShowing == true
    }

    fun sendEmail(text: String) {
        Log.i("Receipt", "📧 Sending Email with:\n$text")
        // TODO: Implement actual email logic
    }

    fun sendSms(text: String, phone: String) {
        Log.i("Receipt", "📲 Sending SMS to $phone:\n$text")
        // TODO: Launch SMS intent or API
    }

    fun sendWhatsApp(text: String, phone: String) {
        Log.i("Receipt", "💬 Sending WhatsApp to $phone:\n$text")
        // TODO: Open WhatsApp Intent with message
    }

    fun printReceipt(text: String) {
        if (!hasPrinter) {
            Log.e("Receipt", "❌ No printer available.")
            return
        }
        Log.i("Receipt", "🖨 Printing from $brand $model:\n$text")
        // TODO: Implement actual printing logic

        val printer = DevicePrinterFactory.getPrinter()

        printer.print(context, receiptBitmap ) {} // `this` = Activity or context
    }
    fun cleanPhoneNumber(input: String): String {
        return input.replace(Regex("[^\\d+]"), "")
            .replace(Regex("(?<!^)\\+"), "") // remove any extra '+' not at start
    }

    fun showDeliveryOptions(receiptText: String) {

        val receiptImage = rootView.findViewById<ImageView>(R.id.receiptImageView)
        receiptImage.setImageBitmap(receiptBitmap)

        var button = rootView.findViewById<LinearLayout>(R.id.buttonSms)
        button.setOnClickListener {
            promptPhoneNumber("SMS") { number -> sendSms(receiptText, number) }
        }
        button.alpha = 0.5f // makes it look disabled
        button.isEnabled = false

        button = rootView.findViewById<LinearLayout>(R.id.buttonEmail)
        button.setOnClickListener {
            sendEmail(receiptText)
        }
        button.alpha = 0.5f // makes it look disabled
        button.isEnabled = false

        button = rootView.findViewById<LinearLayout>(R.id.buttonWhatsApp)
        button.setOnClickListener {
            promptPhoneNumber("WhatsApp") { number ->
                // Convert Bitmap to PNG base64
                val byteArrayOutputStream = ByteArrayOutputStream()
                receiptBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val base64Image = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)

                // Build the request
                val receiptPayload = postReceiptDC(
                    method = "whatsapp_template",
                    target = cleanPhoneNumber(number),
                    content = null,
                    contentSid = null, // or use default from server env
                    variables = null,  // or use additional template variables
                    base64Image = base64Image,
                    filename = "${imageId}.png"
                )

                // Send the request
                postReceipt(locationId, receiptPayload)
            }
        }

        button = rootView.findViewById<LinearLayout>(R.id.buttonPrint)
        if (hasPrinter) {
            button.setOnClickListener {
                Log.d ("ReceiptDelivery" , "printButton.setOnClickListener")
                printReceipt(receiptText)
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
//        dialog?.setOnDismissListener {
//            onDismiss?.invoke()
//        }
//
//        dialog?.show()
    }

//    fun dismiss() {
//        dialog?.dismiss()
//    }

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
