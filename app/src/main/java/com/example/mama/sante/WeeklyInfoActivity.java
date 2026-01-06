package com.example.mama.sante;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mama.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WeeklyInfoActivity extends AppCompatActivity {

    private List<WeeklyInfo> weeklyInfoList = new ArrayList<>();
    private WeeklyInfoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_info);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewWeeklyInfo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
            Type listType = new TypeToken<ArrayList<WeeklyInfo>>(){}.getType();
            weeklyInfoList = gson.fromJson(reader, listType);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors du chargement des informations.", Toast.LENGTH_SHORT).show();
        }
    }
}
