package com.liquag.schoolattendance

import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult

class MyLocationCallback : LocationCallback() {
    override fun onLocationResult(locationResult: LocationResult) {
        super.onLocationResult(locationResult)
        locationResult ?: return
        for (location in locationResult.locations) {
            // Handle location updates here
        }
    }
}
