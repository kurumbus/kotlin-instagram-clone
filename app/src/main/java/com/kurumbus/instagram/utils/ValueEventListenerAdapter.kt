package com.kurumbus.instagram.utils

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ValueEventListenerAdapter(val handler: (DataSnapshot) -> Unit) :
    ValueEventListener {
    private val TAG = "ValueEventListenerAd"

    override fun onDataChange(snapshot: DataSnapshot) {
        handler(snapshot)
    }

    override fun onCancelled(error: DatabaseError) {
        Log.e(TAG, "onCancelled", error.toException())
    }
}