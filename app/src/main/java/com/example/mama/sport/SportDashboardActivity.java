// ========================================
// 1. SportDashboardActivity.java
// ========================================
package com.example.mama.sport;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mama.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SportDashboardActivity extends AppCompatActivity implements SensorEventListener {

    private TextView txtSteps, txtCalories, txtDistance, txtTime, txtStepProgress;
    private ProgressBar progressSteps;
    private ActiviteDatabase db;

    // Variable pour stocker l'objectif actuel (au lieu de STEP_GOAL constant)
    private int currentStepGoal;

    // Variables pour le capteur
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private int currentSessionSteps = 0;
    private int databaseStepsAtStart = 0;
    private double magnitudePrevious = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_dashboard);

        db = ActiviteDatabase.getInstance(this);

        // Charger l'objectif sauvegardé (par défaut 6000)
        SharedPreferences prefs = getSharedPreferences("SportPrefs", MODE_PRIVATE);
        currentStepGoal = prefs.getInt("step_goal", 6000);

        // Liaison UI
        txtSteps = findViewById(R.id.txtSteps);
        txtStepProgress = findViewById(R.id.txtStepProgress);
        txtCalories = findViewById(R.id.txtCalories);
        txtDistance = findViewById(R.id.txtDistance);
        txtTime = findViewById(R.id.txtTime);
        progressSteps = findViewById(R.id.progressSteps);

        // Initialisation des capteurs
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        findViewById(R.id.fabAdd).setOnClickListener(v ->
                new AddSessionDialog(this, null, this::loadStats).show()
        );

        findViewById(R.id.stepCard).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class))
        );

        loadStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharge les données et l'objectif au retour de l'activité
        loadStats();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        // Optionnel : Sauvegarder la session de marche actuelle en base quand on quitte
        saveCurrentSessionToDatabase();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calcul de la magnitude du mouvement (Algorithme de base)
            double magnitude = Math.sqrt(x * x + y * y + z * z);
            double magnitudeDelta = magnitude - magnitudePrevious;
            magnitudePrevious = magnitude;

            // Si le mouvement dépasse un certain seuil, on compte un pas
            if (magnitudeDelta > 6) {
                currentSessionSteps++;
                updateUI(databaseStepsAtStart + currentSessionSteps);
            }
        }
    }

    private void updateUI(int totalStepsNow) {
        // Recharger l'objectif au cas où il a été modifié dans le dialogue
        SharedPreferences prefs = getSharedPreferences("SportPrefs", MODE_PRIVATE);
        currentStepGoal = prefs.getInt("step_goal", 6000);

        txtSteps.setText(String.valueOf(totalStepsNow));

        // Utilisation de la variable dynamique au lieu de STEP_GOAL
        txtStepProgress.setText(totalStepsNow + " / " + currentStepGoal + " Steps");

        // Calculs dynamiques
        txtDistance.setText(String.format(Locale.getDefault(), "%.2f km", totalStepsNow * 0.0007));
        txtCalories.setText(String.format(Locale.getDefault(), "%.1f kcal", totalStepsNow * 0.04));

        // La barre de progression utilise aussi l'objectif dynamique
        progressSteps.setMax(currentStepGoal);
        progressSteps.setProgress(Math.min(totalStepsNow, currentStepGoal));
    }

    private void loadStats() {
        new Thread(() -> {
            List<ActiviteEntity> list = db.activiteDao().getAllActivities();
            int total = 0;
            for (ActiviteEntity a : list) {
                total += a.steps;
            }
            databaseStepsAtStart = total;
            int finalTotal = total;
            runOnUiThread(() -> updateUI(finalTotal));
        }).start();
    }

    private void saveCurrentSessionToDatabase() {
        if (currentSessionSteps > 0) {
            int stepsToSave = currentSessionSteps;
            currentSessionSteps = 0; // Reset pour éviter les doublons
            new Thread(() -> {
                ActiviteEntity session = new ActiviteEntity();
                session.steps = stepsToSave;
                session.duration = 1; // Simulation ou calcul de durée
                session.date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                session.time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                session.type = "OUTDOOR";
                db.activiteDao().insert(session);
            }).start();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}

