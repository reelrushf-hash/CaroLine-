package com.example

import android.app.Application
import android.util.Log
import com.example.data.local.AppDatabase

class CarolineApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i("CarolineApp", "CARO LINE AI Jarvis System Initialized")
        // Initialize Room DB
        AppDatabase.getInstance(this)
    }
}
