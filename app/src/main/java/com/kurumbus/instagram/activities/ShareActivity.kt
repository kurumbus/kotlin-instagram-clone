package com.kurumbus.instagram.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.kurumbus.instagram.R
import com.kurumbus.instagram.utils.CameraHelper
import com.kurumbus.instagram.utils.FirebaseHelper
import kotlinx.android.synthetic.main.activity_share.*

class ShareActivity : BaseActivity(2) {
    private val TAG = "ShareActivity"
    lateinit var mCameraHelper: CameraHelper
    lateinit var mFirebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        Log.d(TAG, "onCreate")

        mCameraHelper = CameraHelper(this)
        mFirebaseHelper = FirebaseHelper(this)
        mCameraHelper.takeCameraPicture()

        back_image.setOnClickListener{ finish() }
        share_text.setOnClickListener{share()}
}

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == mCameraHelper.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Glide.with(this).load(mCameraHelper.imageUri).centerCrop().into(post_image)
            } else {
                finish()
            }
        }
    }

    private fun share()  {
        val imageUri = mCameraHelper.imageUri
        if (imageUri !== null) {
            val uid = mFirebaseHelper.mAuth.currentUser!!.uid
            mFirebaseHelper.mStorage.child("users")
                .child(uid).child("images")
                .child(imageUri.lastPathSegment!!).putFile(imageUri).addOnCompleteListener{
                    if (it.isSuccessful) {
                        mFirebaseHelper.mStorage.child("users")
                            .child(uid).child("images").child(imageUri.lastPathSegment!!).downloadUrl.addOnCompleteListener{ task ->
                            mFirebaseHelper.mDatabase.child("images").child(uid)
                                .push().setValue(task.result.toString()).addOnCompleteListener{
                                    if (it.isSuccessful) {
                                        startActivity(Intent(this, ProfileActivity::class.java))
                                        finish()
                                    }
                                }
                        }
                    } else {
                        showToast(it.exception!!.message!!)
                    }
                }
        }
    }
}
