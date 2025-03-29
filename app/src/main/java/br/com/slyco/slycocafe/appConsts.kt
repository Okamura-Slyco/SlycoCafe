package br.com.slyco.slycocafe

object AppConstants {
    const val ON_STOCK_ALPHA = 255
    const val OUT_OF_STOCK_ALPHA = 25
    const val ON_STOCK_ALPHA_FLOAT = 1.0f
    const val OUT_OF_STOCK_ALPHA_FLOAT = OUT_OF_STOCK_ALPHA.toFloat()/ON_STOCK_ALPHA.toFloat()
    const val INACTIVITY_TIMEOUT = 30000L // 30s (em milissegundos)
    const val dispenserIdSufix = "_itemQty"
    const val dispenserFlavorSufix = "_itemFlavor"
    const val isvTaxId = "55833084000136"

}

enum class ACTIVITY_IDS (val value:Int){
    NONE (0),

    PAYMENT(1),
    RELEASE_COFFEE(2),
    SCREEN_SAVER(3),
    SLYCO_WALLET (4),
    REQUEST_ACTIVATE_TEF(99)
}