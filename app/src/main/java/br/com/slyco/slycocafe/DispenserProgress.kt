package br.com.slyco.slycocafe

import android.animation.Animator
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.animation.ObjectAnimator
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.airbnb.lottie.LottieAnimationView
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.BounceInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBar

import android.widget.Button
import android.widget.Toast
import br.com.slyco.slycocafe.utils.Receipt

data class DISPENSER_ELEMENTS(
    var counter: Int = 0, var id: String = "", var flavor: Int = 0, var flavorName: String = ""
)

data class ANIMATION_ELEMENTS(
    var counter: Int = 0, var imgId: Int = 0, var imgSrcId: Int = 0
)

object GlobalVariables {
    var dispenserElements = arrayOf<DISPENSER_ELEMENTS>(
        DISPENSER_ELEMENTS(0, "A"),
        DISPENSER_ELEMENTS(0, "B"),
        DISPENSER_ELEMENTS(0, "C"),
        DISPENSER_ELEMENTS(0, "D"),
        DISPENSER_ELEMENTS(0, "E"),
        DISPENSER_ELEMENTS(0, "F"),
        DISPENSER_ELEMENTS(0, "G"),
        DISPENSER_ELEMENTS(0, "H")
    )
    var animationElements = arrayOf<ANIMATION_ELEMENTS>(
        ANIMATION_ELEMENTS(0, R.drawable.brasil_organic_trn, 0),
        ANIMATION_ELEMENTS(0, R.drawable.bianco_delicato_trn, 0),
        ANIMATION_ELEMENTS(0, R.drawable.bianco_intenso_trn, 0),
        ANIMATION_ELEMENTS(0, R.drawable.caffe_caramelo_trn, 0),
        ANIMATION_ELEMENTS(0, R.drawable.caffe_vanilio_trn, 0),
        ANIMATION_ELEMENTS(0, R.drawable.caffe_nocciola_trn, 0),
        ANIMATION_ELEMENTS(0, R.drawable.descafeinado_trn, 0),
        ANIMATION_ELEMENTS(0, R.drawable.finezzo_trn, 0)
    )
}

fun generateQrCodeBitmap(content: String, size: Int = 512): Bitmap {
    val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bmp.setPixel(
                x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            )
        }
    }
    return bmp
}

class DispenserProgress : AppCompatActivity() {

    private lateinit var manager: UsbManager
    private lateinit var driver: UsbSerialDriver
    private var dispenserPort: UsbSerialPort? = null
    private var connection: UsbDeviceConnection? = null

    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var capsuleContainer: FrameLayout
    private var itemLine: String = ""

    private var lastDataReceivedTime: Long = System.currentTimeMillis()
    private var totalIterations = 0
    private var currentIteration = 0

    private lateinit var locationName: String
    private lateinit var locationCode: String

    private var flavorsQty = 0
    private var lastActivityToSetWatchdog = 0

    private var watchdogTimeout: Long = 10000

    private lateinit var finalContainer: LinearLayout
    private lateinit var finalMessage: TextView
    private lateinit var coffeeCup: LottieAnimationView

    private lateinit var helpDialog: helperDialog

    private var helpBlockUntil: Long = 0
    private var capsulesStillFalling = false

    private lateinit var  receipt: Receipt


    private lateinit var helpIcon: ImageView

