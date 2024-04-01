package com.liquag.schoolattendance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClassAdapter(private val classList: List<Class>, private val onAttendClickListener: OnAttendClickListener) : RecyclerView.Adapter<ClassAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.faculty_classes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val classItem = classList[position]
        holder.bind(classItem)
    }

    override fun getItemCount(): Int = classList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(classItem: Class) {
            // Bind the class data to the item view
            itemView.findViewById<TextView>(R.id.recycler_view_classes).text = classItem.name

            // Update the "Attend" button text based on the attend value
            val attendButton = itemView.findViewById<Button>(R.id.attend)
            if (classItem.attend == 1) {
                attendButton.text = "Stop Attendance"
            } else {
                attendButton.text = "Take Attendance"
            }

            // Set up click listeners for the button
            attendButton.setOnClickListener {
                // Call the callback function defined in the interface
                onAttendClickListener.onAttendClick(classItem)
            }
        }
    }

    // Interface to handle button click events
    interface OnAttendClickListener {
        fun onAttendClick(classItem: Class)
    }
}
