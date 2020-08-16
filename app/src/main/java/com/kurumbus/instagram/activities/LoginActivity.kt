package com.kurumbus.instagram.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.kurumbus.instagram.R
import kotlinx.android.synthetic.main.activity_login.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

class LoginActivity : AppCompatActivity(), KeyboardVisibilityEventListener, View.OnClickListener {


    private val TAG = "LoginActivity"
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d(TAG, "onCreate")
        KeyboardVisibilityEvent.setEventListener(this, this)
        coordinateButtonAndInputs(login_button, email_input, password_input)
        mAuth = FirebaseAuth.getInstance()

        login_button.setOnClickListener(this)

        create_account_text.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onClick(v: View) {
        validate(email_input.text.toString(), password_input.text.toString())
        val email = email_input.text.toString()
        val password = password_input.text.toString()
        if (validate(email, password)) {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                if (it.isSuccessful) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    showToast("Incorrect credentials")
                }
            }
        } else {
            showToast("Please enter email and password")
        }
    }

    override fun onVisibilityChanged(isKeyboardOpen: Boolean) {
        if (isKeyboardOpen) {
            create_account_text.visibility = View.GONE
        } else {
            create_account_text.visibility = View.VISIBLE
        }
    }

    private fun validate(email: String, password: String) = email.isNotEmpty() && password.isNotEmpty()

}
