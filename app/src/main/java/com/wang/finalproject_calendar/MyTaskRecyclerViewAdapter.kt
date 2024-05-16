package com.wang.finalproject_calendar

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.content.Context
import android.widget.CheckBox

import com.wang.finalproject_calendar.placeholder.PlaceholderContent.PlaceholderItem
import com.wang.finalproject_calendar.databinding.FragmentTaskBinding


class MyTaskRecyclerViewAdapter(
    private var values: List<Task>
) : RecyclerView.Adapter<MyTaskRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FragmentTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, parent.context)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = values[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(
        private val binding: FragmentTaskBinding,
        private val context: Context
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Task) {
            binding.apply {
                idTextView.text = item.taskId
                taskTitle.text = item.taskTitle
                taskDetails.text = item.taskDetails
                taskDate.text = item.taskDate
                taskTime.text = item.taskTime
                taskCheckBox.isChecked = false
                singleTaskView.setBackgroundColor(android.graphics.Color.parseColor(item.taskColor))
                taskCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val removedTask = values[position]
                            deleteTaskFromDatabase(removedTask)
                            values = values.toMutableList().apply { removeAt(position) } // Update values list
                            notifyItemRemoved(position)
                        }
                    }
                }
            }
        }

        private fun deleteTaskFromDatabase(itemToDelete: Task) {
            val dbHelper = DatabaseHelper(context)
            val db = dbHelper.writableDatabase
            val selection = "title LIKE ?"
            val itemName = itemToDelete.taskTitle
            val selectionArgs = arrayOf("%$itemName%")
            db.delete("tasks", selection, selectionArgs)
        }

    }

}