package com.tharusha.wellnesstracker.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tharusha.wellnesstracker.models.Habit
import com.tharusha.wellnesstracker.models.MoodEntry

class PrefsManager(private val context: Context) {
    private val sharedPrefs = context.getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val TAG = "PrefsManager"
    }

    // Habit methods
    fun saveHabits(habits: List<Habit>) {
        try {
            val json = gson.toJson(habits)
            sharedPrefs.edit().putString("habits", json).apply()
            Log.d(TAG, "Saved ${habits.size} habits")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving habits", e)
        }
    }

    fun getHabits(): List<Habit> {
        return try {
            val json = sharedPrefs.getString("habits", null)
            if (json != null) {
                val type = object : TypeToken<List<Habit>>() {}.type
                gson.fromJson<List<Habit>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading habits", e)
            emptyList()
        }
    }

    // Mood methods
    fun saveMoodEntries(entries: List<MoodEntry>) {
        try {
            val json = gson.toJson(entries)
            sharedPrefs.edit().putString("mood_entries", json).apply()
            Log.d(TAG, "Saved ${entries.size} mood entries")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving mood entries", e)
        }
    }

    fun getMoodEntries(): List<MoodEntry> {
        return try {
            val json = sharedPrefs.getString("mood_entries", null)
            if (json != null) {
                val type = object : TypeToken<List<MoodEntry>>() {}.type
                gson.fromJson<List<MoodEntry>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading mood entries", e)
            emptyList()
        }
    }

    // Settings methods
    fun setWaterReminderEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("water_reminder", enabled).apply()
    }

    fun isWaterReminderEnabled(): Boolean {
        return sharedPrefs.getBoolean("water_reminder", false)
    }

    fun setReminderInterval(minutes: Int) {
        sharedPrefs.edit().putInt("reminder_interval", minutes).apply()
    }

    fun getReminderInterval(): Int {
        return sharedPrefs.getInt("reminder_interval", 60)
    }
}