package com.kurumbus.instagram

import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : BaseActivity(0) {
    private val TAG = "HomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupBottomNavigation()
        Log.d(TAG, "onCreate")

        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword("grigory.shein@gmail.com", "orion123")
            .addOnCompleteListener{
                if (it.isSuccessful) {
                    Log.d(TAG, "Sign in successful")
                } else {
                    Log.d(TAG, "Sign in failure", it.exception)
                }
            }
    }
}
