package com.liquag.schoolattendance

// AttendanceFragment.kt
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

class AttendanceFragment : Fragment() {

    private var fsId: String? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_attendance, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.attend_stud_toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "Attendance"

        //val sharedPref = requireActivity().getSharedPreferences(KEY_S_USERID, Context.MODE_PRIVATE)


        // Retrieve staff ID (f_id) from shared preferences
        val sharedPref = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        fsId = sharedPref.getString(KEY_S_USERID, null)
        //fname = sharedPref.getString(KEY_F_NAME, null)


        // Check if fsId is not null before using it
        if (fsId != null) {
            // Construct the URL with the student ID
            val url = "https://liquag.com/dev/school/admin/mobile/student_attendance.php?s_id=$fsId"

            // Log the constructed URL
            Log.d("AttendanceFragment", "setupWebView: $url")

            // Find the WebView and load the URL
            val webView = view.findViewById<WebView>(R.id.attend_stud_webview)
            webView.webViewClient = MyWebViewClient()
            webView.loadUrl(url)

            // Enable JavaScript
            val webSettings = webView.settings
            webSettings.javaScriptEnabled = true
        } else {
            // Handle case where student ID is not available
            Log.e("AttendanceFragment", "Failed to retrieve student ID from SharedPreferences")
        }

        return view
    }


    companion object {

        private const val KEY_S_USERID = "s_id"
        private const val KEY_S_NAME = "s_name"
        private const val PREF_NAME = "faculty_id"
    }


    // Inner class to handle WebView navigation
    private class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
            view?.loadUrl(url)
            return true
        }
    }
}
