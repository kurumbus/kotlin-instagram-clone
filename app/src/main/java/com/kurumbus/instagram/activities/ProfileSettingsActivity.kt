package com.kurumbus.instagram.activities

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import com.kurumbus.instagram.R
import com.kurumbus.instagram.utils.FirebaseHelper

import kotlinx.android.synthetic.main.activity_profile_settings.*

class ProfileSettingsActivity : AppCompatActivity() {

    private val TAG = "ProfileSettingsActivity"
    private lateinit var mFirebase: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)
        Log.d(TAG, "onCreate")

        sign_out_text.setOnClickListener {
            mFirebase = FirebaseHelper(this)
            mFirebase.mAuth.signOut()
        }

        back_image.setOnClickListener{
            finish()
        }

    }

}
