package com.example.mama.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query; // On garde UNIQUEMENT celui de Retrofit

public interface OverpassService {
    // On appelle l'interpréteur de l'API Overpass
    @GET("api/interpreter")
    Call<OverpassResponse> getNearbyHospitals(@Query("data") String data);
}