package com.example.mama.sante;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import com.example.mama.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WeeklyInfoActivity extends AppCompatActivity {

    private LightSensorManager lightSensorManager;
    private View rootView;

    private List<WeeklyInfo> weeklyInfoList = new ArrayList<>();
    private WeeklyInfoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_info);

        rootView = findViewById(android.R.id.content);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewWeeklyInfo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize light sensor
        lightSensorManager = new LightSensorManager(this);
        lightSensorManager.setThemeChangeListener(this::applyTheme);

        loadWeeklyInfo();

        adapter = new WeeklyInfoAdapter(this, weeklyInfoList, weeklyInfo -> {
            // Afficher les détails dans un dialogue
            new AlertDialog.Builder(this)
                    .setTitle(weeklyInfo.getTitle())
                    .setMessage(weeklyInfo.getInfo())
                    .setPositiveButton("OK", null)
                    .show();
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadWeeklyInfo() {
        try {
            // Charger le fichier JSON depuis le dossier assets
            InputStream is = getAssets().open("pregnancy_info.json");
            InputStreamReader reader = new InputStreamReader(is);

            // Utiliser Gson pour parser le JSON dans une liste d'objets WeeklyInfo
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<WeeklyInfo>>() {
            }.getType();
            weeklyInfoList = gson.fromJson(reader, listType);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors du chargement des informations.", Toast.LENGTH_SHORT).show();
        }
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
            rootView.setBackgroundColor(ContextCompat.getColor(this, R.color.sante_dark_background));
            // Update Header texts if accessible... we didn't add IDs in this file
            // modification either?
            // activity_weekly_info.xml (Step 205)
            // It has `header` ID for LinearLayout.
            // TextView 1: @color/text_primary
            // TextView 2: @color/grey_text
            // CardView: bg white.
            // Items: RecyclerView.

            // Let's update the header and card manually by finding them.
            View header = findViewById(R.id.header);
            if (header != null && header instanceof android.view.ViewGroup) {
                android.view.ViewGroup headerGroup = (android.view.ViewGroup) header;
                if (headerGroup.getChildCount() >= 2) {
                    ((TextView) headerGroup.getChildAt(0))
                            .setTextColor(ContextCompat.getColor(this, R.color.sante_dark_text_primary));
                    ((TextView) headerGroup.getChildAt(1))
                            .setTextColor(ContextCompat.getColor(this, R.color.sante_dark_text_secondary));
                }
            }
        } else {
            rootView.setBackgroundResource(R.drawable.bg_gradient_health);
            View header = findViewById(R.id.header);
            if (header != null && header instanceof android.view.ViewGroup) {
                android.view.ViewGroup headerGroup = (android.view.ViewGroup) header;
                if (headerGroup.getChildCount() >= 2) {
                    ((TextView) headerGroup.getChildAt(0))
                            .setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                    ((TextView) headerGroup.getChildAt(1))
                            .setTextColor(ContextCompat.getColor(this, R.color.grey_text));
                }
            }
        }

        if (adapter != null) {
            adapter.setDarkMode(isDarkMode);
        }
    }
}
