package com.kurumbus.instagram.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kurumbus.instagram.R
import com.kurumbus.instagram.utils.FirebaseHelper
import kotlinx.android.synthetic.main.activity_profile_settings.*

class AddFriendsActivity: AppCompatActivity() {

    private val TAG = "AddFriendsActivity"
    private lateinit var mFirebase: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friends)
        Log.d(TAG, "onCreate")

        back_image.setOnClickListener{
            finish()
        }

    }
}