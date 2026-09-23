package com.valeno.shaktisetu

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.valeno.shaktisetu.database.AppDatabase
import com.valeno.shaktisetu.ui.screens.SosAlertScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

class SosAlertActivity :
    AppCompatActivity(),
    SensorEventListener {

    private var countdownValue by mutableStateOf(10)
    private var isSosActiveState by mutableStateOf(false)
    private var isMutedState by mutableStateOf(false)

    // Audio State
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecordingAudio = false
    private var isStartingAudio = false
    private var audioFilePath = ""

    // Countdown
    private var countDownTimer: CountDownTimer? = null

    // Camera State
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var isCameraInitializing = false
    private var isCameraBound = false
    private var isCapturingPhoto = false
    private var pendingPhotoCapture = false

    // Location
    private var currentLatitude = 0.0
    private var currentLongitude = 0.0

    // Shake Detection
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var shakeTriggered = false

    // State
    private var sosActive = false

    // User
    private var userEmail = ""
    private var userUid = ""
    private var userPin = ""

    // Firebase
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    // SOS Loop
    private var sosJob: kotlinx.coroutines.Job? = null

    // Cached Contacts
    private var cachedContacts: List<String> = emptyList()

    companion object {
        private const val SHAKE_THRESHOLD = 25f
        private const val LOCATION_SMS_INTERVAL = 60000L
    }

    // Permission Launcher
    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            Log.d("SHAKTI_PERM", "Permissions result callback: $permissions")

            val cameraGranted = permissions[Manifest.permission.CAMERA] == true ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

            if (cameraGranted) {
                Log.d("SHAKTI_CAMERA", "CAMERA permission granted in result callback")
                setupCamera()
            } else {
                Log.w("SHAKTI_CAMERA", "CAMERA permission denied in result callback")
                Toast.makeText(this, "⚠️ Camera permission denied. Photo evidence disabled.", Toast.LENGTH_SHORT).show()
            }

            if (audioGranted) {
                Log.d("SHAKTI_AUDIO", "RECORD_AUDIO permission granted in result callback")
                startAudioRecording()
            } else {
                Log.w("SHAKTI_AUDIO", "RECORD_AUDIO permission denied in result callback")
                Toast.makeText(this, "⚠️ Audio permission denied. Audio evidence disabled.", Toast.LENGTH_SHORT).show()
            }
        }

    // PIN Launcher
    private val dismissLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                dismissEmergency()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SosAlertScreen(
                countdown = countdownValue,
                isSosActive = isSosActiveState,
                isMuted = isMutedState,
                onMuteToggle = { toggleMute() },
                onDismiss = {
                    if (sosActive) {
                        showPinDialog()
                    } else {
                        dismissEmergency()
                    }
                }
            )
        }

        // User Email
        val sharedPref = getSharedPreferences("ShaktiSetuPrefs", MODE_PRIVATE)
        userEmail = intent.getStringExtra("user_email") ?: sharedPref.getString("user_email", "") ?: ""
        userUid = sharedPref.getString("user_uid", "") ?: ""
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Prefetched Location
        val passedLat = intent.getDoubleExtra("latitude", 0.0)
        val passedLng = intent.getDoubleExtra("longitude", 0.0)

        if (passedLat != 0.0 && passedLng != 0.0) {
            currentLatitude = passedLat
            currentLongitude = passedLng
        }

        fetchUserPin()
        cacheEmergencyContacts()
        requestAllPermissions()

        lifecycleScope.launch {
            refreshLocation()
        }

        // Defer hardware initializations to ensure smooth activity transition
        window.decorView.postDelayed({
            if (!isFinishing) {
                startSiren()
                startVibration()
                setupCamera()
                startAudioRecording()
                setupShakeDetection()
            }
        }, 500)

        // Back Press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (sosActive) {
                    Toast.makeText(this@SosAlertActivity, "🔒 Enter PIN to dismiss SOS", Toast.LENGTH_SHORT).show()
                    showPinDialog()
                } else {
                    finish()
                }
            }
        })

        // Countdown
        sharedPref.edit().putBoolean("sos_active", true).apply()

        countDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countdownValue = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                isSosActiveState = true
                activateSOS()
            }
        }.start()
    }

    private fun toggleMute() {
        if (isMutedState) {
            startSiren()
            startVibration()
            isMutedState = false
            Toast.makeText(this, "🔊 Siren ON", Toast.LENGTH_SHORT).show()
        } else {
            stopSiren()
            stopVibration()
            isMutedState = true
            Toast.makeText(this, "🔇 Siren Muted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun activateSOS() {
        sosActive = true
        Toast.makeText(this, "🆘 SOS ACTIVATED", Toast.LENGTH_LONG).show()
        capturePhoto()
        lifecycleScope.launch {
            sendLocationSMS()
        }
        updateLiveLocationInFirebase()
        startPeriodicLocationSMS()
    }

    private fun updateLiveLocationInFirebase() {
        if (userUid.isEmpty() || currentLatitude == 0.0 || currentLongitude == 0.0) return

        val liveData = hashMapOf(
            "uid" to userUid,
            "email" to userEmail,
            "location" to GeoPoint(currentLatitude, currentLongitude),
            "lastUpdated" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            "status" to "EMERGENCY",
            "osm_link" to "https://www.openstreetmap.org/?mlat=$currentLatitude&mlon=$currentLongitude#map=17/$currentLatitude/$currentLongitude"
        )

        db.collection("live_sos").document(userUid)
            .set(liveData)
            .addOnSuccessListener {
                Log.d("SOS_LIVE", "Location updated in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("SOS_LIVE", "Failed to update location: ${e.message}")
            }
    }

    private fun removeLiveLocationFromFirebase() {
        if (userUid.isEmpty()) return
        db.collection("live_sos").document(userUid).delete()
    }

    private fun startPeriodicLocationSMS() {
        sosJob?.cancel()
        sosJob = lifecycleScope.launch {
            while (sosActive) {
                try {
                    refreshLocation()
                    sendLocationSMS()
                    updateLiveLocationInFirebase()
                } catch (e: Exception) {
                    Log.e("SOS_LOOP", "Error in periodic update: ${e.message}")
                }
                kotlinx.coroutines.delay(LOCATION_SMS_INTERVAL)
            }
        }
    }

    private fun stopPeriodicLocationSMS() {
        sosJob?.cancel()
        sosJob = null
    }

    private suspend fun sendLocationSMS() = withContext(Dispatchers.IO) {
        try {
            if (currentLatitude == 0.0 || currentLongitude == 0.0) return@withContext

            if (cachedContacts.isEmpty()) {
                val db = AppDatabase.getDatabase(this@SosAlertActivity)
                cachedContacts = db.emergencyContactDao().getAllContacts().map { it.contact_phone }
            }

            if (cachedContacts.isEmpty() || ContextCompat.checkSelfPermission(this@SosAlertActivity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) return@withContext

            val locationLink = "https://www.openstreetmap.org/?mlat=$currentLatitude&mlon=$currentLongitude#map=18/$currentLatitude/$currentLongitude"
            val message = "🆘 SHAKTI SETU: EMERGENCY!\nI need help!\n📍 View my Live Location:\n$locationLink\n🕐 Time: ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())}"

            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            for (contact in cachedContacts) {
                val cleanedContact = contact.replace(Regex("[^0-9+]"), "")
                val formattedContact = if (cleanedContact.length == 10 && !cleanedContact.startsWith("+")) "+91$cleanedContact" else cleanedContact
                try {
                    val parts = smsManager.divideMessage(message)
                    smsManager.sendMultipartTextMessage(formattedContact, null, parts, null, null)
                } catch (e: Exception) { Log.e("SOS_SMS", "Failed to send to $formattedContact") }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun cacheEmergencyContacts() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@SosAlertActivity)
                cachedContacts = db.emergencyContactDao().getAllContacts().map { it.contact_phone }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun fetchUserPin() {
        val sharedPref = getSharedPreferences("ShaktiSetuPrefs", MODE_PRIVATE)
        userPin = sharedPref.getString("user_pin", "") ?: ""
    }

    private fun showPinDialog() {
        val intent = Intent(this, DismissPinActivity::class.java)
        dismissLauncher.launch(intent)
    }

    private fun requestAllPermissions() {
        val permissions = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.VIBRATE
        )
        val notGranted = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (notGranted.isNotEmpty()) {
            Log.d("SHAKTI_PERM", "Requesting missing permissions: $notGranted")
            permissionsLauncher.launch(notGranted.toTypedArray())
        } else {
            Log.d("SHAKTI_PERM", "All permissions already granted")
            setupCamera()
            startAudioRecording()
        }
    }

    private suspend fun refreshLocation() {
        withContext(Dispatchers.IO) {
            try {
                if (ContextCompat.checkSelfPermission(this@SosAlertActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return@withContext
                val fusedClient = LocationServices.getFusedLocationProviderClient(this@SosAlertActivity)
                val location: Location? = Tasks.await(fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null))
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun startSiren() {
        try {
            if (mediaPlayer == null) {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                mediaPlayer = MediaPlayer.create(this, alarmUri)
                mediaPlayer?.isLooping = true
            }
            mediaPlayer?.start()
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun stopSiren() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun startVibration() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 500, 200, 500)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    private fun stopVibration() { vibrator?.cancel() }

    private fun setupCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.w("SHAKTI_CAMERA", "Cannot setup camera: CAMERA permission not granted")
            return
        }

        if (isCameraBound) {
            Log.d("SHAKTI_CAMERA", "Camera is already initialized and bound.")
            return
        }

        if (isCameraInitializing) {
            Log.d("SHAKTI_CAMERA", "Camera setup is already in progress.")
            return
        }

        isCameraInitializing = true
        Log.d("SHAKTI_CAMERA", "Initializing ProcessCameraProvider...")

        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            try {
                val provider = future.get()
                cameraProvider = provider
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                provider.unbindAll()

                val boundCamera = try {
                    provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, imageCapture)
                } catch (e: Exception) {
                    Log.w("SHAKTI_CAMERA", "Front camera bind failed, trying back camera: ${e.message}")
                    provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, imageCapture)
                }

                isCameraBound = boundCamera != null
                Log.d("SHAKTI_CAMERA", "Camera successfully bound to lifecycle! Camera active.")

                if (pendingPhotoCapture) {
                    pendingPhotoCapture = false
                    Log.d("SHAKTI_CAMERA", "Executing pending photo capture...")
                    capturePhoto()
                }
            } catch (e: Exception) {
                Log.e("SHAKTI_CAMERA", "Failed to setup camera: ${e.message}", e)
                isCameraBound = false
                Toast.makeText(this, "⚠️ Camera init failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                isCameraInitializing = false
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.w("SHAKTI_CAMERA", "Cannot capture photo: CAMERA permission not granted")
            pendingPhotoCapture = true
            requestAllPermissions()
            return
        }

        val imgCapture = imageCapture
        if (imgCapture == null || !isCameraBound) {
            Log.w("SHAKTI_CAMERA", "ImageCapture is null or camera not bound. Queuing photo capture.")
            pendingPhotoCapture = true
            setupCamera()
            return
        }

        if (isCapturingPhoto) {
            Log.d("SHAKTI_CAMERA", "Photo capture already in progress. Skipping duplicate call.")
            return
        }

        val picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (picturesDir == null) {
            Log.e("SHAKTI_CAMERA", "Pictures directory external storage is unavailable")
            Toast.makeText(this, "⚠️ Storage unavailable for camera photo", Toast.LENGTH_SHORT).show()
            return
        }

        if (!picturesDir.exists()) {
            picturesDir.mkdirs()
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFile = File(picturesDir, "SOS_$timeStamp.jpg")

        try {
            if (photoFile.exists()) photoFile.delete()
            val fileCreated = photoFile.createNewFile()
            if (!fileCreated || !photoFile.canWrite()) {
                Log.e("SHAKTI_CAMERA", "Photo file cannot be created or written to: ${photoFile.absolutePath}")
                Toast.makeText(this, "⚠️ File creation failed for photo evidence", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: Exception) {
            Log.e("SHAKTI_CAMERA", "Failed to create photo file: ${e.message}", e)
            Toast.makeText(this, "⚠️ Photo file error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            return
        }

        isCapturingPhoto = true
        Log.d("SHAKTI_CAMERA", "Taking picture to file: ${photoFile.absolutePath}")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imgCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    isCapturingPhoto = false
                    Log.d("SHAKTI_CAMERA", "Photo captured successfully: ${photoFile.absolutePath}, size=${photoFile.length()} bytes")
                    uploadEvidence(photoFile, "image")
                }

                override fun onError(exception: ImageCaptureException) {
                    isCapturingPhoto = false
                    Log.e("SHAKTI_CAMERA", "Photo capture failed: ${exception.message}", exception)
                    Toast.makeText(this@SosAlertActivity, "⚠️ Photo capture failed: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun uploadEvidence(file: File, type: String) {
        if (userUid.isEmpty()) {
            Log.w("SHAKTI_EVIDENCE", "User UID is empty. Skipping upload.")
            return
        }

        if (!file.exists() || file.length() == 0L) {
            Log.w("SHAKTI_EVIDENCE", "Evidence file does not exist or is empty: ${file.absolutePath}")
            return
        }

        Log.d("SHAKTI_EVIDENCE", "Uploading $type evidence (${file.name}, ${file.length()} bytes) to Firebase Storage...")

        val ref = storage.reference.child("evidence/$userUid/${file.name}")
        ref.putFile(Uri.fromFile(file))
            .addOnSuccessListener {
                Log.d("SHAKTI_EVIDENCE", "Successfully uploaded $type evidence to Firebase Storage.")
                ref.downloadUrl.addOnSuccessListener { url ->
                    val field = if (type == "image") "last_photo" else "last_audio"
                    db.collection("live_sos").document(userUid)
                        .update(field, url.toString())
                        .addOnSuccessListener {
                            Log.d("SHAKTI_EVIDENCE", "Updated live_sos with $field URL: $url")
                        }
                        .addOnFailureListener { e ->
                            Log.e("SHAKTI_EVIDENCE", "Failed to update live_sos doc: ${e.message}", e)
                        }
                }.addOnFailureListener { e ->
                    Log.e("SHAKTI_EVIDENCE", "Failed to get download URL for $type evidence: ${e.message}", e)
                }
            }
            .addOnFailureListener { e ->
                Log.e("SHAKTI_EVIDENCE", "Failed to upload $type evidence to Firebase Storage: ${e.message}", e)
            }
    }

    private fun startAudioRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w("SHAKTI_AUDIO", "Cannot start audio recording: RECORD_AUDIO permission not granted")
            return
        }

        if (isRecordingAudio || isStartingAudio) {
            Log.d("SHAKTI_AUDIO", "Audio recording already active or starting")
            return
        }

        isStartingAudio = true

        try {
            val musicDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            if (musicDir == null) {
                Log.e("SHAKTI_AUDIO", "Music directory external storage is null/unavailable")
                Toast.makeText(this, "⚠️ Storage unavailable for audio recording", Toast.LENGTH_SHORT).show()
                isStartingAudio = false
                return
            }

            if (!musicDir.exists()) {
                val created = musicDir.mkdirs()
                Log.d("SHAKTI_AUDIO", "Directory created: $created at ${musicDir.absolutePath}")
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val outputFile = File(musicDir, "SOS_$timeStamp.3gp")

            try {
                if (outputFile.exists()) outputFile.delete()
                val fileCreated = outputFile.createNewFile()
                if (!fileCreated || !outputFile.canWrite()) {
                    Log.e("SHAKTI_AUDIO", "Output audio file cannot be created or written to: ${outputFile.absolutePath}")
                    Toast.makeText(this, "⚠️ Cannot create audio evidence file", Toast.LENGTH_SHORT).show()
                    isStartingAudio = false
                    return
                }
            } catch (e: Exception) {
                Log.e("SHAKTI_AUDIO", "Failed to create audio output file: ${e.message}", e)
                Toast.makeText(this, "⚠️ File creation error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                isStartingAudio = false
                return
            }

            audioFilePath = outputFile.absolutePath
            Log.d("SHAKTI_AUDIO", "Audio output file prepared: $audioFilePath")

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            val audioSources = listOf(
                MediaRecorder.AudioSource.MIC,
                MediaRecorder.AudioSource.CAMCORDER,
                MediaRecorder.AudioSource.DEFAULT
            )

            var prepared = false
            var lastException: Exception? = null

            for (source in audioSources) {
                try {
                    recorder.reset()
                    recorder.setAudioSource(source)
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    recorder.setOutputFile(audioFilePath)
                    recorder.prepare()
                    prepared = true
                    Log.d("SHAKTI_AUDIO", "MediaRecorder prepared successfully with audio source: $source")
                    break
                } catch (e: Exception) {
                    Log.w("SHAKTI_AUDIO", "Failed to prepare MediaRecorder with audio source $source: ${e.message}")
                    lastException = e
                }
            }

            if (!prepared) {
                recorder.release()
                Log.e("SHAKTI_AUDIO", "MediaRecorder preparation failed for all audio sources", lastException)
                Toast.makeText(this, "⚠️ Audio recorder failed to initialize", Toast.LENGTH_SHORT).show()
                isStartingAudio = false
                return
            }

            recorder.start()
            mediaRecorder = recorder
            isRecordingAudio = true
            Log.d("SHAKTI_AUDIO", "Audio recording started successfully!")

        } catch (e: Exception) {
            Log.e("SHAKTI_AUDIO", "Error starting audio recording: ${e.message}", e)
            Toast.makeText(this, "⚠️ Audio recording error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecordingAudio = false
        } finally {
            isStartingAudio = false
        }
    }

    private fun stopAudioRecording() {
        if (!isRecordingAudio && mediaRecorder == null) return
        Log.d("SHAKTI_AUDIO", "Stopping audio recording...")

        try {
            mediaRecorder?.apply {
                try {
                    stop()
                    Log.d("SHAKTI_AUDIO", "MediaRecorder stopped successfully.")
                    if (audioFilePath.isNotEmpty()) {
                        val file = File(audioFilePath)
                        if (file.exists() && file.length() > 0) {
                            uploadEvidence(file, "audio")
                        } else {
                            Log.w("SHAKTI_AUDIO", "Recorded audio file is missing or empty: $audioFilePath")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SHAKTI_AUDIO", "Error stopping MediaRecorder (recording might be too short): ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("SHAKTI_AUDIO", "Unexpected error in stopAudioRecording: ${e.message}", e)
        } finally {
            try {
                mediaRecorder?.release()
            } catch (e: Exception) {
                Log.e("SHAKTI_AUDIO", "Error releasing MediaRecorder: ${e.message}", e)
            }
            mediaRecorder = null
            isRecordingAudio = false
            isStartingAudio = false
        }
    }

    private fun setupShakeDetection() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        lastAcceleration = currentAcceleration
        currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta = currentAcceleration - lastAcceleration
        acceleration = acceleration * 0.9f + delta
        if (acceleration > SHAKE_THRESHOLD && !shakeTriggered) {
            shakeTriggered = true
            countDownTimer?.cancel()
            activateSOS()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun dismissEmergency() {
        sosActive = false
        stopSiren()
        stopVibration()
        stopAudioRecording()
        stopPeriodicLocationSMS()
        removeLiveLocationFromFirebase()
        countDownTimer?.cancel()
        sensorManager?.unregisterListener(this)
        sendFinalSMS()
        getSharedPreferences("ShaktiSetuPrefs", MODE_PRIVATE).edit().putBoolean("sos_active", false).apply()
        Toast.makeText(this, "✅ Emergency Dismissed", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 1500)
    }

    private fun sendFinalSMS() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (cachedContacts.isEmpty()) {
                    val db = AppDatabase.getDatabase(this@SosAlertActivity)
                    cachedContacts = db.emergencyContactDao().getAllContacts().map { it.contact_phone }
                }
                if (cachedContacts.isEmpty() || ContextCompat.checkSelfPermission(this@SosAlertActivity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) return@launch
                val message = "✅ I am safe now. Emergency dismissed.\n- ShaktiSetu"
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) getSystemService(SmsManager::class.java) else @Suppress("DEPRECATION") SmsManager.getDefault()
                for (contact in cachedContacts) {
                    val cleanedContact = contact.replace(Regex("[^0-9+]"), "")
                    val formattedContact = if (cleanedContact.length == 10 && !cleanedContact.startsWith("+")) "+91$cleanedContact" else cleanedContact
                    try { smsManager.sendTextMessage(formattedContact, null, message, null, null) } catch (e: Exception) {}
                }
            } catch (e: Exception) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSiren()
        stopVibration()
        stopAudioRecording()
        stopPeriodicLocationSMS()
        countDownTimer?.cancel()
        sensorManager?.unregisterListener(this)

        try {
            cameraProvider?.unbindAll()
            cameraProvider = null
            isCameraBound = false
        } catch (e: Exception) {
            Log.e("SHAKTI_CAMERA", "Error unbinding camera on destroy: ${e.message}", e)
        }
    }
}