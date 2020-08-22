package com.kurumbus.instagram.activities

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kurumbus.instagram.R
import kotlinx.android.synthetic.main.fragment_register_email.view.*

class ValueEventListenerAdapter(val handler: (DataSnapshot) -> Unit) : ValueEventListener {
    private val TAG = "ValueEventListenerAd"

    override fun onDataChange(snapshot: DataSnapshot) {
        handler(snapshot)
    }

    override fun onCancelled(error: DatabaseError) {
        Log.e(TAG, "onCancelled", error.toException())
    }
}

fun Context.showToast(text: String = "", duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, text, duration).show()
}

fun ImageView.loadUserPhoto(photoUrl: String?) {
    if (! (context as Activity).isDestroyed) {
        Glide.with(this).load(photoUrl).fallback(R.drawable.person).into(this )
    }
}

fun Editable.toStringOrNull(): String? {
    val str = toString()
    return if (str.isEmpty()) null else str
}

fun coordinateButtonAndInputs(btn: Button, vararg inputs: EditText) {
    val watcher = object: TextWatcher {
        override fun afterTextChanged(s: Editable?) { btn.isEnabled = inputs.all { it.text.isNotEmpty() }}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
    inputs.forEach { it.addTextChangedListener(watcher) }
    btn.isEnabled = inputs.all { it.text.isNotEmpty() }
}