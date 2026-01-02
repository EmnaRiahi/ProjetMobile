package com.example.mama;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SleepTrackingService extends Service implements SensorEventListener {

    public static final String ACTION_SLEEP_DATA_SAVED = "com.example.mama.ACTION_SLEEP_DATA_SAVED";
    private static final String TAG = "SleepTrackingService";
    private static final String CHANNEL_ID = "SleepTrackingServiceChannel";
    private static final int SOUND_THRESHOLD = 5000; // Start recording threshold
    private static final int SOUND_KEEP_ALIVE_THRESHOLD = 2000; // Keep recording threshold (Hysteresis)
    private static final long SOUND_RECORDING_MIN_DURATION_MS = 1000; // Minimum duration for a recording
    private static final long SOUND_SILENCE_TIMEOUT_MS = 3000; // Increased timeout to prevent fragmentation

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private MediaRecorder mediaRecorder;

    private boolean isTracking = false;
    private long startTime;
    private long deepSleepMillis = 0;
    private long lightSleepMillis = 0;
    private int soundEvents = 0;
    private float lastAccel = SensorManager.GRAVITY_EARTH;

    // Window-based analysis variables
    private float accumulatedMovement = 0;
    private int windowSoundEvents = 0;
    private long lastWindowCheckTime = 0;
    private static final long ANALYSIS_WINDOW_MS = 60 * 1000; // 1 minute window

    private boolean isRecording = false;
    private boolean isMonitoring = false; // New state for monitoring
    private Handler soundHandler = new Handler();
    private String currentRecordingFile;
    private String monitoringFile; // Temp file for monitoring

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ("START".equals(intent.getAction())) {
            startTracking();
        } else if ("STOP".equals(intent.getAction())) {
            stopTracking();
        }
        return START_STICKY;
    }

    private void startTracking() {
        if (isTracking)
            return;
        Log.d(TAG, "Starting sleep tracking.");
        isTracking = true;
        startTime = System.currentTimeMillis();
        lastSensorUpdateTime = 0;
        lastWindowCheckTime = startTime;
        deepSleepMillis = 0;
        lightSleepMillis = 0;
        soundEvents = 0;
        accumulatedMovement = 0;
        windowSoundEvents = 0;

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Analyse du sommeil en cours")
                .setContentText("MAMA surveille votre sommeil.")
                .setSmallIcon(R.drawable.ic_sleep_tracker)
                .build();
        startForeground(1, notification);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        startMonitoring(); // Start monitoring for sound
        soundHandler.post(soundCheckRunnable);
    }

    private void stopTracking() {
        if (!isTracking)
            return;
        Log.d(TAG, "Stopping sleep tracking.");
        isTracking = false;

        // Analyze remaining partial window
        long currentTime = System.currentTimeMillis();
        long partialWindow = currentTime - lastWindowCheckTime;
        if (partialWindow > 0) {
            analyzeWindow(partialWindow);
        }

        sensorManager.unregisterListener(this);
        soundHandler.removeCallbacks(soundCheckRunnable);
        stopRecording(); // Ensure any ongoing recording is stopped
        stopMonitoring(); // Ensure monitoring is stopped

        MyDatabaseHelper db = new MyDatabaseHelper(this);
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(startTime));

        int deepMinutes = (int) Math.round(deepSleepMillis / 60000.0);
        int lightMinutes = (int) Math.round(lightSleepMillis / 60000.0);

        // Ensure at least 1 minute if there was some sleep but less than 30s, to avoid
        // "0 min" confusion during tests
        if (deepMinutes == 0 && deepSleepMillis > 1000)
            deepMinutes = 1;
        if (lightMinutes == 0 && lightSleepMillis > 1000)
            lightMinutes = 1;

        db.addSleepSession(date, deepMinutes, lightMinutes, soundEvents);

        // Notify Activity that data is saved
        Intent intent = new Intent(ACTION_SLEEP_DATA_SAVED);
        intent.setPackage(getPackageName()); // Explicit package for security and reliability
        sendBroadcast(intent);

        stopForeground(true);
        stopSelf();
    }

    private long lastSensorUpdateTime = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isTracking || event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;

        long currentTime = System.currentTimeMillis();
        if (lastSensorUpdateTime == 0) {
            lastSensorUpdateTime = currentTime;
            return;
        }

        long timeDelta = currentTime - lastSensorUpdateTime;
        lastSensorUpdateTime = currentTime;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float currentAccel = (float) Math.sqrt(x * x + y * y + z * z);
        float delta = Math.abs(currentAccel - lastAccel);
        lastAccel = currentAccel;

        // Accumulate movement for the window
        accumulatedMovement += delta;

        // Check if window is complete
        if (currentTime - lastWindowCheckTime >= ANALYSIS_WINDOW_MS) {
            analyzeWindow(currentTime - lastWindowCheckTime);
            lastWindowCheckTime = currentTime;
            accumulatedMovement = 0;
            windowSoundEvents = 0;
        }
    }

    private void analyzeWindow(long windowDuration) {
        // Scientific deduction:
        // High movement OR Frequent sounds = Light Sleep (or Awake)
        // Low movement AND Quiet = Deep Sleep

        // Thresholds (tunable)
        float movementThreshold = 5.0f; // Total delta over 1 minute
        int soundThreshold = 1; // Any significant sound implies light sleep

        if (accumulatedMovement > movementThreshold || windowSoundEvents >= soundThreshold) {
            lightSleepMillis += windowDuration;
        } else {
            deepSleepMillis += windowDuration;
        }
    }

    private boolean isSoundSpike = false;

    private final Runnable soundCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isTracking)
                return;

            int amplitude = getAmplitude();
            if (amplitude > SOUND_THRESHOLD || (isRecording && amplitude > SOUND_KEEP_ALIVE_THRESHOLD)) {
                if (!isRecording) {
                    isSoundSpike = true; // Mark as a sound event
                    stopMonitoring(); // Stop monitoring to free up MIC
                    startRecording(); // Start actual recording
                }
                // Reset silence timeout
                soundHandler.removeCallbacks(stopRecordingRunnable);
                soundHandler.postDelayed(stopRecordingRunnable, SOUND_SILENCE_TIMEOUT_MS);
            } else {
                if (isSoundSpike) {
                    soundEvents++;
                    windowSoundEvents++; // Add to current window
                    isSoundSpike = false;
                }
            }

            soundHandler.postDelayed(this, 300); // Check for sound more frequently
        }
    };

    private final Runnable stopRecordingRunnable = () -> {
        if (isRecording) {
            stopRecording();
            startMonitoring(); // Go back to monitoring
        }
    };

    private int getAmplitude() {
        if (mediaRecorder != null) {
            try {
                return mediaRecorder.getMaxAmplitude();
            } catch (IllegalStateException e) {
                return 0;
            }
        }
        return 0;
    }

    private void startRecording() {
        if (isRecording)
            return;

        File dir = new File(getExternalFilesDir(null), "SleepRecordings");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date()) + ".mp3";
        currentRecordingFile = new File(dir, fileName).getAbsolutePath();

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(currentRecordingFile);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Log.d(TAG, "Started recording to " + currentRecordingFile);
        } catch (IOException e) {
            Log.e(TAG, "startRecording failed", e);
            isRecording = false;
        }
    }

    private void startMonitoring() {
        if (isMonitoring || isRecording)
            return;

        try {
            File tempFile = File.createTempFile("temp_monitoring", ".3gp", getCacheDir());
            monitoringFile = tempFile.getAbsolutePath();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(monitoringFile);

            mediaRecorder.prepare();
            mediaRecorder.start();
            isMonitoring = true;
        } catch (IOException e) {
            Log.e(TAG, "startMonitoring failed", e);
        }
    }

    private void stopMonitoring() {
        if (!isMonitoring || mediaRecorder == null)
            return;

        try {
            mediaRecorder.stop();
            mediaRecorder.release();
        } catch (RuntimeException e) {
            Log.e(TAG, "stopMonitoring failed", e);
        } finally {
            mediaRecorder = null;
            isMonitoring = false;
            // Delete temp file
            if (monitoringFile != null) {
                new File(monitoringFile).delete();
            }
        }
    }

    private void stopRecording() {
        if (!isRecording || mediaRecorder == null)
            return;

        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            Log.d(TAG, "Stopped recording.");
        } catch (RuntimeException e) {
            Log.e(TAG, "stopRecording failed", e);
            // Don't delete the file, even if it's short.
        } finally {
            mediaRecorder = null;
            isRecording = false;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Sleep Tracking Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTracking();
        Log.d(TAG, "Service destroyed.");
    }
}
