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
    private val modelPath = "best_float32.tflite"
    private val inputSize = 640

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
            Log.d("SimpleTensorFlow", "Starting prediction...")

            // Resize bitmap to input size
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

            // Prepare input buffer
            val inputBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
            inputBuffer.order(ByteOrder.nativeOrder())

            // Convert bitmap to float array
            val pixels = IntArray(inputSize * inputSize)
            resizedBitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

            for (pixel in pixels) {
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                inputBuffer.putFloat(r / 255.0f)
                inputBuffer.putFloat(g / 255.0f)
                inputBuffer.putFloat(b / 255.0f)
            }

            // Get model input and output info
            val inputTensor = interpreter.getInputTensor(0)
            val outputTensor = interpreter.getOutputTensor(0)

            Log.d("SimpleTensorFlow", "Input shape: ${inputTensor.shape().contentToString()}")
            Log.d("SimpleTensorFlow", "Output shape: ${outputTensor.shape().contentToString()}")

            // Prepare output buffer based on actual model output shape
            val outputShape = outputTensor.shape()
            val outputSize = outputShape.fold(1) { acc, dim -> acc * dim }
            val outputBuffer = ByteBuffer.allocateDirect(4 * outputSize)
            outputBuffer.order(ByteOrder.nativeOrder())

            // Run inference
            interpreter.run(inputBuffer, outputBuffer)

            // Convert output to float array
            outputBuffer.rewind()
            val outputArray = FloatArray(outputSize)
            for (i in 0 until outputSize) {
                outputArray[i] = outputBuffer.float
            }

            Log.d("SimpleTensorFlow", "Output array size: $outputSize")
            Log.d("SimpleTensorFlow", "First 10 output values: ${outputArray.take(10).joinToString()}")

            // Try to parse as different formats
            return parseOutput(outputArray, outputShape)

        } catch (e: Exception) {
            Log.e("SimpleTensorFlow", "Prediction failed: ${e.message}")
            e.printStackTrace()
            return emptyList()
        }
    }

    private fun parseOutput(output: FloatArray, shape: IntArray): List<Detection> {
        val detections = mutableListOf<Detection>()

        Log.d("SimpleTensorFlow", "Parsing output with shape: ${shape.contentToString()}")

        // YOLO output format: [batch, detections, 6] where 6 = [x, y, w, h, confidence, class_id]
        // or [batch, detections, 5] where 5 = [x, y, w, h, confidence] for single class

        when (shape.size) {
            3 -> {
                val batchSize = shape[0]
                val numDetections = shape[1]
                val numFeatures = shape[2]

                Log.d("SimpleTensorFlow", "3D output: batch=$batchSize, detections=$numDetections, features=$numFeatures")

                for (i in 0 until numDetections) {
                    val baseIndex = i * numFeatures
                    if (baseIndex + 4 < output.size) {
                        val x = output[baseIndex]
                        val y = output[baseIndex + 1]
                        val w = output[baseIndex + 2]
                        val h = output[baseIndex + 3]
                        val confidence = output[baseIndex + 4]

                        Log.d("SimpleTensorFlow", "Detection $i: x=$x, y=$y, w=$w, h=$h, conf=$confidence")

                        if (confidence > 0.3f) {
                            detections.add(
                                Detection(
                                    x = x,
                                    y = y,
                                    width = w,
                                    height = h,
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

                Log.d("SimpleTensorFlow", "2D output: detections=$numDetections, features=$numFeatures")

                for (i in 0 until numDetections) {
                    val baseIndex = i * numFeatures
                    if (baseIndex + 4 < output.size) {
                        val x = output[baseIndex]
                        val y = output[baseIndex + 1]
                        val w = output[baseIndex + 2]
                        val h = output[baseIndex + 3]
                        val confidence = output[baseIndex + 4]

                        if (confidence > 0.3f) {
                            detections.add(
                                Detection(
                                    x = x,
                                    y = y,
                                    width = w,
                                    height = h,
                                    confidence = confidence,
                                    className = "Ổ gà"
                                )
                            )
                        }
                    }
                }
            }
            else -> {
                Log.d("SimpleTensorFlow", "Unknown output shape, trying flat parsing")
                // Try to parse as flat array
                val numFeatures = 5 // Assume 5 features: x, y, w, h, confidence
                for (i in 0 until output.size step numFeatures) {
                    if (i + 4 < output.size) {
                        val x = output[i]
                        val y = output[i + 1]
                        val w = output[i + 2]
                        val h = output[i + 3]
                        val confidence = output[i + 4]

                        if (confidence > 0.3f) {
                            detections.add(
                                Detection(
                                    x = x,
                                    y = y,
                                    width = w,
                                    height = h,
                                    confidence = confidence,
                                    className = "Ổ gà"
                                )
                            )
                        }
                    }
                }
            }
        }

        Log.d("SimpleTensorFlow", "Found ${detections.size} detections")
        return detections
    }


    fun close() {
        interpreter?.close()
    }
}

