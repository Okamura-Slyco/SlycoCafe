package br.com.slyco.slycocafe.model

data class Caller (
    var merchantSiTef : String,
    var sitefIP : String,
    var merchantTaxId : String,
    var tenderOperation : String,
    var enabledTransactions : String,
    var userInputTimeout : String?,
    var communicationMode : String?,
    var tokenTLS : String?,
    var sitefTerminal : String?,
    var otp : String?
) {
    constructor() : this(
        "","","","",
        "","",null,null,null,null
    )
}