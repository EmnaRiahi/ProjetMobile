package com.example.mama.medication; // Correct package structure

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.mama.user.MyDatabaseHelper;
import com.example.mama.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MaternalHealthRiskActivity extends AppCompatActivity implements SensorEventListener {

    private Interpreter tflite;
    private MyDatabaseHelper myDB;

    // UI Components
    private TextView tvAgeDisplay, tvResultMain, tvResultSub;
    private TextInputEditText etSystolic, etDiastolic, etBS, etBodyTemp, etHeartRate;
    private MaterialButton btnPredict;
    private MaterialCardView cardResult;
    private CoordinatorLayout rootLayout;
    
    // Sensor
    private SensorManager sensorManager;
    private Sensor lightSensor;

    // Data
    private int userAge = 25; // Default fallback

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maternal_health_risk);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarRisk);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Init Views
        tvAgeDisplay = findViewById(R.id.tvAgeDisplay);
        etSystolic = findViewById(R.id.etSystolic);
        etDiastolic = findViewById(R.id.etDiastolic);
        etBS = findViewById(R.id.etBS);
        etBodyTemp = findViewById(R.id.etBodyTemp);
        etHeartRate = findViewById(R.id.etHeartRate);
        btnPredict = findViewById(R.id.btnPredict);
        cardResult = findViewById(R.id.cardResult);
        tvResultMain = findViewById(R.id.tvResultMain);
        tvResultSub = findViewById(R.id.tvResultSub);
        rootLayout = findViewById(R.id.riskRoot);

        myDB = new MyDatabaseHelper(this);

        // Load Model
        try {
            tflite = new Interpreter(loadModelFile());
        } catch (Exception e) {
            Toast.makeText(this, "Erreur chargement Modèle IA", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        // Fetch User Age
        loadUserAge();

        // Predict Action
        btnPredict.setOnClickListener(v -> predictRisk());

        // Sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
    }

    private void loadUserAge() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String email = prefs.getString("email", "");

        if (!email.isEmpty()) {
            Cursor cursor = myDB.getUserDetails(email);
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    int colAgeIndex = cursor.getColumnIndex("age");
                    if (colAgeIndex != -1) {
                        userAge = cursor.getInt(colAgeIndex);
                        tvAgeDisplay.setText(userAge + " ans");
                    }
                } catch (Exception ignored) {}
                cursor.close();
            }
        }
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd("maternal_health_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Normalization parameters
    private static final float[] SCALER_MEAN = { 29.87f, 113.19f, 76.46f, 8.72f, 98.66f, 74.30f };
    private static final float[] SCALER_STD = { 13.47f, 18.40f, 13.88f, 3.29f, 1.37f, 8.08f };

    private void predictRisk() {
        if (tflite == null) {
            Toast.makeText(this, "Modèle IA non chargé.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String sSys = etSystolic.getText().toString();
            String sDia = etDiastolic.getText().toString();
            String sBS = etBS.getText().toString();
            String sTemp = etBodyTemp.getText().toString();
            String sHR = etHeartRate.getText().toString();

            if (sSys.isEmpty() || sDia.isEmpty() || sBS.isEmpty() || sTemp.isEmpty() || sHR.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
                return;
            }

            float systolic = Float.parseFloat(sSys);
            float diastolic = Float.parseFloat(sDia);
            float bs = Float.parseFloat(sBS);
            float tempC = Float.parseFloat(sTemp);
            float heartRate = Float.parseFloat(sHR);

            if (!validateInputs(systolic, diastolic, bs, tempC, heartRate)) {
                return;
            }

            float tempF = (tempC * 9 / 5) + 32;

            // Prepare Input Tensor [1, 6] normalized
            float[][] inputVals = new float[1][6];
            inputVals[0][0] = (userAge - SCALER_MEAN[0]) / SCALER_STD[0];
            inputVals[0][1] = (systolic - SCALER_MEAN[1]) / SCALER_STD[1];
            inputVals[0][2] = (diastolic - SCALER_MEAN[2]) / SCALER_STD[2];
            inputVals[0][3] = (bs - SCALER_MEAN[3]) / SCALER_STD[3];
            inputVals[0][4] = (tempF - SCALER_MEAN[4]) / SCALER_STD[4];
            inputVals[0][5] = (heartRate - SCALER_MEAN[5]) / SCALER_STD[5];

            // Output Tensor [1, 3] -> 0: High, 1: Low, 2: Mid
            float[][] outputVals = new float[1][3];
            tflite.run(inputVals, outputVals);

            int maxIdx = 0;
            float maxVal = outputVals[0][0];
            for (int i = 1; i < 3; i++) {
                if (outputVals[0][i] > maxVal) {
                    maxVal = outputVals[0][i];
                    maxIdx = i;
                }
            }
            displayResult(maxIdx);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valeurs numériques invalides.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs(float systolic, float diastolic, float bs, float tempC, float heartRate) {
        if (systolic < 70 || systolic > 200) {
            etSystolic.setError("Valeur irréaliste (70-200)");
            etSystolic.requestFocus();
            return false;
        }
        if (diastolic < 40 || diastolic > 130) {
            etDiastolic.setError("Valeur irréaliste (40-130)");
            etDiastolic.requestFocus();
            return false;
        }
        if (bs < 3 || bs > 30) {
            etBS.setError("Valeur irréaliste (3-30 mmol/L)");
            etBS.requestFocus();
            return false;
        }
        if (tempC < 35 || tempC > 43) {
            etBodyTemp.setError("Base scientifique: 35.0 - 43.0 °C");
            etBodyTemp.requestFocus();
            return false;
        }
        if (heartRate < 40 || heartRate > 180) {
            etHeartRate.setError("Valeur irréaliste (40-180 bpm)");
            etHeartRate.requestFocus();
            return false;
        }
        return true;
    }

    private void displayResult(int resultClass) {
        cardResult.setVisibility(View.VISIBLE);
        int color;
        String main, sub;

        switch (resultClass) {
            case 0: // HIGH
                color = Color.parseColor("#D32F2F");
                main = "RISQUE ÉLEVÉ";
                sub = "Consultation immédiate recommandée auprès de votre médecin 🚨";
                cardResult.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
                break;
            case 2: // MID
                color = Color.parseColor("#F57C00");
                main = "RISQUE MODÉRÉ";
                sub = "Surveillez vos symptômes et reposez-vous bien 🍹";
                cardResult.setCardBackgroundColor(Color.parseColor("#FFF3E0"));
                break;
            case 1: // LOW
            default:
                color = Color.parseColor("#388E3C");
                main = "RISQUE FAIBLE";
                sub = "Tout semble normal. Continuez votre suivi régulier ✅";
                cardResult.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
                break;
        }

        tvResultMain.setText(main);
        tvResultMain.setTextColor(color);
        tvResultSub.setText(sub);
        
        // Scroll to result
        cardResult.post(() -> cardResult.requestFocus());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lux = event.values[0];
            WindowManager.LayoutParams layout = getWindow().getAttributes();
            if (lux < 10) {
                layout.screenBrightness = 0.2f;
                rootLayout.setBackgroundColor(Color.parseColor("#263238"));
            } else {
                layout.screenBrightness = -1f;
                rootLayout.setBackgroundColor(Color.parseColor("#F8F9FA"));
            }
            getWindow().setAttributes(layout);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && lightSensor != null)
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) sensorManager.unregisterListener(this);
    }
}
