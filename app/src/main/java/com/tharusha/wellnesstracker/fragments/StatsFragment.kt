package com.tharusha.wellnesstracker.fragments

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tharusha.wellnesstracker.R
import java.text.SimpleDateFormat
import java.util.*

class StatsFragment : Fragment() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var tvHabitsPercent: TextView
    private lateinit var tvMoodPercent: TextView
    private lateinit var tvWaterStats: TextView
    private lateinit var tvHabitsStats: TextView
    private lateinit var tvMoodStats: TextView
    private lateinit var tvWeeklyProgress: TextView
    private lateinit var tvWeeklyComparison: TextView
    private lateinit var waterProgressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_stats, container, false)

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_green_light,
            android.R.color.holo_blue_light,
            android.R.color.holo_orange_light
        )

        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        initializeViews(view)

        // ADDED: Share button functionality
        val btnShareProgress = view.findViewById<Button>(R.id.btnShareProgress)
        btnShareProgress.setOnClickListener {
            shareWeeklyProgress()
        }

        updateStatsWithRealData()

        return view
    }

    // ADDED: Sharing function
    private fun shareWeeklyProgress() {
        val shareText = """
            🌟 My Wellness Progress This Week 🌟
            
            📊 Weekly Stats:
            ✅ Habits Completion: 75%
            💧 Hydration: 1.8L / 2.0L Daily  
            😊 Most Common Mood: Happy
            📈 Overall Progress: Good!
            
            "Small daily improvements lead to stunning results" ✨
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(shareIntent, "Share Your Progress"))
    }

    private fun initializeViews(view: View) {
        // Initialize all TextViews and ProgressBar
        tvHabitsPercent = view.findViewById(R.id.tvHabitsPercent)
        tvMoodPercent = view.findViewById(R.id.tvMoodPercent)
        tvWaterStats = view.findViewById(R.id.tvWaterStats)
        tvHabitsStats = view.findViewById(R.id.tvHabitsStats)
        tvMoodStats = view.findViewById(R.id.tvMoodStats)
        tvWeeklyProgress = view.findViewById(R.id.tvWeeklyProgress)
        tvWeeklyComparison = view.findViewById(R.id.tvWeeklyComparison)
        waterProgressBar = view.findViewById(R.id.waterProgress)

        // Set current week range
        val tvStatsTitle = view.findViewById<TextView>(R.id.tvStatsTitle)
        val calendar = Calendar.getInstance()
        val weekFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val weekStart = weekFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val weekEnd = weekFormat.format(calendar.time)
        tvStatsTitle.text = "Weekly Stats ($weekStart - $weekEnd)"
    }

    private fun refreshData() {
        // Show refreshing animation
        swipeRefreshLayout.isRefreshing = true

        // Simulate data loading
        Handler(Looper.getMainLooper()).postDelayed({
            updateStatsWithRealData()
            swipeRefreshLayout.isRefreshing = false
            Toast.makeText(requireContext(), "Stats updated!", Toast.LENGTH_SHORT).show()
        }, 1500)
    }

    private fun updateStatsWithRealData() {
        // Sample data
        val habitsCompletion = 75
        val moodScore = 82
        val waterConsumed = 1800
        val waterGoal = 2000
        val waterPercentage = (waterConsumed.toDouble() / waterGoal * 100).toInt()

        // Weekly comparison data
        val habitsComparison = 12
        val moodComparison = 5
        val waterComparison = -8

        // Update text views
        tvWaterStats.text = "${waterConsumed/1000}L / ${waterGoal/1000}L Daily"
        tvHabitsStats.text = "• Morning Routine: 6/7 days\n• Exercise: 5/7 days\n• Meditation: 4/7 days"
        tvMoodStats.text = "• 😊 Happy: 3 days\n• 😌 Calm: 2 days\n• 😐 Neutral: 2 days"
        tvWeeklyProgress.text = "🎯 Overall Progress: 78%\n⭐ Best Day: Wednesday\n📈 Consistency: Good"

        // Weekly comparison text
        val comparisonText = """
            • Habits: ${if (habitsComparison > 0) "↑" else "↓"} ${Math.abs(habitsComparison)}% from last week
            • Mood: ${if (moodComparison > 0) "↑" else "↓"} ${Math.abs(moodComparison)}% from last week
            • Hydration: ${if (waterComparison > 0) "↑" else "↓"} ${Math.abs(waterComparison)}% from last week
        """.trimIndent()
        tvWeeklyComparison.text = comparisonText

        // Animate everything
        animateProgressBar(waterProgressBar, 0, waterPercentage, 1000)
        animateTextCount(tvHabitsPercent, 0, habitsCompletion, 1000)
        animateTextCount(tvMoodPercent, 0, moodScore, 1000)
    }

    private fun animateProgressBar(progressBar: ProgressBar, start: Int, end: Int, duration: Long) {
        val animator = ObjectAnimator.ofInt(progressBar, "progress", start, end)
        animator.duration = duration
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }

    private fun animateTextCount(textView: TextView, start: Int, end: Int, duration: Long) {
        val animator = ValueAnimator.ofInt(start, end)
        animator.duration = duration
        animator.addUpdateListener { valueAnimator ->
            textView.text = "${valueAnimator.animatedValue}%"
        }
        animator.start()
    }

    override fun onResume() {
        super.onResume()
        updateStatsWithRealData()
    }
}