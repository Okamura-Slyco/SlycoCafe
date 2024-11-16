package br.com.slyco.slycocafe

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Bundle
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

class DispenserProgress : AppCompatActivity()
{
    var dispenserPort: UsbSerialPort? = null

    var thisIntent: Intent? = null

    var countA:Int = 0
    var countB:Int = 0
    var countC:Int = 0
    var countD:Int = 0
    var countE:Int = 0
    var countF:Int = 0

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

        countA = intent.getIntExtra("A_itemQty", 0)
        countB = intent.getIntExtra("B_itemQty", 0)
        countC = intent.getIntExtra("C_itemQty", 0)
        countD = intent.getIntExtra("D_itemQty", 0)
        countE = intent.getIntExtra("E_itemQty", 0)
        countF = intent.getIntExtra("F_itemQty", 0)

        Log.d("DispenserProgress","A: ${countA}; B: ${countB}; C: ${countC}; D: ${countD}; E: ${countE}; F: ${countF}")

        findViewById<ImageView>(R.id.imgRistretto).alpha = 0.0f
        findViewById<ImageView>(R.id.imgBrasilOrganic).alpha = 0.0f
        findViewById<ImageView>(R.id.imgLeggero).alpha = 0.0f
        findViewById<ImageView>(R.id.imgForte).alpha = 0.0f
        findViewById<ImageView>(R.id.imgCaffeVanilio).alpha = 0.0f
        findViewById<ImageView>(R.id.imgDescafeinado).alpha = 0.0f


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

            val dispenserBufferString = "A".repeat(countA) +
                    "B".repeat(countB) +
                    "C".repeat(countC) +
                    "D".repeat(countD) +
                    "E".repeat(countE) +
                    "F".repeat(countF) + "\n"

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

            val maxIt = max(countA,max(countB,max(countC,max(countD,max(countE,countF)))))
            var myIt = 0

            val inBuffer:ByteArray = ByteArray(500)
            dispenserPort?.read(inBuffer,1000)
            Log.d("DispenserProgress USB IB", inBuffer.toHexString(0,500))

            while (myIt++ < maxIt){

                if (countA > 0) { dispenserAnimation(findViewById<ImageView>(R.id.imgRistretto)); countA -- }
                if (countB > 0) { dispenserAnimation(findViewById<ImageView>(R.id.imgBrasilOrganic)); countB -- }
                if (countC > 0) { dispenserAnimation(findViewById<ImageView>(R.id.imgLeggero)); countC -- }
                if (countD > 0) { dispenserAnimation(findViewById<ImageView>(R.id.imgForte)); countD -- }
                if (countE > 0) { dispenserAnimation(findViewById<ImageView>(R.id.imgCaffeVanilio)); countE -- }
                if (countF > 0) { dispenserAnimation(findViewById<ImageView>(R.id.imgDescafeinado)); countF -- }

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
