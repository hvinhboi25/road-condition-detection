package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.compose.ui.text.font.FontWeight

class CameraActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        setContent {
            MyApplicationTheme {
                CameraScreen(
                    onDetectionComplete = { bitmap, detections ->
                        // Tự động return về MainActivity với kết quả
                        finishWithResult(bitmap, detections)
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }
    
    private fun finishWithResult(bitmap: Bitmap, detections: List<Detection>) {
        // Lưu bitmap tạm (có thể dùng cache hoặc singleton)
        CapturedImageHolder.bitmap = bitmap
        CapturedImageHolder.detections = detections
        
        val intent = Intent()
        intent.putExtra("has_detection", true)
        setResult(RESULT_OK, intent)
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

// Holder để giữ bitmap tạm giữa activities
object CapturedImageHolder {
    var bitmap: Bitmap? = null
    var detections: List<Detection>? = null
}

@Composable
fun CameraScreen(
    onDetectionComplete: (Bitmap, List<Detection>) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val tensorFlowHelper = remember { SimpleTensorFlowHelper(context) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    
    var detectionStatus by remember { mutableStateOf("🔍 Đang quét...") }
    var isProcessing by remember { mutableStateOf(false) }
    var detectionCount by remember { mutableStateOf(0) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    
    // Threshold để tự động chụp
    val CONFIDENCE_THRESHOLD = 0.6f
    val MIN_DETECTIONS = 1
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    previewView = this
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        
                        // Preview use case
                        val preview = Preview.Builder()
                            .build()
                            .also {
                                it.setSurfaceProvider(surfaceProvider)
                            }
                        
                        // Image analysis use case
                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                    processImageProxy(
                                        imageProxy = imageProxy,
                                        tensorFlowHelper = tensorFlowHelper,
                                        isProcessing = isProcessing,
                                        onStatusUpdate = { status, count ->
                                            detectionStatus = status
                                            detectionCount = count
                                        },
                                        onDetection = { bitmap, detections ->
                                            if (!isProcessing && detections.isNotEmpty()) {
                                                val highConfidenceDetections = detections.filter { 
                                                    it.confidence >= CONFIDENCE_THRESHOLD 
                                                }
                                                
                                                if (highConfidenceDetections.size >= MIN_DETECTIONS) {
                                                    isProcessing = true
                                                    detectionStatus = "✅ Phát hiện ${highConfidenceDetections.size} ổ gà!"
                                                    
                                                    // Vẽ bounding box
                                                    val resultBitmap = ImageUtils.drawBoundingBoxes(
                                                        bitmap, 
                                                        highConfidenceDetections
                                                    )
                                                    
                                                    // Delay 500ms để user thấy status rồi mới return
                                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                        onDetectionComplete(resultBitmap, highConfidenceDetections)
                                                    }, 500)
                                                }
                                            }
                                        }
                                    )
                                    imageProxy.close()
                                }
                            }
                        
                        // Camera selector
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalyzer
                            )
                        } catch (exc: Exception) {
                            Log.e("CameraActivity", "Use case binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xCC000000)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = detectionStatus,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (detectionCount > 0) {
                        Text(
                            text = "Đã phát hiện: $detectionCount vị trí",
                            color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Text(
                        text = "💡 Hướng camera vào ổ gà trên đường",
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            
            // Bottom button
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFFEF4444)
                )
            ) {
                Text(
                    text = "❌ Hủy",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Loading indicator khi đang xử lý
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xEE000000)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = androidx.compose.ui.graphics.Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Đang xử lý...",
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

private fun processImageProxy(
    imageProxy: ImageProxy,
    tensorFlowHelper: SimpleTensorFlowHelper,
    isProcessing: Boolean,
    onStatusUpdate: (String, Int) -> Unit,
    onDetection: (Bitmap, List<Detection>) -> Unit
) {
    if (isProcessing) return
    
    try {
        // Convert ImageProxy to Bitmap
        val bitmap = imageProxy.toBitmap()
        
        // Run detection
        val detections = tensorFlowHelper.predict(bitmap)
        
        // Update status
        if (detections.isNotEmpty()) {
            onStatusUpdate("🎯 Phát hiện ${detections.size} đối tượng", detections.size)
            onDetection(bitmap, detections)
        } else {
            onStatusUpdate("🔍 Đang quét...", 0)
        }
    } catch (e: Exception) {
        Log.e("CameraActivity", "Error processing image", e)
        onStatusUpdate("⚠️ Lỗi xử lý: ${e.message}", 0)
    }
}

