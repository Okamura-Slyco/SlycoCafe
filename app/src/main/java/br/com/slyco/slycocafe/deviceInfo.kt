package br.com.slyco.slycocafe

import android.os.Build

object DeviceInfoModule  {

    val deviceModel = Build.MODEL
    val deviceBrand = Build.MANUFACTURER
    val deviceName = Build.DEVICE

}