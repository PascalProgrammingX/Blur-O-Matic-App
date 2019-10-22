package com.example.background

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Handler().postDelayed({
            homepage()
        }, 3000)

    }


    private fun homepage() {
        startActivity(Intent(this, SelectImageActivity::class.java))
    }
}
