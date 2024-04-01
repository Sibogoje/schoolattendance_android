package com.liquag.schoolattendance

import LocationHelper
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.Executor
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import android.Manifest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.android.volley.RequestQueue


class LoginActivity : AppCompatActivity() {



    private lateinit var fingerprintView: View
    private lateinit var studentLoginView: View
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var requestQueue: RequestQueue

    companion object {
        private const val PREF_NAME = "faculty_id"
        private const val KEY_F_USERID = "f_id"
        private const val KEY_F_NAME = "f_name"
        private const val KEY_S_USERID = "s_id"
        private const val KEY_S_NAME = "s_name"
        private const val KEY_S_CLASS = "s_class"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
    private lateinit var sharedPref: SharedPreferences

    var classId = ""
    var s_id = ""

    var currlong = 0.0
    var currla = 0.0


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationHelper: LocationHelper
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        locationHelper = LocationHelper(this)
        requestQueue = Volley.newRequestQueue(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        }

        // Initialize location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    //handleLocationUpdate(location)
                }
            }
        }

        val etUsername: EditText = findViewById(R.id.rollnum_txt)
        val etPassword: EditText = findViewById(R.id.password_txt)
        val check_faculty: CheckBox = findViewById(R.id.check_faculty)

        val rollnumberTxt: TextView? = findViewById(R.id.rollnumbertxt)



        sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val isStudentLoggedIn = sharedPref.getString(KEY_S_USERID, null) != null

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Handle error
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Handle failure
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Check if s_id and classId are not empty

                    s_id = sharedPref.getString(KEY_S_USERID, null).toString()
                    val rootView = findViewById<View>(android.R.id.content)

                    if (!s_id.isNullOrEmpty() && !classId.isNullOrEmpty()) {
                        // Proceed to update attendance status
                        // Obtain the user's current location
                         updateAttendanceStatus(s_id, classId)
                    } else {
                        // Handle case where s_id or classId is empty
                        Log.e("BiometricLogin", "s_id or classId is empty")
                        // show snackbar

                        Snackbar.make(rootView, "No Student or Class to Mark Attendance for!", Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
            }
        )
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your biometric credential")
            // Adjust the builder based on the Android version
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                } else {
                    setNegativeButtonText("Use account password")
                }
            }
            .build()



        val btnFingerprint: Button = findViewById(R.id.fingerptintbtn)
        btnFingerprint.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }


        fingerprintView = findViewById(R.id.fingerprintLoginView)
        studentLoginView = findViewById(R.id.studentLoginView)


        if (isStudentLoggedIn) {
            studentLoginView.visibility = View.GONE
            fingerprintView.visibility = View.VISIBLE
            // set rollnumberxt to KEY_S_USERID
            rollnumberTxt?.text = sharedPref.getString(KEY_S_USERID, null)

            setClass()

        } else {
            studentLoginView.visibility = View.VISIBLE
            fingerprintView.visibility = View.GONE
        }

        val btnProfile: Button = findViewById(R.id.btn_profile)
        btnProfile.setOnClickListener {
            if (studentLoginView.visibility == View.GONE) {
                flipViews(fingerprintView, studentLoginView)
            }
        }

        val btnToAttend: Button = findViewById(R.id.btn_login)
        btnToAttend.setOnClickListener {
            if (fingerprintView.visibility == View.GONE) {
                flipViews(studentLoginView, fingerprintView)
            }
        }

        val btnLogin: Button = findViewById(R.id.loginBtn)
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val check_faculty = check_faculty.isChecked

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (check_faculty) {
                faculty_login(username, password)
            } else {
                login(username, password)
            }

        }

    }

