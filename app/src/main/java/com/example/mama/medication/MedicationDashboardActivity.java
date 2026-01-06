package com.example.mama.medication;

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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mama.user.MyDatabaseHelper;
import com.example.mama.R;

import java.util.ArrayList;
import java.util.List;

public class MedicationDashboardActivity extends AppCompatActivity implements SensorEventListener {

    private ProgressBar progressGlobal;
    private TextView tvGlobalPercentage, tvTotalTaken, tvTotalMissed;
    private RecyclerView recyclerAdherence;
    private MyDatabaseHelper myDB;
    private AdherenceAdapter adapter;
    private List<AdherenceStat> statList = new ArrayList<>();

    private CoordinatorLayout rootLayout;
    private SensorManager sensorManager;
    private Sensor lightSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbarStats);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        progressGlobal = findViewById(R.id.progressGlobal);
        tvGlobalPercentage = findViewById(R.id.tvGlobalPercentage);
        tvTotalTaken = findViewById(R.id.tvTotalTaken);
        tvTotalMissed = findViewById(R.id.tvTotalMissed);
        recyclerAdherence = findViewById(R.id.recyclerAdherence);
        rootLayout = findViewById(R.id.dashboardRoot);

        myDB = new MyDatabaseHelper(this);
        loadStatistics();

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
            } else {
                // MODE JOUR
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

    private void loadStatistics() {
        statList.clear();
        Cursor cursor = myDB.getAdherenceStats();
        
        int totalTaken = 0;
        int totalMissed = 0;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                int taken = cursor.getInt(1);
                int missed = cursor.getInt(2);
                
                statList.add(new AdherenceStat(name, taken, missed));
                totalTaken += taken;
                totalMissed += missed;
            }
            cursor.close();
        }

        tvTotalTaken.setText(String.valueOf(totalTaken));
        tvTotalMissed.setText(String.valueOf(totalMissed));

        int totalExpected = totalTaken + totalMissed;
        if (totalExpected > 0) {
            int percentage = (totalTaken * 100) / totalExpected;
            progressGlobal.setProgress(percentage);
            tvGlobalPercentage.setText(percentage + "%");
        } else {
            progressGlobal.setProgress(0);
            tvGlobalPercentage.setText("0%");
        }

        adapter = new AdherenceAdapter(statList);
        recyclerAdherence.setLayoutManager(new LinearLayoutManager(this));
        recyclerAdherence.setAdapter(adapter);
    }

    // Models & Adapter
    static class AdherenceStat {
        String name;
        int taken;
        int missed;
        AdherenceStat(String name, int taken, int missed) {
            this.name = name; this.taken = taken; this.missed = missed;
        }
    }

    class AdherenceAdapter extends RecyclerView.Adapter<AdherenceAdapter.VH> {
        List<AdherenceStat> list;
        AdherenceAdapter(List<AdherenceStat> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_adherence, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            AdherenceStat stat = list.get(position);
            holder.tvName.setText(stat.name);
            int total = stat.taken + stat.missed;
            int perc = total > 0 ? (stat.taken * 100) / total : 0;
            
            holder.tvPercentage.setText(perc + "%");
            holder.progressBar.setProgress(perc);
            holder.tvCounts.setText(stat.taken + " pris / " + total + " total");
            
            // Color logic based on performance
            if (perc >= 80) holder.tvPercentage.setTextColor(Color.parseColor("#4CAF50"));
            else if (perc >= 50) holder.tvPercentage.setTextColor(Color.parseColor("#FF9800"));
            else holder.tvPercentage.setTextColor(Color.parseColor("#F44336"));
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvPercentage, tvCounts;
            ProgressBar progressBar;
            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvMedName);
                tvPercentage = v.findViewById(R.id.tvMedPercentage);
                tvCounts = v.findViewById(R.id.tvMedCounts);
                progressBar = v.findViewById(R.id.progressMed);
            }
        }
    }
}
