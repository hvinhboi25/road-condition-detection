package com.example.myapplication

import android.Manifest
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.*
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
    // Helper to upload detections (optimized version)
    fun uploadDetections(imageWithBoxes: Bitmap, results: List<Detection>, isLocationPermissionGranted: Boolean, originalVideoUri: Uri? = null, videoDetections: List<Map<String, Any>>? = null) {
        if (results.isEmpty()) return

        fun uploadToFirebase(locationData: LocationData?) {
            fun saveToFirestore(imageUrl: String, videoUrl: String?) {
                firebaseService.saveDetectionToFirestore(imageUrl, results, locationData, videoUrl, videoDetections)
                firebaseService.saveDailyStats(results.size)
            }

            fun uploadImage(videoUrl: String?) {
                cloudinaryService.uploadImageWithBoxes(
                    bitmap = imageWithBoxes,
                    detections = results,
                    onSuccess = { imageUrl -> saveToFirestore(imageUrl, videoUrl) },
                    onFailure = { saveToFirestore("upload_failed", videoUrl) }
                )
            }

            if (originalVideoUri != null) {
                cloudinaryService.uploadVideo(
                    uri = originalVideoUri,
                    onSuccess = { videoUrl -> uploadImage(videoUrl) },
                    onFailure = { uploadImage(null) }
                )
            } else {
                uploadImage(null)
            }
        }

        if (isLocationPermissionGranted) {
            locationService.getCurrentLocation(
                onSuccess = { uploadToFirebase(it) },
                onFailure = {
                    locationService.getLastKnownLocation(
                        onSuccess = { uploadToFirebase(it) },
                        onFailure = { uploadToFirebase(null) }
                    )
                }
            )
        } else {
            uploadToFirebase(null)
        }
    }

    
    // Permissions
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    
    // Media picker (images + videos)
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val mime = context.contentResolver.getType(it) ?: ""
                if (mime.startsWith("video")) {
                    // Process video frames with Coroutines
                    isLoading = true
                    resultImage = null
                    predictions = emptyList()

                    CoroutineScope(Dispatchers.Default).launch {
                        val retriever = MediaMetadataRetriever()
                        var bestFrame: Bitmap? = null
                        var bestDetections: List<Detection> = emptyList()
                        val videoDetectionsTimeline = mutableListOf<Map<String, Any>>()
                        try {
                            retriever.setDataSource(context, it)
                            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                            val totalSamples = 10 // Reduced from 20 to 10 for better performance
                            val step = if (durationMs > 0) durationMs / totalSamples else 0L

                            repeat(totalSamples) { index ->
                                if (!isActive) return@launch // Cancel if coroutine is cancelled
                                
                                val t = step * index
                                val frame = retriever.getFrameAtTime(t * 1000, MediaMetadataRetriever.OPTION_CLOSEST)
                                
                                frame?.let { f ->
                                    val dets = tensorFlowHelper.predict(f)
                                    
                                    if (dets.isNotEmpty()) {
                                        videoDetectionsTimeline.add(
                                            mapOf(
                                                "timeMs" to t,
                                                "detections" to dets.map { d ->
                                                    mapOf(
                                                        "className" to d.className,
                                                        "confidence" to d.confidence,
                                                        "x" to d.x,
                                                        "y" to d.y,
                                                        "width" to d.width,
                                                        "height" to d.height
                                                    )
                                                }
                                            )
                                        )
                                        
                                        if (dets.size > bestDetections.size) {
                                            bestDetections = dets
                                            bestFrame = f
                                        }
                                    }
                                }
                            }

                            bestFrame?.let { frame ->
                                val imageWithBoxes = ImageUtils.drawBoundingBoxes(frame, bestDetections)
                                uploadDetections(
                                    imageWithBoxes,
                                    bestDetections,
                                    isLocationPermissionGranted = locationPermissionState.status.isGranted,
                                    originalVideoUri = it,
                                    videoDetections = videoDetectionsTimeline
                                )

                                withContext(Dispatchers.Main) {
                                    selectedImage = frame
                                    resultImage = imageWithBoxes
                                    predictions = bestDetections
                                    isLoading = false
                                }
                            } ?: withContext(Dispatchers.Main) {
                                isLoading = false
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) { isLoading = false }
                        } finally {
                            try { retriever.release() } catch (_: Exception) {}
                        }
                    }
                } else {
                    // Image flow
                    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                    selectedImage = bitmap
                    resultImage = null
                    predictions = emptyList()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
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

            // Camera Button (requires camera permission)
            Button(
                onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        // Camera functionality can be added here
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                },
                enabled = false,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBDBDBD)
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
                    CoroutineScope(Dispatchers.Default).launch {
                        val results = tensorFlowHelper.predict(bitmap)
                        val imageWithBoxes = ImageUtils.drawBoundingBoxes(bitmap, results)
                        uploadDetections(imageWithBoxes, results, isLocationPermissionGranted = locationPermissionState.status.isGranted)
                        
                        withContext(Dispatchers.Main) {
                            predictions = results
                            resultImage = imageWithBoxes
                            isLoading = false
                        }
                    }
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
                                        text = "Vị trí: ${String.format("%.1f", detection.x * 100)}%, ${String.format("%.1f", detection.y * 100)}%",
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