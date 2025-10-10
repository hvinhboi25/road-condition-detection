package com.example.myapplication

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class FirebaseService(private val context: Context) {
    
    private val db = FirebaseFirestore.getInstance()
    
    init {
        Log.d("FirebaseService", "FirebaseService initialized")
        
        // Test Firebase connection
        testFirebaseConnection()
    }
    
    /**
     * Test Firebase connection
     */
    private fun testFirebaseConnection() {
        Log.d("FirebaseService", "Testing Firebase connection...")
        
        // Test Firestore connection
        db.collection("connection_test")
            .document("test")
            .set(hashMapOf(
                "message" to "Firebase connected successfully",
                "timestamp" to System.currentTimeMillis()
            ))
            .addOnSuccessListener {
                Log.d("FirebaseService", "✅ Firebase connection test successful")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "❌ Firebase connection test failed", e)
            }
    }
    
    /**
     * Lưu kết quả phát hiện vào Firestore
     */
    fun saveDetectionToFirestore(imageUrl: String, detections: List<Detection>, locationData: LocationData? = null) {
        Log.d("FirebaseService", "=== STARTING SAVE TO FIRESTORE ===")
        Log.d("FirebaseService", "Image URL: $imageUrl")
        Log.d("FirebaseService", "Detections count: ${detections.size}")
        Log.d("FirebaseService", "Location data: $locationData")
        
        // Chuẩn bị dữ liệu detection
        val detectionData = hashMapOf(
            "imageUrl" to imageUrl,
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
            } else null
        )

        Log.d("FirebaseService", "Detection data prepared: $detectionData")
        Log.d("FirebaseService", "Attempting to save to Firestore collection 'detections'...")

        db.collection("detections")
            .add(detectionData)
            .addOnSuccessListener { docRef ->
                Log.d("Firebase", "✅ SUCCESS: Document added with ID: ${docRef.id}")
                Log.d("Firebase", "✅ Data saved to collection: detections")
                Log.d("Firebase", "✅ Check Firebase Console > Firestore Database")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "❌ FAILED: Error adding document", e)
                Log.e("Firebase", "❌ Error message: ${e.message}")
                Log.e("Firebase", "❌ Error type: ${e.javaClass.simpleName}")
            }
        
        Log.d("FirebaseService", "=== SAVE DETECTION COMPLETED ===")
    }
    
    /**
     * Lưu thống kê phát hiện theo ngày
     */
    fun saveDailyStats(detectionCount: Int) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val statsRef = db.collection("daily_stats").document(today)
        
        statsRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Update existing document
                val currentCount = document.getLong("totalDetections") ?: 0L
                statsRef.update("totalDetections", currentCount + detectionCount)
            } else {
                // Create new document
                val statsData = hashMapOf(
                    "date" to today,
                    "totalDetections" to detectionCount,
                    "lastUpdated" to System.currentTimeMillis()
                )
                statsRef.set(statsData)
            }
        }
    }
}