package com.example.mama.meds;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenFdaService {
    // API Gratuite du gouvernement américain (FDA)
    @GET("drug/label.json")
    Call<DrugResponse> searchDrug(@Query("search") String query, @Query("limit") int limit);
}