package br.com.slyco.slycocafe.printing

import android.content.Context
import android.os.Build

interface DevicePrinter {
    fun print(context: Context, text: String)
}


object DevicePrinterFactory {
    fun getPrinter(): DevicePrinter {
        val brand = Build.MANUFACTURER.lowercase()
        val model = Build.MODEL.lowercase()

        return when {
            brand.contains("clover") -> CloverPrinter()
            brand.contains("ingenico") -> IngenicoPrinter()
            else -> DefaultPrinter()
        }
    }
}
