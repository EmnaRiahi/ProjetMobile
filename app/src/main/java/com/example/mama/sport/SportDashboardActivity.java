package com.example.mama.sport;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mama.R;
import java.util.Locale;

public class SportDashboardActivity extends AppCompatActivity implements SensorEventListener {

    private TextView txtSteps, txtCalories, txtDistance, txtTime, txtStepProgress;
    private ProgressBar progressSteps;
    private LinearLayout layoutGoalAchieved;
    private ActiviteDatabase db;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    
    // Active session tracking
    private ActiviteEntity currentSession;
    private double magnitudePrevious = 0;
    private boolean isGoalAnimationPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_dashboard);

        db = ActiviteDatabase.getInstance(this);

        // UI Binding
        txtSteps = findViewById(R.id.txtSteps);
        txtStepProgress = findViewById(R.id.txtStepProgress);
        txtCalories = findViewById(R.id.txtCalories);
        txtDistance = findViewById(R.id.txtDistance);
        txtTime = findViewById(R.id.txtTime);
        progressSteps = findViewById(R.id.progressSteps);
        layoutGoalAchieved = findViewById(R.id.layoutGoalAchieved);

        // Sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Add Session Button
        findViewById(R.id.fabAdd).setOnClickListener(v ->
                new AddSessionDialog(this, null, this::loadActiveSession).show()
        );

        // Indoor / Outdoor Logic
        findViewById(R.id.cardIndoor).setOnClickListener(v -> {
            Intent intent = new Intent(this, ExercisesActivity.class);
            intent.putExtra("type", "INDOOR");
            startActivity(intent);
        });

        findViewById(R.id.cardOutdoor).setOnClickListener(v -> {
            Intent intent = new Intent(this, ExercisesActivity.class);
            intent.putExtra("type", "OUTDOOR");
            startActivity(intent);
        });

        // Click on Current Session Card -> Open History (Filtered for current/unachieved?)
        // User said: "When I tap on it, only new sessions should be shown." -> I assume "new" means current/unachieved.
        findViewById(R.id.stepCard).setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            intent.putExtra("filter", "UNACHIEVED");
            startActivity(intent);
        });



        loadActiveSession();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActiveSession();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void loadActiveSession() {
        new Thread(() -> {
            // Get the single active session
            currentSession = db.activiteDao().getActiveSession();
            runOnUiThread(() -> updateUI());
        }).start();
    }

    private void updateUI() {
        if (currentSession != null) {
            int steps = currentSession.steps;
            int target = currentSession.targetSteps;

            txtSteps.setText(String.valueOf(steps));
            txtStepProgress.setText(steps + " / " + target + " Steps");
            
            // Calc stats
            txtDistance.setText(String.format(Locale.getDefault(), "%.2f km", steps * 0.0007));
            txtCalories.setText(String.format(Locale.getDefault(), "%.1f kcal", steps * 0.04));
            
            // Duration is not really tracked in real-time in this snippet, but we can show what's in DB
            txtTime.setText(String.format(Locale.getDefault(), "%d min", currentSession.duration));

            progressSteps.setMax(target);
            progressSteps.setProgress(Math.min(steps, target));
        } else {
            // No active session
            txtSteps.setText("0");
            txtStepProgress.setText("Start a new session");
            progressSteps.setProgress(0);
            txtDistance.setText("0.00 km");
            txtCalories.setText("0.0 kcal");
            txtTime.setText("0 min");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (currentSession == null) return; // Don't count layout if no session active
        if (isGoalAnimationPlaying) return; // Pause counting during animation

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double magnitude = Math.sqrt(x * x + y * y + z * z);
            double magnitudeDelta = magnitude - magnitudePrevious;
            magnitudePrevious = magnitude;

            if (magnitudeDelta > 6) {
                currentSession.steps++;
                // Check Goal
                if (currentSession.steps >= currentSession.targetSteps && !currentSession.isAchieved) {
                    onGoalAchieved();
                } else {
                    // Update UI often, save to DB less often? 
                    // For safety, let's save every X steps or just update UI and save onPause?
                    // User requirement: "Save every step I make in local storage".
                    // Saving to DB on every step might be heavy, but requirement is explicit.
                    // Doing it in background thread.
                    saveSessionProgress();
                }
                updateUI();
            }
        }
    }

    private void saveSessionProgress() {
        new Thread(() -> {
             if (currentSession != null) {
                 db.activiteDao().update(currentSession);
             }
        }).start();
    }

    private void onGoalAchieved() {
        currentSession.isAchieved = true;
        isGoalAnimationPlaying = true;
        saveSessionProgress();

        // Show Animation
        layoutGoalAchieved.setVisibility(View.VISIBLE);
        layoutGoalAchieved.setAlpha(0f);
        layoutGoalAchieved.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        // Wait a bit then hide and reset UI
                        layoutGoalAchieved.postDelayed(() -> {
                            layoutGoalAchieved.animate()
                                    .alpha(0f)
                                    .setDuration(500)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            layoutGoalAchieved.setVisibility(View.GONE);
                                            isGoalAnimationPlaying = false;
                                            // Reload to clear the current session (since it's now Achieved)
                                            // The user said: "When a session becomes achieved, I want it to be removed from the Current Session list."
                                            // Since `getActiveSession()` only returns isAchieved=0, it will return null or the next unachieved one.
                                            loadActiveSession();
                                        }
                                    });
                        }, 2000);
                    }
                });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}

