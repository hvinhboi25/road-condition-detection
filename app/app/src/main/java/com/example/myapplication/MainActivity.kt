package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.media.MediaMetadataRetriever
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DeepLearningApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DeepLearningApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    var resultImage by remember { mutableStateOf<Bitmap?>(null) }
    var predictions by remember { mutableStateOf<List<Detection>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val tensorFlowHelper = remember { SimpleTensorFlowHelper(context) }
    val firebaseService = remember { FirebaseService(context) }
    val cloudinaryService = remember { CloudinaryService(context) }
    val locationService = remember { LocationService(context) }
    // Helper to upload detections (used for both image and video flows)
    fun uploadDetections(imageWithBoxes: Bitmap, results: List<Detection>, isLocationPermissionGranted: Boolean, originalVideoUri: Uri? = null, videoDetections: List<Map<String, Any>>? = null) {
        if (results.isEmpty()) {
            android.util.Log.d("MainActivity", "❌ Không phát hiện ổ gà - không gửi lên Firebase")
            return
        }

        android.util.Log.d("MainActivity", "✅ Phát hiện ${results.size} ổ gà - sẽ gửi lên Firebase")

        // Get current location before saving to Firebase
        android.util.Log.d("MainActivity", "🌍 === GETTING LOCATION ===")
        android.util.Log.d("MainActivity", "Getting current location...")

        if (isLocationPermissionGranted) {
            android.util.Log.d("MainActivity", "✅ Location permission granted")
            locationService.getCurrentLocation(
                onSuccess = { locationData ->
                    android.util.Log.d("MainActivity", "✅ Location obtained: ${locationData.latitude}, ${locationData.longitude}")
                    android.util.Log.d("MainActivity", "=== FIREBASE SAVE WITH LOCATION ===")
                    // If original video exists, upload it first, then image
                    if (originalVideoUri != null) {
                        cloudinaryService.uploadVideo(
                            uri = originalVideoUri,
                            onSuccess = { videoUrl ->
                                cloudinaryService.uploadImageWithBoxes(
                                    bitmap = imageWithBoxes,
                                    detections = results,
                                    onSuccess = { imageUrl ->
                                        firebaseService.saveDetectionToFirestore(imageUrl, results, locationData, videoUrl, videoDetections)
                                        firebaseService.saveDailyStats(results.size)
                                    },
                                    onFailure = { e ->
                                        firebaseService.saveDetectionToFirestore("upload_failed", results, locationData, videoUrl, videoDetections)
                                        firebaseService.saveDailyStats(results.size)
                                    }
                                )
                            },
                            onFailure = { e ->
                                // video upload failed, continue with image only
                                cloudinaryService.uploadImageWithBoxes(
                                    bitmap = imageWithBoxes,
                                    detections = results,
                                    onSuccess = { imageUrl ->
                                        firebaseService.saveDetectionToFirestore(imageUrl, results, locationData, null, videoDetections)
                                        firebaseService.saveDailyStats(results.size)
                                    },
                                    onFailure = { e2 ->
                                        firebaseService.saveDetectionToFirestore("upload_failed", results, locationData, null, videoDetections)
                                        firebaseService.saveDailyStats(results.size)
                                    }
                                )
                            }
                        )
                    } else {
                        cloudinaryService.uploadImageWithBoxes(
                            bitmap = imageWithBoxes,
                            detections = results,
                            onSuccess = { imageUrl ->
                                firebaseService.saveDetectionToFirestore(imageUrl, results, locationData, null)
                                firebaseService.saveDailyStats(results.size)
                            },
                            onFailure = { exception ->
                                firebaseService.saveDetectionToFirestore("upload_failed", results, locationData, null)
                                firebaseService.saveDailyStats(results.size)
                            }
                        )
                    }
                },
                onFailure = { exception ->
                    android.util.Log.w("MainActivity", "⚠️ Failed to get location: ${exception.message}")
                    // Try to get last known location
                    locationService.getLastKnownLocation(
                        onSuccess = { locationData ->
                            android.util.Log.d("MainActivity", "✅ Using last known location: ${locationData.latitude}, ${locationData.longitude}")
                            if (originalVideoUri != null) {
                                cloudinaryService.uploadVideo(
                                    uri = originalVideoUri,
                                    onSuccess = { videoUrl ->
                                        cloudinaryService.uploadImageWithBoxes(
                                            bitmap = imageWithBoxes,
                                            detections = results,
                                            onSuccess = { imageUrl ->
                                firebaseService.saveDetectionToFirestore(imageUrl, results, locationData, videoUrl, videoDetections)
                                                firebaseService.saveDetectionToFirestore(imageUrl, results, locationData, videoUrl, videoDetections)
                                                firebaseService.saveDailyStats(results.size)
                                            },
                                            onFailure = { uploadException ->
                                                firebaseService.saveDetectionToFirestore("upload_failed", results, locationData, videoUrl, videoDetections)
                                                firebaseService.saveDailyStats(results.size)
                                            }
                                        )
                                    },
                                    onFailure = { _ ->
                                        cloudinaryService.uploadImageWithBoxes(
                                            bitmap = imageWithBoxes,
                                            detections = results,
                                            onSuccess = { imageUrl ->
                                                firebaseService.saveDetectionToFirestore(imageUrl, results, locationData, null, videoDetections)
                                                firebaseService.saveDailyStats(results.size)
                                            },
                                            onFailure = { _ ->
                                                firebaseService.saveDetectionToFirestore("upload_failed", results, locationData, null, videoDetections)
                                                firebaseService.saveDailyStats(results.size)
                                            }
                                        )
                                    }
                                )
                            } else {
                                cloudinaryService.uploadImageWithBoxes(
                                    bitmap = imageWithBoxes,
                                    detections = results,
                                    onSuccess = { imageUrl ->
                                        firebaseService.saveDetectionToFirestore(imageUrl, results, locationData, null)
                                        firebaseService.saveDailyStats(results.size)
                                    },
                                    onFailure = { uploadException ->
                                        firebaseService.saveDetectionToFirestore("upload_failed", results, locationData, null)
                                        firebaseService.saveDailyStats(results.size)
                                    }
                                )
                            }
                        },
                        onFailure = { lastException ->
                            android.util.Log.w("MainActivity", "⚠️ No location available: ${lastException.message}")
                            if (originalVideoUri != null) {
                                cloudinaryService.uploadVideo(
                                    uri = originalVideoUri,
                                    onSuccess = { videoUrl ->
                                        cloudinaryService.uploadImageWithBoxes(
                                            bitmap = imageWithBoxes,
                                            detections = results,
                                            onSuccess = { imageUrl ->
                                                firebaseService.saveDetectionToFirestore(imageUrl, results, null, videoUrl, videoDetections)
                                                firebaseService.saveDailyStats(results.size)
                                            },
                                            onFailure = { _ ->
                                                firebaseService.saveDetectionToFirestore("upload_failed", results, null, videoUrl, videoDetections)
                                                firebaseService.saveDailyStats(results.size)
                                            }
                                        )
                                    },
                                    onFailure = { _ ->
                                        cloudinaryService.uploadImageWithBoxes(
                                            bitmap = imageWithBoxes,
                                            detections = results,
                                            onSuccess = { imageUrl ->
                                                firebaseService.saveDetectionToFirestore(imageUrl, results, null, null, videoDetections)
                                                firebaseService.saveDailyStats(results.size)
                                            },
                                            onFailure = { _ ->
                                                firebaseService.saveDetectionToFirestore("upload_failed", results, null, null, videoDetections)
                                                firebaseService.saveDailyStats(results.size)
                                            }
                                        )
                                    }
                                )
                            } else {
                                cloudinaryService.uploadImageWithBoxes(
                                    bitmap = imageWithBoxes,
                                    detections = results,
                                    onSuccess = { imageUrl ->
                                        firebaseService.saveDetectionToFirestore(imageUrl, results, null, null)
                                        firebaseService.saveDailyStats(results.size)
                                    },
                                    onFailure = { _ ->
                                        firebaseService.saveDetectionToFirestore("upload_failed", results, null, null)
                                        firebaseService.saveDailyStats(results.size)
                                    }
                                )
                            }
                        }
                    )
                }
            )
        } else {
            android.util.Log.w("MainActivity", "⚠️ Location permission not granted, saving without location")
            if (originalVideoUri != null) {
                cloudinaryService.uploadVideo(
                    uri = originalVideoUri,
                    onSuccess = { videoUrl ->
                        cloudinaryService.uploadImageWithBoxes(
                            bitmap = imageWithBoxes,
                            detections = results,
                            onSuccess = { imageUrl ->
                                firebaseService.saveDetectionToFirestore(imageUrl, results, null, videoUrl, videoDetections)
                                firebaseService.saveDailyStats(results.size)
                            },
                            onFailure = { _ ->
                                firebaseService.saveDetectionToFirestore("upload_failed", results, null, videoUrl, videoDetections)
                                firebaseService.saveDailyStats(results.size)
                            }
                        )
                    },
                    onFailure = { _ ->
                        cloudinaryService.uploadImageWithBoxes(
                            bitmap = imageWithBoxes,
                            detections = results,
                            onSuccess = { imageUrl ->
                                firebaseService.saveDetectionToFirestore(imageUrl, results, null, null, videoDetections)
                                firebaseService.saveDailyStats(results.size)
                            },
                            onFailure = { _ ->
                                firebaseService.saveDetectionToFirestore("upload_failed", results, null, null, videoDetections)
                                firebaseService.saveDailyStats(results.size)
                            }
                        )
                    }
                )
            } else {
                cloudinaryService.uploadImageWithBoxes(
                    bitmap = imageWithBoxes,
                    detections = results,
                    onSuccess = { imageUrl ->
                        firebaseService.saveDetectionToFirestore(imageUrl, results, null, null)
                        firebaseService.saveDailyStats(results.size)
                    },
                    onFailure = { _ ->
                        firebaseService.saveDetectionToFirestore("upload_failed", results, null, null)
                        firebaseService.saveDailyStats(results.size)
                    }
                )
            }
        }
    }

    
    // Permissions
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    
    // Document picker (images + videos)
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val mime = context.contentResolver.getType(it) ?: ""
                if (mime.startsWith("video")) {
                    // manual location dialog removed (rollback)
                    // Process video frames automatically
                    isLoading = true
                    resultImage = null
                    predictions = emptyList()

                    Thread {
                        val retriever = MediaMetadataRetriever()
                        var bestFrame: Bitmap? = null
                        var bestDetections: List<Detection> = emptyList()
                        val videoDetectionsTimeline = mutableListOf<Map<String, Any>>()
                        try {
                            retriever.setDataSource(context, it)
                            val durationMs = (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                                ?: 0L)
                            val totalSamples = 20 // cap number of frames to evaluate
                            val step = if (durationMs > 0) durationMs / totalSamples else 0L

                            var t = 0L
                            var samples = 0
                            while (samples < totalSamples) {
                                val frame = retriever.getFrameAtTime(t * 1000, MediaMetadataRetriever.OPTION_CLOSEST)
                                if (frame != null) {
                                    val dets = tensorFlowHelper.predict(frame)
                                    // collect per-sample detections with timestamp (ms)
                                    if (dets.isNotEmpty()) {
                                        val detectionsMap = dets.map { d ->
                                            mapOf(
                                                "className" to d.className,
                                                "confidence" to d.confidence,
                                                // store normalized xywh (as current ImageUtils expects)
                                                "x" to d.x,
                                                "y" to d.y,
                                                "width" to d.width,
                                                "height" to d.height
                                            )
                                        }
                                        videoDetectionsTimeline.add(
                                            mapOf(
                                                "timeMs" to t,
                                                "detections" to detectionsMap
                                            )
                                        )
                                    }
                                    if (dets.isNotEmpty() && dets.size > bestDetections.size) {
                                        bestDetections = dets
                                        bestFrame = frame
                                    }
                                }
                                if (step <= 0L) break
                                t += step
                                samples++
                            }

                            if (bestFrame != null) {
                                val imageWithBoxes = ImageUtils.drawBoundingBoxes(bestFrame!!, bestDetections)

                                // Immediately upload detections for video input (original flow)
                                uploadDetections(
                                    imageWithBoxes,
                                    bestDetections,
                                    isLocationPermissionGranted = locationPermissionState.status.isGranted,
                                    originalVideoUri = it,
                                    videoDetections = videoDetectionsTimeline
                                )

                                // Update UI on main thread
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    selectedImage = bestFrame
                                    resultImage = imageWithBoxes
                                    predictions = bestDetections
                                    isLoading = false
                                }
                            } else {
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    isLoading = false
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            android.os.Handler(android.os.Looper.getMainLooper()).post { isLoading = false }
                        } finally {
                            try { retriever.release() } catch (_: Exception) {}
                        }
                    }.start()
                } else {
                    // Image flow
                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                    // manual location dialog removed (rollback)
                    selectedImage = bitmap
                    resultImage = null
                    predictions = emptyList()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Handle camera result if needed
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFF3E5F5)
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header với gradient background
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Phát hiện Ổ gà",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E3440),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ứng dụng AI phát hiện ổ gà thông minh",
                    fontSize = 14.sp,
                    color = Color(0xFF5E6C84),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Image display
        val displayImage = resultImage ?: selectedImage
        displayImage?.let { bitmap ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(bottom = 20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = if (resultImage != null) "Ảnh với bounding box" else "Ảnh gốc",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                    )
                    
                    // Overlay với thông tin
                    if (resultImage != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(
                                    Color(0x80000000),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Đã phân tích",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        
        // Control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gallery Button
            Button(
                onClick = { mediaPickerLauncher.launch(arrayOf("image/*", "video/*")) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Thư viện",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Camera Button
            Button(
                onClick = {
                    when {
                        cameraPermissionState.status.isGranted -> {
                            // Open camera activity
                            val intent = Intent(context, CameraActivity::class.java)
                            context.startActivity(intent)
                        }
                        cameraPermissionState.status.shouldShowRationale -> {
                            // Show rationale
                        }
                        else -> {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Camera",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Location permission button
        if (!locationPermissionState.status.isGranted) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Button(
                    onClick = { locationPermissionState.launchPermissionRequest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Cấp quyền vị trí",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // Predict button
        Button(
            onClick = {
                selectedImage?.let { bitmap ->
                    isLoading = true
                    Thread {
                        android.util.Log.d("MainActivity", "=== STARTING DETECTION PROCESS ===")
                        android.util.Log.d("MainActivity", "Starting prediction thread...")
                        val results = tensorFlowHelper.predict(bitmap)
                        android.util.Log.d("MainActivity", "Prediction completed, found ${results.size} detections")
                        android.util.Log.d("MainActivity", "Results details: $results")
                        
                        // Draw bounding boxes on image
                        val imageWithBoxes = ImageUtils.drawBoundingBoxes(bitmap, results)
                        // Upload detections (if any)
                        uploadDetections(imageWithBoxes, results, isLocationPermissionGranted = locationPermissionState.status.isGranted)
                        
                        // Update UI on main thread
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            predictions = results
                            resultImage = imageWithBoxes
                            isLoading = false
                            android.util.Log.d("MainActivity", "UI updated with ${results.size} detections")
                        }
                    }.start()
                }
            },
            enabled = selectedImage != null && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedImage != null) Color(0xFFFF6B35) else Color(0xFFBDBDBD)
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Đang phân tích...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            } else {
                Text(
                    text = "Phát hiện ổ gà",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        // Results display
        if (predictions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Header với số lượng
                    Text(
                        text = "Phát hiện ${predictions.size} ổ gà",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E3440),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(predictions) { detection ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF3E0)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Ổ gà #${predictions.indexOf(detection) + 1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF2E3440)
                                        )
                                        
                                        // Badge độ tin cậy
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (detection.confidence > 0.7f) Color(0xFF4CAF50) else Color(0xFFFF9800)
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "${String.format("%.0f", detection.confidence * 100)}%",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Độ tin cậy: ${String.format("%.1f", detection.confidence * 100)}%",
                                        fontSize = 14.sp,
                                        color = Color(0xFF5E6C84)
                                    )
                                    Text(
                                        text = "Kích thước: ${String.format("%.0f", detection.width)} x ${String.format("%.0f", detection.height)}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF5E6C84)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}