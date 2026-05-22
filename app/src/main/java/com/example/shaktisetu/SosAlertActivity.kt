package com.example.shaktisetu

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.SmsManager
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shaktisetu.database.AppDatabase
import kotlinx.coroutines.launch
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

class SosAlertActivity :
    AppCompatActivity(),
    SensorEventListener {

    // Audio
    private var mediaPlayer: MediaPlayer? = null

    private var vibrator: Vibrator? = null

    // Countdown
    private var countDownTimer:
            CountDownTimer? = null

    // Recording
    private var mediaRecorder:
            MediaRecorder? = null

    private var isRecording = false

    private var audioFilePath = ""

    // Camera
    private var imageCapture:
            ImageCapture? = null

    // Location
    private var locationManager:
            LocationManager? = null

    private var currentLatitude = 0.0

    private var currentLongitude = 0.0

    // Shake Detection
    private var sensorManager:
            SensorManager? = null

    private var acceleration = 0f

    private var currentAcceleration = 0f

    private var lastAcceleration = 0f

    private var shakeTriggered = false

    // State
    private var isMuted = false

    private var sosActive = false

    // User
    private var userEmail = ""

    private var userPin = ""

    // SMS Loop
    private var locationHandler:
            Handler? = null

    private var locationRunnable:
            Runnable? = null

    // Cached Contacts
    private var cachedContacts:
            List<String> = emptyList()

    companion object {

        private const val
                PERMISSION_REQUEST_CODE = 100

        private const val
                SHAKE_THRESHOLD = 25f

        private const val
                LOCATION_SMS_INTERVAL =
            60000L
    }

    // PIN Launcher
    private val dismissLauncher =

        registerForActivityResult(

            ActivityResultContracts
                .StartActivityForResult()

        ) { result ->

            if (
                result.resultCode ==
                RESULT_OK
            ) {

                dismissEmergency()
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_sos_alert
        )

        val tvCountdown =
            findViewById<TextView>(
                R.id.tvCountdown
            )

        val btnMuteSiren =
            findViewById<LinearLayout>(
                R.id.btnMuteSiren
            )

        val btnDismiss =
            findViewById<TextView>(
                R.id.btnDismiss
            )

        // User Email
        val sharedPref =
            getSharedPreferences(
                "ShaktiSetuPrefs",
                MODE_PRIVATE
            )

        userEmail =
            intent.getStringExtra(
                "user_email"
            ) ?: sharedPref.getString(
                "user_email",
                ""
            ) ?: ""

        // Prefetched Location
        val passedLat =
            intent.getDoubleExtra(
                "latitude",
                0.0
            )

        val passedLng =
            intent.getDoubleExtra(
                "longitude",
                0.0
            )

        if (
            passedLat != 0.0 &&
            passedLng != 0.0
        ) {

            currentLatitude =
                passedLat

            currentLongitude =
                passedLng
        }

        fetchUserPin()

        cacheEmergencyContacts()

        requestAllPermissions()

        getCurrentLocation()

        startSiren()

        startVibration()

        setupCamera()

        startAudioRecording()

        setupShakeDetection()

        // Back Press
        onBackPressedDispatcher
            .addCallback(

                this,

                object :
                    OnBackPressedCallback(true) {

                    override fun
                            handleOnBackPressed() {

                        if (sosActive) {

                            Toast.makeText(
                                this@SosAlertActivity,
                                "🔒 Enter PIN to dismiss SOS",
                                Toast.LENGTH_SHORT
                            ).show()

                            showPinDialog()

                        } else {

                            finish()
                        }
                    }
                }
            )

        // Countdown
        countDownTimer =

            object :
                CountDownTimer(
                    10000,
                    1000
                ) {

                override fun onTick(
                    millisUntilFinished: Long
                ) {

                    val secondsLeft =
                        (
                                millisUntilFinished
                                        / 1000
                                ).toInt()

                    tvCountdown.text =
                        secondsLeft.toString()
                }

                override fun onFinish() {

                    activateSOS()
                }
            }.start()

        // Mute
        btnMuteSiren.setOnClickListener {

            if (isMuted) {

                startSiren()

                startVibration()

                isMuted = false

                Toast.makeText(
                    this,
                    "🔊 Siren ON",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                stopSiren()

                stopVibration()

                isMuted = true

                Toast.makeText(
                    this,
                    "🔇 Siren Muted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Dismiss
        btnDismiss.setOnClickListener {

            if (sosActive) {

                showPinDialog()

            } else {

                dismissEmergency()
            }
        }
    }

    // =========================
    // ACTIVATE SOS
    // =========================

    private fun activateSOS() {

        sosActive = true

        Toast.makeText(
            this,
            "🆘 SOS ACTIVATED",
            Toast.LENGTH_LONG
        ).show()

        capturePhoto()

        sendLocationSMS()

        startPeriodicLocationSMS()
    }

    // =========================
    // PERIODIC SMS
    // =========================

    private fun startPeriodicLocationSMS() {

        locationHandler =
            Handler(
                Looper.getMainLooper()
            )

        locationRunnable =
            object : Runnable {

                override fun run() {

                    if (sosActive) {

                        refreshLocation()

                        sendLocationSMS()

                        locationHandler
                            ?.postDelayed(
                                this,
                                LOCATION_SMS_INTERVAL
                            )
                    }
                }
            }

        locationHandler?.postDelayed(
            locationRunnable!!,
            LOCATION_SMS_INTERVAL
        )
    }

    private fun stopPeriodicLocationSMS() {

        locationRunnable?.let {

            locationHandler
                ?.removeCallbacks(it)
        }

        locationHandler = null

        locationRunnable = null
    }

    // =========================
    // SEND SMS
    // =========================

    private fun sendLocationSMS() {
        lifecycleScope.launch {
            try {
                // Ensure contacts are loaded from DB if not already cached
                if (cachedContacts.isEmpty()) {
                    val db = AppDatabase.getDatabase(this@SosAlertActivity)
                    val contacts = db.emergencyContactDao().getAllContacts()
                    cachedContacts = contacts.map { it.contact_phone }
                }

                if (cachedContacts.isEmpty()) {
                    runOnUiThread {
                        Toast.makeText(this@SosAlertActivity, "⚠️ No emergency contacts found!", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                if (ContextCompat.checkSelfPermission(this@SosAlertActivity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    runOnUiThread {
                        Toast.makeText(this@SosAlertActivity, "❌ SMS Permission missing!", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val locationLink = if (currentLatitude != 0.0 && currentLongitude != 0.0) {
                    "https://maps.google.com/?q=$currentLatitude,$currentLongitude"
                } else {
                    "Location unavailable"
                }

                val message = """
🆘 EMERGENCY! I need help!

📍 Location:
$locationLink

🕐 Time:
${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())}

- ShaktiSetu SOS
                """.trimIndent()

                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                var sentCount = 0
                for (contact in cachedContacts) {
                    if (contact.isBlank()) continue
                    
                    // Format number: remove spaces
                    val cleanedContact = contact.replace(Regex("[^0-9+]"), "")
                    
                    // If it's a 10-digit number without a country code, you might want to add a default one or just send as is.
                    // However, to be robust, we should probably keep it as it was if it's already got a country code or is not 10 digits.
                    val formattedContact = if (cleanedContact.length == 10 && !cleanedContact.startsWith("+")) {
                        // Optional: you could still add +91 if you are sure about the locale, but better to let user handle it or be smart.
                        // For now, I'll keep the logic but make it clearer.
                        "+91$cleanedContact" 
                    } else {
                        cleanedContact
                    }

                    try {
                        val parts = smsManager.divideMessage(message)
                        smsManager.sendMultipartTextMessage(formattedContact, null, parts, null, null)
                        sentCount++
                        Log.d("SOS_SMS", "Sent to: $formattedContact")
                    } catch (e: Exception) {
                        Log.e("SOS_SMS", "Failed to send to $formattedContact: ${e.message}")
                    }
                }

                if (sentCount > 0) {
                    runOnUiThread {
                        Toast.makeText(this@SosAlertActivity, "📱 SOS Sent to $sentCount contacts", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // =========================
    // CACHE CONTACTS
    // =========================

    private fun cacheEmergencyContacts() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@SosAlertActivity)
                val contacts = db.emergencyContactDao().getAllContacts()
                cachedContacts = contacts.map { it.contact_phone }
                
                if (cachedContacts.isEmpty()) {
                    Toast.makeText(
                        this@SosAlertActivity,
                        "⚠️ No emergency contacts found!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // =========================
    // FETCH PIN
    // =========================

    private fun fetchUserPin() {

        val sharedPref =
            getSharedPreferences(
                "ShaktiSetuPrefs",
                MODE_PRIVATE
            )

        userPin =
            sharedPref.getString(
                "user_pin",
                ""
            ) ?: ""
    }

    // =========================
    // PIN SCREEN
    // =========================

    private fun showPinDialog() {

        val intent = Intent(
            this,
            DismissPinActivity::class.java
        )

        dismissLauncher.launch(intent)
    }

    // =========================
    // PERMISSIONS
    // =========================

    private fun requestAllPermissions() {

        val permissions = arrayOf(

            Manifest.permission.SEND_SMS,

            Manifest.permission.ACCESS_FINE_LOCATION,

            Manifest.permission.ACCESS_COARSE_LOCATION,

            Manifest.permission.CAMERA,

            Manifest.permission.RECORD_AUDIO,

            Manifest.permission.VIBRATE
        )

        val notGranted = permissions.filter {

            ContextCompat.checkSelfPermission(
                this,
                it
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {

            ActivityCompat.requestPermissions(
                this,
                notGranted.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    // =========================
    // LOCATION
    // =========================

    private fun getCurrentLocation() {

        try {

            locationManager =
                getSystemService(
                    Context.LOCATION_SERVICE
                ) as LocationManager

            if (

                ContextCompat
                    .checkSelfPermission(
                        this,
                        Manifest.permission
                            .ACCESS_FINE_LOCATION
                    )

                != PackageManager.PERMISSION_GRANTED
            ) return

            val fusedClient =
                LocationServices
                    .getFusedLocationProviderClient(this)

            fusedClient.lastLocation
                .addOnSuccessListener { location ->

                    if (location != null) {

                        currentLatitude =
                            location.latitude

                        currentLongitude =
                            location.longitude
                    }
                }

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    private fun refreshLocation() {

        getCurrentLocation()
    }

    // =========================
    // SIREN
    // =========================

    private fun startSiren() {

        try {

            if (mediaPlayer == null) {

                val alarmUri =

                    RingtoneManager
                        .getDefaultUri(
                            RingtoneManager.TYPE_ALARM
                        )

                mediaPlayer =
                    MediaPlayer.create(
                        this,
                        alarmUri
                    )

                mediaPlayer?.isLooping =
                    true
            }

            mediaPlayer?.start()

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    private fun stopSiren() {

        try {

            mediaPlayer?.stop()

            mediaPlayer?.release()

            mediaPlayer = null

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    // =========================
    // VIBRATION
    // =========================

    private fun startVibration() {

        try {

            vibrator =
                getSystemService(
                    Context.VIBRATOR_SERVICE
                ) as Vibrator

            val pattern =
                longArrayOf(
                    0,
                    500,
                    200,
                    500
                )

            vibrator?.vibrate(

                VibrationEffect
                    .createWaveform(
                        pattern,
                        0
                    )
            )

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    private fun stopVibration() {

        vibrator?.cancel()
    }

    // =========================
    // CAMERA
    // =========================

    private fun setupCamera() {

        try {

            val future =
                ProcessCameraProvider
                    .getInstance(this)

            future.addListener({

                try {

                    val provider =
                        future.get()

                    imageCapture =
                        ImageCapture
                            .Builder()
                            .build()

                    provider.unbindAll()

                    provider.bindToLifecycle(
                        this,
                        CameraSelector
                            .DEFAULT_FRONT_CAMERA,
                        imageCapture
                    )

                } catch (e: Exception) {

                    e.printStackTrace()
                }

            }, ContextCompat.getMainExecutor(this))

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    private fun capturePhoto() {

        val imgCapture =
            imageCapture ?: return

        val photoFile = File(

            getExternalFilesDir(
                Environment
                    .DIRECTORY_PICTURES
            ),

            "SOS_${
                SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    Locale.getDefault()
                ).format(Date())
            }.jpg"
        )

        val outputOptions =

            ImageCapture
                .OutputFileOptions
                .Builder(photoFile)
                .build()

        imgCapture.takePicture(

            outputOptions,

            ContextCompat
                .getMainExecutor(this),

            object :
                ImageCapture
                .OnImageSavedCallback {

                override fun onImageSaved(
                    output:
                    ImageCapture
                    .OutputFileResults
                ) {

                    Toast.makeText(
                        this@SosAlertActivity,
                        "📸 Photo Saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(
                    exception:
                    ImageCaptureException
                ) {

                    exception.printStackTrace()
                }
            }
        )
    }

    // =========================
    // AUDIO RECORDING
    // =========================

    private fun startAudioRecording() {

        try {

            if (

                ContextCompat
                    .checkSelfPermission(
                        this,
                        Manifest.permission
                            .RECORD_AUDIO
                    )

                != PackageManager.PERMISSION_GRANTED
            ) return

            audioFilePath =
                "${getExternalFilesDir(Environment.DIRECTORY_MUSIC)}/SOS_${
                    SimpleDateFormat(
                        "yyyyMMdd_HHmmss",
                        Locale.getDefault()
                    ).format(Date())
                }.3gp"

            mediaRecorder =
                MediaRecorder().apply {

                    setAudioSource(
                        MediaRecorder
                            .AudioSource.MIC
                    )

                    setOutputFormat(
                        MediaRecorder
                            .OutputFormat
                            .THREE_GPP
                    )

                    setAudioEncoder(
                        MediaRecorder
                            .AudioEncoder
                            .AMR_NB
                    )

                    setOutputFile(
                        audioFilePath
                    )

                    prepare()

                    start()
                }

            isRecording = true

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    private fun stopAudioRecording() {

        try {

            if (isRecording) {

                mediaRecorder?.stop()

                mediaRecorder?.release()

                mediaRecorder = null

                isRecording = false
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    // =========================
    // SHAKE DETECTION
    // =========================

    private fun setupShakeDetection() {

        sensorManager =
            getSystemService(
                Context.SENSOR_SERVICE
            ) as SensorManager

        val accelerometer =
            sensorManager
                ?.getDefaultSensor(
                    Sensor.TYPE_ACCELEROMETER
                )

        sensorManager?.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        acceleration = 10f

        currentAcceleration =
            SensorManager.GRAVITY_EARTH

        lastAcceleration =
            SensorManager.GRAVITY_EARTH
    }

    override fun onSensorChanged(
        event: SensorEvent?
    ) {

        if (event == null) return

        val x = event.values[0]

        val y = event.values[1]

        val z = event.values[2]

        lastAcceleration =
            currentAcceleration

        currentAcceleration =

            sqrt(
                (
                        x * x +
                                y * y +
                                z * z
                        ).toDouble()
            ).toFloat()

        val delta =
            currentAcceleration -
                    lastAcceleration

        acceleration =
            acceleration * 0.9f + delta

        if (
            acceleration >
            SHAKE_THRESHOLD &&
            !shakeTriggered
        ) {

            shakeTriggered = true

            Toast.makeText(
                this,
                "📳 Shake Detected!",
                Toast.LENGTH_SHORT
            ).show()

            countDownTimer?.cancel()

            activateSOS()
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
    }

    // =========================
    // DISMISS
    // =========================

    private fun dismissEmergency() {

        sosActive = false

        stopSiren()

        stopVibration()

        stopAudioRecording()

        stopPeriodicLocationSMS()

        countDownTimer?.cancel()

        sensorManager
            ?.unregisterListener(this)

        sendFinalSMS()

        Toast.makeText(
            this,
            "✅ Emergency Dismissed",
            Toast.LENGTH_SHORT
        ).show()

        Handler(
            Looper.getMainLooper()
        ).postDelayed({

            finish()

        }, 1500)
    }

    private fun sendFinalSMS() {
        lifecycleScope.launch {
            try {
                if (cachedContacts.isEmpty()) {
                    val db = AppDatabase.getDatabase(this@SosAlertActivity)
                    cachedContacts = db.emergencyContactDao().getAllContacts().map { it.contact_phone }
                }

                if (cachedContacts.isEmpty()) return@launch

                if (ContextCompat.checkSelfPermission(this@SosAlertActivity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) return@launch

                val message = "✅ I am safe now. Emergency dismissed.\n- ShaktiSetu"

                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                for (contact in cachedContacts) {
                    if (contact.isBlank()) continue

                    // Format number: remove spaces
                    val cleanedContact = contact.replace(Regex("[^0-9+]"), "")
                    val formattedContact = if (cleanedContact.length == 10 && !cleanedContact.startsWith("+")) {
                        "+91$cleanedContact"
                    } else {
                        cleanedContact
                    }

                    try {
                        smsManager.sendTextMessage(formattedContact, null, message, null, null)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {

        super.onDestroy()

        stopSiren()

        stopVibration()

        stopAudioRecording()

        stopPeriodicLocationSMS()

        countDownTimer?.cancel()

        sensorManager
            ?.unregisterListener(this)
    }
}