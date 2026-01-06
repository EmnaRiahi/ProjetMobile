package com.example.mama;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mama.sante.HealthActivity;
import com.example.mama.sport.SportDashboardActivity;

public class HomeDashboardActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float mAccel, mAccelCurrent, mAccelLast;
    private MyDatabaseHelper myDB;
    private android.widget.ImageView ivProfileHome;
    private android.widget.TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_dashboard);

        // 1. Navigation SÉCURISÉE (Anti-Crash)
        setupClick(R.id.cardEmergency, EmergencyActivity.class);
        setupClick(R.id.cardRdv, AppointmentsActivity.class);
        setupClick(R.id.cardHealth, HealthActivity.class);
        setupClick(R.id.cardSymptoms, SymptomsActivity.class);
        setupClick(R.id.cardActivityWeather, SportDashboardActivity.class);
        setupClick(R.id.cardNutrition, com.example.mama.Nutrition.MainActivity.class);
        setupClick(R.id.cardMeds, MedicationActivity.class);
        setupClick(R.id.fabChat, ChatActivity.class);

        View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        }

        myDB = new MyDatabaseHelper(this);
        ivProfileHome = findViewById(R.id.ivProfileHome);
        tvName = findViewById(R.id.tvName);

        setupClick(R.id.btnSettings, SettingsActivity.class);

        // 2. Capteur
        try {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
            mAccel = 0.00f; mAccelCurrent = SensorManager.GRAVITY_EARTH; mAccelLast = SensorManager.GRAVITY_EARTH;
        } catch (Exception e) {}
    }

    private void setupClick(int id, Class<?> destination) {
        View view = findViewById(id);
        if (view != null) {
            view.setOnClickListener(v -> startActivity(new Intent(this, destination)));
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float[] v = event.values;
            float x = v[0]; float y = v[1]; float z = v[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            if (mAccel > 12) {
                mAccel = 0;
                Toast.makeText(this, "URGENCE !", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, EmergencyActivity.class));
            }
        }
    }
    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    @Override protected void onResume() { 
        super.onResume(); 
        if(sensorManager!=null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI); 
        loadUserData();
    }

    private void loadUserData() {
        String email = getSharedPreferences("user_session", MODE_PRIVATE).getString("email", "");
        if (!email.isEmpty()) {
            android.database.Cursor cursor = myDB.getUserByEmail(email);
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("fullname"));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image"));
                
                if (tvName != null) tvName.setText(name);
                if (ivProfileHome != null && imagePath != null && !imagePath.isEmpty()) {
                    try {
                        if (imagePath.startsWith("content://")) {
                            ivProfileHome.setImageURI(android.net.Uri.parse(imagePath));
                        } else {
                            ivProfileHome.setImageURI(android.net.Uri.fromFile(new java.io.File(imagePath)));
                        }
                    } catch (Exception e) {
                        ivProfileHome.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }
                cursor.close();
            }
        }
    }

    @Override protected void onPause() { super.onPause(); if(sensorManager!=null) sensorManager.unregisterListener(this); }
}