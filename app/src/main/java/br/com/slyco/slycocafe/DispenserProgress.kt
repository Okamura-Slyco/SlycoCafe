package br.com.slyco.slycocafe

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

data class DISPENSER_ELEMENTS (
    var counter:Int = 0,
    var id:String = "",
    var flavor:Int = 0
    )
data class ANIMATION_ELEMENTS (
    var counter: Int = 0,
    var imgId:Int = 0,
    var imgSrcId:Int = 0
)

object GlobalVariables {
    var dispenserElements = arrayOf <DISPENSER_ELEMENTS> (
    DISPENSER_ELEMENTS(0,"A"),
    DISPENSER_ELEMENTS(0,"B"),
    DISPENSER_ELEMENTS(0,"C"),
    DISPENSER_ELEMENTS(0,"D"),
    DISPENSER_ELEMENTS(0,"E"),
    DISPENSER_ELEMENTS(0,"F"),
    DISPENSER_ELEMENTS(0,"G"),
    DISPENSER_ELEMENTS(0,"H")
    )
    var animationElements = arrayOf<ANIMATION_ELEMENTS> (
    ANIMATION_ELEMENTS(0,R.id.capsule1,0),
    ANIMATION_ELEMENTS(0,R.id.capsule2,0),
    ANIMATION_ELEMENTS(0,R.id.capsule3,0),
    ANIMATION_ELEMENTS(0,R.id.capsule4,0),
    ANIMATION_ELEMENTS(0,R.id.capsule5,0),
    ANIMATION_ELEMENTS(0,R.id.capsule6,0),
    ANIMATION_ELEMENTS(0,R.id.capsule7,0),
    ANIMATION_ELEMENTS(0,R.id.capsule8,0)
    )
}

class DispenserProgress : AppCompatActivity()
{
    var dispenserPort: UsbSerialPort? = null

    var thisIntent: Intent? = null

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dispenser_progress)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        thisIntent = Intent()

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val widthPixels = displayMetrics.widthPixels
        val heightPixels = displayMetrics.heightPixels
        val densityDpi = displayMetrics.densityDpi

        val widthDp = widthPixels / (densityDpi / 160f)
        val heightDp = heightPixels / (densityDpi / 160f)

        var capsuleDp = widthPixels / AppConstants.DISPENSERS_QTY


        for (i in 0..< AppConstants.DISPENSERS_QTY) {
            if (GlobalVariables.dispenserElements[i] != null) {
                GlobalVariables.animationElements[i]?.counter = intent.getIntExtra(GlobalVariables.dispenserElements[i]?.id+AppConstants.dispenserIdSufix, 0)
                if (GlobalVariables.animationElements[i]?.counter!! > 0){
                    GlobalVariables.animationElements[i]?.imgSrcId = intent.getIntExtra(GlobalVariables.dispenserElements[i]?.id+"_itemFlavor", 0)
                    Log.d("DispenserProgress","${GlobalVariables.dispenserElements[i]?.id} : ${GlobalVariables.animationElements[i]?.counter} : ${GlobalVariables.animationElements[i]?.imgSrcId} ")

                    var image = findViewById<ImageView>(GlobalVariables.animationElements[i].imgId)
                    image.alpha = 0.0f
                    image.setImageResource(GlobalVariables.animationElements[i].imgSrcId)
                    image.layoutParams.height = capsuleDp.toInt()

                    image.layoutParams.width = capsuleDp.toInt()
                }


            }
        }


        // Find all available drivers from attached devices.
        val manager = getSystemService(USB_SERVICE) as UsbManager
        val availableDrivers: List<UsbSerialDriver> =
            UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            Log.i("Slyco-USB","No USB Driver found")
        }
        else {
            // Open a connection to the first available driver.
            val driver = availableDrivers[0]
            val connection = manager.openDevice(driver.device)
                ?: // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
                return


            dispenserPort = driver.ports[0] // Most devices have just one port (port 0)
            dispenserPort?.open(connection)
            dispenserPort?.setParameters(
                115200,
                8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
            )

            var dispenserBufferString = ""
            for (dispenserElement in GlobalVariables.dispenserElements){
                dispenserBufferString += dispenserElement!!.id.repeat(dispenserElement!!.counter)
            }
            dispenserBufferString += "\n"

            Log.d("DispenserProgress","Buffer: ${dispenserBufferString}")

            dispenserPort?.write(dispenserBufferString!!.toByteArray(),100)

        }

        // Run a function after the UI has loaded
        lifecycleScope.launch {
            // Perform any UI-related tasks here
            processAfterUILoad()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private suspend fun processAfterUILoad() {
        withContext(Dispatchers.Main) {
            // Your code to run after UI load
            // For example, updating a TextView
            var progressBar = findViewById<ProgressBar>(R.id.progressBar)

            progressBar.max = 100
            progressBar.progress = 0

            var maxIt = 0
            for (animationElement in GlobalVariables.animationElements)
            {
                maxIt = max(maxIt,animationElement!!.counter)
            }
            var myIt = 0

            val inBuffer:ByteArray = ByteArray(500)
            dispenserPort?.read(inBuffer,1000)
            Log.d("DispenserProgress USB IB", inBuffer.toHexString(0,500))

            while (myIt++ < maxIt){
                var elements = 0

                for (animationElement in GlobalVariables.animationElements)
                {
                    if (animationElement!!.counter > 0) {elements++; dispenserAnimation(findViewById<ImageView>(animationElement.imgId)); animationElement!!.counter --}
                }

                if (elements %2 == 0){

                }

                ObjectAnimator.ofInt(progressBar,"progress",(100*(myIt.toFloat()/maxIt.toFloat())).toInt())
                    .setDuration(1000L)
                    .start()
                delay(1000L)
            }

            var bomCafeLabel = findViewById<TextView>(R.id.obrigadoLabel)

            bomCafeLabel.visibility = View.VISIBLE

            delay(1000)

            dispenserPort?.close()


            setResult(Activity.RESULT_OK, thisIntent)
            finish()

        }
    }
    private suspend fun dispenserAnimation(coffeeImageView: ImageView){
        coroutineScope {
            async {
                coffeeImageView.alpha = 0.0f
                coffeeImageView.translationY = 0.0f
                ObjectAnimator.ofFloat(coffeeImageView,"alpha",1.0f)
                    .setDuration(50L)
                    .start()

                //delay(500L)

                ObjectAnimator.ofFloat(coffeeImageView,"translationY",1000.0f)
                    .setDuration(1000L)
                    .start()
            }
        }

    }

}
