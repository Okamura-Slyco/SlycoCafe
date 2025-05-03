package br.com.slyco.slycocafe.utils

import br.com.slyco.slycocafe.R
import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
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
import br.com.slyco.slycocafe.printing.DevicePrinterFactory

class Receipt(
    private val context: Context,
    private val brand: String,
    private val model: String,
    private val hasPrinter: Boolean
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

        val receiptText = """
    Slyco Café
    Produto: Espresso
    Preço: R$10,00
""".trimIndent()

        printer.print(context, receiptText, ) {dismiss()} // `this` = Activity or context
    }

    fun showDeliveryOptions(receiptText: String) {
        val view = LayoutInflater.from(context).inflate(R.layout.receipt_delivery_dialog, null)

        dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(true)
            .create()

        view.findViewById<LinearLayout>(R.id.buttonSms).setOnClickListener {
            //dialog?.dismiss()
            promptPhoneNumber("SMS") { number -> sendSms(receiptText, number) }
        }

        view.findViewById<LinearLayout>(R.id.buttonEmail).setOnClickListener {
            //dialog?.dismiss()
            sendEmail(receiptText)
        }

        view.findViewById<LinearLayout>(R.id.buttonWhatsApp).setOnClickListener {
            //dialog?.dismiss()
            promptPhoneNumber("WhatsApp") { number -> sendWhatsApp(receiptText, number) }
        }

        val printButton = view.findViewById<LinearLayout>(R.id.buttonPrint)
        if (hasPrinter) {
            printButton.setOnClickListener {
                printReceipt(receiptText)
            }
        } else {
            printButton.visibility = LinearLayout.GONE
        }


        dialog?.setOnDismissListener {
            onDismiss?.invoke()
        }

        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
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
