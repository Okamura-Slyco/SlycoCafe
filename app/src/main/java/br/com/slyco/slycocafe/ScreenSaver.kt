package br.com.slyco.slycocafe

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Canvas
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import java.net.URLEncoder

import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet


class objectAnimationVector {
    var origin:Int = 0
    var destination:Int = 0
    var origin_x:Int = 0
    var origin_y:Int = 0
    var destination_x:Int = 0
    var destination_y:Int = 0
}

class ScreenSaver : AppCompatActivity() {
    var thisIntent: Intent? = null
    var origin:Int = 0
    var destination:Int = 0
    var origin_coord:IntArray = IntArray(2)
    var destination_coord:IntArray = IntArray(2)
    var height = 0
    var width = 0
    var deviceId: String = ""
    lateinit var parentLayout : ConstraintLayout
    var set = ConstraintSet()


    override fun onResume() {
        val actionBar: androidx.appcompat.app.ActionBar? = supportActionBar
        if (actionBar != null) actionBar.hide()
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        super.onResume()
    }

    fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        enableEdgeToEdge()
        setContentView(R.layout.activity_screen_saver)
        window.addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            LayoutParams.FLAG_FULLSCREEN,
            LayoutParams.FLAG_FULLSCREEN
        )

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        height = displayMetrics.heightPixels
        width = displayMetrics.widthPixels

        deviceId = getAndroidId(this).toUpperCase().chunked(4).joinToString("-")

        var textView = findViewById<TextView>(R.id.didTextView)
        textView.text = deviceId

        textView = findViewById<TextView>(R.id.buildInfoTextView)
        textView.text = "${BuildConfig.VERSION_NAME}${BuildConfig.SLYCO_API_ENVIRONMENT} ${BuildConfig.SLYCO_APP_BUILD_TIMESTAMP}"

        var button = findViewById<MaterialButton>(R.id.buttonNew)
        button.setOnClickListener(listener)

        var activateContinueButton = intent.getIntExtra("activateContinueButton", 0)
        if (activateContinueButton == 1) {
            button.text = "Nova Compra"
            button = findViewById<MaterialButton>(R.id.buttonContinue)
            button.setVisibility(View.VISIBLE)
            button.setOnClickListener(listener)
        }


        var locationName = intent.getStringExtra("locationName")
        findViewById<TextView>(R.id.locationNameTextView).text = locationName

        val encodedLocationName = URLEncoder.encode(locationName, "UTF-8")
        val encodedDeviceId = URLEncoder.encode(deviceId, "UTF-8")

        val fullUrl = "https://www.slyco.com.br/contact?locationName=$encodedLocationName&locationCode=$encodedDeviceId"
        val qrCodeContainer = findViewById<View>(R.id.qrCodeContainer)
        val qrCodeImageView = findViewById<ImageView>(R.id.qrCodeImageView)
        val qrBitmap = generateQrCodeWithLogo(this, fullUrl)
        qrCodeImageView.setImageBitmap(qrBitmap)

        parentLayout = findViewById<ConstraintLayout>(R.id.parentLayout)

        // Set the constraints for qrCodeContainer directly
        val params = qrCodeContainer.layoutParams as ConstraintLayout.LayoutParams
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID

        // Apply the modified layout params back to qrCodeContainer
        qrCodeContainer.layoutParams = params

        // If you want to apply more constraints to qrCodeImageView, you can do it similarly
        val qrImageParams = qrCodeImageView.layoutParams as ConstraintLayout.LayoutParams
        qrImageParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        qrImageParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        qrCodeImageView.layoutParams = qrImageParams

        thisIntent = Intent()

        lifecycleScope.launch {
            val positions = listOf("topLeft", "topRight", "bottomRight")
            while (true) {
                val nextPosition = positions.random()
                Log.d ("lifecycleScope.launch", nextPosition)
                positionQrCode(nextPosition, qrCodeContainer,parentLayout)
                delay(30000L)
            }
        }

        // Coroutine 2: Long-running UI logic
        lifecycleScope.launch(Dispatchers.Default) {
            processAfterUILoad()
        }
    }


    private fun positionQrCode(position: String, qrCodeContainer: View, parentLayout: ConstraintLayout) {
        // Get the layout params of the qrCodeContainer
        val params = qrCodeContainer.layoutParams as ConstraintLayout.LayoutParams

        when (position) {
            "topLeft" -> {
                params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                params.endToEnd = ConstraintLayout.LayoutParams.UNSET
                params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
            }
            "topRight" -> {
                params.startToStart = ConstraintLayout.LayoutParams.UNSET
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
            }
            "bottomRight" -> {
                params.startToStart = ConstraintLayout.LayoutParams.UNSET
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                params.topToTop = ConstraintLayout.LayoutParams.UNSET
                params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            }
        }

        // Apply the modified layout parameters to the qrCodeContainer
        qrCodeContainer.layoutParams = params

        // Optionally, if you need to update the layout immediately:
        parentLayout.requestLayout()
    }



    @OptIn(ExperimentalStdlibApi::class)
    private suspend fun processAfterUILoad() {
        withContext(Dispatchers.Main) {
            var ids:IntArray = intArrayOf(R.id.imageView, R.id.imageView2,R.id.imageView3,R.id.imageView4,R.id.imageView5,R.id.imageView6,R.id.imageView7,R.id.imageView8,R.id.imageView9,R.id.imageView10,R.id.imageView11,R.id.imageView12,R.id.imageView13,R.id.imageView14,R.id.imageView15,R.id.imageView16)
            while (true)
            {
                for (imageElement in ids) {
                    origin = (1..4).random()
                    destination = (1..4).random()

                    while (destination == origin) {
                        destination = (1..4).random()
                    }

                    origin_coord = selectCoordinates(origin)
                    destination_coord = selectCoordinates(destination)

//                    Log.d(
//                        "ScreenSaver",
//                        "ori_x,ori_y: " + origin_coord[0].toString() + "," + origin_coord[1].toString()
//                    )
//                    Log.d(
//                        "ScreenSaver",
//                        "dest_x,dest_y: " + destination_coord[0].toString() + "," + destination_coord[1].toString()
//                    )

                    screenSaverAnimation(
                        findViewById<ImageView>(imageElement),
                        origin_coord,
                        destination_coord
                    )
                    delay(((7..10).random()*1000L)/ids.size)
                }

            }
        }
    }
    private suspend fun screenSaverAnimation(coffeeImageView: ImageView,origin: IntArray,destination:IntArray ){
        coroutineScope {
            async {
                coffeeImageView.alpha = 0.6f
                coffeeImageView.translationX = origin[0].toFloat()
                coffeeImageView.translationY = origin[1].toFloat()

                //delay(500L)
                val time = (4..6).random()*1000
                ObjectAnimator.ofFloat(coffeeImageView,"translationX",destination[0].toFloat())
                    .setDuration(time.toLong())
                    .start()
                ObjectAnimator.ofFloat(coffeeImageView,"translationY",destination[1].toFloat())
                    .setDuration(time.toLong())
                    .start()
                ObjectAnimator.ofFloat(coffeeImageView,"alpha",0.0f,0.8f,0.0f)
                    .setDuration(time.toLong())
                    .start()
            }
        }

    }

    fun selectCoordinates(side:Int):IntArray
    {
        val retArray = IntArray(2)
        when (side)
        {
            1,2 -> { // top or botton
                if (side == 1) retArray[0] = -50
                else retArray[0] = (width * 1.1).toInt()
                retArray[1] = (-50..(height * 1.1).toInt()).random()
            }
            3,4 -> { // left or right
                retArray[0] = (-50..(width * 1.1).toInt()).random()
                if (side == 3) retArray[1] = -50
                else retArray[1] = (height * 1.1).toInt()
            }
        }
        return retArray
    }

    val listener= View.OnClickListener { view ->

        when (view.getId()) {
            R.id.buttonContinue -> {
                thisIntent?.putExtra("action", "Continue")
            }
            R.id.buttonNew -> {
                thisIntent?.putExtra("action", "New")
            }
        }
        setResult(Activity.RESULT_OK, thisIntent)
        finish()
    }

    fun generateQrCodeWithLogo(context: Context, content: String, size: Int = 512): Bitmap {
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val qrBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        for (x in 0 until size) {
            for (y in 0 until size) {
                qrBitmap.setPixel(x, y, if (bitMatrix[x, y]) Color
                    .BLACK else Color.WHITE)
            }
        }

        val overlaySize = size / 6
        val logo = BitmapFactory.decodeResource(context.resources, R.drawable.slyco_icon_zero_alpha)
        val scaledLogo = Bitmap.createScaledBitmap(logo, overlaySize, overlaySize, true)

        val canvas = Canvas(qrBitmap)
        val left = (qrBitmap.width - scaledLogo.width) / 2
        val top = (qrBitmap.height - scaledLogo.height) / 2
        canvas.drawBitmap(scaledLogo, left.toFloat(), top.toFloat(), null)

        return qrBitmap
    }

}
