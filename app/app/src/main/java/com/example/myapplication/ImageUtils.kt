package com.example.myapplication

import android.graphics.*
import kotlin.math.min

object ImageUtils {
    
    // Cache Paint objects to avoid recreation
    private val boxPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }
    
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        isAntiAlias = true
    }
    
    private val bgPaint = Paint().apply {
        color = Color.BLACK
        alpha = 160
    }
    
    private val textBounds = Rect() // Reuse for all text measurements

    fun drawBoundingBoxes(
        bitmap: Bitmap,
        detections: List<Detection>,
        inputSize: Int = 640
    ): Bitmap {
        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)

        detections.forEach { det ->
            // Model output: XYXY format (x1, y1, x2, y2) - normalized (0-1)
            val left = (det.x * bitmap.width).coerceIn(0f, bitmap.width.toFloat())
            val top = (det.y * bitmap.height).coerceIn(0f, bitmap.height.toFloat())
            val right = (det.width * bitmap.width).coerceIn(0f, bitmap.width.toFloat())
            val bottom = (det.height * bitmap.height).coerceIn(0f, bitmap.height.toFloat())
            
            if (right > left && bottom > top) {
                canvas.drawRect(left, top, right, bottom, boxPaint)
                
                val label = "${det.className} ${(det.confidence * 100).toInt()}%"
                textPaint.getTextBounds(label, 0, label.length, textBounds)
                
                val labelTop = (top - textBounds.height() - 16f).coerceAtLeast(0f)
                canvas.drawRect(left, labelTop, left + textBounds.width() + 16f, top, bgPaint)
                canvas.drawText(label, left + 8f, top - 8f, textPaint)
            }
        }
        
        return output
    }
    
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val scaleWidth = maxWidth.toFloat() / width
        val scaleHeight = maxHeight.toFloat() / height
        val scale = min(scaleWidth, scaleHeight)
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
