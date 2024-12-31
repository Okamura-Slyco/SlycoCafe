package br.com.slyco.slycocafe

object AppConstants {
    const val DISPENSERS_QTY = 6
    const val NESPRESSO_FLAVORS_QTY = 16
    const val MAX_DISPENSER_CAPACITY = 50
    const val ON_STOCK_ALPHA = 255
    const val OUT_OF_STOCK_ALPHA = 25
    const val ON_STOCK_ALPHA_FLOAT = 1.0f
    const val OUT_OF_STOCK_ALPHA_FLOAT = OUT_OF_STOCK_ALPHA.toFloat()/ON_STOCK_ALPHA.toFloat()
    const val DISPENSER_PID = 60000
    const val DISPENSER_VID = 4292
    const val ACTION_USB_PERMISSION = "com.android.pinpad.USB_PERMISSION"
    const val INACTIVITY_TIMEOUT = 30000L // 30s (em milissegundos)
    const val dispenserIdSufix = "_itemQty"
    const val dispenserFlavorSufix = "_itemFlavor"
    const val merchantTaxId = "55833084000136"
    const val isvTaxId = "55833084000136"
}