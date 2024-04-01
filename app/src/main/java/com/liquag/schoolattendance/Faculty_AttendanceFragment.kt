import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.liquag.schoolattendance.ClassListAdapter
import com.liquag.schoolattendance.R
import org.json.JSONException

class Faculty_AttendanceFragment : Fragment() {
    private lateinit var classListAdapter: ClassListAdapter
    private lateinit var classList: MutableList<com.liquag.schoolattendance.Class>

    private var fId: String? = null
    private var fname: String? = null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.faculty_attendance, container, false)
        setupToolbar(view)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_classes)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        classList = mutableListOf()
        classListAdapter = ClassListAdapter(requireActivity(), classList, PREF_NAME, KEY_F_USERID)
        recyclerView.adapter = classListAdapter

        // Implement the interface method to handle button clicks
        fun onAttendClick(classItem: com.liquag.schoolattendance.Class) {
            // Update the attend value
            classItem.attend = if (classItem.attend == 1) 0 else 1
            classListAdapter.notifyDataSetChanged()
        }




        // Retrieve staff ID (f_id) from shared preferences
        val sharedPref = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        fId = sharedPref.getString(KEY_F_USERID, null)
        fname = sharedPref.getString(KEY_F_NAME, null)

        // Set the toolbar title with the retrieved fId
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "$fname's Classes"

        fetchData()



        return view
    }

    companion object {
        private const val PREF_NAME = "faculty_id"
        private const val KEY_F_USERID = "f_id"
        private const val KEY_F_NAME = "f_name"
    }

    // Setup toolbar
    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = fname
    }

    // Setup RecyclerView
    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_classes)
        recyclerView.layoutManager = LinearLayoutManager(context)
        classList = mutableListOf()
        classListAdapter = ClassListAdapter(requireActivity(), classList, PREF_NAME, KEY_F_USERID)
        recyclerView.adapter = classListAdapter
    }





    // Fetch data from MySQL database using Volley
    @SuppressLint("NotifyDataSetChanged")
    private fun fetchData() {
        val url = "https://liquag.com/dev/school/admin/mobile/fetch_classes.php?id=$fId " // Replace with your PHP script URL
        val requestQueue = Volley.newRequestQueue(requireContext())

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    for (i in 0 until response.length()) {
                        val jsonObject = response.getJSONObject(i)
                        val id = jsonObject.getInt("id")
                        val className = jsonObject.getString("name")
                        val attend = jsonObject.getInt("attend")
                        // Add other properties as needed
                        val classItem = com.liquag.schoolattendance.Class(id, className, attend)
                        classList.add(classItem)

                    }
                    classListAdapter.notifyDataSetChanged()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
                // Handle error
            }
        )

        requestQueue.add(jsonArrayRequest)
    }






}
