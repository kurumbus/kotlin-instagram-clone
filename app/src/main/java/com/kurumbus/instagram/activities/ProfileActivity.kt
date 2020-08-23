package com.kurumbus.instagram.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.MonthDisplayHelper
import com.kurumbus.instagram.R
import com.kurumbus.instagram.models.User
import com.kurumbus.instagram.utils.FirebaseHelper
import com.kurumbus.instagram.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : BaseActivity(4) {
    private val TAG = "ProfileActivity"
    private lateinit var mFirebaseHelper: FirebaseHelper
    private lateinit var mUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupBottomNavigation()
        Log.d(TAG, "onCreate")

        mFirebaseHelper = FirebaseHelper(this)
        mFirebaseHelper.currentUserReference().addValueEventListener(ValueEventListenerAdapter {
            mUser = it.getValue(User::class.java)!!
            toolbar_title.text = mUser.username
            profile_image.loadUserPhoto(mUser.photo)

        })

        edit_profile_button.setOnClickListener{
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
    }
}
