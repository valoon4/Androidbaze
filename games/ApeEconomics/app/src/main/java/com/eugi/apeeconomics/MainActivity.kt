package com.eugi.apeeconomics

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {
    private lateinit var gameView: PixelVillageV09

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = PixelVillageV09(this)
        setContentView(gameView)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!gameView.handleBack()) super.onBackPressed()
    }
}
