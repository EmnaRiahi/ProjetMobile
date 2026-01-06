package com.example.mama;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HealthTrackerActivity extends AppCompatActivity {

    private MyDatabaseHelper db;
    private List<HealthMetric> metricsList = new ArrayList<>();
    private HealthMetricAdapter adapter;
    private LineChart lineChart;
    private TextView tvHealthInsight;
    private SleepSession lastSleepSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_tracker);

        db = new MyDatabaseHelper(this);

        // Initialisation des vues
        lineChart = findViewById(R.id.lineChart);
        tvHealthInsight = findViewById(R.id.tvHealthInsight);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        // RecyclerView pour Poids/Tension
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false); // Important pour le scroll dans NestedScrollView
        adapter = new HealthMetricAdapter(this, metricsList, new HealthMetricAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(HealthMetric metric) {
                showAddEditDialog(metric);
            }

            @Override
            public void onDeleteClick(HealthMetric metric) {
                confirmDelete(metric);
            }
        });
        recyclerView.setAdapter(adapter);

        // Bouton pour ajouter une entrée Poids/Tension
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showAddEditDialog(null));

        // Charger toutes les données
        loadAllData();
    }

    private void loadAllData() {
        loadHealthMetrics();
        loadLastSleepSession();
        generateHealthInsights();
    }

    private void loadHealthMetrics() {
        metricsList.clear();
        Cursor cursor = db.getAllHealthMetrics();
        if (cursor.moveToFirst()) {
            do {
                int idCol = cursor.getColumnIndex("id");
                int dateCol = cursor.getColumnIndex("date");
                int weightCol = cursor.getColumnIndex("weight");
                int systolicCol = cursor.getColumnIndex("systolic");
                int diastolicCol = cursor.getColumnIndex("diastolic");

                metricsList.add(new HealthMetric(
                        cursor.getString(idCol),
                        cursor.getString(dateCol),
                        cursor.getFloat(weightCol),
                        cursor.getInt(systolicCol),
                        cursor.getInt(diastolicCol)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
        setupLineChart();
        updateLineChartData();
    }

    private void loadLastSleepSession() {
        lastSleepSession = db.getLastSleepSession();
        updateSleepUI();
    }

    private void generateHealthInsights() {
        if (lastSleepSession == null && metricsList.isEmpty()) {
            tvHealthInsight.setText("Bienvenue ! Enregistrez vos données pour obtenir des conseils.");
            return;
        }

        if (lastSleepSession != null) {
            if (lastSleepSession.getSoundEvents() > 10) {
                tvHealthInsight.setText(
                        "Plusieurs événements sonores ont été détectés cette nuit. Un environnement calme favorise un meilleur sommeil.");
                return;
            }
            if (lastSleepSession.getDeepSleepMinutes() < 60) {
                tvHealthInsight.setText(
                        "Votre temps de sommeil profond semble court. Essayez de vous détendre avant de dormir.");
                return;
            }
        }

        if (!metricsList.isEmpty()) {
            HealthMetric lastMetric = metricsList.get(metricsList.size() - 1);
            if (lastMetric.getSystolic() > 140 || lastMetric.getDiastolic() > 90) {
                tvHealthInsight.setText("Votre tension semble élevée. Pensez à consulter un professionnel de santé.");
                return;
            }
        }

        tvHealthInsight.setText("Vos constantes semblent stables. Continuez comme ça !");
    }

    private void showAddEditDialog(final HealthMetric metric) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_health_metric, null);
        builder.setView(dialogView);

        final EditText etDate = dialogView.findViewById(R.id.etDate);
        final EditText etWeight = dialogView.findViewById(R.id.etWeight);
        final EditText etSystolic = dialogView.findViewById(R.id.etSystolic);
        final EditText etDiastolic = dialogView.findViewById(R.id.etDiastolic);

        if (metric != null) {
            // Mode édition
            builder.setTitle("Modifier l'entrée");
            etDate.setText(metric.getDate());
            etWeight.setText(String.valueOf(metric.getWeight()));
            etSystolic.setText(String.valueOf(metric.getSystolic()));
            etDiastolic.setText(String.valueOf(metric.getDiastolic()));
        } else {
            // Mode ajout
            builder.setTitle("Ajouter une entrée");
            // Pré-remplir la date actuelle
            etDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        }

        builder.setPositiveButton("Sauvegarder", (dialog, which) -> {
            String date = etDate.getText().toString().trim();
            float weight = Float.parseFloat(etWeight.getText().toString().trim());
            int systolic = Integer.parseInt(etSystolic.getText().toString().trim());
            int diastolic = Integer.parseInt(etDiastolic.getText().toString().trim());

            if (metric != null) {
                db.updateHealthMetric(metric.getId(), date, weight, systolic, diastolic);
                Toast.makeText(this, "Entrée mise à jour", Toast.LENGTH_SHORT).show();
            } else {
                db.addHealthMetric(date, weight, systolic, diastolic);
                Toast.makeText(this, "Entrée ajoutée", Toast.LENGTH_SHORT).show();
            }
            loadAllData(); // Recharger toutes les données
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void confirmDelete(final HealthMetric metric) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmer la suppression")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette entrée ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    db.deleteHealthMetric(metric.getId());
                    Toast.makeText(this, "Entrée supprimée", Toast.LENGTH_SHORT).show();
                    loadAllData(); // Recharger toutes les données
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
    }

    private void updateLineChartData() {
        if (metricsList.isEmpty()) {
            lineChart.clear();
            lineChart.invalidate();
            return;
        }

        ArrayList<Entry> weightEntries = new ArrayList<>();
        ArrayList<Entry> systolicEntries = new ArrayList<>();
        ArrayList<Entry> diastolicEntries = new ArrayList<>();
        final ArrayList<String> xLabels = new ArrayList<>();

        for (int i = 0; i < metricsList.size(); i++) {
            HealthMetric metric = metricsList.get(i);
            weightEntries.add(new Entry(i, metric.getWeight()));
            systolicEntries.add(new Entry(i, metric.getSystolic()));
            diastolicEntries.add(new Entry(i, metric.getDiastolic()));
            // Garder seulement la partie jour/mois pour l'affichage
            xLabels.add(metric.getDate().substring(5));
        }

        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return xLabels.get((int) value % xLabels.size());
            }
        });

        LineDataSet weightDataSet = new LineDataSet(weightEntries, "Poids (kg)");
        weightDataSet.setColor(Color.BLUE);
        weightDataSet.setCircleColor(Color.BLUE);
        weightDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        weightDataSet.setDrawFilled(true);
        weightDataSet.setFillColor(Color.BLUE);
        weightDataSet.setFillAlpha(30);

        LineDataSet systolicDataSet = new LineDataSet(systolicEntries, "Tension Systolique");
        systolicDataSet.setColor(Color.RED);
        systolicDataSet.setCircleColor(Color.RED);
        systolicDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineDataSet diastolicDataSet = new LineDataSet(diastolicEntries, "Tension Diastolique");
        diastolicDataSet.setColor(Color.GREEN);
        diastolicDataSet.setCircleColor(Color.GREEN);
        diastolicDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(weightDataSet, systolicDataSet, diastolicDataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // Rafraîchir le graphique
    }

    private void updateSleepUI() {
        if (lastSleepSession == null) {
            return;
        }

        // Calculate Score
        int totalMinutes = lastSleepSession.getDeepSleepMinutes() + lastSleepSession.getLightSleepMinutes();
        int score = calculateSleepScore(totalMinutes, lastSleepSession.getDeepSleepMinutes(),
                lastSleepSession.getSoundEvents());

        // Update Score UI
        android.widget.ProgressBar progressBarScore = findViewById(R.id.progressBarScore);
        TextView tvSleepScore = findViewById(R.id.tvSleepScore);
        TextView tvSleepDate = findViewById(R.id.tvSleepDate);

        progressBarScore.setProgress(score);
        tvSleepScore.setText(String.valueOf(score));
        tvSleepDate.setText(lastSleepSession.getDate());

        // Update Stats Grid
        TextView tvTotalDurationVal = findViewById(R.id.tvTotalDurationVal);
        TextView tvDeepSleepVal = findViewById(R.id.tvDeepSleepVal);
        TextView tvLightSleepVal = findViewById(R.id.tvLightSleepVal);
        TextView tvSoundEventsVal = findViewById(R.id.tvSoundEventsVal);

        tvTotalDurationVal.setText(formatDuration(totalMinutes));
        tvDeepSleepVal.setText(formatDuration(lastSleepSession.getDeepSleepMinutes()));
        tvLightSleepVal.setText(formatDuration(lastSleepSession.getLightSleepMinutes()));
        tvSoundEventsVal.setText(String.valueOf(lastSleepSession.getSoundEvents()));
    }

    private int calculateSleepScore(int totalMinutes, int deepMinutes, int soundEvents) {
        // Simple algorithm:
        // Duration: 50 pts (Target 8h = 480m)
        // Deep Sleep: 30 pts (Target 2h = 120m)
        // Sound: 20 pts (Target 0 events)

        int durationScore = Math.min(50, (int) ((totalMinutes / 480.0) * 50));
        int deepScore = Math.min(30, (int) ((deepMinutes / 120.0) * 30));
        int soundScore = Math.max(0, 20 - (soundEvents * 2)); // Lose 2 pts per sound event

        return durationScore + deepScore + soundScore;
    }

    private String formatDuration(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return String.format(Locale.getDefault(), "%dh %02dm", h, m);
    }
}
