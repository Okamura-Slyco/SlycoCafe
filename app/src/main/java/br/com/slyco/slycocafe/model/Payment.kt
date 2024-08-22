package br.com.slyco.slycocafe.model

data class Payment (
    val funcionId : String,
    val transactionAmount : String,
    val transactionTip : String,
    val transactionInstallments : String?,
    val invoiceNumber : String?,
    val invoiceDate : String?,
    val invoiceTime : String?,
    val merchantTaxId : String,
    val isvTaxId : String,

    val merchantSiTef : String,
    val sitefIP : String,
    val communicationMode : String
)