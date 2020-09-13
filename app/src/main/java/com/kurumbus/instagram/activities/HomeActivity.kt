package com.kurumbus.instagram.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.kurumbus.instagram.R
import com.kurumbus.instagram.utils.FirebaseHelper
import com.kurumbus.instagram.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : BaseActivity(0) {
    private val TAG = "HomeActivity"
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mFirebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupBottomNavigation()
        Log.d(TAG, "onCreate")

        mFirebaseHelper = FirebaseHelper(this)

        sign_out_text.setOnClickListener{
            mFirebaseHelper.mAuth.signOut()
        }

        mFirebaseHelper.mAuth.addAuthStateListener {
            if (it.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        mFirebaseHelper.mDatabase.child("feed-posts").child(mFirebaseHelper.mAuth.currentUser!!.uid)
                                 .addValueEventListener(ValueEventListenerAdapter{
                                     val posts = it.children.map{ it.getValue(FeedPost::class.java)!!}
                                     Log.d(TAG, "feedPosts: ${posts.joinToString("\n", "\n")}")
                                     Log.d(TAG, "timestamps: ${posts.first().timestampDate()}")
                                 })
    }

    override fun onStart() {
        super.onStart()
        if (mFirebaseHelper.mAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
