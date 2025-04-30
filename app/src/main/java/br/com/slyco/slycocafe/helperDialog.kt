package br.com.slyco.slycocafe

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.net.URLEncoder

class helperDialog(private val activity: Activity) {
    private var dialog: AlertDialog? = null
    private var countdownAnimator: ObjectAnimator? = null

    var onDismissCallback: (() -> Unit)? = null

    fun show(locationName: String, locationCode: String, flavorsList: String = "", phone: String = "5511915216660", timeoutMillis: Long = 30_000L,cancellable:Boolean = false) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_assistance, null)
        val qrImage = dialogView.findViewById<ImageView>(R.id.qrcodeImage)
        val countdownBar = dialogView.findViewById<ProgressBar>(R.id.helpCountdownBar)

        // Build WhatsApp message
        val flavorsSection = flavorsList.toString().trim()
        val flavorBlock = if (flavorsSection.isNotEmpty()) {
            "\nCompra:\n$flavorsSection"
        } else {
            ""
        }

        val message = """
    Preciso de ajuda na máquina Slyco.
    *Local*: $locationName ($locationCode)$flavorBlock
""".trimIndent()

        val encodedMessage = URLEncoder.encode(message, "UTF-8")

        val whatsappUrl = "https://wa.me/$phone?text=$encodedMessage"

        val qrBitmap = generateQrCodeWithLogo(activity,whatsappUrl)
        qrImage.setImageBitmap(qrBitmap)

        dialog = AlertDialog.Builder(activity, R.style.NoActionBarDialog)
            .setView(dialogView)
            .setCancelable(cancellable)
            .create()

        dialog?.setOnDismissListener {
            onDismissCallback?.invoke()
        }

        dialog?.setCanceledOnTouchOutside(cancellable)

        dialog?.show()

        dialog?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        (activity as? AppCompatActivity)?.supportActionBar?.hide()


        countdownAnimator = ObjectAnimator.ofInt(countdownBar, "progress", 100, 0).apply {
            duration = timeoutMillis
            interpolator = android.view.animation.LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (dialog?.isShowing == true) {
                        Log.d ("onAnimationEnd", "dismiss")
                        dialog?.dismiss()
                    }
                }
            })
            start()
        }
    }

    fun generateQrCodeWithLogo(context: Context, content: String, size: Int = 512): Bitmap {
        // 1. Generate the QR code
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val qrBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
             ;   qrBitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        // 2. Load WhatsApp icon from drawable
        val overlaySize = size / 6  // 25% of QR code size
        val overlayBitmap = BitmapFactory.decodeResource(
            context.resources,  // use `context.resources` if available
            R.drawable.whatsapp  // your WhatsApp logo (PNG, transparent)
        )

        val scaledOverlay = Bitmap.createScaledBitmap(overlayBitmap, overlaySize, overlaySize, true)

        // 3. Draw the logo on top of the QR code
        val canvas = Canvas(qrBitmap)
        val left = (qrBitmap.width - scaledOverlay.width) / 2
        val top = (qrBitmap.height - scaledOverlay.height) / 2
        canvas.drawBitmap(scaledOverlay, left.toFloat(), top.toFloat(), null)

        return qrBitmap
    }

    fun dismiss() {
        countdownAnimator?.cancel()
        Log.d ("helperDialog", "dismiss")
        dialog?.dismiss()
    }

    fun isShowing(): Boolean = dialog?.isShowing == true
}
