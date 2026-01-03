package com.example.mama.Nutrition;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.example.mama.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WaterTrackerActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "WaterTrackerPrefs";
    private static final String KEY_WATER_INTAKE = "water_intake";
    private static final String KEY_LAST_DATE = "last_date";
    private static final float DAILY_GOAL = 2.0f; // 2 litres par jour

    private TextView tvWaterAmount;
    private TextView tvWaterPercentage;
    private ProgressBar progressBar;
    private Switch switchService;

    private SharedPreferences prefs;
    private float currentWaterIntake = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_tracker);
        // Demander permission notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }


        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialiser les vues
        tvWaterAmount = findViewById(R.id.tvWaterAmount);
        tvWaterPercentage = findViewById(R.id.tvWaterPercentage);
        progressBar = findViewById(R.id.progressBar);
        switchService = findViewById(R.id.switchService);

        // Vérifier si c'est un nouveau jour
        checkAndResetDaily();

        // Charger les données
        loadWaterIntake();

        // Configurer le switch du service
        boolean isServiceRunning = WaterReminderService.isServiceRunning(this);
        switchService.setChecked(isServiceRunning);

        switchService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startWaterReminderService();
            } else {
                stopWaterReminderService();
            }
        });

        // Boutons d'ajout manuel
        findViewById(R.id.btnAdd200ml).setOnClickListener(v -> addWater(0.2f));
        findViewById(R.id.btnAdd400ml).setOnClickListener(v -> addWater(0.4f));
        findViewById(R.id.btnAdd600ml).setOnClickListener(v -> addWater(0.6f));

        // Bouton reset
        findViewById(R.id.btnReset).setOnClickListener(v -> resetWaterIntake());

        // Bouton retour
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWaterIntake();
        updateUI();
    }

    private void checkAndResetDaily() {
        String today = getCurrentDate();
        String lastDate = prefs.getString(KEY_LAST_DATE, "");

        if (!today.equals(lastDate)) {
            // Nouveau jour, réinitialiser
            prefs.edit()
                    .putFloat(KEY_WATER_INTAKE, 0f)
                    .putString(KEY_LAST_DATE, today)
                    .apply();
            currentWaterIntake = 0f;
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void loadWaterIntake() {
        currentWaterIntake = prefs.getFloat(KEY_WATER_INTAKE, 0f);
    }

    public void addWater(float amount) {
        currentWaterIntake += amount;

        // Limiter à 10 litres max (sécurité)
        if (currentWaterIntake > 10f) {
            currentWaterIntake = 10f;
        }

        saveWaterIntake();
        updateUI();
    }

    private void saveWaterIntake() {
        prefs.edit()
                .putFloat(KEY_WATER_INTAKE, currentWaterIntake)
                .putString(KEY_LAST_DATE, getCurrentDate())
                .apply();
    }

    private void updateUI() {
        // Afficher la quantité en litres
        tvWaterAmount.setText(String.format(Locale.getDefault(), "%.1f L", currentWaterIntake));

        // Calculer le pourcentage
        int percentage = (int) ((currentWaterIntake / DAILY_GOAL) * 100);
        if (percentage > 100) percentage = 100;

        tvWaterPercentage.setText(percentage + "%");
        progressBar.setProgress(percentage);

        // Changer la couleur selon le pourcentage
        if (percentage >= 100) {
            progressBar.getProgressDrawable().setColorFilter(
                    getResources().getColor(android.R.color.holo_green_dark),
                    android.graphics.PorterDuff.Mode.SRC_IN
            );
        } else if (percentage >= 50) {
            progressBar.getProgressDrawable().setColorFilter(
                    getResources().getColor(android.R.color.holo_blue_light),
                    android.graphics.PorterDuff.Mode.SRC_IN
            );
        } else {
            progressBar.getProgressDrawable().setColorFilter(
                    getResources().getColor(android.R.color.holo_orange_light),
                    android.graphics.PorterDuff.Mode.SRC_IN
            );
        }
    }

    private void resetWaterIntake() {
        currentWaterIntake = 0f;
        saveWaterIntake();
        updateUI();
    }

    private void startWaterReminderService() {
        Intent serviceIntent = new Intent(this, WaterReminderService.class);
        startService(serviceIntent);
    }

    private void stopWaterReminderService() {
        Intent serviceIntent = new Intent(this, WaterReminderService.class);
        stopService(serviceIntent);
    }

    public static float getCurrentWaterIntake(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getFloat(KEY_WATER_INTAKE, 0f);
    }
}