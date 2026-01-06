package com.example.mama.user;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.mama.R;
import com.example.mama.medication.MedicationActivity;
import com.example.mama.urgence.AppointmentsActivity;
import com.example.mama.urgence.EmergencyActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Interface
    CardView cardEmergency;
    android.widget.Button btnAppointments, btnLogout, btnSport, btnNutrition, btnMeds;
    TextView tvWelcome;

    // Capteurs pour le Shake (Secousse)
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Liaison XML
        cardEmergency = findViewById(R.id.cardEmergency);
        btnAppointments = findViewById(R.id.btnAppointments);
        btnLogout = findViewById(R.id.btnLogout);
        btnSport = findViewById(R.id.btnSport);
        btnNutrition = findViewById(R.id.btnNutrition);
        btnMeds = findViewById(R.id.btnMeds);
        tvWelcome = findViewById(R.id.tvWelcome);

        // --- GESTION DU CAPTEUR (ACCÉLÉROMÈTRE) ---
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Initialisation des variables de calcul
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        // --- CLICS BOUTONS ---

        // 1. Clic manuel sur SOS
        cardEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEmergencyPage();
            }
        });

        // 2. Clic sur Rendez-vous (MODIFIÉ : Ouvre maintenant la liste des RDV)
        btnAppointments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AppointmentsActivity.class);
                startActivity(intent);
            }
        });

        // 3. Déconnexion
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On retourne au login et on ferme l'activité actuelle
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 4. Sport
        if (btnSport != null) {
            btnSport.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, com.example.mama.sport.SportDashboardActivity.class);
                startActivity(intent);
            });
        }

        // 5. Nutrition
        if (btnNutrition != null) {
            btnNutrition.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, com.example.mama.Nutrition.MainActivity.class);
                startActivity(intent);
            });
        }

        // 6. Médicaments
        if (btnMeds != null) {
            btnMeds.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, MedicationActivity.class);
                startActivity(intent);
            });
        }
    }

    // --- LOGIQUE DU SHAKE (SECOUSSE) ---

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float[] mGravity = event.values.clone();

            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];

            mAccelLast = mAccelCurrent;
            // Formule : Racine carrée de (x² + y² + z²) pour avoir la force globale
            mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            // Seuil de déclenchement (12 est une bonne sensibilité pour une secousse ferme)
            if (mAccel > 12) {
                Toast.makeText(this, "⚠️ Secousse détectée : URGENCE !", Toast.LENGTH_SHORT).show();
                openEmergencyPage();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Pas nécessaire ici
    }

    // Fonction pour ouvrir la page d'urgence
    private void openEmergencyPage() {
        Intent intent = new Intent(MainActivity.this, EmergencyActivity.class);
        startActivity(intent);
    }

    // IMPORTANT : Activer le capteur quand l'appli est visible
    @Override
    protected void onResume() {
        super.onResume();
        if(sensorManager != null){
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    // IMPORTANT : Désactiver le capteur quand l'appli est en pause (économie batterie)
    @Override
    protected void onPause() {
        super.onPause();
        if(sensorManager != null){
            sensorManager.unregisterListener(this);
        }
    }
}