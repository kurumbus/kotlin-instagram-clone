package com.kurumbus.instagram.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.kurumbus.instagram.R
import com.kurumbus.instagram.models.User
import com.kurumbus.instagram.utils.CameraHelper
import com.kurumbus.instagram.utils.FirebaseHelper
import com.kurumbus.instagram.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.activity_share.*
import java.sql.Date
import java.sql.Timestamp

class ShareActivity : BaseActivity(2) {
    private val TAG = "ShareActivity"
    lateinit var mCameraHelper: CameraHelper
    lateinit var mFirebaseHelper: FirebaseHelper
    private lateinit var mUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        Log.d(TAG, "onCreate")

        mCameraHelper = CameraHelper(this)
        mFirebaseHelper = FirebaseHelper(this)
        mCameraHelper.takeCameraPicture()

        back_image.setOnClickListener{ finish() }
        share_text.setOnClickListener{share()}

        mFirebaseHelper.currentUserReference()
            .addValueEventListener(ValueEventListenerAdapter{
                mUser = it.asUser()!!
            })
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
                            val imageDownloadUrl = task.result.toString()
                            mFirebaseHelper.mDatabase.child("images").child(uid)
                                .push().setValue(imageDownloadUrl).addOnCompleteListener{
                                    Log.d(TAG, "created image")
                                    if (it.isSuccessful) {
                                        mFirebaseHelper.mDatabase.child("feed-posts").child(uid)
                                                       .push().setValue(mkFeedPost(uid, imageDownloadUrl))
                                                        .addOnCompleteListener{
                                                            Log.d(TAG, "created feedpost")
                                                            if (it.isSuccessful) {
                                                                startActivity(Intent(this, ProfileActivity::class.java))
                                                                finish()
                                                            } else {
                                                                showToast(it.exception!!.message!!)
                                                            }
                                                        }
                                    } else {
                                        showToast(it.exception!!.message!!)
                                    }
                                }
                        }
                    } else {
                        showToast(it.exception!!.message!!)
                    }
                }
        }
    }

    private fun mkFeedPost(
        uid: String,
        imageDownloadUrl: String
    ): FeedPost {
        return FeedPost(
            uid = uid,
            username = mUser.username,
            image = imageDownloadUrl,
            caption = caption_input.text.toString(),
            photo = mUser.photo
        )
    }
}

data class FeedPost(val uid: String = "", val username: String = "", val image: String = "",
                    val likesCount: Int = 0, val commentsCount: Int = 0, val caption: String = "",
                    val comments: List<Comment> = emptyList(),
                    val timestamp: Any = ServerValue.TIMESTAMP,  val photo: String? = "") {
    fun timestampDate(): Date = Date(timestamp as Long)
}

data class Comment(val uid: String, val username: String, val text: String) {

}