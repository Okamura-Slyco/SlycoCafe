package br.com.slyco.slycocafe.usb

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

    fun initialize(usbManager: UsbManager): Boolean {
        val drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (drivers.isEmpty()) return false

        val driver = drivers[0]
        connection = usbManager.openDevice(driver.device)
        if (connection == null) return false

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
        send("reset:$items\n")
    }

    fun disable(items: String) {
        send("empty:$items\n")
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
