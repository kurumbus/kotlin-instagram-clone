package com.kurumbus.instagram.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.kurumbus.instagram.R
import com.kurumbus.instagram.models.User
import com.kurumbus.instagram.views.PasswordDialog
import kotlinx.android.synthetic.main.activity_edit_profile.*
import android.annotation.SuppressLint
import com.kurumbus.instagram.utils.CameraHelper
import com.kurumbus.instagram.utils.FirebaseHelper
import com.kurumbus.instagram.utils.ValueEventListenerAdapter

class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {
    private lateinit var mPendingUser: User
    private lateinit var cameraHelper: CameraHelper
    private lateinit var mFireBaseHelper: FirebaseHelper
    private lateinit var mUser: User

    private val TAG = "EditProfileActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_profile)
        Log.d(TAG, "onCreate")

        cameraHelper = CameraHelper(this)
        close_image.setOnClickListener{
            finish()
        }

        save_image.setOnClickListener{
            updateProfile()
        }

        mFireBaseHelper = FirebaseHelper(this)

        change_photo_text.setOnClickListener{ cameraHelper.takeCameraPicture() }

        mFireBaseHelper.currentUserReference()
            .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                Log.d(TAG, "snapshot")
                mUser = it.getValue(User::class.java)!!
                name_input.setText(mUser.name)
                website_input.setText(mUser.website)
                bio_input.setText(mUser.bio)
                email_input.setText(mUser.email)
                phone_input.setText(mUser.phone?.toString())
                username_input.setText(mUser.username)
                profile_image.loadUserPhoto(mUser.photo)
            })
    }
    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode ==  cameraHelper.REQUEST_CODE && resultCode == RESULT_OK) {
            mFireBaseHelper.uploadUserPhoto(cameraHelper.imageUri!!) { url ->
                mFireBaseHelper.updateUserPhoto(url) {
                    mUser = mUser.copy(photo = url)
                    profile_image.loadUserPhoto(mUser.photo)
                    showToast("Photo Saved")
                }
            }
        }
    }


    private fun updateProfile() {
        mPendingUser = readInputs()
        val error = validate(mPendingUser)
        if (error == null) {
            if (mPendingUser.email == mUser.email) {
                updateUser(mPendingUser)
            } else {
                PasswordDialog().show(supportFragmentManager, "password_dialog")
            }
        } else {
            showToast(error)
        }
    }

    private fun readInputs(): User {
        return return User(
            email = email_input.text.toString(),
            username = username_input.text.toString(),
            name = name_input.text.toStringOrNull(),
            website = website_input.text.toStringOrNull(),
            bio = bio_input.text.toStringOrNull(),
            phone = phone_input.text.toString().toLongOrNull()
        )
    }

    private fun validate(user: User): String? =
        when {
            user.name!!.isEmpty() -> "Please enter name"
            user.username.isEmpty() -> "Please enter username"
            user.email.isEmpty() -> "Please enter email"
            else -> null
        }

    override fun onPasswordConfirm(password: String) {
        if (password.isNotEmpty()) {
            val credential = EmailAuthProvider.getCredential(mUser.email, password)
            mFireBaseHelper.reauthenticate(credential) {
                mFireBaseHelper.updateEmail(mPendingUser.email) {
                    updateUser(mPendingUser)
                }
            }
        } else {
            showToast("You must enter password")
        }
    }

    private fun updateUser(user: User) {
        val updatesMap =  mutableMapOf<String, Any? >()
        if (user.name != mUser.name) updatesMap["name"] = user.name
        if (user.username != mUser.username) updatesMap["username"] = user.username
        if (user.website != mUser.website) updatesMap["website"] = user.website
        if (user.bio != mUser.bio) updatesMap["bio"] = user.bio
        if (user.email != mUser.email) updatesMap["email"] = user.email
        if (user.phone != mUser.phone) updatesMap["phone"] = user.phone

        mFireBaseHelper.updateUser(updatesMap) {
            showToast("Profile Saved")
            finish()
        }
    }


}
