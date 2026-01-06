package com.example.mama.sport;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
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
import com.example.mama.user.MyDatabaseHelper;

import java.util.Locale;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.Manifest;

public class SportDashboardActivity extends AppCompatActivity implements SensorEventListener {

    private TextView txtSteps, txtCalories, txtDistance, txtTime, txtStepProgress;
    private TextView lblStepsTitle, lblCaloriesTitle; 
    private TextView txtDailySteps, txtDailyCals;
    private TextView txtDailyStepsTargetText, txtDailyCalsTargetText;
    private ProgressBar progressSteps, progressDailySteps, progressDailyCals;
    private LinearLayout layoutGoalAchieved;
    
    // Weather Views
    private TextView txtCityName, txtCondition, txtTemperature, txtWeatherRec;
    private android.widget.ImageView imgWeatherIcon, imgRecIcon;
    private LinearLayout layoutWeatherRec;

    private ActiviteDatabase db;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    
    // Active session tracking
    private ActiviteEntity currentSession;
    private double magnitudePrevious = 0;
    private boolean isGoalAnimationPlaying = false;
    private static final String CHANNEL_ID = "step_goal_channel";
    
    // Real-time daily stats
    private int stepsFromOtherSessionsToday = 0;
    private int dailyStepsTotal = 0;

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
        txtTime = findViewById(R.id.txtTime);
        progressSteps = findViewById(R.id.progressSteps);
        layoutGoalAchieved = findViewById(R.id.layoutGoalAchieved);
        
        // Daily Summary Binding
        lblStepsTitle = findViewById(R.id.lblStepsTitle);
        lblCaloriesTitle = findViewById(R.id.lblCaloriesTitle);
        txtDailySteps = findViewById(R.id.txtDailySteps);
        txtDailyCals = findViewById(R.id.txtDailyCals);
        
        progressDailySteps = findViewById(R.id.progressDailySteps);
        progressDailyCals = findViewById(R.id.progressDailyCals);
        txtDailyStepsTargetText = findViewById(R.id.txtDailyStepsTargetText);
        txtDailyCalsTargetText = findViewById(R.id.txtDailyCalsTargetText);
        
        // Weather Binding
        txtCityName = findViewById(R.id.txtCityName);
        txtCondition = findViewById(R.id.txtCondition);
        txtTemperature = findViewById(R.id.txtTemperature);
        imgWeatherIcon = findViewById(R.id.imgWeatherIcon);
        
        layoutWeatherRec = findViewById(R.id.layoutWeatherRec);
        txtWeatherRec = findViewById(R.id.txtWeatherRec);
        imgRecIcon = findViewById(R.id.imgRecIcon);

        fetchWeather();

        // Sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Add Session Button
        findViewById(R.id.fabAdd).setOnClickListener(v ->
                new AddSessionDialog(this, null, targetSteps, this::loadActiveSession).show()
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
        createNotificationChannel();
        checkNotificationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActiveSession();
        loadWeeklyStats(); 
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void loadWeeklyStats() {
        new Thread(() -> {
            // Weekly Chart Data
            // We need 7 days of data. Simple aggregation.
            java.util.List<ActiviteEntity> allActivities = db.activiteDao().getAllActivities();
            java.util.Map<String, Integer> dailySteps = new java.util.HashMap<>();
            
            // Generate last 7 dates
            java.util.List<String> last7Days = new java.util.ArrayList<>();
            
            // Correct loop:
            for(int i=6; i>=0; i--) {
               java.util.Calendar c = java.util.Calendar.getInstance();
               c.add(java.util.Calendar.DAY_OF_YEAR, -i);
               last7Days.add(new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(c.getTime()));
            }

            for (ActiviteEntity a : allActivities) {
                dailySteps.put(a.date, dailySteps.getOrDefault(a.date, 0) + a.steps);
            }

            // Prepare Chart Data
            java.util.List<Float> chartPoints = new java.util.ArrayList<>();
            java.util.List<String> chartLabels = new java.util.ArrayList<>();
            
            for (String day : last7Days) {
                int steps = dailySteps.getOrDefault(day, 0);
                float pct = Math.min(100f, (steps / 6000f) * 100f);
                chartPoints.add(pct);
                chartLabels.add(day.substring(0, 2));
            }


            runOnUiThread(() -> {
                TextView txtDailyStepsRef = findViewById(R.id.txtDailySteps); 
                TextView txtDailyCalsRef = findViewById(R.id.txtDailyCals);   
                
                String today = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new java.util.Date());
                // Calculate baseline: steps from finished sessions (or all sessions excluding current if we could filter, 
                // but simpler: sum all for today from DB, subtract current session valid steps if it's already saved in DB)
                // Actually, `allActivities` comes from DB. 
                int dbStepsToday = dailySteps.getOrDefault(today, 0);
                
                // If currentSession is saved in DB, it's included in dbStepsToday.
                // We want stepsFromOtherSessions = dbStepsToday - (currentSession.steps if it is in DB).
                // This is getting tricky. 
                // Simpler approach: `stepsFromOtherSessionsToday` = Sum of steps of all activities today WHERE id != currentSession.id
                // Since we iterate `allActivities`, let's do it there.
                
                int otherSteps = 0;
                int currentId = (currentSession != null) ? currentSession.id : -1;
                for (ActiviteEntity a : allActivities) {
                    if (a.date.equals(today)) {
                         if (a.id != currentId) {
                             otherSteps += a.steps;
                         }
                    }
                }
                stepsFromOtherSessionsToday = otherSteps;

                // Update Chart
                TrendChartView chart = findViewById(R.id.chartView); 
                if (chart != null) {
                    chart.setData(chartPoints, chartLabels);
                }
                
                // Trigger an immediate UI update to refresh totals including current session
                updateUI();
            });
        }).start();
        
        loadPersonalizedAdvice();
    }
    
    // Default goals if no user found
    private int targetSteps = 6000;
    private double targetCalories = 300;

