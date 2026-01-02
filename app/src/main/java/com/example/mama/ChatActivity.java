package com.example.mama;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Imports de tes classes Bot
import com.example.mama.bot.ChatAdapter;
import com.example.mama.bot.ChatMessage;
import com.example.mama.bot.GeminiApi;
import com.example.mama.bot.GeminiRequest;
import com.example.mama.bot.GeminiResponse;

import java.util.ArrayList;
import java.util.List;

// Imports Retrofit
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText etMessage;
    ImageButton btnSend;
    List<ChatMessage> messages;
    ChatAdapter adapter;

    // TA CLÉ API
    String API_KEY = "AIzaSyBEB_t-rvNjYxNLo-Ff0w3bbiMva75VvxU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Liaison des vues
        recyclerView = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // Initialisation de la liste et de l'adapter
        messages = new ArrayList<>();

        // Message de bienvenue
        messages.add(new ChatMessage("Bonjour ! Je suis MamaBot. Posez-moi vos questions sur votre grossesse.", "BOT"));

        adapter = new ChatAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Clic sur le bouton envoyer
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
            }
        });
    }

    void sendMessage(String question) {
        // 1. AFFICHER LE MESSAGE DE L'UTILISATEUR
        messages.add(new ChatMessage(question, "USER"));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.smoothScrollToPosition(messages.size() - 1);
        etMessage.setText(""); // Vider le champ texte

        // 2. PRÉPARER RETROFIT
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GeminiApi api = retrofit.create(GeminiApi.class);

        // 3. PRÉPARER LE CONTEXTE (PROMPT ENGINEERING)
        String context = "Rôle : Tu es MamaBot, une assistante virtuelle experte pour les femmes enceintes.\n" +
                "Ton ton : Tu es douce, empathique, rassurante et très polie.\n" +
                "Règles de sécurité : Tu donnes des conseils de bien-être, de nutrition et de suivi.\n" +
                "IMPORTANT : Si la question semble médicale ou grave (sang, forte douleur), tu dois IMPÉRATIVEMENT dire à l'utilisatrice de consulter un médecin ou d'aller aux urgences immédiatement.\n" +
                "Réponds brièvement à cette question de l'utilisatrice : ";

        String fullPrompt = context + question;

        GeminiRequest request = new GeminiRequest(fullPrompt);

        // 4. APPEL API (ENVOI À GOOGLE)
        api.getChatResponse(API_KEY, request).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                // Vérifier si la réponse est valide
                if (response.isSuccessful() && response.body() != null && response.body().candidates != null && !response.body().candidates.isEmpty()) {
                    try {
                        // Récupérer le texte de la réponse
                        String botReply = response.body().candidates.get(0).content.parts.get(0).text;

                        // Afficher la réponse du Bot
                        messages.add(new ChatMessage(botReply, "BOT"));
                        adapter.notifyItemInserted(messages.size() - 1);
                        recyclerView.smoothScrollToPosition(messages.size() - 1);

                    } catch (Exception e) {
                        Toast.makeText(ChatActivity.this, "Erreur de lecture de la réponse", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Gestion des erreurs API
                    try {
                        String errorBody = "";
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            android.util.Log.e("GEMINI_ERROR", errorBody);
                        }
                        Toast.makeText(ChatActivity.this, "Erreur API: " + response.code(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(ChatActivity.this, "Erreur inconnue", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Erreur de connexion Internet", Toast.LENGTH_SHORT).show();
            }
        });
    }
}