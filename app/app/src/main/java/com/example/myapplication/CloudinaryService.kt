package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
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
        } catch (e: Exception) {
            Log.e("CloudinaryService", "Failed to initialize Cloudinary: ${e.message}")
        }
    }
    
    fun uploadImageWithBoxes(
        bitmap: Bitmap,
        detections: List<Detection>,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val imageData = byteArrayOutputStream.toByteArray()
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "detection_${timestamp}_${System.currentTimeMillis()}.jpg"
            
            MediaManager.get().upload(imageData)
                .option("public_id", filename)
                .option("folder", "road_detection")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    
                    override fun onSuccess(requestId: String, resultData: Map<*, *>?) {
                        val imageUrl = resultData?.get("secure_url") as? String
                        if (imageUrl != null) {
                            onSuccess(imageUrl)
                        } else {
                            onFailure(Exception("No URL returned from Cloudinary"))
                        }
                    }
                    
                    override fun onError(requestId: String, error: ErrorInfo) {
                        onFailure(Exception("Upload failed: ${error.description}"))
                    }
                    
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch()
                
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    fun uploadVideo(
        uri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            MediaManager.get().upload(uri)
                .option("resource_type", "video")
                .option("folder", "road_detection/videos")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>?) {
                        val videoUrl = resultData?.get("secure_url") as? String
                        if (videoUrl != null) {
                            onSuccess(videoUrl)
                        } else {
                            onFailure(Exception("No video URL returned"))
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        onFailure(Exception("Video upload failed: ${error.description}"))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    fun uploadVideoFile(
        filePath: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            MediaManager.get().upload(filePath)
                .option("resource_type", "video")
                .option("folder", "road_detection/annotated")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String, resultData: Map<*, *>?) {
                        val url = resultData?.get("secure_url") as? String
                        if (url != null) onSuccess(url) else onFailure(Exception("No URL"))
                    }
                    override fun onError(requestId: String, error: ErrorInfo) {
                        onFailure(Exception(error.description))
                    }
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch()
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}