    private void loadPersonalizedAdvice() {
         String email = getSharedPreferences("user_session", MODE_PRIVATE).getString("email", null);
         if (email == null) return;

         android.database.Cursor cursor = new MyDatabaseHelper(this).getUserDetails(email);
         if (cursor != null && cursor.moveToFirst()) {
             // Indexes based on MyDatabaseHelper creation order
             int idxWeight = cursor.getColumnIndex("weight");
             int idxHeight = cursor.getColumnIndex("height");
             int idxAge = cursor.getColumnIndex("age");
             int idxWeek = cursor.getColumnIndex("pregnancy_week");
             
             if (idxWeight != -1 && idxHeight != -1 && idxAge != -1) {
                 double weight = cursor.getDouble(idxWeight);
                 double height = cursor.getDouble(idxHeight);
                 int age = cursor.getInt(idxAge);
                 int week = cursor.getInt(idxWeek);
                 
                 // BMI Calc
                 double heightM = height / 100.0;
                 double bmi = weight / (heightM * heightM);
                 
                 String bmiCategory;
                 String stepAdvice;
                 
                 if (bmi < 18.5) {
                     bmiCategory = "Insuffisance pondérale";
                     targetSteps = 5000; // Moderate
                     targetCalories = 2200; 
                     stepAdvice = "Activité légère. Concentrez-vous sur la nutrition.";
                 } else if (bmi < 25) {
                     bmiCategory = "Poids normal";
                     targetSteps = 8000;
                     targetCalories = 350; 
                     stepAdvice = "Maintenez un mode de vie actif.";
                 } else if (bmi < 30) {
                     bmiCategory = "Surpoids";
                     targetSteps = 6000;
                     targetCalories = 300;
                     stepAdvice = "Surveillez la prise de poids. Marchez quotidiennement.";
                 } else {
                     bmiCategory = "Obésité";
                     targetSteps = 5000;
                     targetCalories = 250;
                     stepAdvice = "Exercices à faible impact uniquement.";
                 }
                 
                 // Update UI
                 TextView txtBMI = findViewById(R.id.txtAdviceBMI);
                 TextView txtAdvSteps = findViewById(R.id.txtAdviceSteps);
                 TextView txtAdvCals = findViewById(R.id.txtAdviceCalories);
                 
                 if (txtBMI != null) {
                     txtBMI.setText(String.format(Locale.getDefault(), "IMC: %.1f (%s) - Semaine %d", bmi, bmiCategory, week));
                     txtAdvSteps.setText("Pas quot. rec. : " + targetSteps + ". " + stepAdvice);
                     txtAdvCals.setText("Objectif de brûlage : " + (int)targetCalories + " kcal");
                 }
                 
                 // Update Dynamic Titles & Targets
                 if (lblStepsTitle != null) lblStepsTitle.setText("Objectif de Pas");
                 if (lblCaloriesTitle != null) lblCaloriesTitle.setText("Objectif de Calories");
                 
                 if (txtDailyStepsTargetText != null) txtDailyStepsTargetText.setText("sur " + targetSteps);
                 if (txtDailyCalsTargetText != null) txtDailyCalsTargetText.setText("sur " + (int)targetCalories + " kcal");
             }
             cursor.close();
         } else {
             // Default if no user info found
             if (lblStepsTitle != null) lblStepsTitle.setText("Daily Steps Target");
             if (lblCaloriesTitle != null) lblCaloriesTitle.setText("Calories Burn Target");
             
             if (txtDailyStepsTargetText != null) txtDailyStepsTargetText.setText("of " + targetSteps);
             if (txtDailyCalsTargetText != null) txtDailyCalsTargetText.setText("of " + (int)targetCalories + " kcal");
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
            txtStepProgress.setText(steps + " / " + target + " Pas");
            
            // Calc stats
            double dist = steps * 0.0007;
            double cals = steps * 0.04;
            
            // Update Entity fields for saving, ensuring consistency
            currentSession.distance = dist;
            currentSession.calories = cals;

            txtDistance.setText(String.format(Locale.getDefault(), "%.2f km", dist));
            txtCalories.setText(String.format(Locale.getDefault(), "%.1f kcal", cals));
            
            txtTime.setText(String.format(Locale.getDefault(), "%d min", currentSession.duration));
            progressSteps.setMax(target);
            progressSteps.setProgress(Math.min(steps, target));
        } else {
            // No active session
            txtSteps.setText("0");
            txtStepProgress.setText("Démarrer une session");
            progressSteps.setProgress(0);
            txtDistance.setText("0.00 km");
            txtCalories.setText("0.0 kcal");
            txtTime.setText("0 min");
        }
        
        // UPDATE REAL-TIME DAILY STATS
        // Total = Baseline (other sessions) + Current Session Steps
        int currentSteps = (currentSession != null) ? currentSession.steps : 0;
        dailyStepsTotal = stepsFromOtherSessionsToday + currentSteps;
        double dailyCalsTotal = dailyStepsTotal * 0.04; // approx
        
        if (txtDailySteps != null) {
            txtDailySteps.setText(String.valueOf(dailyStepsTotal));
        }
        
        if (txtDailyCals != null) {
            txtDailyCals.setText(String.format(Locale.getDefault(), "%.0f", dailyCalsTotal));
        }

        if (progressDailySteps != null) {
            progressDailySteps.setMax(targetSteps);
            progressDailySteps.setProgress(Math.min(dailyStepsTotal, targetSteps));
        }

        if (progressDailyCals != null) {
            progressDailyCals.setMax((int)targetCalories);
            progressDailyCals.setProgress(Math.min((int)dailyCalsTotal, (int)targetCalories));
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (currentSession == null) return; 
        if (isGoalAnimationPlaying) return; 

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double magnitude = Math.sqrt(x * x + y * y + z * z);
            double magnitudeDelta = magnitude - magnitudePrevious;
            magnitudePrevious = magnitude;

            if (magnitudeDelta > 6) {
                currentSession.steps++;
                
                // Update stats immediately on step so they are ready for save
                currentSession.distance = currentSession.steps * 0.0007;
                currentSession.calories = currentSession.steps * 0.04;

                if (currentSession.steps >= currentSession.targetSteps && !currentSession.isAchieved) {
                    onGoalAchieved();
                } else {
                    saveSessionProgress();
                }
                updateUI();
            }
        }
    }
    
