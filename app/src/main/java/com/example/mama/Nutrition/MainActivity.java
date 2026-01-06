package com.example.mama.Nutrition;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mama.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MealAdapter.OnMealClickListener {

    private RecyclerView recyclerView;
    private MealAdapter mealAdapter;
    private DatabaseHelper dbHelper;
    private List<Meal> mealList;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_rayen);

        // Initialiser la base de données
        dbHelper = new DatabaseHelper(this);

        // Créer le canal de notification
        createNotificationChannel();

        // Configurer les rappels de repas
        setupMealReminders();

        // Initialiser les vues
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);

        // Configurer RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mealList = new ArrayList<>();
        mealAdapter = new MealAdapter(mealList, this);
        recyclerView.setAdapter(mealAdapter);

        // Charger les repas
        loadMeals();

        // Animation du FAB au démarrage
        animateFAB();

        // Bouton ajouter - Afficher le choix entre scanner et saisie manuelle
        fabAdd.setOnClickListener(v -> {
            // Animation de pulsation
            Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            v.startAnimation(pulse);
            v.postDelayed(this::showAddMealOptions, 150);
        });

        // Bouton analyse IA
        findViewById(R.id.btnAnalyze).setOnClickListener(v -> {
            animateButton(v);
            v.postDelayed(this::analyzeWithAI, 150);
        });

        // Bouton Water Tracker
        findViewById(R.id.btnWaterTracker).setOnClickListener(v -> {
            animateButton(v);
            v.postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, WaterTrackerActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
            }, 150);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mealAdapter.resetAnimation();
        loadMeals();
    }

    private void loadMeals() {
        mealList.clear();
        mealList.addAll(dbHelper.getAllMeals());
        mealAdapter.notifyDataSetChanged();

        // Animation d'apparition du RecyclerView si vide avant
        if (!mealList.isEmpty()) {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            recyclerView.startAnimation(fadeIn);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Rappels de repas";
            String description = "Notifications pour les rappels de repas";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("MEAL_REMINDER", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setupMealReminders() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Petit-déjeuner - 8:00
        scheduleReminder(alarmManager, 8, 0, "Petit-déjeuner", 1);

        // Déjeuner - 13:00
        scheduleReminder(alarmManager, 13, 0, "Déjeuner", 2);

        // Collation - 16:30
        scheduleReminder(alarmManager, 16, 30, "Collation", 3);

        // Dîner - 20:00
        scheduleReminder(alarmManager, 20, 0, "Dîner", 4);
    }

    private void scheduleReminder(AlarmManager alarmManager, int hour, int minute, String mealType, int requestCode) {
        Intent intent = new Intent(this, MealReminderReceiver.class);
        intent.putExtra("mealType", mealType);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Si l'heure est déjà passée aujourd'hui, planifier pour demain
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Répéter tous les jours
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    private void showAddMealOptions() {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Ajouter un repas");
        builder.setMessage("Comment souhaitez-vous ajouter ce repas ?");

        builder.setPositiveButton("📸 Scanner un produit", (dialog, which) -> {
            Intent intent = new Intent(MainActivity.this, BarcodeScannerActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
        });

        builder.setNegativeButton("✍️ Saisie manuelle", (dialog, which) -> {
            Intent intent = new Intent(MainActivity.this, AddMealActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
        });

        builder.setNeutralButton("Annuler", (dialog, which) -> dialog.dismiss());

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Animation du dialogue
        if (dialog.getWindow() != null) {
            Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
            dialog.getWindow().getDecorView().startAnimation(scaleUp);
        }
    }

    private void analyzeWithAI() {
        List<Meal> meals = dbHelper.getAllMeals();

        if (meals.isEmpty()) {
            showAnimatedToast("Aucune donnée à analyser");
            return;
        }

        // Lancer l'activité d'analyse
        Intent intent = new Intent(MainActivity.this, AnalysisActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onEditClick(Meal meal) {
        Intent intent = new Intent(MainActivity.this, EditMealActivity.class);
        intent.putExtra("meal_id", meal.getId());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
    }

    @Override
    public void onDeleteClick(Meal meal) {
        dbHelper.deleteMeal(meal.getId());
        loadMeals();
        showAnimatedToast("✨ Repas supprimé");
    }

    private void animateFAB() {
        Animation bounceIn = AnimationUtils.loadAnimation(this, R.anim.bounce_in);
        fabAdd.startAnimation(bounceIn);
    }

    private void animateButton(View button) {
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        button.startAnimation(pulse);
    }

    private void showAnimatedToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_left);
    }
}