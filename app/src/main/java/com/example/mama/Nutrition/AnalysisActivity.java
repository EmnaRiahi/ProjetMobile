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
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        tvAnalysisResult = findViewById(R.id.tvAnalysisResult);
        progressBar = findViewById(R.id.progressBar);
        dbHelper = new DatabaseHelper(this);
        executorService = Executors.newSingleThreadExecutor();

        // Lancer l'analyse
        analyzeHabits();
    }

    private void analyzeHabits() {
        progressBar.setVisibility(View.VISIBLE);
        tvAnalysisResult.setText("Analyse en cours...");

        executorService.execute(() -> {
            List<Meal> meals = dbHelper.getAllMeals();

            if (meals.isEmpty()) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvAnalysisResult.setText("Aucune donnée disponible pour l'analyse.");
                });
                return;
            }

            // Préparer les données pour l'IA
            StringBuilder mealData = new StringBuilder();
            mealData.append("Analyser les habitudes alimentaires suivantes:\n\n");

            for (Meal meal : meals) {
                mealData.append("- ").append(meal.getName())
                        .append(" le ").append(meal.getDate())
                        .append(" à ").append(meal.getTime());

                if (meal.getNotes() != null && !meal.getNotes().isEmpty()) {
                    mealData.append(" (Note: ").append(meal.getNotes()).append(")");
                }
                mealData.append("\n");
            }

            mealData.append("\nDonner des recommandations simples sur:\n");
            mealData.append("1. La régularité des repas\n");
            mealData.append("2. Les pauses entre les repas\n");
            mealData.append("3. Les observations négatives répétées\n");
            mealData.append("4. Conseils pour améliorer l'alimentation\n");

            // Appel à l'API Claude (exemple avec Anthropic API)
            String analysis = callClaudeAPI(mealData.toString());

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (analysis != null && !analysis.isEmpty()) {
                    tvAnalysisResult.setText(analysis);
                } else {
                    tvAnalysisResult.setText("Erreur lors de l'analyse. Vérifiez votre connexion internet.");
                }
            });
        });
    }


    private String callClaudeAPI(String prompt) {
        try {
            String apiKey = "AIzaSyBbd4Ezx1GrG3kbBDbgAdtva2z2I358qo0";

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