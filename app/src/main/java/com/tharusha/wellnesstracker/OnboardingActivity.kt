package com.tharusha.wellnesstracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.tharusha.wellnesstracker.fragments.OnboardingAdapter

class OnboardingActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var dotIndicator: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        dotIndicator = findViewById(R.id.dotIndicator)

        val adapter = OnboardingAdapter(this)
        viewPager.adapter = adapter

        setupDotIndicators()
    }

    private fun setupDotIndicators() {
        // Clear any existing dots
        dotIndicator.removeAllViews()

        val dots = arrayOfNulls<ImageView>(2)
        for (i in dots.indices) {
            dots[i] = ImageView(this)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(8, 0, 8, 0)
            dots[i]?.layoutParams = layoutParams
            dots[i]?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_inactive))
            dotIndicator.addView(dots[i])
        }

        // Set first dot as active
        (dotIndicator.getChildAt(0) as ImageView).setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.dot_active)
        )

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                for (i in 0 until dotIndicator.childCount) {
                    val imageView = dotIndicator.getChildAt(i) as ImageView
                    if (i == position) {
                        imageView.setImageDrawable(ContextCompat.getDrawable(this@OnboardingActivity, R.drawable.dot_active))
                    } else {
                        imageView.setImageDrawable(ContextCompat.getDrawable(this@OnboardingActivity, R.drawable.dot_inactive))
                    }
                }
            }
        })
    }

    fun onNextClicked(view: View) {
        if (viewPager.currentItem < 1) {
            viewPager.currentItem += 1
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    fun onSkipClicked(view: View) {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}