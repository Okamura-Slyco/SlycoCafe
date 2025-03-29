package br.com.slyco.slycocafe

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean



class SlycoWallet : AppCompatActivity(), SurfaceHolder.Callback {
    private lateinit var surfaceView: SurfaceView
    private var camera: Camera? = null
    private val processing = AtomicBoolean(false)
    private val executor = Executors.newSingleThreadExecutor()
    private val multiFormatReader = MultiFormatReader()

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
        setContentView(R.layout.slyco_wallet_purchase)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        var thisIntent = Intent()

        thisIntent?.putExtra("hostTrasactionId", "TODO")
        thisIntent?.putExtra("authCode", "TODO")
        thisIntent?.putExtra("pan", "TODO")
        thisIntent?.putExtra("idMethod", "TODO")
        thisIntent?.putExtra("transactionTimestamp", "123456789")

        initMultiFormatReader()

        // Create SurfaceView
        surfaceView = SurfaceView(this)
        setContentView(surfaceView)

        // Request camera permission
        if (checkCameraPermission()) {
            initializeCamera()
        } else {
            requestCameraPermission()
        }

        setResult(Activity.RESULT_OK, thisIntent)
        //finish()
    }

    private fun initMultiFormatReader() {
        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to arrayListOf(
                BarcodeFormat.QR_CODE,
                BarcodeFormat.CODE_128,
                BarcodeFormat.EAN_13,
                // Add other formats as needed
            )
        )
        multiFormatReader.setHints(hints)
    }


    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }

    private fun initializeCamera() {
        surfaceView.holder.addCallback(this)
    }

    private fun getOptimalPreviewSize(
        sizes: List<Camera.Size>,
        width: Int,
        height: Int
    ): Camera.Size {
        val targetRatio = width.toDouble() / height
        return sizes.minByOrNull { size ->
            val ratio = size.width.toDouble() / size.height
            Math.abs(ratio - targetRatio)
        } ?: sizes.first()
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            camera = Camera.open()
            camera?.setPreviewDisplay(holder)
            camera?.setDisplayOrientation(90)

            // Set camera parameters
            camera?.parameters = camera?.parameters?.apply {
                focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                // Optimize preview size
                val sizes = supportedPreviewSizes
                val optimalSize = getOptimalPreviewSize(sizes, surfaceView.width, surfaceView.height)
                setPreviewSize(optimalSize.width, optimalSize.height)
            }
            startPreviewAndDecode()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up camera: ${e.message}")
            Toast.makeText(this, "Error setting up camera", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (holder.surface == null) {
            return
        }

        try {
            camera?.stopPreview()
        } catch (e: Exception) {
            // Ignore: tried to stop a non-existent preview
        }

        try {
            camera?.setPreviewDisplay(holder)
            camera?.startPreview()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting camera preview: ${e.message}")
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    private fun startPreviewAndDecode() {
        camera?.startPreview()

        camera?.setPreviewCallback { data, camera ->
            if (!processing.get()) {
                processing.set(true)
                executor.execute {
                    try {
                        val size = camera.parameters.previewSize
                        val source = PlanarYUVLuminanceSource(
                            data,
                            size.width,
                            size.height,
                            0,
                            0,
                            size.width,
                            size.height,
                            false
                        )

                        val bitmap = BinaryBitmap(HybridBinarizer(source))
                        try {
                            val result = multiFormatReader.decode(bitmap)
                            onBarcodeDetected(result)
                        } catch (e: NotFoundException) {
                            // No barcode found, continue scanning
                            Log.d("ttt","tt")
                        }
                    } finally {
                        processing.set(false)
                    }
                }
            }
        }
    }

    private fun onBarcodeDetected(result: Result) {
        runOnUiThread {
            handleBarcodeResult(result.text)
        }
    }

    private fun handleBarcodeResult(barcodeValue: String?) {
        barcodeValue?.let { value ->
            // Handle the barcode value
            Toast.makeText(this, "Barcode: $value", Toast.LENGTH_SHORT).show()
            // Add your business logic here
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCamera()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
        camera?.release()
    }

    companion object {
        private const val TAG = "BarcodeScannerActivity"
        private const val CAMERA_PERMISSION_REQUEST = 100
    }


}
