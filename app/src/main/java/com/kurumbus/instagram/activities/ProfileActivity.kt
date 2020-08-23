package com.kurumbus.instagram.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.util.MonthDisplayHelper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.ValueEventListener
import com.kurumbus.instagram.R
import com.kurumbus.instagram.models.User
import com.kurumbus.instagram.utils.FirebaseHelper
import com.kurumbus.instagram.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : BaseActivity(4) {
    private val TAG = "ProfileActivity"
    private lateinit var mFirebaseHelper: FirebaseHelper
    private lateinit var mUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupBottomNavigation()
        Log.d(TAG, "onCreate")

        mFirebaseHelper = FirebaseHelper(this)
        mFirebaseHelper.currentUserReference().addValueEventListener(ValueEventListenerAdapter {
            mUser = it.getValue(User::class.java)!!
            toolbar_title.text = mUser.username
            profile_image.loadUserPhoto(mUser.photo)

        })

        edit_profile_button.setOnClickListener{
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        images_recycler.layoutManager = GridLayoutManager(this, 3)
        mFirebaseHelper.mDatabase.child("images").child(mFirebaseHelper.mAuth.currentUser!!.uid).addValueEventListener(
            ValueEventListenerAdapter  {
                val images =it.children.map { it.getValue(String::class.java)!! }
                images_recycler.adapter = ImagesAdapter(images + images + images+ images)
            }
        )
    }
}

class ImagesAdapter(private val images: List<String>): RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {
    class ViewHolder(val image: ImageView) : RecyclerView.ViewHolder(image)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val image = LayoutInflater.from(parent.context).
            inflate(R.layout.image_item, parent, false) as ImageView
        return ViewHolder(image)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.loadImage(images[position])
    }

    private fun ImageView.loadImage(image: String) {
        Glide.with(this).load(image).into(this)
    }

    override fun getItemCount(): Int = images.size
}

class SquareImageView(context: Context, attributes: AttributeSet): ImageView(context, attributes) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}