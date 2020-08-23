package com.kurumbus.instagram.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.kurumbus.instagram.R
import com.kurumbus.instagram.utils.CameraHelper
import kotlinx.android.synthetic.main.activity_share.*

class ShareActivity : BaseActivity(2) {
    private val TAG = "ShareActivity"
    lateinit var mCameraHelper: CameraHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        Log.d(TAG, "onCreate")

        mCameraHelper = CameraHelper(this)
        mCameraHelper.takeCameraPicture()

        back_image.setOnClickListener{ finish() }
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == mCameraHelper.REQUEST_CODE && resultCode == RESULT_OK) {
            Glide.with(this).load(mCameraHelper.imageUri).centerCrop().into(post_image)
        }
    }
}
