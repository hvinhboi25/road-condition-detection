package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long
)

class LocationService(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    
    fun getCurrentLocation(onSuccess: (LocationData) -> Unit, onFailure: (Exception) -> Unit) {
        if (!hasLocationPermissions()) {
            onFailure(Exception("Location permissions not granted"))
            return
        }
        
        if (!isLocationEnabled()) {
            onFailure(Exception("Location services not enabled"))
            return
        }
        
        try {
            val cancellationTokenSource = CancellationTokenSource()
            
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onSuccess(LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = location.time
                    ))
                } else {
                    onFailure(Exception("Unable to get current location"))
                }
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }
        } catch (e: SecurityException) {
            onFailure(e)
        }
    }
    
    private fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    
    fun getLastKnownLocation(onSuccess: (LocationData) -> Unit, onFailure: (Exception) -> Unit) {
        if (!hasLocationPermissions()) {
            onFailure(Exception("Location permissions not granted"))
            return
        }
        
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onSuccess(LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = location.time
                    ))
                } else {
                    onFailure(Exception("No last known location available"))
                }
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }
        } catch (e: SecurityException) {
            onFailure(e)
        }
    }
}
