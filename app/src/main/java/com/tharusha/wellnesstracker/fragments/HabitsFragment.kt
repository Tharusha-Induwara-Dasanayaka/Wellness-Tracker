package com.tharusha.wellnesstracker.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.tharusha.wellnesstracker.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HabitsFragment : Fragment() {

    private lateinit var habitsContainer: LinearLayout
    private lateinit var tvCompletedCount: TextView
    private lateinit var tvTotalCount: TextView
    private lateinit var fabAddHabit: FloatingActionButton
    private val habitsList = mutableListOf<Habit>()
    private val sharedPrefKey = "habits_data"

    data class Habit(
        val id: Int,
        var name: String,
        var completed: Boolean = false
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_habits, container, false)

        // Find views from the NEW modern layout
        habitsContainer = view.findViewById(R.id.habitsContainer)
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount)
        tvTotalCount = view.findViewById(R.id.tvTotalCount)
        fabAddHabit = view.findViewById(R.id.btnTest) // This is now a FAB

        // Remove old hidden buttons
        view.findViewById<Button>(R.id.btnExercise).visibility = View.GONE
        view.findViewById<Button>(R.id.btnMeditation).visibility = View.GONE
        view.findViewById<Button>(R.id.btnReading).visibility = View.GONE

        loadHabits()
        refreshHabitsDisplay()

        fabAddHabit.setOnClickListener {
            showAddHabitDialog()
        }

        return view
    }

    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)
        val etHabitName = dialogView.findViewById<EditText>(R.id.etHabitName)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Habit")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val habitName = etHabitName.text.toString().trim()
                if (habitName.isNotEmpty()) {
                    addHabit(habitName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addHabit(name: String) {
        val newId = if (habitsList.isEmpty()) 1 else habitsList.maxOf { it.id } + 1
        habitsList.add(Habit(newId, name))
        saveHabits()
        refreshHabitsDisplay()
    }

    private fun editHabit(habit: Habit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)
        val etHabitName = dialogView.findViewById<EditText>(R.id.etHabitName)
        etHabitName.setText(habit.name)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = etHabitName.text.toString().trim()
                if (newName.isNotEmpty()) {
                    habit.name = newName
                    saveHabits()
                    refreshHabitsDisplay()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteHabit(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                habitsList.remove(habit)
                saveHabits()
                refreshHabitsDisplay()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun refreshHabitsDisplay() {
        habitsContainer.removeAllViews()

        if (habitsList.isEmpty()) {
            // Show empty state
            val emptyView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_empty_habits, habitsContainer, false)
            habitsContainer.addView(emptyView)
        } else {
            habitsList.forEach { habit ->
                val habitView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_habit, habitsContainer, false)

                val tvHabitName = habitView.findViewById<TextView>(R.id.tvHabitName)
                val cbCompleted = habitView.findViewById<CheckBox>(R.id.cbCompleted)
                val btnEdit = habitView.findViewById<Button>(R.id.btnEdit)
                val btnDelete = habitView.findViewById<Button>(R.id.btnDelete)

                tvHabitName.text = habit.name
                cbCompleted.isChecked = habit.completed

                // FIXED: Use ContextCompat instead of requireContext().getColor()
                if (habit.completed) {
                    tvHabitName.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                    tvHabitName.paintFlags = tvHabitName.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    tvHabitName.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                    tvHabitName.paintFlags = tvHabitName.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }

                cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                    habit.completed = isChecked
                    saveHabits()
                    refreshHabitsDisplay() // Refresh to update styles
                }

                btnEdit.setOnClickListener {
                    editHabit(habit)
                }

                btnDelete.setOnClickListener {
                    deleteHabit(habit)
                }

                habitsContainer.addView(habitView)
            }
        }

        updateProgress()
    }

    private fun updateProgress() {
        val completedCount = habitsList.count { it.completed }
        val total = habitsList.size

        tvCompletedCount.text = completedCount.toString()
        tvTotalCount.text = total.toString()
    }

    private fun saveHabits() {
        val sharedPref = requireActivity().getSharedPreferences(sharedPrefKey, Context.MODE_PRIVATE)
        val json = Gson().toJson(habitsList)
        // FIXED: Use KTX extension function for SharedPreferences
        sharedPref.edit().apply {
            putString("habits", json)
            apply()
        }
    }

    private fun loadHabits() {
        val sharedPref = requireActivity().getSharedPreferences(sharedPrefKey, Context.MODE_PRIVATE)
        val json = sharedPref.getString("habits", null)

        habitsList.clear()
        if (json != null) {
            val type = object : TypeToken<List<Habit>>() {}.type
            val savedHabits: List<Habit> = Gson().fromJson(json, type)
            habitsList.addAll(savedHabits)
        } else {
            // Add default habits
            habitsList.addAll(listOf(
                Habit(1, "Morning Exercise"),
                Habit(2, "Meditation"),
                Habit(3, "Read a Book"),
                Habit(4, "Drink Water")
            ))
            saveHabits()
        }
    }
}