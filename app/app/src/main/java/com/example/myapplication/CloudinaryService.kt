package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CloudinaryService(private val context: Context) {
    
    companion object {
        // Cloudinary credentials từ Dashboard
        private const val CLOUD_NAME = "dkcc9skxe"
        private const val API_KEY = "597943171973618"
        private const val API_SECRET = "f5Flry6oqWeFAiJ8_-_lgdIp6ec"
        private const val UPLOAD_PRESET = "ml_default" // Sử dụng unsigned upload
    }
    
    init {
        try {
            val config = mapOf(
                "cloud_name" to CLOUD_NAME,
                "api_key" to API_KEY,
                "api_secret" to API_SECRET
            )
            MediaManager.init(context, config)
            Log.d("CloudinaryService", "✅ Cloudinary initialized successfully")
        } catch (e: Exception) {
            Log.e("CloudinaryService", "❌ Failed to initialize Cloudinary", e)
        }
    }
    
    fun uploadImageWithBoxes(
        bitmap: Bitmap,
        detections: List<Detection>,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d("CloudinaryService", "🔄 === STARTING CLOUDINARY UPLOAD ===")
        
        try {
            // Convert bitmap to byte array
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val imageData = byteArrayOutputStream.toByteArray()
            
            // Generate unique filename
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "detection_${timestamp}_${System.currentTimeMillis()}.jpg"
            
            Log.d("CloudinaryService", "📤 Uploading image: $filename")
            Log.d("CloudinaryService", "📊 Image size: ${imageData.size} bytes")
            Log.d("CloudinaryService", "🎯 Detections count: ${detections.size}")
            
            // Upload to Cloudinary (simplified)
            MediaManager.get().upload(imageData)
                .option("public_id", filename)
                .option("folder", "road_detection")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("CloudinaryService", "🚀 Upload started with ID: $requestId")
                    }
                    
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = (bytes * 100 / totalBytes).toInt()
                        Log.d("CloudinaryService", "📈 Upload progress: $progress% ($bytes/$totalBytes bytes)")
                    }
                    
                    override fun onSuccess(requestId: String, resultData: Map<*, *>?) {
                        val imageUrl = resultData?.get("secure_url") as? String
                        if (imageUrl != null) {
                            Log.d("CloudinaryService", "✅ Image uploaded successfully!")
                            Log.d("CloudinaryService", "🔗 Image URL: $imageUrl")
                            onSuccess(imageUrl)
                        } else {
                            Log.e("CloudinaryService", "❌ No URL returned from Cloudinary")
                            onFailure(Exception("No URL returned from Cloudinary"))
                        }
                    }
                    
                    override fun onError(requestId: String, error: ErrorInfo) {
                        val errorMsg = "Upload failed: ${error.description}"
                        Log.e("CloudinaryService", "❌ $errorMsg")
                        onFailure(Exception(errorMsg))
                    }
                    
                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.w("CloudinaryService", "🔄 Upload rescheduled: ${error.description}")
                    }
                })
                .dispatch()
                
        } catch (e: Exception) {
            Log.e("CloudinaryService", "❌ Upload failed with exception", e)
            onFailure(e)
        }
    }
}
