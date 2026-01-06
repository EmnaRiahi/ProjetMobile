package com.example.mama.bot;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeminiApi {
    @Headers("Content-Type: application/json")

    // C'EST ICI LA CORRECTION : On utilise le modèle 2.5 de ta liste
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    Call<GeminiResponse> getChatResponse(
            @Query("key") String apiKey,
            @Body GeminiRequest request
    );
}