package com.example.mama.sante;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mama.R;
import com.google.android.material.card.MaterialCardView;

public class HealthActivity extends AppCompatActivity {

    private LightSensorManager lightSensorManager;
    private LinearLayout mainLayout;
    private TextView headerTitle;
    private MaterialCardView cardWeightTracker, cardWeeklyInfo, cardSleepTracker;

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

        // Initialize views
        mainLayout = findViewById(R.id.main);
        cardWeightTracker = findViewById(R.id.cardWeightTracker);
        cardWeeklyInfo = findViewById(R.id.cardWeeklyInfo);
        cardSleepTracker = findViewById(R.id.cardSleepTracker);

        // Initialize light sensor manager
        lightSensorManager = new LightSensorManager(this);
        lightSensorManager.setThemeChangeListener(this::applyTheme);

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

    @Override
    protected void onResume() {
        super.onResume();
        lightSensorManager.register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        lightSensorManager.unregister();
    }

    private void applyTheme(boolean isDarkMode) {
        if (isDarkMode) {
            // Apply dark theme
            mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.sante_dark_background));
            cardWeightTracker.setCardBackgroundColor(ContextCompat.getColor(this, R.color.sante_dark_card_weight));
            cardWeeklyInfo.setCardBackgroundColor(ContextCompat.getColor(this, R.color.sante_dark_card_weekly));
            cardSleepTracker.setCardBackgroundColor(ContextCompat.getColor(this, R.color.sante_dark_card_sleep));

            // Update text colors in cards
            updateCardTextColors(true);
        } else {
            // Apply light theme - use gradient drawable
            mainLayout.setBackgroundResource(R.drawable.bg_gradient_health);
            cardWeightTracker.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            cardWeeklyInfo.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            cardSleepTracker.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

            // Update text colors in cards
            updateCardTextColors(false);
        }
    }

    private void updateCardTextColors(boolean isDarkMode) {
        int primaryColor = ContextCompat.getColor(this,
                isDarkMode ? R.color.sante_dark_text_primary : R.color.black);
        int secondaryColor = ContextCompat.getColor(this,
                isDarkMode ? R.color.sante_dark_text_secondary : R.color.grey_text);

        // Update all TextViews in the layout
        updateTextViewsInCard(cardWeightTracker, primaryColor, secondaryColor);
        updateTextViewsInCard(cardWeeklyInfo, primaryColor, secondaryColor);
        updateTextViewsInCard(cardSleepTracker, primaryColor, secondaryColor);
    }

    private void updateTextViewsInCard(MaterialCardView card, int primaryColor, int secondaryColor) {
        TextView titleWeight = card.findViewById(R.id.titleWeight);
        TextView titleInfo = card.findViewById(R.id.titleInfo);
        TextView titleSleep = card.findViewById(R.id.titleSleep);

        if (titleWeight != null) {
            titleWeight.setTextColor(primaryColor);
            // Find and update description text
            TextView desc = (TextView) card.getChildAt(0).findViewById(
                    ((android.view.ViewGroup) card.getChildAt(0)).getChildAt(2).getId());
            if (desc != null)
                desc.setTextColor(secondaryColor);
        }
        if (titleInfo != null) {
            titleInfo.setTextColor(primaryColor);
        }
        if (titleSleep != null) {
            titleSleep.setTextColor(primaryColor);
        }
    }
}
