package br.com.slyco.slycocafe

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class support : AppCompatActivity() {
    private lateinit var thisIntent:Intent


    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val actionBar: androidx.appcompat.app.ActionBar? = supportActionBar
        if (actionBar != null) actionBar.hide()
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        enableEdgeToEdge()
        setContentView(R.layout.activity_support)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        thisIntent = Intent()
        val barra = createVerticalBar()

        var button = findViewById<Button>(R.id.buttonReturnSupport)
        button.setOnClickListener(listener)

        val layout = findViewById<FrameLayout>(R.id.mainSupport)
        layout.addView(barra)


        lifecycleScope.launch {
            // Perform any UI-related tasks here
            processAfterUILoad()
        }

        val imageView: ImageView = findViewById(R.id.qrCodeImageViewSupport)
        val qrCodeData = "https://www.example.com"
        try {
            val bitMatrix = MultiFormatWriter().encode(qrCodeData, BarcodeFormat.QR_CODE, 200, 200)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
            imageView.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private suspend fun processAfterUILoad() {
        withContext(Dispatchers.Main) {
            // Your code to run after UI load
            // For example, updating a TextView

            var progressBar = findViewById<ProgressBar>(R.id.progressBarTimeoutHelperSupport)

            progressBar.max = 100
            progressBar.progress = 100

            ObjectAnimator.ofInt(progressBar,"progress",0)
                .setDuration(30000L)
                .start()

        }


        delay(30000L)

        setResult(Activity.RESULT_OK, thisIntent)
        finish()

    }

    val listener= View.OnClickListener { view ->

        when (view.getId()) {
            R.id.buttonReturn -> {
                setResult(Activity.RESULT_OK, thisIntent)
                finish()
            }
        }
    }

    private fun createVerticalBar(): FrameLayout {
        // Definindo as dimensões da barra
        val barWidth = 15
        val barHeight = 350

        // Criando a forma arredondada
        val radius = floatArrayOf(10f, 10f, 10f, 10f, 10f, 10f, 10f, 10f)
        val roundRectShape = RoundRectShape(radius, null, null)

        // Definindo o gradiente de cores (do cinza escuro ao cinza claro)
        val gradient = LinearGradient(
            0f, 0f, 0f, barHeight.toFloat(),
            Color.parseColor("#4B371C"), Color.parseColor("#E6B68B"),
            Shader.TileMode.CLAMP
        )

        // Criando o Drawable com o gradiente
        val shapeDrawable = ShapeDrawable(roundRectShape).apply {
            paint.shader = gradient
        }

        // Criando a view que vai conter a barra
        val bar = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(barWidth, barHeight)
            background = shapeDrawable
        }


        return bar
    }


}