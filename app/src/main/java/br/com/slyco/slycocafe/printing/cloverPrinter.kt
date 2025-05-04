package br.com.slyco.slycocafe.printing

import br.com.slyco.slycocafe.R
import android.animation.Animator
import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.clover.sdk.util.CloverAccount
import com.clover.sdk.v1.printer.job.ImagePrintJob2
import kotlinx.coroutines.*

class CloverPrinter : DevicePrinter {

    override fun print(context: Context, receiptBitmap:Bitmap, onDialogDismissed: () -> Unit) {
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


                val job = ImagePrintJob2.Builder(context)
                    .bitmap(receiptBitmap)
                    .build()

                withContext(Dispatchers.Main) {
                    job.print(context, account)

                    Log.d("CloverPrinter", "Bitmap: width=${receiptBitmap.width}, height=${receiptBitmap.height}")
                    showPrintSuccessDialog(
                        context,
                        onReprint = { print(context, receiptBitmap, onDialogDismissed) },
                        onDismiss = onDialogDismissed
                    )
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


    private fun showPrintSuccessDialog(
        context: Context,
        onReprint: () -> Unit,
        onDismiss: () -> Unit = {}
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_print_status, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.setOnDismissListener {
            onDismiss()  // ✅ notify external logic when dialog is dismissed
        }

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
