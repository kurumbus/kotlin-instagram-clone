package com.kurumbus.instagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bottom_navigation_view.setTextVisibility(false)
        bottom_navigation_view.setItemHorizontalTranslationEnabled(false) // for enableItemShiftingMode(false)
        bottom_navigation_view.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED)  // for enableShiftingMode(false) // for enableItemShiftingMode(false)
        bottom_navigation_view.setIconSize(29f, 29f)  // for enableShiftingMode(false) // for enableItemShiftingMode(false)
        bottom_navigation_view.enableAnimation(false)
        for (i in 0  until bottom_navigation_view.menu.size()) {
            bottom_navigation_view.setIconTintList(i,null)
        }

    }
}
