package com.liquag.schoolattendance
import Faculty_AttendanceFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class Faculty : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.faculty_main)

        bottomNav = findViewById(R.id.bottom_navigation_faculty)
        bottomNav.setOnNavigationItemSelectedListener(navListener)

        // Set initial fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container_faculty, Faculty_AttendanceFragment()).commit()
        }
    }

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        var selectedFragment: Fragment? = null

        when (item.itemId) {
            R.id.faculty_nav_attendance -> selectedFragment = Faculty_AttendanceFragment()
            R.id.faculty_nav_notifications -> selectedFragment = Faculty_NotificationsFragment()
        }

        if (selectedFragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container_faculty, selectedFragment).commit()
            true
        } else {
            false
        }
    }
}

