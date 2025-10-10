package com.example.myapplication

import android.graphics.*
import android.graphics.Paint.Align
import kotlin.math.max
import kotlin.math.min

object ImageUtils {

    fun drawBoundingBoxes(
        bitmap: Bitmap,
        detections: List<Detection>,
        inputSize: Int = 640,
        assumeXYXY: Boolean = false      // nếu bạn chắc output là xyxy thì set = true
    ): Bitmap {
        val out = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)

        val boxPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 34f
            isAntiAlias = true
        }
        val bgPaint = Paint().apply {
            color = Color.BLACK
            alpha = 140
        }

        // scale từ "không gian model 640x640" → "không gian ảnh gốc"
        val sx = bitmap.width  / inputSize.toFloat()
        val sy = bitmap.height / inputSize.toFloat()
        
        android.util.Log.d("ImageUtils", "Bitmap size: ${bitmap.width}x${bitmap.height}")
        android.util.Log.d("ImageUtils", "Input size: $inputSize")
        android.util.Log.d("ImageUtils", "Scale factors: sx=$sx, sy=$sy")

        detections.forEach { det ->
            // 1) Lấy ra xywh/xyxy gốc từ model
            var cx = det.x
            var cy = det.y
            var w  = det.width
            var h  = det.height
            
            android.util.Log.d("ImageUtils", "Original detection: cx=$cx, cy=$cy, w=$w, h=$h, conf=${det.confidence}")

            // 2) Convert normalized coordinates (0-1) directly to image coordinates
            val left   = (cx * bitmap.width).coerceIn(0f, bitmap.width.toFloat())
            val top    = (cy * bitmap.height).coerceIn(0f, bitmap.height.toFloat())
            val right  = ((cx + w) * bitmap.width).coerceIn(0f, bitmap.width.toFloat())
            val bottom = ((cy + h) * bitmap.height).coerceIn(0f, bitmap.height.toFloat())

            android.util.Log.d("ImageUtils", "Final coordinates: left=$left, top=$top, right=$right, bottom=$bottom")
            
            if (right > left && bottom > top) {
                val r = RectF(left, top, right, bottom)
                canvas.drawRect(r, boxPaint)

                val label = "${det.className} ${(det.confidence * 100).coerceIn(0f,100f).let { String.format("%.1f", it) }}%"
                val tb = Rect()
                textPaint.getTextBounds(label, 0, label.length, tb)
                val bg = RectF(r.left, (r.top - tb.height() - 8f).coerceAtLeast(0f), r.left + tb.width() + 16f, r.top)
                canvas.drawRect(bg, bgPaint)
                canvas.drawText(label, bg.left + 8f, r.top - 8f, textPaint)
            }
        }
        return out
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

