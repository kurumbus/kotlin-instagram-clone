package com.kurumbus.instagram.activities

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.kurumbus.instagram.R
import kotlinx.android.synthetic.main.bottom_navigation_view.*

abstract class BaseActivity (val navNumber: Int) : AppCompatActivity(){

    private val TAG = "BaseActivity"

    fun setupBottomNavigation() {
        bottom_navigation_view.setItemHorizontalTranslationEnabled(false) // for enableItemShiftingMode(false)
        bottom_navigation_view.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED)  // for enableShiftingMode(false) // for enableItemShiftingMode(false)
        bottom_navigation_view.setIconSize(29f, 29f)  // for enableShiftingMode(false) // for enableItemShiftingMode(false)
        bottom_navigation_view.enableAnimation(false)
        for (i in 0  until bottom_navigation_view.menu.size()) {
            bottom_navigation_view.setIconTintList(i,null)
        }

        bottom_navigation_view.setOnNavigationItemSelectedListener {
            val nextActivity = when (it.itemId) {
                R.id.nav_item_home -> HomeActivity::class.java
                R.id.nav_item_search -> SearchActivity::class.java
                R.id.nav_item_share -> ShareActivity::class.java
                R.id.nav_item_likes -> LikesActivity::class.java
                R.id.nav_item_profile -> ProfileActivity::class.java
                else -> {
                    Log.e(TAG, "Unknown item clicked $it")
                    null
                }
            }
            if (nextActivity != null) {
                val intent = Intent (this, nextActivity)
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(intent)
                overridePendingTransition(0,0)
                true
            } else {
                false
            }
        }
        bottom_navigation_view.menu.getItem(navNumber).isChecked = true

    }
 }