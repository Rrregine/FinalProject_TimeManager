package com.wang.finalproject_calendar

import android.Manifest
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var context: Context

    private lateinit var dbHelper: DatabaseHelper

    private lateinit var datePickerButton: Button
    private lateinit var selectTimeButton: Button
    private lateinit var datePickerTextView: TextView
    private lateinit var selectTimeTextView: TextView
    private lateinit var saveEventButton: Button
    private lateinit var editTitleText: EditText
    private lateinit var editDetailsText: EditText

    private lateinit var addColor: Spinner
    private lateinit var backToTaskList: Button

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        createNotificationChannel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        datePickerButton = view.findViewById(R.id.selectDateButton)
        selectTimeButton = view.findViewById(R.id.selectTimeButton)
        datePickerTextView = view.findViewById(R.id.selectDateTextView)
        selectTimeTextView = view.findViewById(R.id.selectTimeTextView)
        saveEventButton = view.findViewById(R.id.saveTaskButton)
        editTitleText = view.findViewById(R.id.editTitleText)
        editDetailsText = view.findViewById(R.id.editDetailsText)

        val myCalendar = Calendar.getInstance()
        val date = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.CANADA)
            datePickerTextView.text = sdf.format(myCalendar.time)
        }

        datePickerButton.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        selectTimeButton.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val hour = currentTime.get(Calendar.HOUR_OF_DAY)
            val minute = currentTime.get(Calendar.MINUTE)
            TimePickerDialog(
                requireContext(),
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    val timeFormat = if (selectedHour < 12) "AM" else "PM"
                    val displayHour = if (selectedHour > 12) selectedHour - 12 else selectedHour
                    selectTimeTextView.text = String.format(
                        Locale.getDefault(),
                        "%02d:%02d %s",
                        displayHour,
                        selectedMinute,
                        timeFormat
                    )
                },
                hour,
                minute,
                false
            ).show()
        }

        // Initialize color spinner
        val colors = arrayOf("#FFD8BFFD", "#FFF6C6CB", "#F3E7C5", "#C5F4BB",
            "#C9DFEF", "#C677F8", "#F896A1", "#F8DC8E", "#9BFF86", "#87C1EC")
        addColor = view.findViewById(R.id.selectColor)
        var selectedColor = ""
        if (addColor != null) {
            val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, colors)
            addColor.adapter = arrayAdapter

            addColor.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {
                    selectedColor = colors[position]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    Toast.makeText(requireContext(), "Please select a color", Toast.LENGTH_SHORT).show()
                }
            }
        }

        saveEventButton.setOnClickListener {
            onClickSubmit(editTitleText, editDetailsText, datePickerTextView, selectTimeTextView, selectedColor)
        }

        backToTaskList = view.findViewById(R.id.backToTaskList)

        backToTaskList.setOnClickListener {
            navigateToTaskListFragment()
        }

        dbHelper = DatabaseHelper(requireContext())

        return view
    }


    private fun onClickSubmit(
        titleEditText: EditText,
        detailsEditText: EditText,
        dateTextView: TextView,
        timeTextView: TextView,
        color: String
    ) {
        // Get user input
        val title = this.editTitleText.text.toString()
        val details = this.editDetailsText.text.toString()
        val date = this.datePickerTextView.text.toString()
        val time = this.selectTimeTextView.text.toString()

        // Insert data into database
        dbHelper.addPerson(title, details, date, time, color)

        // Clear input fields
        this.editTitleText.text.clear()
        this.editDetailsText.text.clear()
        this.datePickerTextView.text = ""
        this.selectTimeTextView.text = ""

        // Show notification when task is added
        showNotification(title)
    }

    private fun showNotification(taskTitle: String) {
        // Create an intent to open the TaskFragment when the notification is clicked
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        // Create the notification
        val notificationBuilder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("New Task Added")
            .setContentText("Task: $taskTitle")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show the notification
        with(NotificationManagerCompat.from(requireContext())) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECEIVE_BOOT_COMPLETED
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is granted, proceed with creating and sending the notification
                with(NotificationManagerCompat.from(requireContext())) {
                    notify(NOTIFICATION_ID, notificationBuilder.build())
                }
            } else {
                // Permission is not granted, handle the situation (e.g., request permission from the user)
                // You can request the notification permission here using ActivityCompat.requestPermissions
            }
            notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

        }
    }

    private fun navigateToTaskListFragment() {
        findNavController().navigate(R.id.action_homeFragment_to_taskFragment)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val CHANNEL_ID = "com.wang.finalproject_calendar.notification_channel"
        const val NOTIFICATION_ID = 1
    }
}