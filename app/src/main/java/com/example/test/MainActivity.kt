package com.example.test

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var countDownTextView: TextView
    private lateinit var countDownTimer: CountDownTimer
    private val pauseDurationMillis = 3 * 60 * 1000L
    private lateinit var cameraButton: FloatingActionButton
    private var latestTmpUri: Uri? = null
    private lateinit var picture: ImageView
    private lateinit var marvelMovieTextView: TextView

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                latestTmpUri?.let { uri ->
                    lifecycleScope.launch {
                        val savedImagePath = saveImageToInternalStorage(uri)
                        if (savedImagePath != null) {
                            withContext(Dispatchers.Main) {
                                picture.setImageBitmap(BitmapFactory.decodeFile(savedImagePath))
                                picture.visibility = ImageView.VISIBLE
                            }
                        }
                    }
                }
            }
        }

    // Launcher for selecting image from gallery
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                if (selectedImageUri != null) {
                    lifecycleScope.launch {
                        val savedImagePath = saveImageToInternalStorage(selectedImageUri)
                        if (savedImagePath != null) {
                            withContext(Dispatchers.Main) {
                                picture.setImageBitmap(BitmapFactory.decodeFile(savedImagePath))
                            }
                        }
                    }
                }
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        countDownTextView = findViewById(R.id.countDown)
        val targetHours =
            listOf("00:12", "00:30", "02:00", "02:30", "03:27", "03:40", "04:45", "05:00")
        lifecycleScope.launch {
            while (isActive) {
                val quote = getQuote()
                withContext(Dispatchers.Main) {
                    findViewById<TextView?>(R.id.qoutes).text = quote
                }
                delay(5000)
            }
        }
        startCountDown(targetHours)
        cameraButton = findViewById(R.id.cameraButton)
        picture = findViewById(R.id.picture)
        cameraButton.setOnClickListener {
            startCameraIntent()
        }
        marvelMovieTextView = findViewById(R.id.Marvel)
        lifecycleScope.launch {
            val movieTitle = returnMovieName()
            if (movieTitle != null) {
                marvelMovieTextView.text = movieTitle
                findViewById<TextView?>(R.id.DaysUntil).text = String.format("Days until: %s", returnDays())
            } else {
                marvelMovieTextView.text = "Funkt net"
            }
        }
    }

    private fun startCountDown(targetHours: List<String>) {
        val currentTimeMillis = System.currentTimeMillis()
        val nextFullHoursMillis = getNextTargetTimeMillis(currentTimeMillis, targetHours)
        val timeLeftInMillis = nextFullHoursMillis - currentTimeMillis

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            @SuppressLint("DefaultLocale", "SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                if (millisUntilFinished <= pauseDurationMillis) {
                    countDownTextView.text = "Pause" // Show "Pause" for the last 3 minutes
                } else {
                    val secondsRemaining = (millisUntilFinished / 1000) % 60
                    val minutesRemaining = (millisUntilFinished / (1000 * 60)) % 60
                    val hoursRemaining = (millisUntilFinished / (1000 * 60 * 60))
                    countDownTextView.text = String.format(
                        "%02d:%02d:%02d",
                        hoursRemaining,
                        minutesRemaining,
                        secondsRemaining
                    )
                }
            }

            override fun onFinish() {
                startCountDown(targetHours)
            }
        }.start()

    }

    private fun startCameraIntent() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_MEDIA_IMAGES // Use the new permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showImageSourceChooserDialog()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_CODE_CAMERA_PERMISSION
            )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), // Request the new permission
                REQUEST_CODE_READ_MEDIA_IMAGES_PERMISSION
            )
        }
    }

    private fun showImageSourceChooserDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Image Source")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> takeImage() // Open camera
                1 -> openImageGallery() // Open photo picker
                2 -> dialog.dismiss() // Cancel
            }
        }
        builder.show()
    }

    private fun rotateImage(source: Bitmap, angle: Float = 90f): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height, matrix, true
        )
    }

    private fun openImageGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    @Suppress("DEPRECATION")
    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takePictureLauncher.launch(uri)
            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "App"
        )
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val imageFile = File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
        return FileProvider.getUriForFile(
            applicationContext,
            "${applicationContext.packageName}.provider",
            imageFile
        )
    }

    private suspend fun saveImageToInternalStorage(imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val contentResolver = applicationContext.contentResolver
                val inputStream = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                val timeStamp =
                    SimpleDateFormat(
                        "yyyyMMdd_HHmmss",
                        Locale.getDefault()
                    ).format(
                        Date()
                    )
                val storageDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "App"
                )
                if (!storageDir.exists()) {
                    storageDir.mkdirs()
                }
                val outputFile = File(
                    storageDir,
                    "JPEG_${timeStamp}_.jpg"
                )

                // Rotate the bitmap by 90 degrees
                val rotatedBitmap = rotateImage(bitmap)

                val outputStream = FileOutputStream(outputFile)
                rotatedBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    outputStream
                ) // Save rotated bitmap
                outputStream.close()

                // Optionally, delete the temporary file
                contentResolver.delete(imageUri, null, null)
                outputFile.absolutePath // Return the path of the saved image
            } catch (e: Exception) {
                // Handle exceptions (e.g., log the error)
                null // Return null if saving fails
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takeImage()
                } else {
                    // Handle permission denied
                    Toast.makeText(
                        this,
                        "Camera permission is required to take photos.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            REQUEST_CODE_READ_MEDIA_IMAGES_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showImageSourceChooserDialog()
                } else {
                    Toast.makeText(
                        this,
                        "Media permission is required to select photos.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSION = 100
        private const val REQUEST_CODE_READ_MEDIA_IMAGES_PERMISSION = 101
    }
}