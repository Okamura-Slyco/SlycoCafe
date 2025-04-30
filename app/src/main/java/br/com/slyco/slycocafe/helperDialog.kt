package br.com.slyco.slycocafe

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.net.URLEncoder

class helperDialog(private val activity: Activity) {
    private var dialog: AlertDialog? = null
    private var countdownAnimator: ObjectAnimator? = null

    fun show(location: String, saleId: String, phone: String = "55119152166660", timeoutMillis: Long = 45_000L) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_assistance, null)
        val qrImage = dialogView.findViewById<ImageView>(R.id.qrcodeImage)
        val countdownBar = dialogView.findViewById<ProgressBar>(R.id.helpCountdownBar)

        // Build WhatsApp message
        val message = "Preciso de ajuda na máquina Slyco. Local: $location. Venda: #$saleId"
        val encodedMessage = URLEncoder.encode(message, "UTF-8")
        val whatsappUrl = "https://wa.me/$phone?text=$encodedMessage"

        val qrBitmap = generateQrCodeBitmap(whatsappUrl)
        qrImage.setImageBitmap(qrBitmap)

        dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog?.show()

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

    private fun generateQrCodeBitmap(content: String, size: Int = 512): Bitmap {
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bmp
    }

    fun dismiss() {
        countdownAnimator?.cancel()
        Log.d ("helperDialog", "dismiss")
        dialog?.dismiss()
    }

    fun isShowing(): Boolean = dialog?.isShowing == true
}
