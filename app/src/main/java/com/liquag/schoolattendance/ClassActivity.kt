package com.liquag.schoolattendance

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.WebView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat



class ClassActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_class)

        val className = intent.getStringExtra("className")
        //val classId = intent.getStringExtra("classId")

        val toolbar: Toolbar = findViewById(R.id.toolbar2)

        // Set the toolbar as the support action bar
        setSupportActionBar(toolbar)

        // Enable the up button (back button) in the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Set the title of the toolbar
        supportActionBar?.title = className

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the WebView in the layout
        val classId = intent.getIntExtra("classId", 0) // Assuming the default value is 0 if "classId" is not found

        val classWebView: WebView = findViewById(R.id.classwebview)

// Enable JavaScript execution in the WebView (if required)
        classWebView.settings.javaScriptEnabled = true

// Load a webpage URL in the WebView
        val webpageUrl = "https://liquag.com/dev/school/admin/mobile/classwebview.php?class_id=$classId" // Replace this with your webpage URL
        classWebView.loadUrl(webpageUrl)

//log classId
        println("classId: $classId")
// log class id
        Log.d("classId", classId.toString())


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
