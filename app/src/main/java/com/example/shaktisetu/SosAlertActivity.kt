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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.shaktisetu.database.AppDatabase
import com.example.shaktisetu.ui.screens.SosAlertScreen
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
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

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    private var countDownTimer: CountDownTimer? = null

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var audioFilePath = ""

    private var imageCapture: ImageCapture? = null

    private var currentLatitude = 0.0
    private var currentLongitude = 0.0

    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var shakeTriggered = false

    private var sosActive = false

    private var userEmail = ""
    private var userUid = ""
    private var userPin = ""

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var locationHandler: Handler? = null
    private var locationRunnable: Runnable? = null

    private var cachedContacts: List<String> = emptyList()

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val SHAKE_THRESHOLD = 25f
        private const val LOCATION_SMS_INTERVAL = 60000L
    }

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

        val sharedPref = getSharedPreferences("ShaktiSetuPrefs", MODE_PRIVATE)
        userEmail = intent.getStringExtra("user_email") ?: sharedPref.getString("user_email", "") ?: ""
        userUid = sharedPref.getString("user_uid", "") ?: ""
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

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

        startSiren()
        startVibration()
        setupCamera()
        startAudioRecording()
        setupShakeDetection()

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
        sendLocationSMS()
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
        locationHandler = Handler(Looper.getMainLooper())
        locationRunnable = object : Runnable {
            override fun run() {
                if (sosActive) {
                    lifecycleScope.launch {
                        refreshLocation()
                        sendLocationSMS()
                        updateLiveLocationInFirebase()
                    }
                    locationHandler?.postDelayed(this, LOCATION_SMS_INTERVAL)
                }
            }
        }
        locationHandler?.postDelayed(locationRunnable!!, LOCATION_SMS_INTERVAL)
    }

    private fun stopPeriodicLocationSMS() {
        locationRunnable?.let { locationHandler?.removeCallbacks(it) }
        locationHandler = null
        locationRunnable = null
    }

    private fun sendLocationSMS() {
        lifecycleScope.launch {
            try {
                refreshLocation()
                if (currentLatitude == 0.0 || currentLongitude == 0.0) return@launch

                if (cachedContacts.isEmpty()) {
                    val db = AppDatabase.getDatabase(this@SosAlertActivity)
                    cachedContacts = db.emergencyContactDao().getAllContacts().map { it.contact_phone }
                }

                if (cachedContacts.isEmpty() || ContextCompat.checkSelfPermission(this@SosAlertActivity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) return@launch

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
        val permissions = arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.VIBRATE)
        val notGranted = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (notGranted.isNotEmpty()) ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), PERMISSION_REQUEST_CODE)
    }

    private suspend fun refreshLocation() {
        withContext(Dispatchers.IO) {
            try {
                if (ActivityCompat.checkSelfPermission(this@SosAlertActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return@withContext
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
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            try {
                val provider = future.get()
                imageCapture = ImageCapture.Builder().build()
                provider.unbindAll()
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, imageCapture)
            } catch (e: Exception) { e.printStackTrace() }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capturePhoto() {
        val imgCapture = imageCapture ?: return
        val photoFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "SOS_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg")
        imgCapture.takePicture(ImageCapture.OutputFileOptions.Builder(photoFile).build(), ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) { uploadEvidence(photoFile, "image") }
            override fun onError(exception: ImageCaptureException) { exception.printStackTrace() }
        })
    }

    private fun uploadEvidence(file: File, type: String) {
        if (userUid.isEmpty()) return
        val ref = storage.reference.child("evidence/$userUid/${file.name}")
        ref.putFile(Uri.fromFile(file)).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { url ->
                val field = if (type == "image") "last_photo" else "last_audio"
                db.collection("live_sos").document(userUid).update(field, url.toString())
            }
        }
    }

    private fun startAudioRecording() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return
            audioFilePath = "${getExternalFilesDir(Environment.DIRECTORY_MUSIC)}/SOS_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.3gp"
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFilePath)
                prepare()
                start()
            }
            isRecording = true
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun stopAudioRecording() {
        if (isRecording) {
            mediaRecorder?.stop()
            uploadEvidence(File(audioFilePath), "audio")
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
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
        lifecycleScope.launch {
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
    }
}