    companion object {
        private const val ACTION_USB_PERMISSION = "br.com.slyco.slycocafe.USB_PERMISSION"
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ACTION_USB_PERMISSION == intent?.action) {
                synchronized(this) {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
                            Log.i("Slyco-USB", "Permission granted for device $device")
                            val availableDrivers =
                                UsbSerialProber.getDefaultProber().findAllDrivers(manager)
                            for (availableDriver in availableDrivers) {
                                if (availableDriver.device.deviceId == device.deviceId) {
                                    driver = availableDriver
                                    openDispenserConnection()
                                    break
                                }
                            }
                        }
                    } else {
                        Log.i("Slyco-USB", "Permission denied for device $device")
                        updateStatus("Permission denied.")
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dispenser_progress)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) actionBar.hide()
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)
        capsuleContainer = findViewById(R.id.capsuleContainer)

        progressBar.visibility = View.GONE
        statusText.text = "Inicializando dispenser..."

        finalContainer = findViewById(R.id.finalContainer)
        finalMessage = findViewById(R.id.finalMessage)
        coffeeCup = findViewById(R.id.coffeeCup)

        statusText.bringToFront()

        helpIcon = findViewById(R.id.helpIcon)
        helpIcon.visibility = View.VISIBLE

        val bounceAnim = ObjectAnimator.ofFloat(helpIcon, "translationY", 0f, -20f, 0f).apply {
            duration = 800
            repeatMode = ObjectAnimator.RESTART
            repeatCount = ObjectAnimator.INFINITE
            interpolator = BounceInterpolator()
        }
        bounceAnim.start()

        flavorsQty = intent.getIntExtra(
            "dispensersQty", 0
        )

        val flavorsList = StringBuilder()
        locationName = intent.getStringExtra(AppConstants.locationNameFieldName).orEmpty()
        locationCode = intent.getStringExtra(AppConstants.locationCodeFieldName).orEmpty()


        for (i in 0 until flavorsQty) {
            val dispenser = GlobalVariables.dispenserElements[i]
            val animation = GlobalVariables.animationElements[i]

            if (dispenser != null) {
                val counter = intent.getIntExtra(dispenser.id + AppConstants.dispenserIdSufix, 0)
                val flavorName =
                    intent.getStringExtra(dispenser.id + AppConstants.dispenserFlavorNameSufix)
                        .orEmpty()
                if (counter > 0) itemLine += "- ${counter}x *$flavorName*\n"
                flavorsList.appendLine(itemLine)

                // Also set values back to your global model if needed
                dispenser.counter = counter
                dispenser.flavorName = flavorName
                animation?.imgId = intent.getIntExtra("${dispenser.id}_itemFlavor", 0)
            }
        }

        helpDialog = helperDialog(this)

        helpIcon.setOnClickListener {
            helpDialog.show(
                locationName = locationName, locationCode = locationCode, flavorsList = itemLine
            )
        }
        helpDialog.onDismissCallback = {
            Log.d("HelpDialog", "Help dialog dismissed")
            finishThisActivity(4)  // Or a dedicated code for help dismiss
        }

        val deviceBrand = intent.getStringExtra(AppConstants.deviceBrandFieldName)
        val deviceModel = intent.getStringExtra(AppConstants.deviceModelFieldName)
        val deviceHasPrinter = intent.getBooleanExtra(AppConstants.deviceHasPrinterFieldName, false)

        Log.d ("DispenserProgress PosInfo","brand: $deviceBrand\nmodel: $deviceModel\nhas printer: $deviceHasPrinter")

        manager = getSystemService(USB_SERVICE) as UsbManager
        val usbPermissionIntent = PendingIntent.getBroadcast(
            this, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE
        )

        val filter = IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)

        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            Log.i("Slyco-USB", "No USB Driver found")
            updateStatus("No USB Dispenser detected.")
        } else {
            driver = availableDrivers[0]
            if (manager.hasPermission(driver.device)) {
                openDispenserConnection()
            } else {
                manager.requestPermission(driver.device, usbPermissionIntent)
            }
        }

        // Also scan all USB devices in case a driver wasn't automatically found
        val deviceList = manager.deviceList
        for (device in deviceList.values) {
            if (!manager.hasPermission(device)) {
                manager.requestPermission(device, usbPermissionIntent)
            }
        }

        val buttonPrint = findViewById<Button>(R.id.buttonPrintReceipt)
        buttonPrint.setOnClickListener {
            receipt = Receipt(this, deviceBrand.toString(), deviceModel.toString(), deviceHasPrinter)
            receipt.showDeliveryOptions("Sale: R$10,00\nObrigado pela compra!")
            receipt.onDismiss = {
                Log.d("Print Dialog", "Print dialog dismissed")
                finishThisActivity(4)
            }
        }

    }
    private fun handleErrorAndReturn(message: String) {
        updateStatus(message)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }, 10000) // 10 seconds delay
    }

    private fun openDispenserConnection() {
        connection = manager.openDevice(driver.device)
        if (connection == null) {
            Log.e("Slyco-USB", "Failed to open device connection")
            updateStatus("Failed to connect to dispenser.")
            handleErrorAndReturn("Failed to connect to dispenser.")
            return
        }

        startUsbIdleMonitor()

        dispenserPort = driver.ports[0]

        try {
            dispenserPort?.open(connection)
            dispenserPort?.setParameters(
                115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE
            )

            progressBar.progress = 0
            progressBar.visibility = View.VISIBLE
            statusText.text = "Liberando cápsulas... por favor aguarde..."

            startUsbReader()
            resetWatchdog()

            var dispenserBufferString = ""
            for (dispenserElement in GlobalVariables.dispenserElements) {
                dispenserBufferString += dispenserElement!!.id.repeat(dispenserElement.counter)
            }
            dispenserBufferString += "\n"

            Log.d("DispenserProgress", "Buffer: ${dispenserBufferString}")

            dispenserPort?.write(dispenserBufferString!!.toByteArray(), 100)


        } catch (e: Exception) {
            Log.e("Slyco-USB", "Error opening/writing to port: ${e.message}")
            updateStatus("Communication error.")
        }
    }

    private fun safeCloseUsb() {
        try {
            dispenserPort?.close()
            connection?.close()
            dispenserPort = null
            connection = null
            Log.d("Slyco-USB", "USB safely closed before finish")
        } catch (e: Exception) {
            Log.w("Slyco-USB", "USB close error: ${e.message}")
        }
    }

    private fun startUsbReader() {
        var messageBuffer = ""

        Thread {
            val buffer = ByteArray(64)
            try {
                while (true) {
                    val len = dispenserPort?.read(buffer, 1000) ?: 0
                    if (len > 0) {
                        val chunk = buffer.copyOf(len).toString(Charsets.UTF_8)
                        messageBuffer += chunk

                        while (messageBuffer.contains("\n")) {
                            val splitIndex = messageBuffer.indexOf("\n")
                            val completeMessage = messageBuffer.substring(0, splitIndex).trim()
                            messageBuffer = messageBuffer.substring(splitIndex + 1)

                            if (completeMessage.isNotEmpty()) {
                                Log.d("Slyco-USB", "Received complete message: $completeMessage")
                                resetWatchdog()
                                handleUsbMessage(completeMessage)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Slyco-USB", "Error reading from USB: ${e.message}")
            }
        }.start()

    }

    private fun handleUsbMessage(message: String) {
        runOnUiThread {
            when {
                message.startsWith("Ri") -> {
                    totalIterations = message.substring(2).toIntOrNull() ?: 0
                    currentIteration = 0
                    progressBar.max = totalIterations * 2 // because ri + rf for each
                    Log.d("Slyco-USB", "Starting dispensing: $totalIterations iterations")
                    capsulesStillFalling = true
                }

                message.startsWith("ri") -> {
                    val parts = message.substring(2).split(":")
                    if (parts.size == 2) {
                        val iteration = parts[0].toIntOrNull() ?: 0
                        val binaryData = parts[1]
                        animateDispensers(binaryData)
                        currentIteration++
                        updateProgress()
                    }
                }

                message.startsWith("rf") -> {
                    currentIteration++
                    updateProgress()
                }

                message.startsWith("Rf") -> {
                    statusText.text = "Operação finalizada!"
                    progressBar.progress = progressBar.max
                    showFinalMessage()
                    finishAfterDelay()
                }
            }
        }
    }

    fun getActiveIndices(binary: String): List<Int> =
        binary.reversed().withIndex().filter { it.value == '1' }.map { it.index }

    private fun animateDispensers(binaryData: String) {
        val activeIndices = getActiveIndices(binaryData)

        val count = activeIndices.size
        if (count == 0) return

        for ((i, capsuleIndex) in activeIndices.withIndex()) {
            val positionFraction = (i + 1).toFloat() / (count + 1) // e.g., 1/2, 2/3, etc.
            val xPos = positionFraction * capsuleContainer.width
            launchCapsule(capsuleIndex, GlobalVariables.animationElements[capsuleIndex].imgId, xPos)
        }
    }

    private fun showFinalMessage() {
        finalContainer.visibility = View.VISIBLE

        finalContainer.alpha = 0f
        finalContainer.scaleX = 0.9f
        finalContainer.scaleY = 0.9f
        finalContainer.translationY = 30f

        finalContainer.animate().alpha(1f).scaleX(1f).scaleY(1f).translationY(0f).setDuration(600)
            .setInterpolator(AccelerateInterpolator()).withEndAction {
                coffeeCup.playAnimation()

                lastActivityToSetWatchdog = 1

                coffeeCup.addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}

                    override fun onAnimationEnd(animation: Animator) {
                        capsulesStillFalling = false
                        Log.d("onAnimationEnd", "finish")

                        Handler(Looper.getMainLooper()).postDelayed({
                            Log.d("onAnimationEnd", "calling finishThisActivity")
                            finishThisActivity(1)
                        }, 3000)
                    }


                    override fun onAnimationCancel(animation: Animator) {}

                    override fun onAnimationRepeat(animation: Animator) {}
                })
            }.start()
    }

    private fun canFinishActivity(): Boolean {
        val now = System.currentTimeMillis()

        // Block if help dialog is still visible
        if (helpDialog?.isShowing() == true) {
            Log.d("canFinishActivity", "Blocked: help dialog still showing")
            return false
        }

        // Block if help timeout is active
        if (now < helpBlockUntil) {
            Log.d("canFinishActivity", "Blocked: help timeout still active")
            return false
        }

        // Block if capsules are still falling (animation in progress)
        if (capsulesStillFalling) {
            Log.d("canFinishActivity", "Blocked: capsule animation in progress")
            return false
        }

        if (receipt.isShowing()) {
            Log.d("canFinishActivity", "Blocked: showing something related to receipt")
            return false // or perform whatever closing logic
        }

        Log.d("canFinishActivity", "YESSS")
        return true // Safe to finish
    }


    private fun finishThisActivity(id: Int) {
        if (!canFinishActivity()) return

        when (id) {
            1, 2, 3 -> {
                if (id == lastActivityToSetWatchdog) {
                    Log.d("finishThisActivity", "Finishing by ID $id")
                    helpDialog?.dismiss()
                    receipt?.dismiss()
                    safeCloseUsb()
                    Handler(Looper.getMainLooper()).postDelayed({
                        finish()
                    }, 1000) // Delay in milliseconds (1000 ms = 1 second)
                }
            }

            else -> {
                Log.d("finishThisActivity", "Finishing by ELSE")
                helpDialog?.dismiss()
                receipt?.dismiss()
                safeCloseUsb()
                Handler(Looper.getMainLooper()).postDelayed({
                    finish()
                }, 1000) // Delay in milliseconds (1000 ms = 1 second)
            }
        }
    }

    private fun launchCapsule(index: Int, resource: Int, x: Float) {
        val capsule = ImageView(this)
        capsule.setImageResource(resource)

        Log.d("launchCapsule", index.toString())

        val size = capsuleContainer.width / flavorsQty // dynamically calculated

        val params = FrameLayout.LayoutParams(size, size)
        capsule.layoutParams = params

        capsule.x = x - size / 2 // center align capsule at computed x
        capsule.y = -size.toFloat()

        capsuleContainer.addView(capsule)

        capsule.animate().translationY(capsuleContainer.height.toFloat())
            .setInterpolator(AccelerateInterpolator()).setDuration(1500)
            .withEndAction { capsuleContainer.removeView(capsule) }.start()
    }


    private fun updateProgress() {
        val targetProgress = currentIteration
        ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, targetProgress).apply {
            duration = 300 // in milliseconds; adjust for smoothness
            start()
        }
    }

    private fun resetWatchdog() {
        lastDataReceivedTime = System.currentTimeMillis()
    }

    private fun startUsbIdleMonitor() {
        Thread {
            while (true) {
                Thread.sleep(1000)
                val now = System.currentTimeMillis()
                if (now - lastDataReceivedTime > watchdogTimeout) {
                    if (!coffeeCup.isAnimating || coffeeCup.progress >= 1f) {
                        Log.w("Watchdog", "No USB activity and animation finished. Exiting.")
                        runOnUiThread {
                            Log.w(
                                "Watchdog runOnUiThread",
                                "No USB activity and animation finished. Exiting."
                            )
                            finishThisActivity(2)
                        }
                    } else resetWatchdog()

                    break
                }
            }
        }.start()
    }

    private fun finishAfterDelay() {
        lastActivityToSetWatchdog = 3
        Thread {
            Thread.sleep(watchdogTimeout)
            runOnUiThread {
                Log.d("FinishAfterDelay", "finish")
                finishThisActivity(3)
            }
        }.start()
    }

    private fun updateStatus(message: String) {
        runOnUiThread {
            statusText.text = message
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(usbReceiver)
        } catch (e: Exception) {
            Log.w("Slyco-USB", "Receiver not registered: ${e.message}")
        }
    }
}
