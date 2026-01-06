package com.example.mama;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputEditText;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class MaternalHealthRiskActivity extends AppCompatActivity {

    private Interpreter tflite;
    private MyDatabaseHelper myDB;

    // UI Components
    private TextView tvAgeDisplay, tvResultMain, tvResultSub;
    private TextInputEditText etSystolic, etDiastolic, etBS, etBodyTemp, etHeartRate;
    private Button btnPredict;
    private CardView cardResult;

    // Data
    private int userAge = 25; // Default fallback

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maternal_health_risk);

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
    }

    private void loadUserAge() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String email = prefs.getString("email", "");

        if (!email.isEmpty()) {
            Cursor cursor = myDB.getUserDetails(email);
            if (cursor != null && cursor.moveToFirst()) {
                // Assuming 'age' is column index 8 based on DATABASE_VERSION 7 schema in
                // MyDatabaseHelper
                // Or safely get by name if possible, column name is "age"
                try {
                    int colAgeIndex = cursor.getColumnIndex("age");
                    if (colAgeIndex != -1) {
                        userAge = cursor.getInt(colAgeIndex);
                        tvAgeDisplay.setText(userAge + " ans");
                    } else {
                        tvAgeDisplay.setText("Age non trouvé (Defaut: 25)");
                    }
                } catch (Exception e) {
                    tvAgeDisplay.setText("Erreur Age");
                }
                cursor.close();
            }
        } else {
            tvAgeDisplay.setText("Non connecté");
        }
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("maternal_health_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // ==========================================
    // 1. PARAMÈTRES DE NORMALISATION (STANDARDIZATION)
    // ==========================================
    private static final float[] SCALER_MEAN = { 29.87f, 113.19f, 76.46f, 8.72f, 98.66f, 74.30f };
    private static final float[] SCALER_STD = { 13.47f, 18.40f, 13.88f, 3.29f, 1.37f, 8.08f };

    private void predictRisk() {
        if (tflite == null) {
            Toast.makeText(this, "Modèle IA non chargé.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Get Inputs
            String sSys = etSystolic.getText().toString();
            String sDia = etDiastolic.getText().toString();
            String sBS = etBS.getText().toString();
            String sTemp = etBodyTemp.getText().toString(); // Celsius
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

            // Convert Temp C -> F
            float tempF = (tempC * 9 / 5) + 32;

            // Apply Standardization (Z-Score)
            // Order: Age, SystolicBP, DiastolicBP, BS, BodyTemp, HeartRate

            float normAge = standardize((float) userAge, 0);
            float normSys = standardize(systolic, 1);
            float normDia = standardize(diastolic, 2);
            // Wait, the array SCALER_MEAN has 6 items. Index 3 is BS.
            float normBS_std = standardize(bs, 3);
            float normTemp = standardize(tempF, 4);
            float normHR = standardize(heartRate, 5);

            // Prepare Input Tensor [1, 6]
            float[][] inputVals = new float[1][6];
            inputVals[0][0] = normAge;
            inputVals[0][1] = normSys;
            inputVals[0][2] = normDia;
            inputVals[0][3] = normBS_std;
            inputVals[0][4] = normTemp;
            inputVals[0][5] = normHR;

            // Output Tensor [1, 3] -> 0: High, 1: Low, 2: Mid
            float[][] outputVals = new float[1][3];
            tflite.run(inputVals, outputVals);

            // Analyze Result
            int maxIdx = -1;
            float maxVal = -1;
            for (int i = 0; i < 3; i++) {
                if (outputVals[0][i] > maxVal) {
                    maxVal = outputVals[0][i];
                    maxIdx = i;
                }
            }
            displayResult(maxIdx);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valeurs numériques invalides.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur d'inférence: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void displayResult(int resultClass) {
        cardResult.setVisibility(View.VISIBLE);

        switch (resultClass) {
            case 0:
                tvResultMain.setText("RISQUE ÉLEVÉ");
                tvResultMain.setTextColor(Color.parseColor("#F44336")); // Red
                tvResultSub.setText("Vous devez prendre vos médicaments ou consulter un médecin rapidement 🚨");
                cardResult.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
                break;
            case 1:
                tvResultMain.setText("RISQUE FAIBLE");
                tvResultMain.setTextColor(Color.parseColor("#4CAF50")); // Green
                tvResultSub.setText("Tout va bien ! Continuez ainsi ✅");
                cardResult.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
                break;
            case 2:
                tvResultMain.setText("RISQUE MODÉRÉ");
                tvResultMain.setTextColor(Color.parseColor("#FF9800")); // Orange
                tvResultSub.setText("Vous avez besoin de vous relaxer, je vous conseille de prendre une tisane 🍵");
                cardResult.setCardBackgroundColor(Color.parseColor("#FFF3E0"));
                break;
            default:
                tvResultMain.setText("INCERTAIN");
                tvResultSub.setText("Réessayez avec des valeurs précises");
        }
    }

    private float standardize(float value, int index) {
        return (value - SCALER_MEAN[index]) / SCALER_STD[index];
    }
}