//    private fun handleLocationUpdate(location: Location) {
//        val pointA = Location("Point A").apply { latitude = 17.734131; longitude = 83.330083}
//
//        val pointB = Location("Point B").apply { latitude = 17.734107; longitude = 83.330190 }
//
//        val pointC = Location("Point C").apply { latitude = 17.733954; longitude = 83.330152 }
//
//        val pointD = Location("Point D").apply { latitude = 17.733981; longitude = 83.330045}
//
//        if (isWithinParameter(location, pointA, pointB, pointC, pointD)) {
//            currla = location.latitude
//            currlong = location.longitude
//            updateAttendanceStatus(s_id, classId)
//        } else {
//            showSnackbar("You are not within the attendance marking area!")
//        }
//    }

    private fun isWithinParameter(
        currentLocation: Location?,
        pointA: Location,
        pointB: Location,
        pointC: Location,
        pointD: Location
    ): Boolean {
        return currentLocation != null &&
                isInsidePolygon(currentLocation, arrayOf(pointA, pointB, pointC, pointD))
    }

    private fun isInsidePolygon(location: Location, polygon: Array<Location>): Boolean {
        val lat = location.latitude
        val lng = location.longitude
        var oddNodes = false
        var j = polygon.size - 1

        for (i in polygon.indices) {
            val polyLat = polygon[i].latitude
            val polyLng = polygon[i].longitude
            val polyJLat = polygon[j].latitude
            val polyJLng = polygon[j].longitude

            if ((polyLat < lat && polyJLat >= lat) || (polyJLat < lat && polyLat >= lat)) {
                if (polyLng + (lat - polyLat) / (polyJLat - polyLat) * (polyJLng - polyLng) < lng) {
                    oddNodes = !oddNodes
                }
            }
            j = i
        }

        return oddNodes
    }


    private fun updateAttendanceStatus(s_id: String, classId: String) {
        val url = "https://liquag.com/dev/school/admin/mobile/mark_attendance.php"

        // Create the postData HashMap
        val postData = HashMap<String, String>()
        postData["student_id"] = s_id
        postData["class_id"] = classId
        postData["status"] = "present"
        postData["date"] = currentDate
        postData["latitude"] = currla.toString()
        postData["longitude"] = currlong.toString()

        // Initialize the StringRequest
        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                if (response.contains("success")) {
                    // Success response from server
                    val rootView = findViewById<View>(android.R.id.content)
                    Snackbar.make(rootView, "Attendance marked successfully!", Snackbar.LENGTH_LONG).show()
                } else {
                    // Server returned an error
                  //  Log.e("Attendance Status", "Error updating attendance status: $response")
                    val rootView = findViewById<View>(android.R.id.content)
                    Snackbar.make(rootView, "Error updating attendance status, Contact Admin", Snackbar.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error ->
                // Handle error
                Snackbar.make(
                    findViewById<View>(android.R.id.content),
                    "There seems to be retrieving data, contact Admin.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return postData
            }
        }

        // Check location permission and make request
        if (locationHelper.checkLocationPermission()) {

            val coordinatesurl = "https://liquag.com/dev/school/admin/mobile/get_corrdinates.php"
            val postClass = HashMap<String, String>()
            postClass["class_id"] = classId

            Log.d("Geofence", "Class ID: $classId")

            val stringRequestGeoFence = object : StringRequest(
                Method.POST, coordinatesurl,
                Response.Listener<String> { response ->
                    try {
                        val obj = JSONObject(response)
                        val success = obj.getBoolean("success")

                        Log.d("TryCatch", "Response: $response")

                        if (success) {
                            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

                            val coordinates = obj.getJSONObject("coordinates")

                            val pointA = Location("Point A").apply {
                                latitude = coordinates.getJSONObject("pointA").getDouble("latitude")
                                longitude = coordinates.getJSONObject("pointA").getDouble("longitude")
                            }

                            val pointB = Location("Point B").apply {
                                latitude = coordinates.getJSONObject("pointB").getDouble("latitude")
                                longitude = coordinates.getJSONObject("pointB").getDouble("longitude")
                            }

                            val pointC = Location("Point C").apply {
                                latitude = coordinates.getJSONObject("pointC").getDouble("latitude")
                                longitude = coordinates.getJSONObject("pointC").getDouble("longitude")
                            }

                            val pointD = Location("Point D").apply {
                                latitude = coordinates.getJSONObject("pointD").getDouble("latitude")
                                longitude = coordinates.getJSONObject("pointD").getDouble("longitude")
                            }


                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                           // return
                        }
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location: Location? ->
                                if (location != null && isWithinParameter(location, pointA, pointB, pointC, pointD)) {
                                    // Location is within the parameter, add request to requestQueue
                                    Log.d(  "GeoLocation", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                                    requestQueue.add(stringRequest)
                                } else {
                                    // Location is not within the parameter, show error message
                                    val rootView = findViewById<View>(android.R.id.content)
                                    Snackbar.make(rootView, "Not in the right location", Snackbar.LENGTH_LONG).show()
                                    Log.e("BiometricLogin", "Location is null")
                                }
                            }
                            .addOnFailureListener { e ->
                                // Handle failure on getting location
                                Snackbar.make(
                                    findViewById<View>(android.R.id.content),
                                    "Error getting location, try again.",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }


                    }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        showSnackbar(e.toString())
                    }
                },
                Response.ErrorListener { error ->
                    // Handle error
                    Snackbar.make(
                        findViewById<View>(android.R.id.content),
                        "There seems to be a problem with GeoServer, contact Admin.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            ) {
                override fun getParams(): Map<String, String> {
                    return postClass
                }
            }


            // Add the StringRequest to the RequestQueue
            requestQueue.add(stringRequestGeoFence)


        } else {
            // Location permission is not granted, request it
            locationHelper.requestLocationPermission(LOCATION_PERMISSION_REQUEST_CODE)
        }
    }


    private fun showSnackbar(message: String) {
        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } else {
            requestLocationPermission()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
    private fun login(username: String, password: String) {
        val loginUrl = "https://liquag.com/dev/school/admin/mobile/login.php"
        val queue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
            Method.POST, loginUrl,
            Response.Listener { response ->
                try {
                    val obj = JSONObject(response)
                    val success = obj.getBoolean("success")

                    if (success) {
                        // Check if s_id and s_name fields exist in the JSON response
                        if (obj.has("s_id") && obj.has("s_name")) {
                            s_id = obj.getString("s_id")
                            val s_name = obj.getString("s_name")

                            // Log student ID before saving to SharedPreferences
                            Log.d("LoginActivity", "Retrieved s_id: $s_id, s_name: $s_name")


                            // val f_id = obj.getString("id")
                            sharedPref.edit().putString(KEY_S_USERID, s_id).apply()
                            sharedPref.edit().putString(KEY_S_NAME, s_name).apply()



                            Log.d("LoginActivity", "s_id: $s_id, s_name: $s_name, success: $success")

//                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
//                            startActivity(intent)
//                            finish()

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else {
                            Log.e("LoginActivity", "s_id or s_name not found in JSON response")
                        }
                    } else {
                        // Using Snackbar to show error message
                        val rootView = findViewById<View>(android.R.id.content)
                        Snackbar.make(rootView, "Login failed. Please try again.", Snackbar.LENGTH_LONG)
                            .show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["username"] = username
                params["password"] = password
                return params
            }
        }


        queue.add(stringRequest)
    }

    private fun flipViews(outgoingView: View, incomingView: View) {
        val flipOut = AnimationUtils.loadAnimation(this, R.anim.flip_out)
        val flipIn = AnimationUtils.loadAnimation(this, R.anim.flip_in)

        flipOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                outgoingView.visibility = View.GONE
                incomingView.visibility = View.VISIBLE
                incomingView.startAnimation(flipIn)
            }
            override fun onAnimationRepeat(animation: Animation) {}
        })

        outgoingView.startAnimation(flipOut)
    }

    private fun setClass(){

        val stdid = sharedPref.getString(KEY_S_USERID, null)

        val checkAttendanceURL = "https://liquag.com/dev/school/admin/mobile/check_attendance.php?std=$stdid" // Change this to your API's URL
        val queue = Volley.newRequestQueue(this)
        val classopentxt: TextView? = findViewById(R.id.classopen)
        // Inside your Volley StringRequest's Response.Listener
        val stringRequest = object : StringRequest(
            Method.GET, checkAttendanceURL,
            Response.Listener { response ->
                try {
                    val obj = JSONObject(response)
                    val success = obj.getBoolean("success")

                    if (success) {
                        // If success, retrieve class information
                        val classesArray = obj.getJSONArray("classes")
                        for (i in 0 until classesArray.length()) {
                            val classObj = classesArray.getJSONObject(i)
                            classId = classObj.getString("classid")
                            val className = classObj.getString("classname")

                            classopentxt?.text = className

                            // Handle the class information here
                            Log.d("Class Info", "Class ID: $classId, Class Name: $className")

                        }
                    } else {
                        // If not success, handle error message
                        classopentxt?.text = "No class open"
                        val errorMessage = obj.getString("message")
                        Log.e("Error Found", errorMessage+" for ID "+KEY_S_USERID)
                        // Log.e("Error STDID", KEY_S_USERID)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
            }
        )


        {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                return params
            }
        }

        queue.add(stringRequest)


    }

    private fun attendace () {
        val loginUrl = "https://liquag.com/dev/school/admin/mobile/attendance.php" // Change this to your API's URL
        val queue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(Method.POST, loginUrl, Response.Listener { response ->
            try {
                val obj = JSONObject(response)
                val success = obj.getBoolean("success")
                val f_id = obj.getString("id")
                val f_name = obj.getString("name")

                if (success) {

                    // val f_id = obj.getString("id")
                    sharedPref.edit().putString(KEY_F_USERID, f_id).apply()
                    sharedPref.edit().putString(KEY_F_NAME, f_name).apply()

                    val intent = Intent(this, Faculty::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Using Snackbar to show error message
                    val rootView = findViewById<View>(android.R.id.content)
                    Snackbar.make(rootView, "Faculty login fail.", Snackbar.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, Response.ErrorListener { error ->
            error.printStackTrace()
        }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                ///params["username"] = username
                // params["password"] = password
                return params
            }
        }

        queue.add(stringRequest)
    }

    private fun faculty_login(username: String, password: String) {
        val loginUrl = "https://liquag.com/dev/school/admin/mobile/faculty_login.php" // Change this to your API's URL
        val queue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(Method.POST, loginUrl, Response.Listener { response ->
            try {
                val obj = JSONObject(response)
                val success = obj.getBoolean("success")
                val f_id = obj.getString("id")
                val f_name = obj.getString("name")

                if (success) {

                    // val f_id = obj.getString("id")
                    sharedPref.edit().putString(KEY_F_USERID, f_id).apply()
                    sharedPref.edit().putString(KEY_F_NAME, f_name).apply()

                    val intent = Intent(this, Faculty::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Using Snackbar to show error message
                    val rootView = findViewById<View>(android.R.id.content)
                    Snackbar.make(rootView, "Faculty login fail.", Snackbar.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, Response.ErrorListener { error ->
            error.printStackTrace()
        }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["username"] = username
                params["password"] = password
                return params
            }
        }

        queue.add(stringRequest)
    }
}
