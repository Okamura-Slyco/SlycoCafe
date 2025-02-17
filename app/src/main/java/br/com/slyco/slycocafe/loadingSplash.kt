package br.com.slyco.slycocafe

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import com.airbnb.lottie.LottieAnimationView

class LoadingDialog(context: Context) : Dialog(context) {
    private lateinit var animationView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_loading)

        // Initialize the animation view
        animationView = findViewById(R.id.loadingAnimation)

        // Make sure animation is playing
        animationView.playAnimation()

        // Dialog window setup
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            decorView.setBackgroundResource(android.R.color.transparent)

            // Set dialog width and height
            setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        // Prevent dialog from being canceled
        setCancelable(false)
    }

    override fun show() {
        super.show()
        animationView.playAnimation()
    }

    override fun dismiss() {
        animationView.cancelAnimation()
        super.dismiss()
    }
}

