package br.com.slyco.slycocafe.printing

import br.com.slyco.slycocafe.R
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.clover.sdk.util.CloverAccount
import com.clover.sdk.v1.printer.job.ImagePrintJob2
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.*

class CloverPrinter : DevicePrinter {

    override fun print(context: Context, text: String) {
        if (!Build.MANUFACTURER.equals("clover", ignoreCase = true)) {
            Toast.makeText(context, "Not a Clover device", Toast.LENGTH_SHORT).show()
            return
        }

        val account = CloverAccount.getAccount(context)
        if (account == null) {
            Toast.makeText(context, "Clover account not found", Toast.LENGTH_SHORT).show()
            Log.e("CloverPrinter", "No Clover account available")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val logo = BitmapFactory.decodeResource(context.resources, R.drawable.slyco_icon)
                val qr = generateQRCode("https://slyco.com.br", 300, 300)
                val receiptBitmap = createReceiptImageFull(context, logo, qr, text)

                val job = ImagePrintJob2.Builder(context)
                    .bitmap(receiptBitmap)
                    .build()

                withContext(Dispatchers.Main) {
                    job.print(context, account)
                    showPrintSuccessDialog(context) {
                        print(context, text) // reprint callback
                    }
                }

                Log.d("CloverPrinter", "🖨️ All-in-one print job sent")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Print failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                Log.e("CloverPrinter", "Print error", e)
            }
        }
    }

    private fun generateQRCode(data: String, width: Int, height: Int): Bitmap? {
        return try {
            val bitMatrix: BitMatrix =
                MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            Log.e("QRCode", "QR code generation failed: ${e.message}")
            null
        }
    }

    private fun createReceiptImageFull(context: Context, logo: Bitmap, qr: Bitmap?, receiptText: String): Bitmap {
        val receiptWidth = 384

        // Resize logo to 1/3 of width
        val logoTargetWidth = receiptWidth / 3
        val resizedLogo = Bitmap.createScaledBitmap(
            logo,
            logoTargetWidth,
            (logo.height * logoTargetWidth / logo.width.toFloat()).toInt(),
            true
        )

        // Resize QR to 4/5 of width
        val qrTargetWidth = (receiptWidth * 4) / 5
        val resizedQR = qr?.let {
            Bitmap.createScaledBitmap(it, qrTargetWidth, qrTargetWidth, true)
        }

        // Measure text height
        val textPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        val textLayout = StaticLayout.Builder
            .obtain(receiptText, 0, receiptText.length, textPaint, receiptWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()

        val totalHeight = resizedLogo.height + textLayout.height + (resizedQR?.height ?: 0)

        val result = Bitmap.createBitmap(receiptWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply { isFilterBitmap = true }

        // Draw logo centered
        val logoLeft = (receiptWidth - resizedLogo.width) / 2
        canvas.drawBitmap(resizedLogo, logoLeft.toFloat(), 0f, paint)

        // Draw text
        canvas.save()
        canvas.translate(0f, resizedLogo.height + 20f)
        textLayout.draw(canvas)
        canvas.restore()

        // Draw QR centered
        resizedQR?.let {
            val qrLeft = (receiptWidth - it.width) / 2
            val qrTop = resizedLogo.height + 20 + textLayout.height + 20
            canvas.drawBitmap(it, qrLeft.toFloat(), qrTop.toFloat(), paint)
        }

        return result
    }

    private fun showPrintSuccessDialog(context: Context, onReprint: () -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_print_status, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val animPrint = dialogView.findViewById<LottieAnimationView>(R.id.animationPrint)
        val animCheck = dialogView.findViewById<LottieAnimationView>(R.id.animationCheck)
        val buttonLayout = dialogView.findViewById<LinearLayout>(R.id.buttonLayout)
        val buttonClose = dialogView.findViewById<Button>(R.id.buttonClose)
        val buttonReprint = dialogView.findViewById<Button>(R.id.buttonReprint)

        animPrint.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                animPrint.visibility = View.GONE
                animCheck.visibility = View.VISIBLE
                animCheck.playAnimation()
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        animCheck.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                buttonLayout.visibility = View.VISIBLE
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        buttonClose.setOnClickListener { dialog.dismiss() }
        buttonReprint.setOnClickListener {
            dialog.dismiss()
            onReprint()
        }

        dialog.show()
    }


}
