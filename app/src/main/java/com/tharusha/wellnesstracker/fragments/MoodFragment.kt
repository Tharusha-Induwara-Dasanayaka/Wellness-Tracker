package com.tharusha.wellnesstracker.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.tharusha.wellnesstracker.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class MoodFragment : Fragment() {

    private lateinit var tvCurrentMood: TextView
    private lateinit var moodHistoryList: ListView
    private lateinit var tvHistoryLabel: TextView
    private val moodEntries = mutableListOf<MoodEntry>()
    private val sharedPrefKey = "mood_data"

    data class MoodEntry(
        val mood: String,
        val emoji: String,
        val dateTime: String
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_mood, container, false)

        // Find views
        tvCurrentMood = view.findViewById(R.id.tvCurrentMood)
        moodHistoryList = view.findViewById(R.id.moodHistoryList)
        tvHistoryLabel = view.findViewById(R.id.tvHistoryLabel)

        loadMoodHistory()
        setupEmojiButtons(view)
        setupMoodHistory()

        return view
    }

    private fun setupEmojiButtons(view: View) {
        // CHANGE: Use MaterialCardView instead of Button
        val emojiCards = listOf(
            view.findViewById<MaterialCardView>(R.id.btnHappy),
            view.findViewById<MaterialCardView>(R.id.btnCalm),
            view.findViewById<MaterialCardView>(R.id.btnNeutral),
            view.findViewById<MaterialCardView>(R.id.btnSad),
            view.findViewById<MaterialCardView>(R.id.btnStressed)
        )

        emojiCards.forEach { card ->
            card.setOnClickListener {
                val moodText = when (card.id) {
                    R.id.btnHappy -> "Happy"
                    R.id.btnCalm -> "Calm"
                    R.id.btnNeutral -> "Neutral"
                    R.id.btnSad -> "Sad"
                    R.id.btnStressed -> "Stressed"
                    else -> "Unknown"
                }

                val emoji = when (card.id) {
                    R.id.btnHappy -> "😊"
                    R.id.btnCalm -> "😌"
                    R.id.btnNeutral -> "😐"
                    R.id.btnSad -> "😔"
                    R.id.btnStressed -> "😫"
                    else -> "❓"
                }

                saveMoodEntry(moodText, emoji)
                updateCurrentMoodDisplay(moodText, emoji)
            }
        }
    }

    private fun saveMoodEntry(mood: String, emoji: String) {
        val dateTime = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())
        val newEntry = MoodEntry(mood, emoji, dateTime)

        moodEntries.add(0, newEntry)

        // Keep only last 10 entries
        if (moodEntries.size > 10) {
            moodEntries.removeLast()
        }

        saveMoodHistory()
        setupMoodHistory()
    }

    private fun updateCurrentMoodDisplay(mood: String, emoji: String) {
        tvCurrentMood.text = "Current Mood: $emoji $mood"
        Toast.makeText(requireContext(), "Mood recorded: $emoji $mood", Toast.LENGTH_SHORT).show()
    }

    private fun setupMoodHistory() {
        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            moodEntries.map { "${it.emoji} ${it.mood} - ${it.dateTime}" }
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.textSize = 16f
                textView.setPadding(20, 20, 20, 20)
                return view
            }
        }

        moodHistoryList.adapter = adapter
        tvHistoryLabel.visibility = if (moodEntries.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun saveMoodHistory() {
        val sharedPref = requireActivity().getSharedPreferences(sharedPrefKey, Context.MODE_PRIVATE)
        val json = Gson().toJson(moodEntries)
        sharedPref.edit().putString("mood_entries", json).apply()
    }

    private fun loadMoodHistory() {
        val sharedPref = requireActivity().getSharedPreferences(sharedPrefKey, Context.MODE_PRIVATE)
        val json = sharedPref.getString("mood_entries", null)

        moodEntries.clear()
        if (json != null) {
            val type = object : TypeToken<List<MoodEntry>>() {}.type
            val savedEntries: List<MoodEntry> = Gson().fromJson(json, type)
            moodEntries.addAll(savedEntries)
        }
    }
}