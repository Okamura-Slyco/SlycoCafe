package br.com.slyco.slycocafe

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.*
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text


class SlycoWallet : AppCompatActivity() {
    var thisIntent: Intent? = null
    var origin:Int = 0
    var destination:Int = 0
    var origin_coord:IntArray = IntArray(2)
    var destination_coord:IntArray = IntArray(2)
    var height = 0
    var width = 0

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

        var textView = findViewById<TextView>(R.id.didTextView)
        textView.text = getAndroidId(this).toUpperCase().chunked(4).joinToString("-")

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

        thisIntent = Intent()

        lifecycleScope.launch {
            // Perform any UI-related tasks here
            processAfterUILoad()
        }


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
}
