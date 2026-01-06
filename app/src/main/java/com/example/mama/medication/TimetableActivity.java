package com.example.mama.medication;

import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.mama.user.MyDatabaseHelper;
import com.example.mama.R;

public class TimetableActivity extends AppCompatActivity implements SensorEventListener {

    private RecyclerView recyclerView;
    private TimetableAdapter adapter;
    private List<ScheduledMedication> scheduledList = new ArrayList<>();
    private MyDatabaseHelper myDB;

    private CoordinatorLayout rootLayout;
    private TextView tvTodayTitle;
    private SensorManager sensorManager;
    private Sensor lightSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        rootLayout = findViewById(R.id.timetableRoot);
        tvTodayTitle = findViewById(R.id.tvTodayTitle);

        Toolbar toolbar = findViewById(R.id.toolbarTimetable);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerTimetable);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        myDB = new MyDatabaseHelper(this);
        loadTodaySchedule();

        // --- SENSOR ---
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lux = event.values[0];
            WindowManager.LayoutParams layout = getWindow().getAttributes();

            if (lux < 10) {
                // MODE NUIT
                layout.screenBrightness = 0.2f;
                rootLayout.setBackgroundColor(Color.parseColor("#263238"));
                if (tvTodayTitle != null) {
                    tvTodayTitle.setTextColor(Color.WHITE);
                    tvTodayTitle.setText("Planning (Mode Nuit 🌙)");
                }
            } else {
                // MODE JOUR
                layout.screenBrightness = -1f;
                rootLayout.setBackgroundColor(Color.parseColor("#F5F5F5"));
                if (tvTodayTitle != null) {
                    tvTodayTitle.setTextColor(Color.parseColor("#2D3436"));
                    tvTodayTitle.setText("Aujourd'hui");
                }
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

    private void loadTodaySchedule() {
        scheduledList.clear();
        Cursor cursor = myDB.getAllMedications();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                String doses = cursor.getString(4);
                String schedule = cursor.getString(6);
                String time = cursor.getString(3);

                if (schedule != null) {
                    if (schedule.contains("Matin")) scheduledList.add(new ScheduledMedication(id, name, doses, "08:00", "Matin"));
                    if (schedule.contains("Midi")) scheduledList.add(new ScheduledMedication(id, name, doses, "12:00", "Midi"));
                    if (schedule.contains("Soir")) scheduledList.add(new ScheduledMedication(id, name, doses, "18:00", "Soir"));
                    if (schedule.contains("Nuit")) scheduledList.add(new ScheduledMedication(id, name, doses, "22:00", "Nuit"));
                }
                if (time != null && time.contains(":")) {
                    scheduledList.add(new ScheduledMedication(id, name, doses, time, "Spécifique"));
                }
            }
            cursor.close();
        }

        // Trier par heure
        Collections.sort(scheduledList, new Comparator<ScheduledMedication>() {
            @Override
            public int compare(ScheduledMedication o1, ScheduledMedication o2) {
                return o1.time.compareTo(o2.time);
            }
        });

        adapter = new TimetableAdapter(scheduledList, this);
        recyclerView.setAdapter(adapter);
    }

    public static class ScheduledMedication {
        public String medId;
        public String name;
        public String dose;
        public String time;
        public String slot;

        public ScheduledMedication(String medId, String name, String dose, String time, String slot) {
            this.medId = medId;
            this.name = name;
            this.dose = dose;
            this.time = time;
            this.slot = slot;
        }
    }
}
