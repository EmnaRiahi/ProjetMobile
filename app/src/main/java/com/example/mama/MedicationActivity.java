package com.example.mama;

import android.app.TimePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mama.meds.DrugResponse;
import com.example.mama.meds.OpenFdaService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MedicationActivity extends AppCompatActivity implements SensorEventListener {

    RecyclerView recyclerView;
    FloatingActionButton fab;
    MyDatabaseHelper myDB;
    List<Medication> medList;
    MedicationAdapter adapter;

    ConstraintLayout rootLayout;
    TextView tvHeader;

    private SensorManager sensorManager;
    private Sensor lightSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication);

        recyclerView = findViewById(R.id.recyclerMeds);
        fab = findViewById(R.id.fabAddMed);
        rootLayout = findViewById(R.id.rootLayout);
        tvHeader = findViewById(R.id.tvHeader);

        myDB = new MyDatabaseHelper(this);
        medList = new ArrayList<>();

        createNotificationChannel();
        requestNotificationPermission(); // Android 13+ runtime permission
        checkAlarmPermission(); // Android 12+ exact alarm check

        loadData();

        // On passe "this::showMedDialog" pour gérer le clic (Modification)
        adapter = new MedicationAdapter(this, medList, med -> showMedDialog(med));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // --- CAPTEUR LUMIÈRE ---
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }

        // Clic sur (+) -> Ajout (donc on passe null)
        fab.setOnClickListener(v -> showMedDialog(null));

        // --- BOUTON IA SANTÉ MATERNELLE ---
        Button btnOpenAI = findViewById(R.id.btnOpenAI);
        btnOpenAI.setOnClickListener(v -> {
            startActivity(new android.content.Intent(MedicationActivity.this, MaternalHealthRiskActivity.class));
        });

    }

    // --- PERMISSIONS ---
    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { android.Manifest.permission.POST_NOTIFICATIONS }, 101);
            }
        }
    }

    private void checkAlarmPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) { // Android 12+
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // User needs to grant permission manually
                Toast.makeText(this, "Veuillez autoriser les alarmes exactes dans les paramètres.", Toast.LENGTH_LONG)
                        .show();
                android.content.Intent intent = new android.content.Intent(
                        android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    void loadData() {
        medList.clear();
        Cursor cursor = myDB.getAllMedications();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                medList.add(new Medication(
                        cursor.getString(0), // ID
                        cursor.getString(1), // Name
                        cursor.getString(2), // Generic
                        cursor.getString(3), // Time
                        cursor.getString(4) // Doses
                ));
            }
        }
    }

    // --- LOGIQUE CAPTEUR : BAISSE DE LUMINOSITÉ ÉCRAN ---
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lux = event.values[0];
            WindowManager.LayoutParams layout = getWindow().getAttributes();

            if (lux < 10) {
                // MODE NUIT : On baisse la luminosité de l'écran à 20%
                layout.screenBrightness = 0.2f;
                rootLayout.setBackgroundColor(Color.parseColor("#263238"));
                tvHeader.setText("Médicaments (Mode Nuit 🌙)");
            } else {
                // MODE JOUR : Luminosité automatique (-1)
                layout.screenBrightness = -1f;
                rootLayout.setBackgroundColor(Color.parseColor("#F5F5F5"));
                tvHeader.setText("Mes Médicaments");
            }
            getWindow().setAttributes(layout);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && lightSensor != null)
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
    }

    // --- DIALOGUE (AJOUT OU MODIF) ---
    void showMedDialog(Medication medToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_med, null);
        builder.setView(view);

        TextView tvTitle = view.findViewById(R.id.tvTitleDialog);
        EditText etName = view.findViewById(R.id.etMedName);
        EditText etDoses = view.findViewById(R.id.etMedDoses);
        EditText etTime = view.findViewById(R.id.etMedTime);
        TextView tvApiResult = view.findViewById(R.id.tvApiResult);
        Button btnCheck = view.findViewById(R.id.btnCheckApi);

        // Pré-remplissage si modification
        if (medToEdit != null) {
            tvTitle.setText("Modifier Médicament");
            etName.setText(medToEdit.name);
            tvApiResult.setText(medToEdit.generic);
            etDoses.setText(medToEdit.doses);
            etTime.setText(medToEdit.time);
        }

        etTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this,
                    (view1, hour, minute) -> etTime.setText(String.format("%02d:%02d", hour, minute)),
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        btnCheck.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty())
                checkDrugWithAPI(name, tvApiResult);
        });

        String btnText = (medToEdit == null) ? "Ajouter" : "Modifier";

        builder.setPositiveButton(btnText, (dialog, which) -> {
            String name = etName.getText().toString();
            String generic = tvApiResult.getText().toString();
            String doses = etDoses.getText().toString();
            String time = etTime.getText().toString();

            if (!name.isEmpty() && !time.isEmpty()) {
                if (medToEdit == null) {
                    // AJOUT
                    myDB.addMedication(name, generic, time, doses);
                    scheduleNotification(time, name); // Schedule Notification
                    sendTestNotification(name); // TEST: Immediate notification
                    Toast.makeText(this, "Ajouté + Rappel activé !", Toast.LENGTH_SHORT).show();
                } else {
                    // MODIF
                    myDB.updateMedication(medToEdit.id, name, generic, time, doses);
                    scheduleNotification(time, name); // Update Notification
                    Toast.makeText(this, "Modifié + Rappel mis à jour !", Toast.LENGTH_SHORT).show();
                }

                loadData();
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.create().show();
    }

    // --- NOTIFICATIONS ---
    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Rappels Médicaments";
            String description = "Canal pour les rappels de prise de médicaments";
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    MedicationAlarmReceiver.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);

            android.app.NotificationManager notificationManager = getSystemService(
                    android.app.NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void scheduleNotification(String timeStr, String medName) {
        android.util.Log.d("MedSchedule", "=== scheduleNotification called ===");
        android.util.Log.d("MedSchedule", "Time: " + timeStr + ", Med: " + medName);

        try {
            // Parse Time
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            long triggerTime = calendar.getTimeInMillis();
            long now = System.currentTimeMillis();

            android.util.Log.d("MedSchedule", "Trigger time (raw): " + triggerTime);
            android.util.Log.d("MedSchedule", "Current time: " + now);

            // If time is in past, set for tomorrow
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DATE, 1);
                android.util.Log.d("MedSchedule",
                        "Time was in past, added 1 day. New trigger: " + calendar.getTimeInMillis());
            }

            long delayMs = calendar.getTimeInMillis() - System.currentTimeMillis();
            android.util.Log.d("MedSchedule", "Alarm will fire in: " + (delayMs / 1000) + " seconds");

            android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(Context.ALARM_SERVICE);
            android.content.Intent intent = new android.content.Intent(this, MedicationAlarmReceiver.class);
            intent.putExtra("medName", medName);

            // Generate unique ID based on medName hash to handle multiple alarms
            int alarmId = medName.hashCode();
            android.util.Log.d("MedSchedule", "Alarm ID: " + alarmId);

            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                    this, alarmId, intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);

            if (alarmManager != null) {
                // Check if we can schedule exact alarms (Android 12+)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    if (!alarmManager.canScheduleExactAlarms()) {
                        android.util.Log.e("MedSchedule", "CANNOT schedule exact alarms! Permission denied.");
                        Toast.makeText(this, "⚠️ Alarmes exactes non autorisées!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                // Use setExactAndAllowWhileIdle for reliable timing (Android 6.0+)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(), pendingIntent);
                    android.util.Log.d("MedSchedule", "✅ setExactAndAllowWhileIdle called!");
                } else {
                    alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                            pendingIntent);
                    android.util.Log.d("MedSchedule", "✅ setExact called!");
                }

                Toast.makeText(this, "⏰ Rappel programmé pour " + timeStr, Toast.LENGTH_SHORT).show();
            } else {
                android.util.Log.e("MedSchedule", "AlarmManager is NULL!");
            }
        } catch (Exception e) {
            android.util.Log.e("MedSchedule", "Exception: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la programmation du rappel", Toast.LENGTH_SHORT).show();
        }
    }

    // --- TEST: Send immediate notification to verify channel works ---
    private void sendTestNotification(String medName) {
        android.app.NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);

        android.content.Intent tapIntent = new android.content.Intent(this, MedicationActivity.class);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                this, 0, tapIntent, android.app.PendingIntent.FLAG_IMMUTABLE);

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this,
                MedicationAlarmReceiver.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Test Notification 🧪")
                .setContentText("Si vous voyez ceci, les notifications fonctionnent! Med: " + medName)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(9999, builder.build());
        android.util.Log.d("MedSchedule", "🧪 Test notification sent!");
    }

    void checkDrugWithAPI(String brandName, TextView output) {
        output.setText("Recherche...");
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.fda.gov/")
                .addConverterFactory(GsonConverterFactory.create()).build();
        OpenFdaService service = retrofit.create(OpenFdaService.class);
        service.searchDrug("openfda.brand_name:\"" + brandName + "\"", 1).enqueue(new Callback<DrugResponse>() {
            @Override
            public void onResponse(Call<DrugResponse> call, Response<DrugResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().results.isEmpty()) {
                    try {
                        String generic = response.body().results.get(0).openfda.generic_name.get(0);
                        output.setText(generic);
                        output.setTextColor(Color.parseColor("#4CAF50"));
                    } catch (Exception e) {
                        output.setText("Nom générique introuvable.");
                    }
                } else {
                    output.setText("Inconnu (FDA)");
                    output.setTextColor(Color.RED);
                }
            }

            @Override
            public void onFailure(Call<DrugResponse> call, Throwable t) {
                output.setText("Erreur réseau");
            }
        });
    }
}