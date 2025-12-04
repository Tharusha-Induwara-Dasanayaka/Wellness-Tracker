package com.tharusha.wellnesstracker.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.tharusha.wellnesstracker.R
import com.tharusha.wellnesstracker.workers.WaterReminderWorker
import java.util.concurrent.TimeUnit

class WaterFragment : Fragment() {

    private var waterIntake = 0
    private val dailyGoal = 2000
    private lateinit var sharedPreferences: android.content.SharedPreferences

    private lateinit var tvWaterStatus: TextView
    private lateinit var progressBar: android.widget.ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var switchReminder: SwitchCompat
    private lateinit var reminderInterval: Spinner
    private lateinit var btn250ml: MaterialCardView
    private lateinit var btn500ml: MaterialCardView
    private lateinit var btnReset: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_water, container, false)

        tvWaterStatus = view.findViewById(R.id.tvWaterStatus)
        progressBar = view.findViewById(R.id.progressBar)
        tvProgress = view.findViewById(R.id.tvProgress)
        switchReminder = view.findViewById(R.id.switchReminder)
        reminderInterval = view.findViewById(R.id.reminderInterval)
        btn250ml = view.findViewById(R.id.btn250ml)
        btn500ml = view.findViewById(R.id.btn500ml)
        btnReset = view.findViewById(R.id.btnReset)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("water_prefs", Context.MODE_PRIVATE)
        waterIntake = sharedPreferences.getInt("water_intake", 0)

        // Setup reminder interval spinner with string resources
        val intervals = arrayOf(
            getString(R.string.minutes_15),
            getString(R.string.minutes_30),
            getString(R.string.hour_1),
            getString(R.string.hours_2)
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, intervals)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reminderInterval.adapter = adapter

        // Load reminder settings
        val reminderEnabled = sharedPreferences.getBoolean("reminder_enabled", false)
        val savedInterval = sharedPreferences.getInt("reminder_interval", 2)
        switchReminder.isChecked = reminderEnabled
        reminderInterval.setSelection(savedInterval)

        updateWaterDisplay()

        // Button click listeners
        btn250ml.setOnClickListener {
            waterIntake += 250
            updateWaterDisplay()
        }

        btn500ml.setOnClickListener {
            waterIntake += 500
            updateWaterDisplay()
        }

        btnReset.setOnClickListener {
            waterIntake = 0
            updateWaterDisplay()
        }

        // Reminder switch
        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("reminder_enabled", isChecked).apply()
            if (isChecked) {
                setupWaterReminder(reminderInterval.selectedItemPosition)
                Toast.makeText(requireContext(), R.string.reminders_enabled, Toast.LENGTH_SHORT).show()
            } else {
                cancelWaterReminder()
                Toast.makeText(requireContext(), R.string.reminders_disabled, Toast.LENGTH_SHORT).show()
            }
        }

        // Reminder interval
        reminderInterval.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sharedPreferences.edit().putInt("reminder_interval", position).apply()
                if (switchReminder.isChecked) {
                    setupWaterReminder(position)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateWaterDisplay() {
        val percentage = (waterIntake.toFloat() / dailyGoal * 100).toInt()
        progressBar.progress = percentage

        // Fixed: Using string resources with placeholders
        tvProgress.text = getString(R.string.percentage_completed, percentage)
        tvWaterStatus.text = getString(R.string.water_intake_format, waterIntake, dailyGoal)

        // Update progress text color
        if (percentage >= 100) {
            tvProgress.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
        } else {
            tvProgress.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
        }

        saveWaterData()
    }

    private fun setupWaterReminder(intervalPosition: Int) {
        val intervalMinutes = when (intervalPosition) {
            0 -> 15L
            1 -> 30L
            2 -> 60L
            3 -> 120L
            else -> 60L
        }

        val reminderRequest = PeriodicWorkRequest.Builder(
            WaterReminderWorker::class.java,
            intervalMinutes,
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(requireContext()).enqueue(reminderRequest)
    }

    private fun cancelWaterReminder() {
        WorkManager.getInstance(requireContext()).cancelAllWork()
    }

    private fun saveWaterData() {
        sharedPreferences.edit().putInt("water_intake", waterIntake).apply()
    }

    override fun onPause() {
        super.onPause()
        saveWaterData()
    }
}