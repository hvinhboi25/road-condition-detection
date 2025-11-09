package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SimpleTensorFlowHelper(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val modelPath = "best_float16_train2.tflite"
    private val inputSize = 640
    
    // Pre-allocate buffers to avoid creating new ones every prediction
    private val inputBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3).apply {
        order(ByteOrder.nativeOrder())
    }
    private val pixels = IntArray(inputSize * inputSize)

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val model = FileUtil.loadMappedFile(context, modelPath)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(model, options)
            Log.d("SimpleTensorFlow", "Model loaded successfully")
        } catch (e: Exception) {
            Log.e("SimpleTensorFlow", "Failed to load model: ${e.message}")
            e.printStackTrace()
        }
    }

    fun predict(bitmap: Bitmap): List<Detection> {
        val interpreter = this.interpreter ?: return emptyList()

        try {
            // Reuse resized bitmap if possible, or use fast scaling
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false)
            
            // Reuse pre-allocated buffer
            inputBuffer.rewind()
            resizedBitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

            // Optimized pixel processing - single pass
            for (pixel in pixels) {
                inputBuffer.putFloat(((pixel shr 16) and 0xFF) * 0.00392157f) // /255.0f
                inputBuffer.putFloat(((pixel shr 8) and 0xFF) * 0.00392157f)
                inputBuffer.putFloat((pixel and 0xFF) * 0.00392157f)
            }

            val outputTensor = interpreter.getOutputTensor(0)
            val outputShape = outputTensor.shape()
            val outputSize = outputShape.fold(1) { acc, dim -> acc * dim }
            val outputBuffer = ByteBuffer.allocateDirect(4 * outputSize).apply {
                order(ByteOrder.nativeOrder())
            }

            inputBuffer.rewind()
            interpreter.run(inputBuffer, outputBuffer)

            // More efficient array conversion
            outputBuffer.rewind()
            val outputArray = FloatArray(outputSize)
            outputBuffer.asFloatBuffer().get(outputArray)

            return parseOutput(outputArray, outputShape)

        } catch (e: Exception) {
            Log.e("SimpleTensorFlow", "Prediction failed: ${e.message}")
            return emptyList()
        }
    }

    private fun parseOutput(output: FloatArray, shape: IntArray): List<Detection> {
        val detections = mutableListOf<Detection>()

        when (shape.size) {
            3 -> {
                val numDetections = shape[1]
                val numFeatures = shape[2]

                for (i in 0 until numDetections) {
                    val baseIndex = i * numFeatures
                    if (baseIndex + 4 < output.size) {
                        val confidence = output[baseIndex + 4]
                        if (confidence > 0.3f) {
                            detections.add(
                                Detection(
                                    x = output[baseIndex],
                                    y = output[baseIndex + 1],
                                    width = output[baseIndex + 2],
                                    height = output[baseIndex + 3],
                                    confidence = confidence,
                                    className = "Ổ gà"
                                )
                            )
                        }
                    }
                }
            }
            2 -> {
                val numDetections = shape[0]
                val numFeatures = shape[1]

                for (i in 0 until numDetections) {
                    val baseIndex = i * numFeatures
                    if (baseIndex + 4 < output.size) {
                        val confidence = output[baseIndex + 4]
                        if (confidence > 0.3f) {
                            detections.add(
                                Detection(
                                    x = output[baseIndex],
                                    y = output[baseIndex + 1],
                                    width = output[baseIndex + 2],
                                    height = output[baseIndex + 3],
                                    confidence = confidence,
                                    className = "Ổ gà"
                                )
                            )
                        }
                    }
                }
            }
            else -> {
                val numFeatures = 5
                for (i in 0 until output.size step numFeatures) {
                    if (i + 4 < output.size) {
                        val confidence = output[i + 4]
                        if (confidence > 0.3f) {
                            detections.add(
                                Detection(
                                    x = output[i],
                                    y = output[i + 1],
                                    width = output[i + 2],
                                    height = output[i + 3],
                                    confidence = confidence,
                                    className = "Ổ gà"
                                )
                            )
                        }
                    }
                }
            }
        }

        return detections
    }


    fun close() {
        interpreter?.close()
        interpreter = null
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

