package com.example.myapplication

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class FirebaseService(private val context: Context) {
    
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    fun saveDetectionToFirestore(
        imageUrl: String,
        detections: List<Detection>,
        locationData: LocationData? = null,
        videoUrl: String? = null,
        videoDetections: List<Map<String, Any>>? = null
    ) {
        val currentUser = auth.currentUser
        
        val detectionData = hashMapOf(
            "imageUrl" to imageUrl,
            "videoUrl" to videoUrl,
            "videoDetections" to videoDetections,
            "detections" to detections.map { detection ->
                mapOf(
                    "className" to detection.className,
                    "confidence" to detection.confidence,
                    "boundingBox" to mapOf(
                        "x" to detection.x,
                        "y" to detection.y,
                        "width" to detection.width,
                        "height" to detection.height
                    )
                )
            },
            "timestamp" to System.currentTimeMillis(),
            "detectionCount" to detections.size,
            "date" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            "location" to if (locationData != null) {
                mapOf(
                    "latitude" to locationData.latitude,
                    "longitude" to locationData.longitude,
                    "accuracy" to locationData.accuracy,
                    "timestamp" to locationData.timestamp
                )
            } else null,
            // Thêm thông tin user
            "userId" to (currentUser?.uid ?: "anonymous"),
            "userName" to (currentUser?.displayName ?: currentUser?.email ?: "Anonymous"),
            "userEmail" to (currentUser?.email ?: ""),
            "source" to "android"
        )

        db.collection("detections")
            .add(detectionData)
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to save detection: ${e.message}")
            }
    }
    
    fun saveDailyStats(detectionCount: Int) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val statsRef = db.collection("daily_stats").document(today)
        
        statsRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentCount = document.getLong("totalDetections") ?: 0L
                statsRef.update("totalDetections", currentCount + detectionCount)
            } else {
                statsRef.set(hashMapOf(
                    "date" to today,
                    "totalDetections" to detectionCount,
                    "lastUpdated" to System.currentTimeMillis()
                ))
            }
        }
    }
}