package com.kurumbus.instagram.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kurumbus.instagram.R
import com.kurumbus.instagram.models.User
import com.kurumbus.instagram.utils.FirebaseHelper
import com.kurumbus.instagram.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.activity_add_friends.*
import kotlinx.android.synthetic.main.activity_profile_settings.*
import kotlinx.android.synthetic.main.activity_profile_settings.back_image
import kotlinx.android.synthetic.main.add_friend_item.view.*

class AddFriendsActivity: AppCompatActivity(), FriendsAdapter.Listener {

    private val TAG = "AddFriendsActivity"
    private lateinit var mFirebase: FirebaseHelper
    private lateinit var mUser: User
    private lateinit var mUsers: List<User>
    private lateinit var mAdapter: FriendsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friends)
        Log.d(TAG, "onCreate")

        mFirebase = FirebaseHelper(this)
        mAdapter = FriendsAdapter( this)
        val uid =mFirebase.mAuth.currentUser!!.uid

        add_friends_recycler.adapter = mAdapter
        add_friends_recycler.layoutManager = LinearLayoutManager(this)

        mFirebase.mDatabase.child("users").addValueEventListener(ValueEventListenerAdapter {
            val allUsers = it.children.map{it.getValue(User::class.java)!!.copy(uid = it.key)}
            val (userList, otherUsersList) = allUsers.partition { it.uid == uid}
            mUser = userList.first()
            mUsers = otherUsersList
            mAdapter.update(mUsers, mUser.follows)
        })

        back_image.setOnClickListener{
            finish()
        }
    }

    override fun follow(uid: String) {
        setFollow(uid, true) {
            mAdapter.followed(uid)
        }
    }

    override fun unfollow(uid: String) {
        setFollow(uid, false) {
            mAdapter.unfollowed(uid)
        }
    }

    private fun setFollow(uid: String, follow: Boolean, onSuccess: () -> Unit ) {
        val usersNode = mFirebase.mDatabase.child("users")
        val followTask = usersNode.child(mUser.uid!!).child("follows").child(uid)
        val followerTask = usersNode.child(uid).child("followers").child(mUser.uid!!)

        val setFollow = if (follow) followTask.setValue(true) else followTask.removeValue()
        val setFollower = if (follow)  followerTask.setValue(true) else followerTask.removeValue()


        setFollow.continueWithTask { setFollower }.addOnCompleteListener{
            if (it.isSuccessful) {
                onSuccess()
            } else {
                showToast(it.exception!!.message!!)
            }
        }
    }

}

class FriendsAdapter(private val listener: Listener) : RecyclerView.Adapter<FriendsAdapter.ViewHolder> () {

    private var mFollows = mapOf<String, Boolean>()
    private var mPositions = mapOf<String, Int>()
    private var mUsers = listOf<User>()

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    interface Listener {
        fun follow(uid: String)
        fun unfollow(uid: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =LayoutInflater.from(parent.context).inflate(R.layout.add_friend_item, parent, false)
        return  ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            val user = mUsers[position]
            view.photo_image.loadImage(user.photo)
            view.name_text.text = user.name
            view.name_text.text = user.username

            view.follow_button.setOnClickListener{listener.follow(user.uid!!)}
            view.unfollow_button.setOnClickListener{listener.unfollow(user.uid!!) }

            if (mFollows[user.uid] ?: false) {
                view.follow_button.visibility = View.GONE
                view.unfollow_button.visibility = View.VISIBLE
            } else {
                view.follow_button.visibility = View.VISIBLE
                view.unfollow_button.visibility = View.GONE
            }
        }
    }


    fun update(users: List<User>, follows: Map<String, Boolean>) {
        mUsers = users
        mPositions = users.withIndex().map { (index, user)  -> user.uid!! to index}.toMap()
        mFollows = follows
        notifyDataSetChanged()
    }

    override fun getItemCount() = mUsers.size

    fun followed(uid: String) {
        mFollows += (uid to true)
        notifyItemChanged(mPositions[uid]!!)
    }

    fun unfollowed(uid: String) {
        mFollows -= uid
        notifyItemChanged(mPositions[uid]!!)
    }
}