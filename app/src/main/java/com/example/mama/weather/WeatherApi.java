package com.example.mama.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("current.json")
    Call<WeatherResponse> getCurrentWeather(
        @Query("key") String key,
        @Query("q") String q,
        @Query("aqi") String aqi
    );
}
