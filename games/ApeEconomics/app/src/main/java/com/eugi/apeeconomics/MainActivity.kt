package com.eugi.apeeconomics

import android.app.Activity
import android.os.Bundle
import android.view.View

class MainActivity : Activity() {
    private lateinit var gameView: PixelVillageV10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        gameView = PixelVillageV10(this)
        setContentView(gameView)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!gameView.handleBack()) super.onBackPressed()
    }
}
