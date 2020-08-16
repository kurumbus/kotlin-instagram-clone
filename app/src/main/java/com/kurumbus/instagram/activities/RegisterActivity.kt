package com.kurumbus.instagram.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kurumbus.instagram.R
import com.kurumbus.instagram.models.User
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_register_email.*
import kotlinx.android.synthetic.main.fragment_register_email.email_input
import kotlinx.android.synthetic.main.fragment_register_name_pass.*

class RegisterActivity : AppCompatActivity(), EmailFragment.Listener, NamePassFragment.Listener {
    private val TAG = "RegisterActivity"

    private var mEmail: String? = null
    private var mFullName: String? = null
    private var mPassword: String? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.frame_layout, EmailFragment()).commit()
        }
    }

    override fun onNext(email: String) {
        if (email.isNotEmpty()) {
            mEmail = email
            mAuth.fetchSignInMethodsForEmail(email) {signInMethods ->
                if (signInMethods.isEmpty()) {
                    supportFragmentManager.beginTransaction().replace(R.id.frame_layout, NamePassFragment())
                        .addToBackStack(null).commit()
                } else {
                    showToast("Email already registered")
                }
            }
        } else {
            showToast("Email must not be empty")
        }
    }

    private fun FirebaseAuth.fetchSignInMethodsForEmail(email: String, onSuccess: (List<String>) -> Unit) {
        fetchSignInMethodsForEmail(email).addOnCompleteListener{
            if (it.isSuccessful) {
                onSuccess(it.result?.signInMethods ?: emptyList<String>())

            } else {
                Log.e(TAG, "Exception", it.exception)
                if (it.exception is FirebaseAuthInvalidCredentialsException) {
                    showToast("Email is badly formatted")
                }
            }
        }
    }

    override fun onRegister(fullName: String, password: String) {
        if (fullName.isNotEmpty() && password.isNotEmpty() ) {
            mFullName = fullName
            mPassword = password

            mAuth.createUserWithEmailAndPassword(mEmail!!, mPassword!!) {
                val user = makeUser(mFullName!!, mEmail!!)
                mDatabase.createUser(it.user!!.uid, user) {
                    startHomeActivity()
                }
            }
        } else {
            showToast("Name and Password must not be empty")
        }
    }

    private fun makeUsername(fullName: String): String = fullName.toLowerCase().replace(" ", ".")

    private fun makeUser(fullName: String, email: String): User {
        val username = makeUsername(mFullName!!)
        return User(fullName, username, "", "", email)
    }

    private fun startHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun FirebaseAuth.createUserWithEmailAndPassword(email: String, password: String, onSuccess: (AuthResult) -> Unit) {
        createUserWithEmailAndPassword(email, password).addOnCompleteListener{
            if (it.isSuccessful) {
                onSuccess(it.result!!)
            } else {
                Log.e(TAG, "Failed to create user", it.exception)
            }
        }
    }

    private fun DatabaseReference.createUser(uid: String, user: User, onSuccess: () -> Unit) {
        val reference = mDatabase.child("users").child(uid)
        reference.setValue(user).addOnCompleteListener{
            if (it.isSuccessful) {
                onSuccess()
            } else {
                Log.e(TAG, "Failed to create a profile", it.exception)
                showToast("Something wrong hapenned")
            }
        }
    }
}

class EmailFragment: Fragment() {
    private lateinit var mListener: Listener

    interface Listener {
        fun onNext(email: String)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        coordinateButtonAndInputs(next_button, email_input)

        next_button.setOnClickListener{
            val email = email_input.text.toString()
            mListener.onNext(email)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }
}

class NamePassFragment: Fragment() {
    private lateinit var mListener: Listener

    interface Listener {
        fun onRegister(fullName: String, password: String)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register_name_pass, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        coordinateButtonAndInputs(register_button, full_name_input, password_input_register)
        register_button.setOnClickListener{
            val fullName = full_name_input.text.toString()
            val password = password_input_register.text.toString()
            mListener.onRegister(fullName, password)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }
}