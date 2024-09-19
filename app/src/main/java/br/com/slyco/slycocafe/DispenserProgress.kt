package br.com.slyco.slycocafe

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DispenserProgress : AppCompatActivity() {
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
