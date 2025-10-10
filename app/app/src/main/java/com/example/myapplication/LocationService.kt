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
    
    init {
        Log.d("LocationService", "LocationService initialized")
    }
    
    /**
     * Lấy vị trí hiện tại
     */
    fun getCurrentLocation(onSuccess: (LocationData) -> Unit, onFailure: (Exception) -> Unit) {
        Log.d("LocationService", "Getting current location...")
        
        // Kiểm tra permissions
        if (!hasLocationPermissions()) {
            Log.e("LocationService", "Location permissions not granted")
            onFailure(Exception("Location permissions not granted"))
            return
        }
        
        // Kiểm tra location services
        if (!isLocationEnabled()) {
            Log.e("LocationService", "Location services not enabled")
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
                    val locationData = LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = location.time
                    )
                    
                    Log.d("LocationService", "✅ Location obtained: ${locationData.latitude}, ${locationData.longitude}")
                    Log.d("LocationService", "Accuracy: ${locationData.accuracy}m, Time: ${locationData.timestamp}")
                    onSuccess(locationData)
                } else {
                    Log.e("LocationService", "Location is null")
                    onFailure(Exception("Unable to get current location"))
                }
            }.addOnFailureListener { exception ->
                Log.e("LocationService", "Failed to get location: ${exception.message}")
                onFailure(exception)
            }
        } catch (e: SecurityException) {
            Log.e("LocationService", "Security exception: ${e.message}")
            onFailure(e)
        }
    }
    
    /**
     * Kiểm tra location permissions
     */
    private fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Kiểm tra location services có được bật không
     */
    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    
    /**
     * Lấy vị trí gần đây nhất (fallback)
     */
    fun getLastKnownLocation(onSuccess: (LocationData) -> Unit, onFailure: (Exception) -> Unit) {
        Log.d("LocationService", "Getting last known location...")
        
        if (!hasLocationPermissions()) {
            onFailure(Exception("Location permissions not granted"))
            return
        }
        
        try {
            val lastKnownLocation = fusedLocationClient.lastLocation
            lastKnownLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val locationData = LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = location.time
                    )
                    
                    Log.d("LocationService", "✅ Last known location: ${locationData.latitude}, ${locationData.longitude}")
                    onSuccess(locationData)
                } else {
                    Log.e("LocationService", "Last known location is null")
                    onFailure(Exception("No last known location available"))
                }
            }.addOnFailureListener { exception ->
                Log.e("LocationService", "Failed to get last known location: ${exception.message}")
                onFailure(exception)
            }
        } catch (e: SecurityException) {
            Log.e("LocationService", "Security exception: ${e.message}")
            onFailure(e)
        }
    }
}
