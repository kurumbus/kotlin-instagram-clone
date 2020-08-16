package com.kurumbus.instagram.activities

import android.content.Intent
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kurumbus.instagram.R
import com.kurumbus.instagram.models.User
import com.kurumbus.instagram.views.PasswordDialog
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.io.File
import java.io.IOException
import java.util.*
import android.annotation.SuppressLint
import android.net.Uri
import androidx.core.content.FileProvider


class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {
    private val TAKE_PICTURE_REQUEST_CODE = 1
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    private lateinit var mPendingUser: User
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mStorage: StorageReference
    private lateinit var mImageUri: Uri

    private lateinit var mUser: User
    private lateinit var mAuth: FirebaseAuth
    private val TAG = "EditProfileActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_profile)
        Log.d(TAG, "onCreate")

        close_image.setOnClickListener{
            finish()
        }

        save_image.setOnClickListener{
            updateProfile()
        }

        mAuth = FirebaseAuth.getInstance()
        val uid = mAuth.currentUser!!.uid.toString()
        mDatabase = FirebaseDatabase.getInstance().reference
        mStorage = FirebaseStorage.getInstance().reference

        change_photo_text.setOnClickListener{ takeCameraPicture() }

        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(ValueEventListenerAdapter {
            Log.d(TAG, "snapshot" )
            mUser = it.getValue(User::class.java)!!
            name_input.setText(mUser.name, TextView.BufferType.EDITABLE)
            website_input.setText(mUser.website, TextView.BufferType.EDITABLE)
            bio_input.setText(mUser.bio, TextView.BufferType.EDITABLE)
            email_input.setText(mUser.email, TextView.BufferType.EDITABLE)
            phone_input.setText(mUser.phone.toString(), TextView.BufferType.EDITABLE)
            username_input.setText(mUser.username, TextView.BufferType.EDITABLE)
        })
    }

    private fun takeCameraPicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null ) {
            val imageFile = createImageFile()
            mImageUri  = FileProvider.getUriForFile(
                this,
                "com.kurumbus.instagram.fileprovider",
                imageFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
            startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode ==  TAKE_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            val uid = mAuth.currentUser!!.uid
            mStorage.child("users/$uid/photo").putFile(mImageUri).addOnCompleteListener{
                if (it.isSuccessful) {
                    mStorage.child("users/$uid/photo").downloadUrl.addOnCompleteListener{
                        mDatabase.child("users/$uid/photo").setValue(it.result.toString()).addOnCompleteListener{
                            if (it.isSuccessful) {
                                showToast("Photo Saved")
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

    private fun createImageFile(): File {
        // Create an image file name

        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
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
        val phoneStr = phone_input.text.toString()
        return return User(
            name = name_input.text.toString(),
            username = username_input.text.toString(),
            website = website_input.text.toString(),
            bio = bio_input.text.toString(),
            email = email_input.text.toString(),
            phone = if (phoneStr.isNotEmpty()) phoneStr.toLong() else 0
        )
    }

    private fun validate(user: User): String? =
        when {
            user.name.isEmpty() -> "Please enter name"
            user.username.isEmpty() -> "Please enter username"
            user.email.isEmpty() -> "Please enter email"
            else -> null
        }

    override fun onPasswordConfirm(password: String) {
        if (password.isNotEmpty()) {
            val credential = EmailAuthProvider.getCredential(mUser.email, password)
            mAuth.currentUser!!.reauthenticate(credential) {
                mAuth.currentUser!!.updateEmail(mPendingUser.email) {
                    updateUser(mPendingUser)
                }
            }
        } else {
            showToast("You must enter password")
        }
    }

    private fun updateUser(user: User) {
        val updatesMap =  mutableMapOf<String, Any>()
        if (user.name != mUser.name) updatesMap["name"] = user.name
        if (user.username != mUser.username) updatesMap["username"] = user.username
        if (user.website != mUser.website) updatesMap["website"] = user.website
        if (user.bio != mUser.bio) updatesMap["bio"] = user.bio
        if (user.email != mUser.email) updatesMap["email"] = user.email
        if (user.phone != mUser.phone) updatesMap["phone"] = user.phone

        mDatabase.updateUser(mAuth.currentUser!!.uid, updatesMap) {
            showToast("Profile Saved")
            finish()
        }
    }

    private fun FirebaseUser.reauthenticate(credential: AuthCredential, onSuccess: () -> Unit) {
        reauthenticate(credential).addOnCompleteListener{
            if (it.isSuccessful) {
                onSuccess()
            } else {
                showToast(it.exception!!.message!!)
            }
        }
    }

    private fun FirebaseUser.updateEmail(email: String, onSuccess: () -> Unit) {
        updateEmail(mPendingUser.email).addOnCompleteListener{
            if (it.isSuccessful) {
                onSuccess()
            } else {
                showToast(it.exception!!.message!!)
            }
        }
    }

    private fun DatabaseReference.updateUser(uid: String, updates: Map<String, Any>, onSuccess: () -> Unit) {
        child("users").child(uid).updateChildren(updates)
            .addOnCompleteListener{
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    showToast(it.exception!!.message!!)
                }
            }
    }


}
