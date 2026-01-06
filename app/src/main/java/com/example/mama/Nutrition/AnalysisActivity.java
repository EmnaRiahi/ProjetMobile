package com.example.mama.Nutrition;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mama.R;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnalysisActivity extends AppCompatActivity {

    private TextView tvAnalysisResult;
    private android.view.View cardLoading, cardResult;
    private DatabaseHelper dbHelper;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        tvAnalysisResult = findViewById(R.id.tvAnalysisResult);
        cardLoading = findViewById(R.id.cardLoading);
        cardResult = findViewById(R.id.cardResult);
        dbHelper = new DatabaseHelper(this);
        executorService = Executors.newSingleThreadExecutor();

        // Initial state
        cardResult.setVisibility(View.GONE);

        // Lancer l'analyse
        analyzeHabits();
    }

    private void analyzeHabits() {
        cardLoading.setVisibility(View.VISIBLE);
        cardResult.setVisibility(View.GONE);

        executorService.execute(() -> {
            // Petite pause pour l'effet "IA"
            try { Thread.sleep(1500); } catch (InterruptedException e) {}

            List<Meal> meals = dbHelper.getAllMeals();

            if (meals.isEmpty()) {
                runOnUiThread(() -> {
                    cardLoading.setVisibility(View.GONE);
                    cardResult.setVisibility(View.VISIBLE);
                    tvAnalysisResult.setText("Ajoutez quelques repas pour que l'IA puisse analyser vos habitudes ! ✨");
                });
                return;
            }

            // Préparer les données pour l'IA
            StringBuilder mealData = new StringBuilder();
            mealData.append("Agis en tant qu'expert en nutrition. Analyse ces repas récents et donne un feedback encourageant et constructif en français:\n\n");

            for (Meal meal : meals) {
                mealData.append("- ").append(meal.getName())
                        .append(" (").append(meal.getDate()).append(" ").append(meal.getTime()).append(")");
                if (meal.getNotes() != null && !meal.getNotes().isEmpty()) {
                    mealData.append(" Note: ").append(meal.getNotes());
                }
                mealData.append("\n");
            }

            mealData.append("\nFormatte ta réponse avec des puces claires.");

            String analysis = callClaudeAPI(mealData.toString());

            runOnUiThread(() -> {
                cardLoading.setVisibility(View.GONE);
                cardResult.setVisibility(View.VISIBLE);
                if (analysis != null && !analysis.isEmpty()) {
                    tvAnalysisResult.setText(analysis);
                } else {
                    tvAnalysisResult.setText("Oups ! Je n'ai pas pu charger l'analyse. Vérifie ta connexion Internet. 🌐");
                }
            });
        });
    }


    private String callClaudeAPI(String prompt) {
        try {
            String apiKey = "AIzaSyC8sfqEMetC1RyIWeAKAT6NmScV60q99Wo";

            URL url = new URL(
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                            + apiKey
            );

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();

            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);

            parts.put(textPart);
            content.put("parts", parts);
            contents.put(content);
            body.put("contents", contents);

            OutputStream os = conn.getOutputStream();
            os.write(body.toString().getBytes("UTF-8"));
            os.close();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                JSONObject json = new JSONObject(response.toString());
                return json
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}