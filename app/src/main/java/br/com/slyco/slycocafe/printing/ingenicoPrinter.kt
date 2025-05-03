package br.com.slyco.slycocafe.printing

import android.content.Context
import android.util.Log

class IngenicoPrinter : DevicePrinter {
    override fun print(context: Context, text: String) {
        Log.i("IngenicoPrinter", "Printing not yet implemented.")
        // TODO: Hook into Ingenico SDK
    }
}