    // ... (rest same)

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
        sendGoalNotification();

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

    private void fetchWeather() {
        new Thread(() -> {
            try {
                retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                        .baseUrl("https://api.weatherapi.com/v1/")
                        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                        .build();

                com.example.mama.weather.WeatherApi api = retrofit.create(com.example.mama.weather.WeatherApi.class);
                // Hardcoded Tunis for now as per request
                api.getCurrentWeather("37fa0fa8bc0b4aefa65154340260501", "Tunis", "no").enqueue(new retrofit2.Callback<com.example.mama.weather.WeatherResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.mama.weather.WeatherResponse> call, retrofit2.Response<com.example.mama.weather.WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            com.example.mama.weather.WeatherResponse weather = response.body();
                            runOnUiThread(() -> {
                                if (txtCityName != null) {
                                    txtCityName.setText(weather.location.name);
                                    txtTemperature.setText(weather.current.temp_c + "°C");
                                    txtCondition.setText(weather.current.condition.text);
                                    
                                    updateWeatherRecommendation(weather.current);
                                }
                            });
                            
                            // Load Icon
                            String iconUrl = "https:" + weather.current.condition.icon;
                            try {
                                java.io.InputStream in = new java.net.URL(iconUrl).openStream();
                                android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(in);
                                runOnUiThread(() -> {
                                    if (imgWeatherIcon != null) imgWeatherIcon.setImageBitmap(bmp);
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.mama.weather.WeatherResponse> call, Throwable t) {
                         runOnUiThread(() -> {
                             if (txtCityName != null) txtCityName.setText("Hors ligne");
                         });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void updateWeatherRecommendation(com.example.mama.weather.WeatherCurrent current) {
        if (layoutWeatherRec == null) return;
        
        layoutWeatherRec.setVisibility(View.VISIBLE);
        
        boolean isBadWeather = false;
        String reason = "";
        
        // thresholds
        if (current.precip_mm > 0.5) {
            isBadWeather = true; // Rain
            reason = "Il pleut.";
        } else if (current.temp_c < 5) {
            isBadWeather = true; // Too cold
            reason = "Il fait trop froid.";
        } else if (current.temp_c > 35) {
            isBadWeather = true; // Too hot
            reason = "Il fait trop chaud.";
        } else if (current.wind_kph > 35) {
            isBadWeather = true; // Windy
            reason = "Vent fort.";
        } else if (current.condition.text.toLowerCase().contains("rain") || 
                   current.condition.text.toLowerCase().contains("snow") ||
                   current.condition.text.toLowerCase().contains("storm") ||
                   current.condition.text.toLowerCase().contains("drizzle")) {
             isBadWeather = true;
             reason = "Le temps est mauvais.";
        }
        
        if (isBadWeather) {
            txtWeatherRec.setText("Marche en intérieur conseillée. " + reason);
            // using generic icons, in real app consider real drawable resources
            imgRecIcon.setImageResource(android.R.drawable.ic_menu_mylocation); // Indoor icon placeholder
            // Tint to differentiate
            imgRecIcon.setColorFilter(android.graphics.Color.parseColor("#E65100"), android.graphics.PorterDuff.Mode.SRC_IN);
            txtWeatherRec.setTextColor(android.graphics.Color.parseColor("#E65100"));
        } else {
            txtWeatherRec.setText("Beau temps ! Faites une marche à l'extérieur.");
            imgRecIcon.setImageResource(android.R.drawable.ic_menu_directions); // Outdoor/Walk
            imgRecIcon.setColorFilter(android.graphics.Color.parseColor("#2E7D32"), android.graphics.PorterDuff.Mode.SRC_IN);
            txtWeatherRec.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
        }
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Step Goals";
            String description = "Notifications for achieving step goals";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void sendGoalNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) 
                .setContentTitle("Objectif Atteint !")
                .setContentText("Votre objectif de pas a été atteint")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1001, builder.build());
    }
}

