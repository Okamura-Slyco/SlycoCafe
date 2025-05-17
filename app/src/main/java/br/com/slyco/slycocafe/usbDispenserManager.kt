package br.com.slyco.slycocafe.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber

object UsbDispenserManager {

    private var port: UsbSerialPort? = null
    private var connection: UsbDeviceConnection? = null
    private var readCallback: ((String) -> Unit)? = null
    private const val ACTION_USB_PERMISSION = "br.com.slyco.slycocafe.USB_PERMISSION"

    private var onReadyCallback: (() -> Unit)? = null

    fun registerAndRequestPermission(context: Context, onReady: () -> Unit): Boolean {
        onReadyCallback = onReady

        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val device = usbManager.deviceList.values.firstOrNull() ?: return false

        if (usbManager.hasPermission(device)) {
            val success = initializeWithDevice(usbManager, device)
            if (success) onReadyCallback?.invoke()
            return success
        }

        val permissionIntent = PendingIntent.getBroadcast(
            context, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE
        )
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        context.registerReceiver(usbPermissionReceiver, filter)
        usbManager.requestPermission(device, permissionIntent)
        return true // pending approval
    }

    private lateinit var applicationContext: Context

    fun attachApplicationContext(ctx: Context) {
        applicationContext = ctx.applicationContext
    }

    private val usbPermissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_USB_PERMISSION) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                if (device != null && granted) {
                    val manager = applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
                    val ok = initializeWithDevice(manager, device)
                    if (ok) {
                        Log.d("UsbDispenserManager", "USB ready")
                        onReadyCallback?.invoke()
                    }
                } else {
                    Log.w("UsbDispenserManager", "Permission denied or device missing")
                }
            }
        }
    }

    fun initializeWithDevice(usbManager: UsbManager, device: UsbDevice): Boolean {
        val driver = UsbSerialProber.getDefaultProber()
            .findAllDrivers(usbManager)
            .firstOrNull { it.device.deviceId == device.deviceId } ?: return false

        connection = usbManager.openDevice(driver.device) ?: return false
        port = driver.ports[0]
        port?.open(connection)
        port?.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

        startReader()
        return true
    }

    fun setReadCallback(callback: (String) -> Unit) {
        readCallback = callback
    }

    fun release(items: String) {
        send("$items\n")
    }

    fun enable(items: String) {
        send("reset:${items.lowercase()}\n")
    }

    fun disable(items: String) {
        send("empty:${items.lowercase()}\n")
    }

    private fun send(buffer: String) {
        try {
            port?.write(buffer.toByteArray(), 100)
            Log.d("UsbDispenserManager", "Sent: $buffer")
        } catch (e: Exception) {
            Log.e("UsbDispenserManager", "Send failed: ${e.message}")
        }
    }

    private fun startReader() {
        Thread {
            val readBuffer = ByteArray(64)
            var messageBuffer = ""

            while (true) {
                try {
                    val len = port?.read(readBuffer, 1000) ?: 0
                    if (len > 0) {
                        val chunk = readBuffer.copyOf(len).toString(Charsets.UTF_8)
                        messageBuffer += chunk

                        while (messageBuffer.contains("\n")) {
                            val splitIndex = messageBuffer.indexOf("\n")
                            val complete = messageBuffer.substring(0, splitIndex).trim()
                            messageBuffer = messageBuffer.substring(splitIndex + 1)

                            if (complete.isNotEmpty()) {
                                Log.d("UsbDispenserManager", "Received: $complete")
                                readCallback?.invoke(complete)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("UsbDispenserManager", "Read failed: ${e.message}")
                    break
                }
            }
        }.start()
    }

    fun close() {
        try {
            port?.close()
            connection?.close()
        } catch (e: Exception) {
            Log.e("UsbDispenserManager", "Close error: ${e.message}")
        } finally {
            port = null
            connection = null
        }
    }
}
