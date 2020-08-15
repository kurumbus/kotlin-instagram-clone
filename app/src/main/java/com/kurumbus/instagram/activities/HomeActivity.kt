package com.kurumbus.instagram.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.kurumbus.instagram.R
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : BaseActivity(0) {
    private val TAG = "HomeActivity"

    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupBottomNavigation()
        Log.d(TAG, "onCreate")
        mAuth = FirebaseAuth.getInstance()
      //  mAuth.signOut()

        sign_out_text.setOnClickListener{
            mAuth.signOut()
        }

        mAuth.addAuthStateListener {
            if (it.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
