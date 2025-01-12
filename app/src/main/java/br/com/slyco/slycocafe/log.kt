package br.com.slyco.slycocafe

import android.util.Log

class log {
    lateinit var myTag:String
    constructor(tag:String){
        myTag = "SLYCO " + tag

    }
    fun log (input:String){
        Log.d (myTag,input)
    }
}