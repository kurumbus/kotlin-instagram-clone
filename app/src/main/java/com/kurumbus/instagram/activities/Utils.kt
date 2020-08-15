package com.kurumbus.instagram.activities

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

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