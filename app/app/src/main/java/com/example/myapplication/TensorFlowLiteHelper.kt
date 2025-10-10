package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TensorFlowLiteHelper(private val context: Context) {
    
    private var interpreter: Interpreter? = null
    private val modelPath = "best_float16.tflite"
    
    // Model input/output specifications (adjust based on your model)
    private val inputSize = 640 // YOLO models typically use 640x640
    private val numClasses = 1 // Only pothole detection
    private val numDetections = 25200 // Typical for YOLO models
    
    // Image processor for preprocessing
    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
        .build()
    
    init {
        loadModel()
    }
    
    private fun loadModel() {
        try {
            val model = FileUtil.loadMappedFile(context, modelPath)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                setUseNNAPI(true)
            }
            interpreter = Interpreter(model, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun predict(bitmap: Bitmap): List<Detection> {
        val interpreter = this.interpreter ?: return emptyList()
        
        try {
            android.util.Log.d("TensorFlowLite", "Starting prediction...")
            
            // Preprocess image
            val tensorImage = TensorImage.fromBitmap(bitmap)
            val processedImage = imageProcessor.process(tensorImage)
            
            // Prepare input buffer
            val inputBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
            inputBuffer.order(ByteOrder.nativeOrder())
            inputBuffer.rewind()
            
            // Convert processed image to input buffer
            val pixels = IntArray(inputSize * inputSize)
            processedImage.bitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
            
            for (pixel in pixels) {
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                
                inputBuffer.putFloat((r / 255.0f))
                inputBuffer.putFloat((g / 255.0f))
                inputBuffer.putFloat((b / 255.0f))
            }
            
            // Try different output formats
            val outputFormats = listOf(
                intArrayOf(1, numDetections, 6), // [batch, detections, 6]
                intArrayOf(1, numDetections, 5), // [batch, detections, 5] - no class prob
                intArrayOf(1, 25200, 85), // Standard YOLO format
                intArrayOf(1, 25200, 5)  // YOLO without class probabilities
            )
            
            for (format in outputFormats) {
                try {
                    android.util.Log.d("TensorFlowLite", "Trying output format: ${format.contentToString()}")
                    
                    val outputBuffer = TensorBuffer.createFixedSize(
                        format,
                        org.tensorflow.lite.DataType.FLOAT32
                    )
                    
                    // Run inference
                    interpreter.run(inputBuffer, outputBuffer.buffer.rewind())
                    
                    val results = parseDetections(outputBuffer.floatArray, format[2])
                    if (results.isNotEmpty()) {
                        android.util.Log.d("TensorFlowLite", "Found ${results.size} detections with format ${format.contentToString()}")
                        return results
                    }
                } catch (e: Exception) {
                    android.util.Log.e("TensorFlowLite", "Failed with format ${format.contentToString()}: ${e.message}")
                }
            }
            
            android.util.Log.d("TensorFlowLite", "No detections found with any format")
            return emptyList()
            
        } catch (e: Exception) {
            android.util.Log.e("TensorFlowLite", "Prediction failed: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }
    
    private fun parseDetections(output: FloatArray, numChannels: Int = 6): List<Detection> {
        val detections = mutableListOf<Detection>()
        
        val confidenceThreshold = 0.1f // Very low threshold for testing
        val nmsThreshold = 0.4f
        
        android.util.Log.d("TensorFlowLite", "Parsing detections with $numChannels channels, output size: ${output.size}")
        
        val detectionsCount = output.size / numChannels
        android.util.Log.d("TensorFlowLite", "Expected detections count: $detectionsCount")
        
        // Parse different output formats
        for (i in 0 until detectionsCount) {
            val baseIndex = i * numChannels
            
            if (baseIndex + numChannels - 1 < output.size) {
                val x = output[baseIndex]
                val y = output[baseIndex + 1]
                val w = output[baseIndex + 2]
                val h = output[baseIndex + 3]
                val confidence = output[baseIndex + 4]
                
                val finalConfidence = if (numChannels >= 6) {
                    val classProb = output[baseIndex + 5]
                    confidence * classProb
                } else {
                    confidence
                }
                
                // Log first few detections for debugging
                if (i < 5) {
                    android.util.Log.d("TensorFlowLite", "Detection $i: x=$x, y=$y, w=$w, h=$h, conf=$confidence, final=$finalConfidence")
                }
                
                if (finalConfidence > confidenceThreshold) {
                    detections.add(
                        Detection(
                            x = x,
                            y = y,
                            width = w,
                            height = h,
                            confidence = finalConfidence,
                            className = "Ổ gà"
                        )
                    )
                }
            }
        }
        
        android.util.Log.d("TensorFlowLite", "Found ${detections.size} detections above threshold")
        
        // Apply Non-Maximum Suppression
        return applyNMS(detections, nmsThreshold)
    }
    
    private fun applyNMS(detections: List<Detection>, threshold: Float): List<Detection> {
        val sortedDetections = detections.sortedByDescending { it.confidence }
        val selected = mutableListOf<Detection>()
        
        for (detection in sortedDetections) {
            var shouldAdd = true
            
            for (selectedDetection in selected) {
                val iou = calculateIoU(detection, selectedDetection)
                if (iou > threshold) {
                    shouldAdd = false
                    break
                }
            }
            
            if (shouldAdd) {
                selected.add(detection)
            }
        }
        
        return selected
    }
    
    private fun calculateIoU(detection1: Detection, detection2: Detection): Float {
        val x1 = maxOf(detection1.x - detection1.width / 2, detection2.x - detection2.width / 2)
        val y1 = maxOf(detection1.y - detection1.height / 2, detection2.y - detection2.height / 2)
        val x2 = minOf(detection1.x + detection1.width / 2, detection2.x + detection2.width / 2)
        val y2 = minOf(detection1.y + detection1.height / 2, detection2.y + detection2.height / 2)
        
        val intersectionArea = maxOf(0f, x2 - x1) * maxOf(0f, y2 - y1)
        val area1 = detection1.width * detection1.height
        val area2 = detection2.width * detection2.height
        val unionArea = area1 + area2 - intersectionArea
        
        return if (unionArea > 0) intersectionArea / unionArea else 0f
    }
    
    fun close() {
        interpreter?.close()
    }
}

data class Detection(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val confidence: Float,
    val className: String
)
