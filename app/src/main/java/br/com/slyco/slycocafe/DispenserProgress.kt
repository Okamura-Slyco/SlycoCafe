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
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DispenserProgress : AppCompatActivity()
{
    var dispenserPort: UsbSerialPort? = null
    var dispenserBufferString = ""
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
        findViewById<ImageView>(R.id.imgRistretto).alpha = 0.0f
        findViewById<ImageView>(R.id.imgBrasilOrganic).alpha = 0.0f
        findViewById<ImageView>(R.id.imgLeggero).alpha = 0.0f
        findViewById<ImageView>(R.id.imgDescafeinado).alpha = 0.0f
        findViewById<ImageView>(R.id.imgIndia).alpha = 0.0f
        findViewById<ImageView>(R.id.imgCaffeVanilio).alpha = 0.0f


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

            dispenserPort?.write(dispenserBufferString.toByteArray(),100)

        }

        // Run a function after the UI has loaded
        lifecycleScope.launch {
            // Perform any UI-related tasks here
            processAfterUILoad()
        }
    }

    private suspend fun processAfterUILoad() {
        withContext(Dispatchers.Main) {
            // Your code to run after UI load
            // For example, updating a TextView
            var progressBar = findViewById<ProgressBar>(R.id.progressBar)

            progressBar.max = 100
            progressBar.progress = 0

            dispenserAnimation(findViewById<ImageView>(R.id.imgRistretto))
            dispenserAnimation(findViewById<ImageView>(R.id.imgBrasilOrganic))
            dispenserAnimation(findViewById<ImageView>(R.id.imgLeggero))
            dispenserAnimation(findViewById<ImageView>(R.id.imgDescafeinado))
            dispenserAnimation(findViewById<ImageView>(R.id.imgIndia))
            dispenserAnimation(findViewById<ImageView>(R.id.imgCaffeVanilio))

            delay(500L)

            ObjectAnimator.ofInt(progressBar,"progress",50)
                .setDuration(500L)
                .start()

            ObjectAnimator.ofInt(progressBar,"progress",100)
                .setDuration(1000L)
                .start()

            while (progressBar.progress != 100)
            {
                delay(500)
            }
            var bomCafeLabel = findViewById<TextView>(R.id.obrigadoLabel)

            bomCafeLabel.visibility = View.VISIBLE

            delay(1000)

            dispenserPort?.close()

            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()

        }
    }
    private suspend fun dispenserAnimation(coffeeImageView: ImageView){
        coroutineScope {
            async {
                coffeeImageView.alpha = 0.0f
                coffeeImageView.translationY = 0.0f
                ObjectAnimator.ofFloat(coffeeImageView,"alpha",1.0f)
                    .setDuration(500L)
                    .start()

                //delay(500L)

                ObjectAnimator.ofFloat(coffeeImageView,"translationY",800.0f)
                    .setDuration(1000L)
                    .start()
            }
        }

    }

}
