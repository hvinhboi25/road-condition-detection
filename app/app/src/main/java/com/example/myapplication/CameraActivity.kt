package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException

class CameraActivity : ComponentActivity() {
    
    private lateinit var tensorFlowHelper: TensorFlowLiteHelper
    
    // Camera launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Handle camera result if needed
            Log.d("CameraActivity", "Picture taken successfully")
        }
    }
    
    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                processImage(bitmap)
            } catch (e: IOException) {
                Log.e("CameraActivity", "Error loading image", e)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize TensorFlow helper
        tensorFlowHelper = TensorFlowLiteHelper(this)
        
        // Check permissions
        if (allPermissionsGranted()) {
            showOptions()
        } else {
            requestPermissions()
        }
    }
    
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
        )
    }
    
    private fun showOptions() {
        // For now, just open image picker
        // You can add a dialog here to choose between camera and gallery
        imagePickerLauncher.launch("image/*")
    }
    
    private fun processImage(bitmap: Bitmap) {
        // Run prediction in background thread
        Thread {
            val detections = tensorFlowHelper.predict(bitmap)
            
            // Log results
            if (detections.isNotEmpty()) {
                Log.d("CameraActivity", "Found ${detections.size} objects:")
                detections.forEach { detection ->
                    Log.d("CameraActivity", 
                        "${detection.className}: ${String.format("%.2f", detection.confidence * 100)}%")
                }
            } else {
                Log.d("CameraActivity", "No objects detected")
            }
        }.start()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                showOptions()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        tensorFlowHelper.close()
    }
    
    companion object {
        private const val TAG = "CameraActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
}