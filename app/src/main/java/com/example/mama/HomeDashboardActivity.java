package com.example.mama;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HomeDashboardActivity extends AppCompatActivity implements SensorEventListener {

    // Capteurs pour l'urgence (Shake)
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_dashboard);

        // --- 1. INITIALISATION DU CAPTEUR URGENCE ---
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        // --- 2. GESTION DES CLICS SUR LA GRILLE ---

        // Carte 1 : Urgence
        CardView cardEmergency = findViewById(R.id.cardEmergency);
        cardEmergency.setOnClickListener(v -> startActivity(new Intent(this, EmergencyActivity.class)));

        // Carte 2 : Rendez-vous
        CardView cardRdv = findViewById(R.id.cardRdv);
        cardRdv.setOnClickListener(v -> startActivity(new Intent(this, AppointmentsActivity.class)));

        // Carte 3 : Santé & Constantes
        CardView cardHealth = findViewById(R.id.cardHealth);
        cardHealth.setOnClickListener(v -> startActivity(new Intent(this, HealthActivity.class)));

        // Carte 4 : Symptômes
        CardView cardSymptoms = findViewById(R.id.cardSymptoms);
        cardSymptoms.setOnClickListener(v -> startActivity(new Intent(this, SymptomsActivity.class)));

        // Carte 5 : Activité & Météo
        CardView cardActivity = findViewById(R.id.cardActivityWeather);
        cardActivity.setOnClickListener(v -> startActivity(new Intent(this, ActivityWeatherActivity.class)));

        // Carte 6 : Nutrition
        CardView cardNutrition = findViewById(R.id.cardNutrition);
        cardNutrition.setOnClickListener(v -> startActivity(new Intent(this, NutritionActivity.class)));

        // Bouton Déconnexion
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        findViewById(R.id.fabChat).setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));

    }

    // --- LOGIQUE SECOUSSE (Copier-Coller de l'ancienne) ---
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float[] mGravity = event.values.clone();
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            if (mAccel > 12) {
                // Secousse détectée
                Toast.makeText(this, "URGENCE DÉTECTÉE !", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, EmergencyActivity.class));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        if(sensorManager != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sensorManager != null) sensorManager.unregisterListener(this);
    }
}