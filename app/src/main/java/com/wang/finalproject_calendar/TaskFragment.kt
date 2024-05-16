package com.wang.finalproject_calendar

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.wang.finalproject_calendar.placeholder.PlaceholderContent
class TaskFragment : Fragment() {

    private var columnCount = 1

    private lateinit var addTask: Button
    private lateinit var updateTask: Button

    private lateinit var darkModeButton: Button

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyTaskRecyclerViewAdapter

    private var isDarkModeEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        isDarkModeEnabled = sharedPreferences.getBoolean("darkMode", false)

        // Apply the theme based on the current dark mode state
        val themeId = if (isDarkModeEnabled) R.style.AppTheme_Dark else R.style.AppTheme
        requireActivity().setTheme(themeId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize and set click listener for the dark mode button
        darkModeButton = view.findViewById(R.id.darkModeButton)
        darkModeButton.setOnClickListener {
            toggleDarkMode()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        // Apply the theme based on the current dark mode state
        val themeId = if (isDarkModeEnabled) R.style.AppTheme_Dark else R.style.AppTheme
        requireActivity().setTheme(themeId)

        val view = inflater.inflate(R.layout.fragment_task_list, container, false)
//
//        val rootLayout = view.findViewById<View>(R.id.rootLayout)
//        rootLayout.setBackgroundColor(
//            ContextCompat.getColor(requireContext(), if (isDarkModeEnabled) R.color.black else R.color.white)
//        )

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewList)
        adapter = MyTaskRecyclerViewAdapter(readEventsFromDatabase())
        recyclerView.layoutManager = when {
            columnCount <= 1 -> LinearLayoutManager(context)
            else -> GridLayoutManager(context, columnCount)
        }
        recyclerView.adapter = adapter

        addTask = view.findViewById(R.id.addEventButton)
        updateTask = view.findViewById(R.id.updateEventButton)

        addTask.setOnClickListener {
            navigateToHomeFragment()
        }

        updateTask.setOnClickListener {
            navigateToRegistrationFragment()
        }

        return view
    }

    private fun toggleDarkMode() {
        isDarkModeEnabled = !isDarkModeEnabled

        // Save the current dark mode state
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("darkMode", isDarkModeEnabled).apply()

        // Recreate the activity to apply the new theme
        requireActivity().recreate()

        // Log the current mode after toggling
        val mode = if (isDarkModeEnabled) "Dark Mode" else "Light Mode"
        Log.d("ModeChange", "Mode changed to: $mode")
    }

    private fun navigateToHomeFragment() {
        findNavController().navigate(R.id.action_taskFragment_to_homeFragment) //to add fragment
    }

    private fun navigateToRegistrationFragment() {
        findNavController().navigate(R.id.action_taskFragment_to_registrationFragment) //to update fragment
    }

    private fun readEventsFromDatabase(): ArrayList<Task> {
        val dbHelper = DatabaseHelper(requireContext())
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM tasks", null)
        val data = ArrayList<Task>()
        with(cursor) {
            while (moveToNext()) {
                val id = getString(getColumnIndexOrThrow("id"))
                val title = getString(getColumnIndexOrThrow("title"))
                val details = getString(getColumnIndexOrThrow("details"))
                val date = getString(getColumnIndexOrThrow("date"))
                val time = getString(getColumnIndexOrThrow("time"))
                val color = getString(getColumnIndexOrThrow("color"))
                val task = Task(id, title, details, date, time, color)
                data.add(task)
            }
        }
        cursor.close()
        return data
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            TaskFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}