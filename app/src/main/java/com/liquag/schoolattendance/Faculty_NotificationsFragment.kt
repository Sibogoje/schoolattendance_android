package com.liquag.schoolattendance

// AttendanceFragment.kt
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

class Faculty_NotificationsFragment : Fragment() {

    private var fId: String? = null
    private var fname: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.faculty_notifications_attendance, container, false)
    }

    // dynamic toolbar
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        val sharedPref = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        fId = sharedPref.getString(KEY_F_USERID, null)
        fname = sharedPref.getString(KEY_F_NAME, null)

        // set title
        (activity as AppCompatActivity).supportActionBar?.title = "Notifications"

        //webview for notifications
        val webView: WebView = view.findViewById(R.id.fac_noti_webview)
        webView.loadUrl("https://liquag.com/dev/school/admin/mobile/fac_notifications.php?f_id=$fId")

        // Enable Javascript
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Force links and redirects to open in the WebView instead of in a browser
        webView.webViewClient = WebViewClient()

    }

    companion object {
        private const val PREF_NAME = "faculty_id"
        private const val KEY_F_USERID = "f_id"
        private const val KEY_F_NAME = "f_name"
    }

}
