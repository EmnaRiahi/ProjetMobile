package com.example.mama.sante;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mama.R;
import com.google.android.material.card.MaterialCardView;

public class HealthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Can cause layout issues with this design
        setContentView(R.layout.activity_health);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialCardView cardWeightTracker = findViewById(R.id.cardWeightTracker);
        MaterialCardView cardWeeklyInfo = findViewById(R.id.cardWeeklyInfo);
        MaterialCardView cardSleepTracker = findViewById(R.id.cardSleepTracker);

        cardWeightTracker.setOnClickListener(v -> {
            Intent intent = new Intent(HealthActivity.this, HealthTrackerActivity.class);
            startActivity(intent);
        });

        cardWeeklyInfo.setOnClickListener(v -> {
            Intent intent = new Intent(HealthActivity.this, WeeklyInfoActivity.class);
            startActivity(intent);
        });

        cardSleepTracker.setOnClickListener(v -> {
            Intent intent = new Intent(HealthActivity.this, SleepTrackingActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnSleepRecordings).setOnClickListener(v -> {
            Intent intent = new Intent(HealthActivity.this, RecordingsActivity.class);
            startActivity(intent);
        });
    }
}