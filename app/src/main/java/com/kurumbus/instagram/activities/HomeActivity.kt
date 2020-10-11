package com.kurumbus.instagram.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import com.kurumbus.instagram.R
import com.kurumbus.instagram.models.FeedPost
import com.kurumbus.instagram.utils.FirebaseHelper
import com.kurumbus.instagram.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.feed_item.view.*

class HomeActivity : BaseActivity(0), FeedAdapter.Listener {
    private val TAG = "HomeActivity"
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAdapter: FeedAdapter
    private var mLikesListenersMap: Map<String, ValueEventListener> = emptyMap()

    private lateinit var mFirebaseHelper: FirebaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupBottomNavigation()
        Log.d(TAG, "onCreate")

        mFirebaseHelper = FirebaseHelper(this)



        mFirebaseHelper.mAuth.addAuthStateListener {
            if (it.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }


    }
    override fun onStart() {
        super.onStart()
        val currentUser = mFirebaseHelper.mAuth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            mFirebaseHelper.mDatabase.child("feed-posts").child(currentUser.uid)
                .addValueEventListener(ValueEventListenerAdapter{
                    val posts = it.children.map{ it.asFeedPost()!!}
                        .sortedByDescending { it.timestampDate() }
                    Log.d(TAG, "feedPosts: ${posts.joinToString("\n", "\n")}")
                    mAdapter = FeedAdapter(this, posts)
                    feed_recycler.adapter = mAdapter
                    feed_recycler.layoutManager = LinearLayoutManager(this)
                })
        }
    }

    override fun toggleLike(postId: String) {
        Log.d(TAG, "toggle like for $postId")
        val reference = mFirebaseHelper.mDatabase.child("likes").child(postId).child(mFirebaseHelper.currentUserUid()!!)
        reference.addListenerForSingleValueEvent(ValueEventListenerAdapter {
            reference.setValueTrueOrRemove(!it.exists())
        })
    }

    override fun loadLikes(postId: String, position: Int) {
        fun createListener() =
        mFirebaseHelper.mDatabase.child("likes").child(postId).
                addValueEventListener(ValueEventListenerAdapter {
                    val userLikes = it.children.map{ it.key}.toSet()
                    val postLikes = FeedPostLikes(
                        userLikes.size,
                        userLikes.contains(mFirebaseHelper.currentUserUid())
                    )
                    mAdapter.updatePostLikes(position, postLikes)
                })


        val isCreatingNewListener = mLikesListenersMap[postId] == null
        Log.d(TAG, "loadlikes for position $position, $isCreatingNewListener")
        if (isCreatingNewListener) {
            mLikesListenersMap += (postId to createListener())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mLikesListenersMap.forEach{mFirebaseHelper.mDatabase.removeEventListener(it.value)}
    }


}

data class FeedPostLikes(val likesCount: Int, val likedByAuth: Boolean) {

}

class FeedAdapter(private val listener: Listener, private val posts: List<FeedPost>)
    : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    private var postLikesMap: Map<Int, FeedPostLikes> = emptyMap()

    interface Listener {
        fun toggleLike(postId: String)
        fun loadLikes(postId: String, position: Int): Any
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder:ViewHolder, position: Int) {
        val feedPost = posts[position]
        val likes = postLikesMap[position] ?: defaultPostLikes
        with (holder.view) {
            val avatar= if (feedPost.photo!!.isNotEmpty()) {  feedPost.photo }  else null // to Prevent previous value bug
            user_photo_image.loadUserPhoto(avatar)
            username_text.text = feedPost.username
            post_image.loadImage(feedPost.image)
            if (likes.likesCount == 0) {
                likes_text.visibility = View.GONE
            } else {
                likes_text.text = "${likes.likesCount} likes"

            }
            caption_text.setCaptionText(feedPost.username, feedPost.caption)
            like_image.setOnClickListener{listener.toggleLike(feedPost.id!!)}
            like_image.setImageResource(if (likes.likedByAuth) R.drawable.ic_likes_active else R.drawable.ic_likes )
            listener.loadLikes(feedPost.id!!, position)
        }
    }

    private val defaultPostLikes: FeedPostLikes = FeedPostLikes(0, false)

    fun updatePostLikes(
        position: Int,
        feedPostLikes: FeedPostLikes
    ) {
        postLikesMap += (position to feedPostLikes)
        notifyItemChanged(position)
    }

    private fun TextView.setCaptionText(username: String, caption: String) {
        val usernameSpannable = SpannableString(username)
        usernameSpannable.setSpan(
            StyleSpan( Typeface.BOLD), 0, usernameSpannable.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        usernameSpannable.setSpan(object: ClickableSpan() {
            override fun onClick(widget: View) {
                widget.context.showToast("Username is clicked")
            }

            override fun updateDrawState(ds: TextPaint) {}
        },0, usernameSpannable.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        text = SpannableStringBuilder().append(usernameSpannable ).append(" ").append(caption)
        movementMethod = LinkMovementMethod.getInstance()
    }

    override fun getItemCount() = posts.size


}