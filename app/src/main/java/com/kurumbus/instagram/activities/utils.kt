package com.kurumbus.instagram.activities

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.kurumbus.instagram.R
import com.kurumbus.instagram.models.FeedPost
import com.kurumbus.instagram.models.User

fun Context.showToast(text: String = "", duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, text, duration).show()
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

fun ImageView.loadUserPhoto(photoUrl: String?) =
    ifNotDestroyed {
        Glide.with(this).load(photoUrl).fallback(R.drawable.person).into(this )
    }


fun ImageView.loadImage(image: String?) =
    ifNotDestroyed {
        Glide.with(this).load(image).into(this)
    }

private fun View.ifNotDestroyed(block: () -> Unit) {
    if (! (context as Activity).isDestroyed) {
        block()
    }
}


fun <T> task(block: (TaskCompletionSource<T>) -> Unit): Task<T> {
    val taskSource = TaskCompletionSource<T>()
    block(taskSource)
    return taskSource.task
}


fun DataSnapshot.asUser(): User? =
    getValue(User::class.java)?.copy(uid = key!!)

fun DataSnapshot.asFeedPost(): FeedPost? =
    getValue(FeedPost::class.java)?.copy(id = key)

fun DatabaseReference.setValueTrueOrRemove(condition: Boolean) =
    if (condition) setValue(true) else removeValue()