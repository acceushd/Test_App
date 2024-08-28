package com.example.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var countDownTextView: TextView
    private lateinit var countDownTimer: CountDownTimer
    private val pauseDurationMillis = 3 * 60 * 1000L
    private lateinit var marvelMovieTextView: TextView
    private lateinit var evenNumber: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        countDownTextView = findViewById(R.id.countDown)
        val targetHours =
            listOf("00:12", "00:30", "02:00", "02:30", "03:27", "03:40", "04:45", "05:00")
        lifecycleScope.launch {
            val movieTitle = returnMovieName()
            if (movieTitle != null) {
                marvelMovieTextView.text = movieTitle
                findViewById<TextView?>(R.id.DaysUntil).text =
                    String.format("Days until: %s", returnDays())
            } else {
                marvelMovieTextView.text = "Funkt net"
            }
            while (isActive) {
                withContext(Dispatchers.Main) {
                    findViewById<TextView?>(R.id.qoutes).text = getQuote()
                }
                delay(5000)
            }
        }
        lifecycleScope.launch {
            while (isActive) {
                findViewById<TextView>(R.id.ChuckNorris).text = getChuckNorrisQuote()
                delay(10000)
            }
        }
        startCountDown(targetHours)
        marvelMovieTextView = findViewById(R.id.Marvel)
        evenNumber = findViewById(R.id.NumberEven)
        val numberString = findViewById<EditText>(R.id.IsEvenNumber)
        numberString.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used in this case
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not used in this case
            }

            override fun afterTextChanged(s: Editable?) {
                lifecycleScope.launch {
                    val number = getNumber(numberString) // Assuming getNumber() extracts the number
                    evenNumber.text = getEvenOrOdd(number)
                }
            }
        })
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

    private fun getNumber(editText: EditText): Int {
        val numberString = editText.text.toString()
        return try {
            numberString.toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSION = 100
        private const val REQUEST_CODE_READ_MEDIA_IMAGES_PERMISSION = 101
    }
}