package com.example.mama.sante;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mama.MyDatabaseHelper;
import com.example.mama.R;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SleepTrackingActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private ToggleButton toggleButton;
    private TextView tvStatus;
    private MaterialCardView resultsCard;
    private TextView tvTotalDuration, tvDeepSleep, tvLightSleep, tvSoundEventsResult;

    private boolean isTracking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_tracker);

        toggleButton = findViewById(R.id.toggleButton);
        tvStatus = findViewById(R.id.tvStatus);
        resultsCard = findViewById(R.id.resultsCard);

        tvTotalDuration = findViewById(R.id.tvTotalDuration);
        tvDeepSleep = findViewById(R.id.tvDeepSleep);
        tvLightSleep = findViewById(R.id.tvLightSleep);
        tvSoundEventsResult = findViewById(R.id.tvSoundEventsResult);

        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkPermissionAndStart();
            } else {
                stopTracking();
            }
        });

        findViewById(R.id.btnListenRecordings).setOnClickListener(v -> {
            startActivity(new Intent(SleepTrackingActivity.this, RecordingsActivity.class));
        });
    }

    private void checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO },
                    REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            startTracking();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTracking();
            } else {
                Toast.makeText(this, "Permission de microphone refusée.", Toast.LENGTH_SHORT).show();
                toggleButton.setChecked(false);
            }
        }
    }

    private void startTracking() {
        isTracking = true;
        tvStatus.setText("Suivi en cours...");
        resultsCard.setVisibility(View.GONE);

        Intent serviceIntent = new Intent(this, SleepTrackingService.class);
        serviceIntent.setAction("START");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private android.content.BroadcastReceiver sleepDataReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            if (SleepTrackingService.ACTION_SLEEP_DATA_SAVED.equals(intent.getAction())) {
                fetchAndDisplayLastSession();
            }
        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();
        android.content.IntentFilter filter = new android.content.IntentFilter(
                SleepTrackingService.ACTION_SLEEP_DATA_SAVED);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(sleepDataReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(sleepDataReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(sleepDataReceiver);
    }

    private void stopTracking() {
        if (!isTracking)
            return;
        isTracking = false;

        Intent serviceIntent = new Intent(this, SleepTrackingService.class);
        serviceIntent.setAction("STOP");
        startService(serviceIntent);

        tvStatus.setText("Sauvegarde en cours...");

        // Safety timeout: if broadcast is missed, refresh anyway after 2 seconds
        new android.os.Handler().postDelayed(this::fetchAndDisplayLastSession, 2000);
    }

    private void fetchAndDisplayLastSession() {
        MyDatabaseHelper db = new MyDatabaseHelper(this);
        SleepSession lastSession = db.getLastSleepSession();

        if (lastSession != null) {
            long deepMillis = TimeUnit.MINUTES.toMillis(lastSession.getDeepSleepMinutes());
            long lightMillis = TimeUnit.MINUTES.toMillis(lastSession.getLightSleepMinutes());
            long totalMillis = deepMillis + lightMillis;
            int sounds = lastSession.getSoundEvents();

            displayResults(totalMillis, deepMillis, lightMillis, sounds);
        } else {
            tvStatus.setText("Aucune donnée de sommeil trouvée.");
            resultsCard.setVisibility(View.GONE);
        }
    }

    private void displayResults(long totalDurationMillis, long deepSleep, long lightSleep, int soundEvents) {
        long totalHours = TimeUnit.MILLISECONDS.toHours(totalDurationMillis);
        long totalMinutesPart = TimeUnit.MILLISECONDS.toMinutes(totalDurationMillis) % 60;

        long deepHours = TimeUnit.MILLISECONDS.toHours(deepSleep);
        long deepMinutesPart = TimeUnit.MILLISECONDS.toMinutes(deepSleep) % 60;

        long lightHours = TimeUnit.MILLISECONDS.toHours(lightSleep);
        long lightMinutesPart = TimeUnit.MILLISECONDS.toMinutes(lightSleep) % 60;

        tvTotalDuration.setText(String.format(Locale.getDefault(), "%dh %02dm\nTotal", totalHours, totalMinutesPart));
        tvDeepSleep.setText(String.format(Locale.getDefault(), "%dh %02dm\nProfond", deepHours, deepMinutesPart));
        tvLightSleep.setText(String.format(Locale.getDefault(), "%dh %02dm\nLéger", lightHours, lightMinutesPart));
        tvSoundEventsResult.setText(String.format(Locale.getDefault(), "%d\nSons détectés", soundEvents));

        tvStatus.setText("Suivi terminé. Voici le résumé de votre nuit.");
        resultsCard.setVisibility(View.VISIBLE);
    }

}