package com.example.myapplication

import android.graphics.*
import kotlin.math.min

object ImageUtils {

    /**
     * VẼ BOUNDING BOX CHO PHÁT HIỆN Ổ GÀ
     * 
     * LOGIC ĐƠN GIẢN:
     * - Model YOLO output format: [x_center, y_center, width, height] (normalized 0-1)
     * - Convert sang corner coordinates và scale lên ảnh gốc
     * - Vẽ box màu đỏ lên ảnh
     */
    fun drawBoundingBoxes(
        bitmap: Bitmap,
        detections: List<Detection>,
        inputSize: Int = 640
    ): Bitmap {
        // Copy ảnh để vẽ lên
        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)

        // Style cho box
        val boxPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        
        // Style cho text
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 36f
            isAntiAlias = true
        }
        
        // Style cho background của text
        val bgPaint = Paint().apply {
            color = Color.BLACK
            alpha = 160
        }

        android.util.Log.d("ImageUtils", "====================")
        android.util.Log.d("ImageUtils", "VẼ BOUNDING BOX")
        android.util.Log.d("ImageUtils", "Ảnh: ${bitmap.width}x${bitmap.height}")
        android.util.Log.d("ImageUtils", "Số ổ gà: ${detections.size}")

        // Vẽ từng box
        detections.forEachIndexed { index, det ->
            android.util.Log.d("ImageUtils", "\n--- Ổ gà #${index + 1} ---")
            android.util.Log.d("ImageUtils", "RAW: x=${det.x}, y=${det.y}, w=${det.width}, h=${det.height}, conf=${det.confidence}")
            
            // QUAN TRỌNG: Model output là XYXY format (x1, y1, x2, y2) - NORMALIZED (0-1)
            // KHÔNG PHẢI center format như YOLO thông thường!
            val x1 = det.x          // Góc trái (normalized)
            val y1 = det.y          // Góc trên (normalized)
            val x2 = det.width      // Góc phải (normalized) - lưu ý: field tên là "width" nhưng thực ra là x2!
            val y2 = det.height     // Góc dưới (normalized) - lưu ý: field tên là "height" nhưng thực ra là y2!
            
            android.util.Log.d("ImageUtils", "XYXY normalized: x1=$x1, y1=$y1, x2=$x2, y2=$y2")
            
            // Scale trực tiếp lên kích thước ảnh thực
            val left = x1 * bitmap.width
            val top = y1 * bitmap.height
            val right = x2 * bitmap.width
            val bottom = y2 * bitmap.height
            
            android.util.Log.d("ImageUtils", "Box: [${left.toInt()}, ${top.toInt()}, ${right.toInt()}, ${bottom.toInt()}]")
            android.util.Log.d("ImageUtils", "Size: ${(right - left).toInt()} x ${(bottom - top).toInt()} px")
            
            // BƯỚC 4: Clamp để không vượt khung ảnh
            val finalLeft = left.coerceIn(0f, bitmap.width.toFloat())
            val finalTop = top.coerceIn(0f, bitmap.height.toFloat())
            val finalRight = right.coerceIn(0f, bitmap.width.toFloat())
            val finalBottom = bottom.coerceIn(0f, bitmap.height.toFloat())
            
            // BƯỚC 5: Vẽ box
            if (finalRight > finalLeft && finalBottom > finalTop) {
                val rect = RectF(finalLeft, finalTop, finalRight, finalBottom)
                canvas.drawRect(rect, boxPaint)
                
                // Vẽ label
                val label = "${det.className} ${(det.confidence * 100).toInt()}%"
                val textBounds = Rect()
                textPaint.getTextBounds(label, 0, label.length, textBounds)
                
                val labelBg = RectF(
                    finalLeft,
                    (finalTop - textBounds.height() - 16f).coerceAtLeast(0f),
                    finalLeft + textBounds.width() + 16f,
                    finalTop
                )
                canvas.drawRect(labelBg, bgPaint)
                canvas.drawText(label, finalLeft + 8f, finalTop - 8f, textPaint)
                
                android.util.Log.d("ImageUtils", "✓ Vẽ thành công")
            } else {
                android.util.Log.e("ImageUtils", "✗ Box không hợp lệ")
            }
        }
        
        android.util.Log.d("ImageUtils", "\n====================\n")
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
