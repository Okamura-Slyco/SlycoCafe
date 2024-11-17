package br.com.slyco.slycocafe

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread
import kotlin.math.max

class helperDialog : AppCompatActivity() {
    var thisIntent: Intent? = null
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
        setContentView(R.layout.activity_helper_dialog)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        thisIntent = Intent()
        val barra = createVerticalBar()

        var button = findViewById<Button>(R.id.buttonReturn)
        button.setOnClickListener(listener)

        val layout = findViewById<FrameLayout>(R.id.layout)
        layout.addView(barra)


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

            var progressBar = findViewById<ProgressBar>(R.id.progressBarTimeoutHelper)

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
        val barWidth = 20
        val barHeight = 350

        // Criando a forma arredondada
        val radius = floatArrayOf(10f, 10f, 10f, 10f, 0f, 0f, 0f, 0f)
        val roundRectShape = RoundRectShape(radius, null, null)

        // Definindo o gradiente de cores (do cinza escuro ao cinza claro)
        val gradient = LinearGradient(
            0f, 0f, 0f, barHeight.toFloat(),
            Color.parseColor("#333333"), Color.parseColor("#EEEEEE"),
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