package com.eugi.apeeconomics

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {
    private lateinit var gameView: LiveVillageGameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = LiveVillageGameView(this)
        setContentView(gameView)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!gameView.handleBack()) super.onBackPressed()
    }
}
