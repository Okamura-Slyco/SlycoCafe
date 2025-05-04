package br.com.slyco.slycocafe.printing

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast

class DefaultPrinter : DevicePrinter {
    override fun print(context: Context, receiptBitmap: Bitmap, onDialogDismissed: () -> Unit) {
        Toast.makeText(context, "This device does not support printing", Toast.LENGTH_SHORT).show()
    }
}
