package br.com.slyco.slycocafe

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class SplashActivity : AppCompatActivity() {

    private var lottieFinished = false
    private var mainReady = false

    private val mainReadyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("Splash", "MainActivity is ready")
            mainReady = true
            maybeFinishSplash()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) actionBar.hide()
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        val lottieView = findViewById<LottieAnimationView>(R.id.splashAnimation)
        lottieView.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                Log.d("Splash", "Lottie animation finished")
                lottieFinished = true
                maybeFinishSplash()
            }
        })

        registerReceiver(mainReadyReceiver, IntentFilter("br.com.slyco.slycocafe.MAIN_READY"))


        Handler(Looper.getMainLooper()).postDelayed({
            if (!mainReady || !lottieFinished) {
                Log.w("Splash", "Timeout reached — forcing transition")
                mainReady = true
                lottieFinished = true
                maybeFinishSplash()
            }
        }, 5000) // force exit after 5s

    }
    private fun maybeFinishSplash() {
        if (lottieFinished && mainReady) {
            unregisterReceiver(mainReadyReceiver)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

}
