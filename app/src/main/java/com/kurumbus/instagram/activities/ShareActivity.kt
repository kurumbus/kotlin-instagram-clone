package com.kurumbus.instagram.activities

import android.os.Bundle
import android.util.Log
import com.kurumbus.instagram.R

class ShareActivity : BaseActivity(2) {
    private val TAG = "ShareActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupBottomNavigation()
        Log.d(TAG, "onCreate")
    }
}
