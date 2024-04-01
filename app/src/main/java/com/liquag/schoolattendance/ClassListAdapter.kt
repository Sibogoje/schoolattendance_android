package com.liquag.schoolattendance

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject


class ClassListAdapter(
    private val activity: Activity,
    private val classList: List<Class>,
    private val prefName: String,
    private val keyFUserId: String
) : RecyclerView.Adapter<ClassListAdapter.ViewHolder>() {
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
            val attendButton = itemView.findViewById<Button>(R.id.attend)
            itemView.findViewById<TextView>(R.id.class_name_text_view).text = classItem.name

            val view_class = itemView.findViewById<Button>(R.id.view_class)

            // Update the "Attend" button text based on the attend value

            if (classItem.attend == 1) {
                attendButton.text = "Stop Attendance"
                attendButton.background.setTint(itemView.resources.getColor(R.color.red))
                attendButton.setTextColor(itemView.resources.getColor(R.color.white))
            } else {
                attendButton.text = "Take Attendance"
                attendButton.background.setTint(itemView.resources.getColor(R.color.purple_200))
                attendButton.setTextColor(itemView.resources.getColor(R.color.white))
            }

            // Set up click listeners or any other logic for the item view

            // Add click listener to the "Attend" button
            attendButton.setOnClickListener {
                updateAttendance(classItem.id, classItem.attend)
            }

            view_class.setOnClickListener {
                val intent = Intent(activity, ClassActivity::class.java)
                intent.putExtra("classId", classItem.id)
                intent.putExtra("className", classItem.name)
                activity.startActivity(intent)

            }
        }




        private fun updateAttendance(classId: Int, currentAttend: Int) {
            val newAttend = if (currentAttend == 1) 0 else 1
            val staffId = activity.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                .getString(keyFUserId, null)

            val url = "https://liquag.com/dev/school/admin/mobile/faculty_attendance.php?classId=$classId&attend=$newAttend"
            val requestQueue = Volley.newRequestQueue(activity)

            val params = HashMap<String, String>()
            params["classId"] = classId.toString()
            params["attend"] = newAttend.toString()

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST, url, JSONObject(params as Map<*, *>),
                { response ->
                    // Handle response
                    Log.d("Volley Response", response.toString())
                    Log.d("classId", classId.toString())
                    Log.d( "newAttend", newAttend.toString())
                    Log.d("currentAttend", currentAttend.toString())
                    // update button text and color
                    val attendButton = itemView.findViewById<Button>(R.id.attend)
                    if (newAttend == 1) {

                        val classItem = classList[adapterPosition]
                        classItem.attend = newAttend
                        notifyItemChanged(adapterPosition)

                    } else {

                    val classItem = classList[adapterPosition]
                    classItem.attend = newAttend
                    notifyItemChanged(adapterPosition)

                    }
                },
                { error ->
                    // Handle error
                    // Handle error
                    val errorString = String(error.networkResponse.data, Charsets.UTF_8)
                    Log.e("Volley Error", errorString)
                }
            )

            requestQueue.add(jsonObjectRequest)
        }





        }
    }


//     }
